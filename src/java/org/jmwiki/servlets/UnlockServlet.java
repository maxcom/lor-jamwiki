package org.jmwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class UnlockServlet extends JMWikiServlet implements Controller {

	private static final Logger logger = Logger.getLogger(UnlockServlet.class);

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
		String topic = request.getParameter("topic");
		if (!Utilities.isAdmin(request)) {
			request.setAttribute("redirect", "Wiki?WikiLockList");
			request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
			request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
			return;
		}
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		logger.debug("Unlocking " + topic);
		try {
			WikiBase.getInstance().unlockTopic(virtualWiki, topic);
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		redirect("Wiki?WikiLockList", response);
	}
}
