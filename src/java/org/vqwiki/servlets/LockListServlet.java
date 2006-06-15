package org.vqwiki.servlets;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.vqwiki.WikiBase;


/**
 * Give a list of topics, which are logged
 *
 * @author garethc
 * Date: 5/03/2003
 */
public class LockListServlet extends VQWikiServlet {

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
		dispatch("/WEB-INF/jsp/locklist.jsp", request, response);
	}
}
