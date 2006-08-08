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

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.parser.alt.BackLinkLex;
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
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		this.pageInfo.setPageTitle(new WikiMessage("alltopics.title"));
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ALL_TOPICS);
		this.pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void orphanedTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Collection all = retrieveOrphanedTopics(virtualWiki);
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		this.pageInfo.setPageTitle(new WikiMessage("orphaned.title"));
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ORPHANED_TOPICS);
		this.pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void toDoTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Collection all = retrieveToDoWikiTopics(virtualWiki);
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		this.pageInfo.setPageTitle(new WikiMessage("todo.title"));
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_TODO_TOPICS);
		this.pageInfo.setSpecial(true);
	}

	/**
	 * Find all topics without links to them
	 */
	private static Collection retrieveOrphanedTopics(String virtualWiki) throws Exception {
		Collection results = new HashSet();
		Collection all = WikiBase.getHandler().getAllTopicNames(virtualWiki);
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			String topicName = (String) iterator.next();
			Collection matches = WikiBase.getSearchEngineInstance().findLinkedTo(virtualWiki, topicName);
			logger.debug(topicName + ": " + matches.size() + " matches");
			if (matches.size() == 0) {
				results.add(topicName);
			}
		}
		logger.debug(results.size() + " orphaned topics found");
		return results;
	}

	/**
	 * Find all topics that haven't been written but are linked to
	 */
	private static Collection retrieveToDoWikiTopics(String virtualWiki) throws Exception {
		Collection results = new TreeSet();
		Collection all = WikiBase.getHandler().getAllTopicNames(virtualWiki);
		Set topicNames = new HashSet();
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			String topicName = (String) iterator.next();
			Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
			String content = topic.getTopicContent();
			StringReader reader = new StringReader(content);
			BackLinkLex lexer = new BackLinkLex(reader);
			while (lexer.yylex() != null) {
				;
			}
			reader.close();
			topicNames.addAll(lexer.getLinks());
		}
		for (Iterator iterator = topicNames.iterator(); iterator.hasNext();) {
			String topicName = (String) iterator.next();
			if (!WikiBase.exists(virtualWiki, topicName) && !"\\\\\\\\link\\\\\\\\".equals(topicName)) {
				results.add(topicName);
			}
		}
		logger.debug(results.size() + " todo topics found");
		return results;
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