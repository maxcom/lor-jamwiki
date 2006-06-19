/**
 * @author garethc
 * 25/10/2002 12:21:23
 */
package org.jmwiki.servlets;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public abstract class JMController extends HttpServlet {

	private static final Logger logger = Logger.getLogger(JMController.class);

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

	/**
	 *
	 */
	public static void buildLayout(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		if (virtualWiki == null) {
			throw new Exception("Invalid virtual wiki");
		}
		// build the layout contents
		String leftMenu = WikiServlet.getCachedContent(virtualWiki, JMController.getMessage("specialpages.leftMenu", request.getLocale()));
		next.addObject("leftMenu", leftMenu);
		String topArea = WikiServlet.getCachedContent(virtualWiki, JMController.getMessage("specialpages.topArea", request.getLocale()));
		next.addObject("topArea", topArea);
		String bottomArea = WikiServlet.getCachedContent(virtualWiki, JMController.getMessage("specialpages.bottomArea", request.getLocale()));
		next.addObject("bottomArea", bottomArea);
		String styleSheet = WikiServlet.getCachedRawContent(virtualWiki, JMController.getMessage("specialpages.stylesheet", request.getLocale()));
		next.addObject("StyleSheet", styleSheet);
		next.addObject("virtualWiki", virtualWiki);
	}

	/**
	 * Get messages for the given locale
	 * @param locale locale
	 * @return
	 */
	public static String getMessage(String key, Locale locale) {
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
		return messages.getString(key);
	}

	/**
	 *
	 */
	public static String getTopicFromURI(HttpServletRequest request) throws Exception {
		String uri = request.getRequestURI().trim();
		if (uri == null || uri.length() <= 0) {
			throw new Exception("URI string is empty");
		}
		int slashIndex = uri.lastIndexOf('/');
		if (slashIndex == -1) {
			throw new Exception("No topic in URL: " + uri);
		}
		String topic = uri.substring(slashIndex + 1);
		logger.info("Retrieved topic from URI as: " + topic);
		return topic;
	}

	/**
	 *
	 */
	public static String getVirtualWikiFromURI(HttpServletRequest request) {
		String uri = request.getRequestURI().trim();
		String contextPath = request.getContextPath().trim();
		String vwiki = null;
		if ((uri == null || uri.length() <= 0) || (contextPath == null || contextPath.length() <= 0)) {
			return null;
		}
		uri = uri.substring(contextPath.length() + 1);
		int slashIndex = uri.indexOf('/');
		if (slashIndex == -1) {
			return null;
		}
		vwiki = uri.substring(0, slashIndex);
		logger.info("Retrieved virtual wiki from URI as: " + vwiki);
		return vwiki;
	}
}
