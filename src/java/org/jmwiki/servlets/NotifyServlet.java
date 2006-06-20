/**
 *
 */
package org.jmwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.Notify;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiException;
import org.jmwiki.utils.JSPUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class NotifyServlet extends HttpServlet implements Controller {

	private static Logger logger = Logger.getLogger(NotifyServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
				throw new WikiException("Topic must be specified");
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
				throw new WikiException("User name not found.");
			}
			Notify notifier = WikiBase.getInstance().getNotifyInstance(virtualWiki, topic);
			if (action == null || action.equals("notify_on")) {
				notifier.addMember(user);
			} else {
				notifier.removeMember(user);
			}
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
		String next = JSPUtils.createLocalRootPath(request, virtualWiki) + "Wiki?" + topic;
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
