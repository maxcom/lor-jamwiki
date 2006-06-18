package org.jmwiki.servlets;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Give a list of topics, which are logged
 *
 * @author garethc
 * Date: 5/03/2003
 */
public class LockListServlet extends JMWikiServlet implements Controller {

	private static Logger logger = Logger.getLogger(LockListServlet.class);

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
	 * Handle the get request: Give back the list.
	 *
	 * @param request The HttpServletRequest
	 * @param response The HttpServletResponse
	 *
	 * @throws ServletException If something goes wrong
	 * @throws IOException If something goes wrong
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources",
			request.getLocale());
		List locks = null;
		try {
			locks = WikiBase.getInstance().getHandler().getLockList(virtualWiki);
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		request.setAttribute("locks", locks);
		request.setAttribute("title", messages.getString("locklist.title"));
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOCKLIST);
		request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
	}
}
