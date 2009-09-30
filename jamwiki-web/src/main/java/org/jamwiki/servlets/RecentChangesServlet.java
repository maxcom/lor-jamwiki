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
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to display a summary of edits made, ordered chronologically.
 */
public class RecentChangesServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(RecentChangesServlet.class.getName());
	protected static final String JSP_RECENT_CHANGES = "recent-changes.jsp";

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.recentChanges(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void recentChanges(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
		Pagination pagination = ServletUtil.loadPagination(request, next);
		Collection changes = WikiBase.getDataHandler().getRecentChanges(virtualWiki, pagination, true);
		next.addObject("changes", changes);
		next.addObject("numChanges", new Integer(changes.size()));
		pageInfo.setPageTitle(new WikiMessage("recentchanges.title"));
		pageInfo.setContentJsp(JSP_RECENT_CHANGES);
		pageInfo.setSpecial(true);
	}
}
