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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.PrintableEntry;
import org.jamwiki.WikiBase;
import org.jamwiki.PseudoTopicHandler;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserInfo;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class PrintableServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(PrintableServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("printable");
		try {
			print(request, next);
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void print(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String strDepth = request.getParameter("depth");
		if (request.getParameter("hideform") != null) {
			next.addObject("hideform", "true");
		}
		int depth = 0;
		try {
			depth = Integer.parseInt(strDepth);
		} catch (NumberFormatException e1) {
			depth = 0;
		}
		next.addObject("depth", String.valueOf(depth));
		String contextPath = request.getContextPath();
		Environment.setValue(Environment.PROP_TOPIC_BASE_CONTEXT, contextPath);
		ArrayList result = new ArrayList();
		Vector alreadyVisited = new Vector();
		try {
			result.addAll(parsePage(request, virtualWiki, topic, depth, alreadyVisited));
		} catch (Exception e) {
			logger.error("Failure while creating printable page", e);
			throw new Exception("Failure while creating printable page " + e.getMessage());
		}
		// now go through all pages and replace
		// all href=/ with href=# for the
		// pages in the alreadyVisited vector
		for (Iterator iter = result.iterator(); iter.hasNext();) {
			PrintableEntry element = (PrintableEntry) iter.next();
			for (Iterator visitedIterator = alreadyVisited.iterator(); visitedIterator.hasNext();) {
				String visitedTopic = (String) visitedIterator.next();
				element.setContent(StringUtils.replace(element.getContent(), "href=\"" + visitedTopic, "href=\"#" + visitedTopic));
			}
		}
		// put the result in the request
		next.addObject("contentList", result);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_PRINT);
		this.pageInfo.setTopicName(topic);
		this.pageInfo.setPageTitle(topic);
	}

	/**
	 * Parse page and supages
	 * @param virtualWiki The virutal wiki to use
	 * @param topic The topic to start with
	 * @param depth The depth to go into
	 * @return Collection of pages
	 */
	private Collection parsePage(HttpServletRequest request, String virtualWiki, String topicName, int depth, Vector alreadyVisited)
		throws Exception {
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		String displayName = request.getRemoteAddr();
		WikiUser user = Utilities.currentUser(request);
		ParserInfo parserInfo = new ParserInfo(request.getContextPath(), request.getLocale());
		parserInfo.setWikiUser(user);
		parserInfo.setTopicName(topicName);
		parserInfo.setUserIpAddress(request.getRemoteAddr());
		parserInfo.setVirtualWiki(virtualWiki);
		String onepage = Utilities.parse(parserInfo, topic.getTopicContent(), topicName);
		Collection result = new ArrayList();
		if (onepage != null) {
			PrintableEntry entry = new PrintableEntry();
			entry.setTopic(topicName);
			entry.setContent(onepage);
			result.add(entry);
			alreadyVisited.add(topicName);
			if (depth > 0) {
				String searchfor = "href=\"";
				int iPos = onepage.indexOf(searchfor);
				int iEndPos = onepage.indexOf(Utilities.getMessage("topic.ismentionedon", request.getLocale()));
				if (iEndPos == -1) iEndPos = Integer.MAX_VALUE;
				while (iPos > -1 && iPos < iEndPos) {
					String link = onepage.substring(iPos + searchfor.length(),
					onepage.indexOf('"', iPos + searchfor.length()));
					if (link.indexOf('&') > -1) {
						link = link.substring(0, link.indexOf('&'));
					}
					link = Utilities.decodeURL(link);
					if (link.length() > 3 &&
						!link.startsWith("topic=") &&
						!link.startsWith("action=") &&
						!alreadyVisited.contains(link) &&
						!PseudoTopicHandler.isPseudoTopic(link)) {
						result.addAll(parsePage(request, virtualWiki, link, (depth - 1), alreadyVisited));
					}
					iPos = onepage.indexOf(searchfor, iPos + 10);
				}
			}
		}
		return result;
	}
}
