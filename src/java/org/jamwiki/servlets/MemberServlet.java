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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.PseudoTopicHandler;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMember;
import org.jamwiki.WikiMembers;
import org.jamwiki.users.Usergroup;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class MemberServlet extends JAMWikiServlet implements Controller {

	private static final Logger logger = Logger.getLogger(MemberServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMWikiServlet.buildLayout(request, next);
		if (false) {
			// FIXME - used with email notifications
			notify(request, response, next);
		}
		username(request, response, next);
		return next;
	}

	/**
	 *
	 */
	// FIXME - shouldn't need to pass in response
	private void notify(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String user = null;
		if (request.getParameter("username") != null && request.getParameter("username").length() > 0) {
			user = request.getParameter("username");
		}
		if (user == null) {
			user = Utilities.getUserFromRequest(request);
		}
		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_MEMBER);
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Wiki Membership");
		next.addObject("user", user);
		WikiMembers members = null;
		Usergroup usergroup = null;
		try {
			members = WikiBase.getInstance().getWikiMembersInstance(virtualWiki);
			usergroup = WikiBase.getInstance().getUsergroupInstance();
		} catch (Exception e) {
			logger.error("Failure while retrieving user list", e);
			// FIXME - hard coding
			throw new Exception("Failure while retrieving user list " + e.getMessage());
		}
		String email = request.getParameter("email");
		String key = request.getParameter("key");
		WikiMember member = null;
		try {
			if (members != null) {
				member = members.findMemberByName(user);
			}
		} catch (Exception e) {
			logger.warn("finding user name", e);
		}
		if (member == null) {
			next.addObject("type", "newMember");
		} else if (member.isPending()) {
			next.addObject("type", "pendingMember");
		} else if (member.isConfirmed()) {
			next.addObject("type", "confirmedMember");
		} else {
			next.addObject("type", "newMember");
		}
		if (email != null) {
			// request for membership - mail the user a key for confirmation
			try {
				if (usergroup.isEmailValidated()) {
					members.createMembershipWithoutRequest(user, email);
					if (Utilities.getUserFromRequest(request) == null) {
						// resend the username cookie
						Cookie cookie = Utilities.createUsernameCookie(user);
						response.addCookie(cookie);
					}
					next.addObject("type", "confirmation");
					next.addObject("valid", new Boolean(true));
				} else {
					members.requestMembership(user, email, request);
					next.addObject("type", "membershipRequested");
				}
			} catch (Exception e) {
				logger.error("Failure while mailing membership key", e);
				// FIXME - hard coding
				throw new Exception("Failure while mailing membership key " + e.getMessage());
			}
		} else if (key != null) {
			// request for confirmation, check that key is valid
			boolean isValid = false;
			try {
				isValid = members.confirmMembership(user, key);
				if (isValid) {
					if (Utilities.getUserFromRequest(request) == null) {
						// resend the username cookie
						Cookie cookie = Utilities.createUsernameCookie(user);
						response.addCookie(cookie);
					}
				}
				next.addObject("type", "confirmation");
			} catch (Exception e) {
				logger.error("Failure while confirming membership", e);
				// FIXME - hard coding
				throw new Exception("Failure while confirming membership " + e.getMessage());
			}
			next.addObject("valid", new Boolean(isValid));
		} else if (user == null) {
			// force user to create a username first
			next.addObject("userList", usergroup.getListOfAllUsers());
			next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_MEMBER);
			next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			return;
		}
		next.addObject("knownEmail", usergroup.getKnownEmailById(user));
	}

	/**
	 *
	 */
	// FIXME - shouldn't need to pass in response
	private void username(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_MEMBER);
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Wiki Membership");
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String user = null;
		if (request.getParameter("username") != null && request.getParameter("username").length() > 0) {
			user = request.getParameter("username");
		}
		if (user == null) {
			user = Utilities.getUserFromRequest(request);
		}
		Cookie c = Utilities.createUsernameCookie(user);
		try {
			response.addCookie(c);
		} catch (Exception e) {
			logger.error("Unable to set cookie " + c.getName() + " with value " + c.getValue());
			throw e;
		}
	}
}
