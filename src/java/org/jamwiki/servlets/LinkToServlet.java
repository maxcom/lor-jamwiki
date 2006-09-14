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
import org.jamwiki.WikiException;
import org.jamwiki.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.search.LuceneSearchEngine;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class LinkToServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(LinkToServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			linksTo(request, next, pageInfo);
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void linksTo(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		if (!StringUtils.hasText(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		WikiMessage pageTitle = new WikiMessage("linkto.title", topicName);
		pageInfo.setPageTitle(pageTitle);
		// grab search engine instance and find
		Collection results = LuceneSearchEngine.findLinkedTo(virtualWiki, topicName);
		next.addObject("results", results);
		next.addObject("link", topicName);
		pageInfo.setAction(WikiPageInfo.ACTION_LINK_TO);
		pageInfo.setTopicName(topicName);
	}
}
