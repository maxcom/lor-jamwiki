package org.jmwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.utils.Encryption;
import org.jmwiki.utils.JSPUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Servlet responsible for managing login and logout.
 *
 * @author garethc
 *		 Date: 5/03/2003
 */
public class LoginServlet extends JMController implements Controller {

	/** Logger */
	private static final Logger logger = Logger.getLogger(LoginServlet.class);

	/**
	 *
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		if (request.getMethod() != null && request.getMethod().equalsIgnoreCase("GET")) {
			this.doGet(request, response);
		} else {
			this.doPost(request, response);
		}
		return null;
	}

	/**
	 * Respond to get request. This will be a logout request from the link that appears at the bottom of pages if
	 * there is a user principal in the session.
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		String logoutParameter = httpServletRequest.getParameter("logout");
		if (logoutParameter != null) {
			Boolean logout = new Boolean(logoutParameter);
			if (logout.booleanValue()) {
				httpServletRequest.getSession().invalidate();
				String redirect = JSPUtils.createRedirectURL(httpServletRequest, httpServletRequest.getParameter("redirect"));
				redirect(redirect, httpServletResponse);
			}
		}
	}

	/**
	 * Respond to post request. This will be called when the login form is filled out in login.jsp and is used for
	 * admin authentication for AdminOnlyTopics and the admin console.
	 * @param request
	 * @param httpServletResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException {
		String password = request.getParameter("password");
		String username = request.getParameter("username");
		String redirect = request.getParameter("redirect");
		if (redirect == null || redirect.length() == 0) {
			redirect ="../jsp/Special:Admin";
		}
		if ("admin".equals(username) && Encryption.getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD).equals(password)) {
			request.getSession().setAttribute("admin", "true");
		} else {
			// should this return a specific message instead?
			request.setAttribute("loginFailure", "true");
			request.setAttribute("redirect", redirect);
			request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
			dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
			return;
		}
		redirect(redirect, response);
	}
}
