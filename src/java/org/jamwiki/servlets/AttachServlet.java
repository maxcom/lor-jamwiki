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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class AttachServlet extends JAMController implements Controller {

	private static Logger logger = Logger.getLogger(AttachServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		attach(request, next);
		return next;
	}

	/**
	 *
	 */
	private void attach(HttpServletRequest request, ModelAndView next) throws Exception {
		String topicName = JAMController.getTopicFromRequest(request);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		next.addObject(JAMController.PARAMETER_TITLE, "Attach Files to " + topicName);
		next.addObject(JAMController.PARAMETER_TOPIC, topicName);
		String user = request.getRemoteAddr();
		if (Utilities.getUserFromRequest(request) != null) {
			user = Utilities.getUserFromRequest(request);
		}
		next.addObject("user", user);
		try {
			Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
			if (topic == null) {
				throw new Exception("Topic does not exist: " + topicName + " / " + virtualWiki);
			}
			if (topic.getReadOnly()) {
				logger.warn("Topic " + topicName + " is read only");
				// FIXME - hard coding
				throw new Exception("Topic " + topicName + " is read only");
			}
			String key = request.getSession().getId();
			if (!WikiBase.getInstance().lockTopic(virtualWiki, topicName, key)) {
				// FIXME - hard coding
				throw new Exception("Topic " + topicName + " is locked");
			}
		} catch (Exception e) {
			logger.error("Failure while getting attachment topic info for " + topicName, e);
			// FIXME - hard coding
			throw new Exception("Failure while getting attachment topic info for " + topicName + " " + e.getMessage());
		}
		next.addObject(JAMController.PARAMETER_ACTION, JAMController.ACTION_ATTACH);
		next.addObject(JAMController.PARAMETER_SPECIAL, new Boolean(true));
	}
}
