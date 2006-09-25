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

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.Pagination;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class TopicServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static WikiLogger logger = WikiLogger.getLogger(TopicServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control. It gets the topic's name and the
	 * virtual wiki name from the uri, loads the topic and returns a view to the end user.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			// FIXME - remove Special:AllTopics
			if (isTopic(request, "Special:Allpages") || isTopic(request, "Special:AllTopics")) {
				allTopics(request, next, pageInfo);
			} else {
				viewTopic(request, next, pageInfo);
			}
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void allTopics(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Pagination pagination = JAMWikiServlet.buildPagination(request, next);
		Collection topics = WikiBase.getHandler().lookupTopicByType(virtualWiki, Topic.TYPE_ARTICLE, pagination);
		next.addObject("topics", topics);
		next.addObject("topicCount", new Integer(topics.size()));
		pageInfo.setPageTitle(new WikiMessage("alltopics.title"));
		pageInfo.setAction(WikiPageInfo.ACTION_ALL_PAGES);
		pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topic = JAMWikiServlet.getTopicFromURI(request);
		if (!StringUtils.hasText(topic)) {
			String virtualWikiName = getVirtualWikiFromURI(request);
			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
			topic = virtualWiki.getDefaultTopicName();
		}
		super.viewTopic(request, next, pageInfo, topic);
	}
}