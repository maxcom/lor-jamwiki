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
package org.jmwiki.servlets;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.model.TopicVersion;
import org.jmwiki.persistency.PersistencyHandler;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class HistoryServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(HistoryServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		history(request, next);
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next) throws Exception {
		PersistencyHandler handler;
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String topicName = JMController.getTopicFromRequest(request);
		try {
			handler = WikiBase.getInstance().getHandler();
			String type = request.getParameter("type");
			if (type.equals("all")) {
				next.addObject(JMController.PARAMETER_TITLE, "History for " + topicName);
				Collection versions = handler.getAllVersions(virtualWiki, topicName);
				next.addObject("versions", versions);
				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
			} else if (type.equals("version")) {
				int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
				int numberOfVersions = handler.getNumberOfVersions(virtualWiki, topicName);
				TopicVersion topicVersion = handler.getTopicVersion(
					request.getContextPath(),
					virtualWiki,
					topicName,
					topicVersionId
				);
				next.addObject("topicVersion", topicVersion);
				next.addObject("numberOfVersions", new Integer(numberOfVersions));
				next.addObject(JMController.PARAMETER_TITLE, topicName + " @" + Utilities.formatDateTime(topicVersion.getEditDate()));
				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
