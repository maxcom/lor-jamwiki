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
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class ContributionsServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(ContributionsServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			contributions(request, next, pageInfo);
		} catch (Exception e) {
			return ServletUtil.viewError(request, e);
		}
		ServletUtil.loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void contributions(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = ServletUtil.getVirtualWikiFromURI(request);
		String userString = request.getParameter("contributor");
		Pagination pagination = ServletUtil.buildPagination(request, next);
		Collection contributions = WikiBase.getHandler().getUserContributions(virtualWiki, userString, pagination, true);
		next.addObject("contributions", contributions);
		next.addObject("numContributions", new Integer(contributions.size()));
		next.addObject("contributor", userString);
		pageInfo.setPageTitle(new WikiMessage("contributions.title", Utilities.escapeHTML(userString)));
		pageInfo.setAction(WikiPageInfo.ACTION_CONTRIBUTIONS);
		pageInfo.setSpecial(true);
	}
}
