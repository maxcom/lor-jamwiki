/**
 *
 */

package org.jmwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class MenuJumpServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(MenuJumpServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		String jumpto = request.getParameter("jumpto");
		if (jumpto != null) {
			jumpTo(request, response, next);
		} else {
			search(request, response, next);
		}
		return null;
	}

	/**
	 *
	 */
	private void jumpTo(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String text = request.getParameter("text");
		// FIXME - need a better way to do redirects
		String redirectURL = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, text);
		redirect(redirectURL, response);
	}

	/**
	 *
	 */
	private void search(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String text = request.getParameter("text");
		// FIXME - need a better way to do redirects
		String redirectURL = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, "Special:Search");
		redirectURL += "?text=" + JSPUtils.encodeURL(text);
		redirect(redirectURL, response);
	}
}
