package org.vqwiki.servlets;

import org.apache.log4j.Logger;
import org.vqwiki.WikiBase;
import org.vqwiki.utils.Utilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author garethc
 * Date: 5/03/2003
 */
public class UnlockServlet extends VQWikiServlet {

	private static final Logger logger = Logger.getLogger(UnlockServlet.class);

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String topic = request.getParameter("topic");
		if (!Utilities.isAdmin(request)) {
			request.setAttribute("redirect", "Wiki?WikiLockList");
			dispatch("/WEB-INF/jsp/login.jsp", request, response);
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
