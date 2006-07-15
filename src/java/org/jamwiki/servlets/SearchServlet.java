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
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.search.SearchEngine;
import org.jamwiki.search.SearchResultEntry;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class SearchServlet extends JAMWikiServlet {

	private static final Logger logger = Logger.getLogger(SearchServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			String jumpto = request.getParameter("jumpto");
			if (jumpto != null) {
				jumpTo(request, response, next);
				return null;
			}
			search(request, response, next);
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void jumpTo(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String text = request.getParameter("text");
		// FIXME - if topic doesn't exist, should probably go to an edit page
		// or else give an error message
		// FIXME - need a better way to do redirects
		String redirectURL = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, text);
		redirect(redirectURL, response);
	}

	/**
	 *
	 */
	private void search(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		try {
			String searchField = request.getParameter("text");
			if (request.getParameter("text") == null) {
				this.pageInfo.setPageTitle("Special:Search");
			} else {
				String message = Utilities.getMessage("searchresult.title", request.getLocale(), searchField);
				this.pageInfo.setPageTitle(message);
			}
			// forward back to the search page if the request is blank or null
			if (!StringUtils.hasText(searchField)) {
				this.pageInfo.setPageAction(JAMWikiServlet.ACTION_SEARCH);
				this.pageInfo.setSpecial(true);
				return;
			}
			// grab search engine instance and find
			boolean fuzzy = false;
			if (request.getParameter("fuzzy") != null) fuzzy = true;
			SearchEngine sedb = WikiBase.getSearchEngineInstance();
			Collection results = sedb.findMultiple(virtualWiki, searchField, fuzzy);
			StringBuffer contents = new StringBuffer();
			if (results != null && results.size() > 0) {
				Iterator it = results.iterator();
				while (it.hasNext()) {
					SearchResultEntry result = (SearchResultEntry) it.next();
					contents.append("<p>");
					contents.append("<div class=\"searchresult\">");
					contents.append("<a href=\"");
					contents.append(
						Utilities.buildInternalLink(request.getContextPath(), virtualWiki, result.getTopic())
					);
					if (result.getFoundWord().length() > 0) {
						contents.append("?highlight=");
						contents.append(Utilities.encodeURL(result.getFoundWord()));
					}
					contents.append("\">" + result.getTopic() + "</a>");
					contents.append("</div>");
					if (result.getTextBefore().length() > 0 || result.getTextAfter().length() > 0
						|| result.getFoundWord().length() > 0) {
						contents.append("<br />");
						contents.append(result.getTextBefore());
						contents.append("<a style=\"background:yellow\" href=\"");
						contents.append(
							Utilities.buildInternalLink(request.getContextPath(), virtualWiki, result.getTopic())
						);
						contents.append("?highlight=");
						contents.append(Utilities.encodeURL(result.getFoundWord()));
						contents.append("\">");
						contents.append(result.getFoundWord());
						contents.append("</a> ");
						contents.append(result.getTextAfter());
					}
					contents.append("</p>");
				}
			} else {
				next.addObject("text", searchField);
				contents.append("<p>");
				String message = Utilities.getMessage("searchresult.notfound", request.getLocale(), searchField);
				contents.append(message);
				contents.append("</p>");
			}
			next.addObject("results", contents.toString());
			next.addObject("titlelink", "Special:Search");
			this.pageInfo.setPageAction(JAMWikiServlet.ACTION_SEARCH_RESULTS);
			this.pageInfo.setSpecial(true);
			return;
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
