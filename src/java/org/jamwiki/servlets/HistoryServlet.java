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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class HistoryServlet extends JAMController implements Controller {

	private static Logger logger = Logger.getLogger(HistoryServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		history(request, next);
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next) throws Exception {
		PersistencyHandler handler;
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String topicName = JAMController.getTopicFromRequest(request);
		try {
			handler = WikiBase.getInstance().getHandler();
			String type = request.getParameter("type");
			if (type.equals("all")) {
				next.addObject(JAMController.PARAMETER_TITLE, "History for " + topicName);
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
				BufferedReader in = new BufferedReader(new StringReader(topicVersion.getVersionContent()));
				String cookedContents = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, in);
				next.addObject("topicVersion", topicVersion);
				next.addObject("numberOfVersions", new Integer(numberOfVersions));
				next.addObject("cookedContents", cookedContents);
				next.addObject(JAMController.PARAMETER_TITLE, topicName + " @" + Utilities.formatDateTime(topicVersion.getEditDate()));
				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
