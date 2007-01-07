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

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiMessage;
import org.jamwiki.db.AnsiDataHandler;
import org.jamwiki.file.FileHandler;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class PersistencyServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(PersistencyServlet.class.getName());
	protected static final String JSP_ADMIN_CONVERT = "admin-convert.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (!Utilities.isAdmin(request)) {
			WikiMessage errorMessage = new WikiMessage("admin.message.loginrequired");
			return ServletUtil.viewLogin(request, pageInfo, "Special:Convert", errorMessage);
		}
		if (StringUtils.hasText(request.getParameter("todatabase"))) {
			convertToDatabase(request, next, pageInfo);
		} else {
			view(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void convertToDatabase(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			FileHandler fromHandler = new FileHandler();
			AnsiDataHandler toHandler = new AnsiDataHandler();
			Vector messages = AnsiDataHandler.convertFromFile(Utilities.currentUser(request), request.getLocale(), fromHandler, toHandler, null);
			next.addObject("message", new WikiMessage("convert.database.success"));
			next.addObject("messages", messages);
		} catch (Exception e) {
			logger.severe("Failure while executing database-to-file conversion", e);
			next.addObject("messageObject", new WikiMessage("convert.database.failure", e.getMessage()));
		}
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setContentJsp(JSP_ADMIN_CONVERT);
		pageInfo.setAdmin(true);
		pageInfo.setPageTitle(new WikiMessage("convert.title"));
	}
}
