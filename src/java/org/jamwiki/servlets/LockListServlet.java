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

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class LockListServlet extends JAMController implements Controller {

	private static Logger logger = Logger.getLogger(LockListServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		if (isTopic(request, "Special:Unlock")) {
			unlock(request, next);
		} else {
			lockList(request, next);
		}
		return next;
	}

	/**
	 *
	 */
	private void lockList(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JAMController.getTopicFromRequest(request);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		List locks = null;
		try {
			locks = WikiBase.getInstance().getHandler().getLockList(virtualWiki);
		} catch (Exception e) {
			logger.error("Error retrieving lock list", e);
			// FIXME - hard coding
			throw new Exception("Error retrieving lock list " + e.getMessage());
		}
		next.addObject("locks", locks);
		next.addObject(JAMController.PARAMETER_TITLE, JAMController.getMessage("locklist.title", request.getLocale()));
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOCKLIST);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
	private void unlock(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JAMController.getTopicFromRequest(request);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		if (!Utilities.isAdmin(request)) {
			String redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, "Special:LockList");
			next.addObject("redirect", redirect);
			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
			next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			return;
		}
		try {
			WikiBase.getInstance().unlockTopic(virtualWiki, topic);
		} catch (Exception e) {
			logger.error("Failure while unlocking " + topic, e);
			// FIXME - hard coding
			throw new Exception("Failure while unlocking " + topic + ": " + e.getMessage());
		}
		lockList(request, next);
	}
}
