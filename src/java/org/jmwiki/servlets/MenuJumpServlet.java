/**
 * Copyright 2004, Gareth Cronin
 * Subject to LGPL
 * User: garethc
 * Date: 5/06/2004
 * Time: 17:47:23
 */

package org.jmwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.utils.JSPUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Servlet for handling quick menu search and jump-to options. Despatches work to other servlets.
 * <p/>
 * Copyright 2004, Gareth Cronin
 *
 * @author $Author: wrh2 $
 *		 Last Modified: $Date: 2006-04-23 08:36:56 +0200 (zo, 23 apr 2006) $
 *		 $Id: MenuJumpServlet.java 643 2006-04-23 06:36:56Z wrh2 $
 */
public class MenuJumpServlet extends JMWikiServlet implements Controller {

	private static Logger logger = Logger.getLogger(MenuJumpServlet.class);

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
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		String jumpto = httpServletRequest.getParameter("jumpto");
		String text = httpServletRequest.getParameter("text");
		if (jumpto != null) {
			// the jump-to submit button was pushed so do a jumpto
			String redirectURL = JSPUtils.createRedirectURL(httpServletRequest, "Wiki?" + JSPUtils.encodeURL(text));
			redirect(redirectURL, httpServletResponse);
			return;
		} else {
			// do a search
			String redirectURL = JSPUtils.createRedirectURL(httpServletRequest, "Wiki?action=" + WikiServlet.ACTION_SEARCH + "&text=" + JSPUtils.encodeURL(text));
			redirect(redirectURL, httpServletResponse);
			return;
		}
	}
}
