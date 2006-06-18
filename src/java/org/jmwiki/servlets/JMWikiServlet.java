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
		logger.error(err.getMessage(), err);
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
			logger.error("Dispatch error", e);
			try {
				dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/servlet-error.jsp");
				dispatcher.forward(request, response);
			} catch (Exception e1) {
				logger.error("Error within dispatch error", e1);
			}
		}
	}

	/**
	 *
	 */
	protected void include(String destination, HttpServletRequest request, HttpServletResponse response) {
		RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
		if (dispatcher == null) {
			logger.info("No dispatcher available for " + destination + " (request=" + request +
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
				logger.error("Error within dispatch error", e1);
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
