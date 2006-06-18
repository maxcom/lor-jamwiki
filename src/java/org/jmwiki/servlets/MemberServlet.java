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
 * @author garethc
 * Date: Jan 8, 2003
 */
public class MemberServlet extends JMWikiServlet implements Controller {

	private static final Logger logger = Logger.getLogger(MemberServlet.class);

	/**
	 *
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user = null;
		if (request.getParameter("userName") != null) {
			if (!"".equals(request.getParameter("userName"))) {
				user = request.getParameter("userName");
				logger.debug("retrieved username by parameter: " + user);
			}
		}
		if (user == null) {
			user = Utilities.getUserFromRequest(request);
			logger.debug("retrieved user from cookie: " + user);
		}
		logger.debug("setting user to: " + user);
		request.setAttribute("title", "Wiki Membership");
		request.setAttribute("user", user);
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		WikiMembers members = null;
		Usergroup usergroup = null;
		try {
			members = WikiBase.getInstance().getWikiMembersInstance(virtualWiki);
			usergroup = WikiBase.getInstance().getUsergroupInstance();
		} catch (Exception e) {
			error(request, response, e);
			return;
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
			request.setAttribute("type", "newMember");
		} else if (member.isPending()) {
			request.setAttribute("type", "pendingMember");
		} else if (member.isConfirmed()) {
			request.setAttribute("type", "confirmedMember");
		} else {
			request.setAttribute("type", "newMember");
		}
		logger.debug("Set type to " + request.getAttribute("type"));
		if (email != null) {
			// request for membership - mail the user a key for confirmation
			try {
				if (usergroup.isEmailValidated()) {
					logger.debug("confirming membership");
					members.createMembershipWithoutRequest(user, email);
					if (Utilities.getUserFromRequest(request) == null) {
						// resend the username cookie
						Cookie cookie = Utilities.createUsernameCookie(user);
						response.addCookie(cookie);
					}
					request.setAttribute("type", "confirmation");
					request.setAttribute("valid", new Boolean(true));
				} else {
					logger.debug("requesting membership");
					members.requestMembership(user, email, request);
					request.setAttribute("type", "membershipRequested");
				}
			} catch (Exception e) {
				error(request, response, e);
				return;
			}
		} else if (key != null) {
			// request for confirmation, check that key is valid
			boolean isValid = false;
			try {
				logger.debug("confirming membership");
				isValid = members.confirmMembership(user, key);
				if (isValid) {
					if (Utilities.getUserFromRequest(request) == null) {
						// resend the username cookie
						Cookie cookie = Utilities.createUsernameCookie(user);
						response.addCookie(cookie);
					}
				}
				request.setAttribute("type", "confirmation");
			} catch (Exception e) {
				error(request, response, e);
				return;
			}
			request.setAttribute("valid", new Boolean(isValid));
		} else {
			if (user == null) {
				// force user to create a username first
				logger.debug("user is null, username needs to be set");
				request.setAttribute("userList", usergroup.getListOfAllUsers());
				dispatch(PseudoTopicHandler.getInstance().getRedirectURL("SetUsername"), request, response);
				return;
			}
		}
		request.setAttribute("knownEmail", usergroup.getKnownEmailById(user));
		request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_MEMBER);
		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
	}

	/**
	 *
	 */
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		this.doGet(httpServletRequest, httpServletResponse);
	}
}
