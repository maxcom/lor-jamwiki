/**
 * @author garethc
 * 25/10/2002 12:21:23
 */
package org.jmwiki.servlets;

import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JMWikiServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(JMWikiServlet.class);

	/**
	 *
	 */
	protected void error(HttpServletRequest request, HttpServletResponse response, Exception err) {
		request.setAttribute("exception", err);
		request.setAttribute("title", "Error");
		err.printStackTrace();
		logger.error(err.getMessage(), err);
		log("Error in " + this.getClass(), err);
		if (err instanceof WikiServletException) {
			request.setAttribute("javax.servlet.jsp.jspException", err);
		}
		dispatch("/WEB-INF/jsp/servlet-error.jsp", request, response);
	}

	/**
	 *
	 */
	protected void dispatch(String destination, HttpServletRequest request, HttpServletResponse response) {
		logger.debug("getting dispatcher for " + destination + ", current URL: " + request.getRequestURL());
		RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
		if (dispatcher == null) {
			logger.error("No dispatcher available for " + destination + " (request=" + request +
				", response=" + response + ")");
			return;
		}
		try {
			dispatcher.forward(request, response);
		} catch (Exception e) {
			log("Dispatch error: " + e.getMessage(), e);
			e.printStackTrace();
			logger.error("dispatch error", e);
			try {
				dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/servlet-error.jsp");
				dispatcher.forward(request, response);
			} catch (Exception e1) {
				log("Error within dispatch error" + e1);
			}
		}
	}

	/**
	 *
	 */
	protected void include(String destination, HttpServletRequest request, HttpServletResponse response) {
		RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
		if (dispatcher == null) {
			log("No dispatcher available for " + destination + " (request=" + request +
				", response=" + response + ")");
			return;
		}
		try {
			dispatcher.include(request, response);
		} catch (Exception e) {
			log("Dispatch error: " + e.getMessage(), e);
			e.printStackTrace();
			logger.error(e);
			try {
				dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/servlet-error.jsp");
				dispatcher.forward(request, response);
			} catch (Exception e1) {
				log("Error within dispatch error" + e1);
			}
		}
	}

	/**
	 *
	 */
	protected void redirect(String destination, HttpServletResponse response) {
		String url = response.encodeRedirectURL(destination);
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}

// $Log$
// Revision 1.11  2006/04/23 06:36:56  wrh2
// Coding style updates (VQW-73).
//
// Revision 1.10  2006/03/26 23:07:06  studer
// Cleanup: Removing deprecated method access.
//
// Revision 1.9  2003/10/05 05:07:32  garethc
// fixes and admin file encoding option + merge with contributions
//
// Revision 1.8  2003/06/12 20:37:37  garethc
// merge
//
// Revision 1.7  2003/05/18 21:16:07  garethc
// lex fixes
//
// Revision 1.6  2003/04/15 23:11:04  garethc
// lucene fixes
//
// Revision 1.5  2003/04/15 08:33:11  mrgadget4711
// ADD: Search using Lucene
// ADD: RSS feed
//
// Revision 1.4  2003/04/09 20:44:29  garethc
// package org
//
// Revision 1.3  2003/03/11 20:21:16  garethc
// fixes and 2.5 enhancements
//
// Revision 1.2  2003/03/05 00:56:18  garethc
// lock list
//
// Revision 1.1  2003/02/02 19:41:25  garethc
// servlets
//
// Revision 1.5  2003/01/07 03:11:53  garethc
// beginning of big cleanup, taglibs etc
//
// Revision 1.4  2002/11/26 00:26:19  garethc
// fixes
//
// Revision 1.3  2002/11/15 03:31:44  garethc
// small fixes
//
// Revision 1.2  2002/11/10 23:53:02  garethc
// manual and plugins
//
// Revision 1.1  2002/11/01 03:12:43  garethc
// starting work on new two pass lexer
//