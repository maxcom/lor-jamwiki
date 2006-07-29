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
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.VirtualWiki;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class TopicServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(TopicServlet.class.getName());

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
		try {
			if (isTopic(request, "Special:AllTopics")) {
				allTopics(request, next);
			} else if (isTopic(request, "Special:OrphanedTopics")) {
				orphanedTopics(request, next);
			} else if (isTopic(request, "Special:ToDoTopics")) {
				toDoTopics(request, next);
			} else {
				viewTopic(request, next);
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
	private void allTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Collection all = WikiBase.getHandler().getAllTopicNames(virtualWiki);
		String title = "Special:AllTopics";
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		this.pageInfo.setPageTitle(title);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ALL_TOPICS);
		this.pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void orphanedTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Collection all = WikiBase.getOrphanedTopics(virtualWiki);
		String title = "Special:OrphanedTopics";
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		this.pageInfo.setPageTitle(title);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ORPHANED_TOPICS);
		this.pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void toDoTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Collection all = WikiBase.getToDoWikiTopics(virtualWiki);
		String title = "Special:ToDoTopics";
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		this.pageInfo.setPageTitle(title);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_TODO_TOPICS);
		this.pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void viewTopic(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JAMWikiServlet.getTopicFromURI(request);
		if (!StringUtils.hasText(topic)) {
			String virtualWikiName = getVirtualWikiFromURI(request);
			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
			topic = virtualWiki.getDefaultTopicName();
		}
		super.viewTopic(request, next, topic);
	}
}