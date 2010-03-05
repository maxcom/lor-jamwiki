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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class AdminVirtualWikiServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(AdminVirtualWikiServlet.class.getName());
	/** The name of the JSP file used to render the servlet output when searching. */
	protected static final String JSP_ADMIN_VIRTUAL_WIKI = "admin-virtual-wiki.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		next.addObject("function", function);
		if (StringUtils.isBlank(function)) {
			view(request, next, pageInfo);
		} else if (function.equals("virtualwiki")) {
			virtualWiki(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setAdmin(true);
		List<VirtualWiki> virtualWikiList = WikiBase.getDataHandler().getVirtualWikiList();
		next.addObject("wikis", virtualWikiList);
		pageInfo.setContentJsp(JSP_ADMIN_VIRTUAL_WIKI);
		pageInfo.setPageTitle(new WikiMessage("admin.vwiki.title"));
	}

	/**
	 *
	 */
	private void virtualWiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		WikiUser user = ServletUtil.currentWikiUser();
		try {
			VirtualWiki virtualWiki = new VirtualWiki();
			if (!StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
				virtualWiki.setVirtualWikiId(Integer.valueOf(request.getParameter("virtualWikiId")));
			}
			virtualWiki.setName(request.getParameter("name"));
			String defaultTopicName = WikiUtil.getParameterFromRequest(request, "defaultTopicName", true);
			virtualWiki.setDefaultTopicName(defaultTopicName);
			WikiBase.getDataHandler().writeVirtualWiki(virtualWiki);
			if (StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
				WikiBase.getDataHandler().setupSpecialPages(request.getLocale(), user, virtualWiki);
			}
			next.addObject("message", new WikiMessage("admin.message.virtualwikiadded"));
		} catch (Exception e) {
			logger.severe("Failure while adding virtual wiki", e);
			next.addObject("message", new WikiMessage("admin.message.virtualwikifail", e.getMessage()));
		}
		view(request, next, pageInfo);
	}
}
