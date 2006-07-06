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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Notify;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class NotifyServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(NotifyServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		if (request.getMethod() != null && request.getMethod().equalsIgnoreCase("GET")) {
			this.doGet(request, response);
		} else {
			this.doPost(request, response);
		}
		return null;
	}

	/**
	 *
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String virtualWiki = null;
		String topic = null;
		try {
			virtualWiki = (String) request.getAttribute("virtualWiki");
			String action = request.getParameter("notify_action");
			topic = request.getParameter("topic");
			if (topic == null || topic.equals("")) {
				throw new Exception("Topic must be specified");
			}
			String user = "";
			user = request.getParameter("username");
			if (user == null || user.equals("")) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					if (cookies.length > 0) {
						for (int i = 0; i < cookies.length; i++) {
							if (cookies[i].getName().equals("username")) {
								user = cookies[i].getValue();
							}
						}
					}
				}
			}
			if (user == null || user.equals("")) {
				throw new Exception("User name not found.");
			}
			Notify notifier = WikiBase.getNotifyInstance(virtualWiki, topic);
			if (action == null || action.equals("notify_on")) {
				notifier.addMember(user);
			} else {
				notifier.removeMember(user);
			}
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
		String next = Utilities.createLocalRootPath(request, virtualWiki) + "Wiki?" + topic;
		response.sendRedirect(response.encodeRedirectURL(next));
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		this.doPost(httpServletRequest, httpServletResponse);
	}
}
