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
import org.jamwiki.search.LuceneSearchEngine;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class LinkToServlet extends JAMWikiServlet {

	private static final Logger logger = Logger.getLogger(LinkToServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			linksTo(request, response, next);
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void linksTo(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		try {
			String topicName = JAMWikiServlet.getTopicFromRequest(request);
			if (!StringUtils.hasText(topicName)) {
				throw new WikiException(new WikiMessage("common.exception.notopic"));
			}
			WikiMessage pageTitle = new WikiMessage("linkto.title", topicName);
			this.pageInfo.setPageTitle(pageTitle);
			// grab search engine instance and find
			Collection results = LuceneSearchEngine.findLinkedTo(virtualWiki, topicName);
			next.addObject("results", results);
			next.addObject("link", topicName);
			this.pageInfo.setPageAction(JAMWikiServlet.ACTION_LINK_TO);
			this.pageInfo.setTopicName(topicName);
			return;
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
