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

import java.text.DateFormat;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class HistoryServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(HistoryServlet.class.getName());

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (!StringUtils.hasText(request.getParameter("topicVersionId"))) {
			history(request, next, pageInfo);
		} else {
			viewVersion(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = ServletUtil.getVirtualWikiFromURI(request);
		String topicName = ServletUtil.getTopicFromRequest(request);
		pageInfo.setAction(WikiPageInfo.ACTION_HISTORY);
		pageInfo.setTopicName(topicName);
		pageInfo.setPageTitle(new WikiMessage("history.title", topicName));
		Pagination pagination = ServletUtil.buildPagination(request, next);
		Collection changes = WikiBase.getHandler().getRecentChanges(virtualWiki, topicName, pagination, true);
		next.addObject("changes", changes);
		next.addObject("numChanges", new Integer(changes.size()));
	}

	/**
	 *
	 */
	private void viewVersion(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// display an older version
		String virtualWiki = ServletUtil.getVirtualWikiFromURI(request);
		String topicName = ServletUtil.getTopicFromRequest(request);
		int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
		TopicVersion topicVersion = WikiBase.getHandler().lookupTopicVersion(topicName, topicVersionId);
		if (topicVersion == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		topic.setTopicContent(topicVersion.getVersionContent());
		String versionDate = DateFormat.getDateTimeInstance().format(topicVersion.getEditDate());
		WikiMessage pageTitle = new WikiMessage("topic.title", topicName + " @" + versionDate);
		ServletUtil.viewTopic(request, next, pageInfo, pageTitle, topic, false);
	}
}
