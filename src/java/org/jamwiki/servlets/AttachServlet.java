/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class AttachServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(AttachServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		attach(request, next);
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void attach(HttpServletRequest request, ModelAndView next) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String user = request.getRemoteAddr();
		if (Utilities.currentUser(request) != null) {
			// FIXME - handle this better
			WikiUser wikiUser = Utilities.currentUser(request);
			user = wikiUser.getLogin();
		}
		next.addObject("user", user);
		try {
			Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
			if (topic == null) {
				throw new Exception("Topic does not exist: " + topicName + " / " + virtualWiki);
			}
			if (topic.getReadOnly()) {
				logger.warn("Topic " + topicName + " is read only");
				// FIXME - hard coding
				throw new Exception("Topic " + topicName + " is read only");
			}
			String key = request.getSession().getId();
			if (!WikiBase.getHandler().lockTopic(virtualWiki, topicName, key)) {
				// FIXME - hard coding
				throw new Exception("Topic " + topicName + " is locked");
			}
		} catch (Exception e) {
			logger.error("Failure while getting attachment topic info for " + topicName, e);
			// FIXME - hard coding
			throw new Exception("Failure while getting attachment topic info for " + topicName + " " + e.getMessage());
		}
		this.pageInfo.setPageTitle("Attach Files to " + topicName);
		this.pageInfo.setTopicName(topicName);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ATTACH);
		this.pageInfo.setSpecial(true);
	}
}
