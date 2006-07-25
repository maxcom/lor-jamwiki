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

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class HistoryServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(HistoryServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			String type = request.getParameter("type");
			if (type != null && type.equals("all")) {
				history(request, next);
			} else {
				viewVersion(request, next);
			}
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_HISTORY);
		this.pageInfo.setTopicName(topicName);
		this.pageInfo.setPageTitle("History for " + topicName);
		Vector changes = WikiBase.getHandler().getRecentChanges(virtualWiki, topicName, true);
		next.addObject("changes", changes);
	}

	/**
	 *
	 */
	private void viewVersion(HttpServletRequest request, ModelAndView next) throws Exception {
		// display an older version
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
		TopicVersion topicVersion = WikiBase.getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId);
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		topic.setTopicContent(topicVersion.getVersionContent());
		String pageTitle = topicName + " @" + Utilities.formatDateTime(topicVersion.getEditDate());
		viewTopic(request, next, pageTitle, topic);
	}
}
