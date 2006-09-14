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
import org.jamwiki.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.search.LuceneSearchEngine;
import org.jamwiki.utils.LinkUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class SearchServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(SearchServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			String jumpto = request.getParameter("jumpto");
			if (jumpto != null) {
				jumpTo(request, response);
				return null;
			}
			search(request, next, pageInfo);
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void jumpTo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String text = request.getParameter("text");
		// FIXME - if topic doesn't exist, should probably go to an edit page
		// or else give an error message
		// FIXME - need a better way to do redirects
		String redirectURL = LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWiki, text);
		redirect(redirectURL, response);
	}

	/**
	 *
	 */
	private void search(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String searchField = request.getParameter("text");
		if (request.getParameter("text") == null) {
			pageInfo.setPageTitle(new WikiMessage("search.title"));
		} else {
			pageInfo.setPageTitle(new WikiMessage("searchresult.title", searchField));
		}
		// forward back to the search page if the request is blank or null
		if (!StringUtils.hasText(searchField)) {
			pageInfo.setAction(WikiPageInfo.ACTION_SEARCH);
			pageInfo.setSpecial(true);
			return;
		}
		// grab search engine instance and find
		Collection results = LuceneSearchEngine.findMultiple(virtualWiki, searchField);
		next.addObject("searchField", searchField);
		next.addObject("results", results);
		pageInfo.setAction(WikiPageInfo.ACTION_SEARCH_RESULTS);
		pageInfo.setSpecial(true);
	}
}
