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

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class LockListServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(LockListServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		if (isTopic(request, "Special:Unlock")) {
			unlock(request, next);
		} else {
			lockList(request, next);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void lockList(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		List locks = null;
		try {
			locks = WikiBase.getHandler().getLockList(virtualWiki);
		} catch (Exception e) {
			logger.error("Error retrieving lock list", e);
			// FIXME - hard coding
			throw new Exception("Error retrieving lock list " + e.getMessage());
		}
		next.addObject("locks", locks);
		this.pageInfo.setPageTitle(Utilities.getMessage("locklist.title", request.getLocale()));
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_LOCKLIST);
		this.pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void unlock(HttpServletRequest request, ModelAndView next) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		if (!Utilities.isAdmin(request)) {
			String redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, "Special:LockList");
			next.addObject("redirect", redirect);
			this.pageInfo.setPageAction(JAMWikiServlet.ACTION_LOGIN);
			this.pageInfo.setSpecial(true);
			return;
		}
		try {
			WikiBase.getHandler().unlockTopic(topic);
		} catch (Exception e) {
			logger.error("Failure while unlocking " + topic, e);
			// FIXME - hard coding
			throw new Exception("Failure while unlocking " + topic + ": " + e.getMessage());
		}
		lockList(request, next);
	}
}
