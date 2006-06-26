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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class LoginServlet extends JAMController implements Controller {

	/** Logger */
	private static final Logger logger = Logger.getLogger(LoginServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		if (isAction(request, null, WikiServlet.ACTION_LOGOUT)) {
			// FIXME - response is non-standard here
			logout(request, response, next);
			return null;
		} else {
			// FIXME - response is non-standard here
			if (login(request, response, next)) {
				// FIXME - use Spring
				// login successful, non-Spring redirect
				return null;
			}
		}
		return next;
	}

	/**
	 *
	 */
	private void logout(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		request.getSession().invalidate();
		String redirect = request.getParameter("redirect");
		if (redirect == null || redirect.length() == 0) {
			redirect = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
		}
		redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, redirect);
		// FIXME - can a redirect be done with Spring?
		redirect(redirect, response);
	}

	/**
	 *
	 */
	private boolean login(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String password = request.getParameter("password");
		String username = request.getParameter("username");
		String redirect = request.getParameter("redirect");
		if (redirect == null || redirect.length() == 0) {
			redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, "Special:Admin");
		}
		// FIXME - hard coding
		if (!username.equals("admin") || !Encryption.getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD).equals(password)) {
			// should this return a specific message instead?
			next.addObject("loginFailure", "true");
			next.addObject("redirect", redirect);
			next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
			return false;
		}
		request.getSession().setAttribute("admin", "true");
		// FIXME - can a redirect be done with Spring?
		redirect(redirect, response);
		return true;
	}
}
