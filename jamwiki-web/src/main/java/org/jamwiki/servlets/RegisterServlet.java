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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.WebAuthenticationDetails;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.WikiUserAuth;
import org.jamwiki.model.Role;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Used to process new user account setup.
 */
public class RegisterServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(RegisterServlet.class.getName());
	protected static final String JSP_REGISTER = "register.jsp";

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (request.getParameter("function") == null) {
			view(request, next, pageInfo);
		} else {
			register(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void loadDefaults(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiUser user, WikiUserInfo userInfo) throws Exception {
		if (StringUtils.isBlank(user.getDefaultLocale()) && request.getLocale() != null) {
			user.setDefaultLocale(request.getLocale().toString());
		}
		TreeMap locales = new TreeMap();
		Map translations = WikiConfiguration.getInstance().getTranslations();
		Iterator iterator = translations.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = key + " - " + (String)translations.get(key);
			locales.put(value, key);
		}
		Locale[] localeArray = Locale.getAvailableLocales();
		for (int i = 0; i < localeArray.length; i++) {
			String key = localeArray[i].toString();
			String value = key + " - " + localeArray[i].getDisplayName(localeArray[i]);
			locales.put(value, key);
		}
		next.addObject("locales", locales);
		next.addObject("newuser", user);
		next.addObject("newuserinfo", userInfo);
		pageInfo.setSpecial(true);
		pageInfo.setContentJsp(JSP_REGISTER);
		pageInfo.setPageTitle(new WikiMessage("register.title"));
	}

	/**
	 *
	 */
	private void login(HttpServletRequest request, WikiUserAuth user) {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 *
	 */
	private void register(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWikiName = pageInfo.getVirtualWikiName();
		WikiUserAuth user = this.setWikiUser(request);
		WikiUserInfo userInfo = this.setWikiUserInfo(request);
		next.addObject("newuser", user);
		next.addObject("newuserinfo", userInfo);
		Vector errors = validate(request, user);
		if (!errors.isEmpty()) {
			next.addObject("errors", errors);
			String oldPassword = request.getParameter("oldPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");
			if (oldPassword != null) {
				next.addObject("oldPassword", oldPassword);
			}
			if (newPassword != null) {
				next.addObject("newPassword", newPassword);
			}
			if (confirmPassword != null) {
				next.addObject("confirmPassword", confirmPassword);
			}
			this.loadDefaults(request, next, pageInfo, user, userInfo);
		} else {
			WikiBase.getDataHandler().writeWikiUser(user, userInfo, null);
			// login the user
			this.login(request, user);
			// update the locale key since the user may have changed default locale
			if (!StringUtils.isBlank(user.getDefaultLocale())) {
				Locale locale = LocaleUtils.toLocale(user.getDefaultLocale());
				request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
			}
			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
			String topic = virtualWiki.getDefaultTopicName();
			ServletUtil.redirect(next, virtualWikiName, topic);
		}
	}

	/**
	 *
	 */
	private WikiUserAuth setWikiUser(HttpServletRequest request) throws Exception {
		String username = request.getParameter("login");
		WikiUserAuth user = new WikiUserAuth(username);
		String userIdString = request.getParameter("userId");
		if (!StringUtils.isBlank(userIdString)) {
			int userId = new Integer(userIdString).intValue();
			if (userId > 0) {
				user = new WikiUserAuth(WikiBase.getDataHandler().lookupWikiUser(userId, null));
			}
		}
		user.setDisplayName(request.getParameter("displayName"));
		String newPassword = request.getParameter("newPassword");
		if (!StringUtils.isBlank(newPassword)) {
			user.setPassword(Encryption.encrypt(newPassword));
		}
		user.setDefaultLocale(request.getParameter("defaultLocale"));
		// FIXME - need to distinguish between add & update
		user.setCreateIpAddress(ServletUtil.getIpAddress(request));
		user.setLastLoginIpAddress(ServletUtil.getIpAddress(request));
		return user;
	}

	/**
	 *
	 */
	private WikiUserInfo setWikiUserInfo(HttpServletRequest request) throws Exception {
		WikiUserInfo userInfo = new WikiUserInfo();
		String username = request.getParameter("login");
		String userIdString = request.getParameter("userId");
		if (!StringUtils.isBlank(userIdString)) {
			int userId = new Integer(userIdString).intValue();
			if (userId > 0) {
				userInfo = WikiBase.getUserHandler().lookupWikiUserInfo(username);
			}
		}
		if (!WikiBase.getUserHandler().isWriteable()) {
			return userInfo;
		}
		userInfo.setUsername(username);
		userInfo.setEmail(request.getParameter("email"));
		userInfo.setFirstName(request.getParameter("firstName"));
		userInfo.setLastName(request.getParameter("lastName"));
		String newPassword = request.getParameter("newPassword");
		if (!StringUtils.isBlank(newPassword)) {
			userInfo.setEncodedPassword(Encryption.encrypt(newPassword));
		}
		return userInfo;
	}

	/**
	 *
	 */
	private Vector validate(HttpServletRequest request, WikiUser user) throws Exception {
		Vector errors = new Vector();
		try {
			WikiUtil.validateUserName(user.getUsername());
		} catch (WikiException e) {
			errors.add(e.getWikiMessage());
		}
		String oldPassword = request.getParameter("oldPassword");
		if (user.getUserId() > 0 && !WikiBase.getUserHandler().authenticate(user.getUsername(), oldPassword)) {
			errors.add(new WikiMessage("register.error.oldpasswordinvalid"));
		}
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		if (user.getUserId() < 1 && StringUtils.isBlank(newPassword)) {
			errors.add(new WikiMessage("register.error.passwordempty"));
		}
		if (!WikiBase.getUserHandler().isWriteable() && !WikiBase.getUserHandler().authenticate(user.getUsername(), newPassword)) {
			errors.add(new WikiMessage("register.error.oldpasswordinvalid"));
		}
		if (!StringUtils.isBlank(newPassword) || !StringUtils.isBlank(confirmPassword)) {
			try {
				WikiUtil.validatePassword(newPassword, confirmPassword);
			} catch (WikiException e) {
				errors.add(e.getWikiMessage());
			}
		}
		if (user.getUserId() < 1 && WikiBase.getDataHandler().lookupWikiUser(user.getUsername(), null) != null) {
			errors.add(new WikiMessage("register.error.logininvalid", user.getUsername()));
		}
		return errors;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// FIXME - i suspect initializing with a null login is bad
		WikiUser user = new WikiUser("");
		WikiUserInfo userInfo = new WikiUserInfo();
		if (ServletUtil.currentUser().hasRole(Role.ROLE_USER)) {
			user = ServletUtil.currentUser();
			userInfo = WikiBase.getUserHandler().lookupWikiUserInfo(user.getUsername());
		}
		this.loadDefaults(request, next, pageInfo, user, userInfo);
	}
}
