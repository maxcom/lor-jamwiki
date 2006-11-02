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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class LoginServlet extends JAMWikiServlet {

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(LoginServlet.class.getName());

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (ServletUtil.isTopic(request, "Special:Logout")) {
			logout(request, response, next, pageInfo);
		} else if (request.getParameter("function") != null) {
			login(request, response, next, pageInfo);
		} else {
			return ServletUtil.viewLogin(request, pageInfo, null, null);
		}
		return next;
	}

	/**
	 *
	 */
	private void logout(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWikiName = Utilities.getVirtualWikiFromURI(request);
		Utilities.logout(request, response);
		String redirect = request.getParameter("redirect");
		if (!StringUtils.hasText(redirect)) {
			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
			redirect = virtualWiki.getDefaultTopicName();
		}
		ServletUtil.redirect(next, virtualWikiName, redirect);
	}

	/**
	 *
	 */
	private void login(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWikiName = Utilities.getVirtualWikiFromURI(request);
		String password = request.getParameter("password");
		String username = request.getParameter("username");
		String redirect = request.getParameter("redirect");
		if (!StringUtils.hasText(redirect)) {
			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
			redirect = virtualWiki.getDefaultTopicName();
		}
		WikiUser user = WikiBase.getHandler().lookupWikiUser(username, password, false);
		if (user == null) {
			next.addObject("errorMessage", new WikiMessage("error.login"));
			next.addObject("redirect", redirect);
			pageInfo.setPageTitle(new WikiMessage("login.title"));
			pageInfo.setSpecial(true);
			pageInfo.setAction(WikiPageInfo.ACTION_LOGIN);
		} else {
			boolean remember = (request.getParameter("remember") != null);
			Utilities.login(request, response, user, remember);
			ServletUtil.redirect(next, virtualWikiName, redirect);
		}
	}
}
