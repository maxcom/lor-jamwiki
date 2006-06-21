package org.jmwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.PseudoTopicHandler;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiMember;
import org.jmwiki.WikiMembers;
import org.jmwiki.users.Usergroup;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class MemberServlet extends JMController implements Controller {

	private static final Logger logger = Logger.getLogger(MemberServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
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
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String user = null;
		if (request.getParameter("username") != null && request.getParameter("username").length() > 0) {
			user = request.getParameter("username");
		}
		if (user == null) {
			user = Utilities.getUserFromRequest(request);
		}
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_MEMBER);
		next.addObject(JMController.PARAMETER_TITLE, "Wiki Membership");
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
			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_MEMBER);
			next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			return;
		}
		next.addObject("knownEmail", usergroup.getKnownEmailById(user));
	}

	/**
	 *
	 */
	// FIXME - shouldn't need to pass in response
	private void username(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_MEMBER);
		next.addObject(JMController.PARAMETER_TITLE, "Wiki Membership");
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
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
