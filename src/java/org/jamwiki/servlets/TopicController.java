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
import java.io.StringReader;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.servlets.WikiServlet;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class TopicController extends JAMController implements Controller {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(TopicController.class.getName());

	/**
	 * This method handles the request after its parent class receives control. It gets the topic's name and the
	 * virtual wiki name from the uri, loads the topic and returns a view to the end user.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		if (isTopic(request, "Special:AllTopics")) {
			allTopics(request, next);
		} else if (isTopic(request, "Special:OrphanedTopics")) {
			orphanedTopics(request, next);
		} else if (isTopic(request, "Special:ToDoTopics")) {
			toDoTopics(request, next);
		} else {
			view(request, next);
		}
		return next;
	}

	/**
	 *
	 */
	private void allTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		Collection all = WikiBase.getInstance().getSearchEngineInstance().getAllTopicNames(virtualWiki);
		String title = "Special:AllTopics";
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		next.addObject(JAMController.PARAMETER_TITLE, title);
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_ALL_TOPICS);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 * The search servlet offers the opportunity to highlight search results in a page.
	 */
	private String highlight(HttpServletRequest request, String contents) {
		// highlight search result
		if (request.getParameter("highlight") == null) {
			return contents;
		}
		String highlightparam = request.getParameter("highlight");
		String highlighttext = "<b style=\"color:black;background-color:#ffff66\">###</b>";
		contents = markToReplaceOutsideHTML(contents, highlightparam);
		for (int i = 0; i < highlightparam.length(); i++) {
			String myhighlightparam = highlightparam.substring(0, i)
				+ highlightparam.substring(i, i + 1).toUpperCase();
			if ((i + 1) < highlightparam.length()) {
				myhighlightparam += highlightparam.substring(i + 1);
			}
			String highlight = highlighttext;
			highlight = Utilities.replaceString(highlight, "###", myhighlightparam);
			contents = replaceMarked(contents, myhighlightparam, highlight);
			myhighlightparam = highlightparam.substring(0, i)
				+ highlightparam.substring(i, i + 1).toLowerCase();
			if ((i + 1) < highlightparam.length()) {
				myhighlightparam += highlightparam.substring(i + 1);
			}
			highlight = highlighttext;
			highlight = Utilities.replaceString(highlight, "###", myhighlightparam);
			contents = replaceMarked(contents, myhighlightparam, highlight);
		}
		return contents;
	}

	/**
	 * Mark all needles in a haystack, so that they can be replaced later. Take special care on HTML,
	 * so that no needle is replaced inside a HTML tag.
	 *
	 * @param haystack The haystack to go through.
	 * @param needle   The needle to search.
	 * @return The haystack with all needles (outside HTML) marked with the char \u0000
	 */
	public static String markToReplaceOutsideHTML(String haystack, String needle) {
		if (needle.length() == 0) {
			return haystack;
		}
		StringBuffer sb = new StringBuffer();
		boolean inHTMLmode = false;
		int l = haystack.length();
		for (int j = 0; j < l; j++) {
			char c = haystack.charAt(j);
			switch (c) {
				case '<':
					if (((j + 1) < l) && (haystack.charAt(j + 1) != ' ')) {
						inHTMLmode = true;
					}
					break;
				case '>':
					if (inHTMLmode) {
						inHTMLmode = false;
					}
					break;
			}
			if ((c == needle.charAt(0) || Math.abs(c - needle.charAt(0)) == 32) &&
				!inHTMLmode) {
				boolean ok = true;
				if ((j + needle.length()) > l ||
					!haystack.substring(j, j + needle.length()).equalsIgnoreCase(needle)) {
					ok = false;
				}
				if (ok) {
					sb.append('\u0000');
					for (int k = 0; k < needle.length(); k++) {
						sb.append(haystack.charAt(j + k));
					}
					j = j + needle.length() - 1;
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 *
	 */
	private void orphanedTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		Collection all = WikiBase.getInstance().getOrphanedTopics(virtualWiki);
		String title = "Special:OrphanedTopics";
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		next.addObject(JAMController.PARAMETER_TITLE, title);
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_ORPHANED_TOPICS);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 * Replace all needles inside the text with their replacements.
	 *
	 * @param text		The text or haystack, where all needles are already marked with the unicode character \u0000
	 * @param needle	  The needle to search
	 * @param replacement The text, which replaces the needle
	 * @return String containing the text with the needle replaced by the replacement.
	 */
	public static String replaceMarked(String text, String needle, String replacement) {
		needle = '\u0000' + needle;
		text = Utilities.replaceString(text, needle, replacement);
		return text;
	}

	/**
	 *
	 */
	private void toDoTopics(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		Collection all = WikiBase.getInstance().getToDoWikiTopics(virtualWiki);
		String title = "Special:ToDoTopics";
		next.addObject("all", all);
		next.addObject("topicCount", new Integer(all.size()));
		next.addObject(JAMController.PARAMETER_TITLE, title);
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_TODO_TOPICS);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String topicName = JAMController.getTopicFromURI(request);
		Topic topic = new Topic(topicName);
		topic.loadTopic(virtualWiki);
		next.addObject(JAMController.PARAMETER_TITLE, topicName);
		String contents = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, new BufferedReader(new StringReader(topic.getRenderedContent())));
		contents = highlight(request, contents);
		next.addObject("contents", contents);
	}
}