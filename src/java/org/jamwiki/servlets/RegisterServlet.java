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
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class RegisterServlet extends JAMWikiServlet {

	private static final Logger logger = Logger.getLogger(RegisterServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			if (request.getParameter("function") != null) {
				if (register(request, response, next)) {
					// FIXME - use Spring
					// register successful, non-Spring redirect
					return null;
				}
			} else {
				view(request, next);
			}
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_REGISTER);
		this.pageInfo.setPageTitle(Utilities.getMessage("register.title", request.getLocale()));
	}

	/**
	 *
	 */
	// FIXME - shouldn't need to pass in response
	private boolean register(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_REGISTER);
		this.pageInfo.setPageTitle(Utilities.getMessage("register.title", request.getLocale()));
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		WikiUser user = new WikiUser();
		String userIdString = request.getParameter("userId");
		if (StringUtils.hasText(userIdString)) {
			int userId = new Integer(userIdString).intValue();
			if (userId > 0) user = WikiBase.getHandler().lookupWikiUser(userId);
		}
		user.setLogin(request.getParameter("login"));
		user.setDisplayName(request.getParameter("displayName"));
		user.setEmail(request.getParameter("email"));
		String newPassword = request.getParameter("newPassword");
		if (StringUtils.hasText(newPassword)) {
			user.setEncodedPassword(Encryption.encrypt(newPassword));
		}
		// FIXME - need to distinguish between add & update
		user.setCreateIpAddress(request.getRemoteAddr());
		user.setLastLoginIpAddress(request.getRemoteAddr());
		next.addObject("user", user);
		Vector errors = validate(request, next, user);
		if (errors.size() > 0) {
			next.addObject("errors", errors);
			String oldPassword = request.getParameter("oldPassword");
			String confirmPassword = request.getParameter("confirmPassword");
			if (oldPassword != null) next.addObject("oldPassword", oldPassword);
			if (newPassword != null) next.addObject("newPassword", newPassword);
			if (confirmPassword != null) next.addObject("confirmPassword", confirmPassword);
			return false;
		} else {
			WikiBase.getHandler().writeWikiUser(user);
			request.getSession().setAttribute(JAMWikiServlet.PARAMETER_USER, user);
			String topic = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
			String redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, topic);
			// FIXME - can a redirect be done with Spring?
			redirect(redirect, response);
			return true;
		}
	}

	/**
	 *
	 */
	private Vector validate(HttpServletRequest request, ModelAndView next, WikiUser user) throws Exception {
		Vector errors = new Vector();
		if (!StringUtils.hasText(user.getLogin())) {
			errors.add(Utilities.getMessage("error.loginempty", request.getLocale()));
		}
		String oldPassword = request.getParameter("oldPassword");
		if (user.getUserId() > 0 && WikiBase.getHandler().lookupWikiUser(user.getLogin(), oldPassword, false) == null) {
			errors.add(Utilities.getMessage("register.error.oldpasswordinvalid", request.getLocale()));
		}
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		if (user.getUserId() < 1 && !StringUtils.hasText(newPassword)) {
			errors.add(Utilities.getMessage("register.error.passwordempty", request.getLocale()));
		}
		if (StringUtils.hasText(newPassword) || StringUtils.hasText(confirmPassword)) {
			if (!StringUtils.hasText(newPassword)) {
				errors.add(Utilities.getMessage("error.newpasswordempty", request.getLocale()));
			} else if (!StringUtils.hasText(confirmPassword)) {
				errors.add(Utilities.getMessage("error.passwordconfirm", request.getLocale()));
			} else if (!newPassword.equals(confirmPassword)) {
				errors.add(Utilities.getMessage("admin.message.passwordsnomatch", request.getLocale()));
			}
		}
		return errors;
	}
}
