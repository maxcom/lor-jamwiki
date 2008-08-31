/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Provides the infrastructure that is common to all JAMWiki servlets.  Unless
 * special handling is required all JAMWiki servlets should extend this
 * servlet.
 */
public abstract class JAMWikiServlet extends AbstractController {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiServlet.class.getName());

	/** Flag to indicate whether or not the servlet should load the nav bar and other layout elements. */
	protected boolean layout = true;
	/** The prefix of the JSP file used to display the servlet output. */
	protected String displayJSP = "wiki";
	/** Any page that take longer than this value (specified in milliseconds) will print a warning to the log. */
	protected static final int SLOW_PAGE_LIMIT = 1000;

	/**
	 * Abstract method that must be implemented by all sub-classes to handle
	 * servlet requests.
	 *
	 * @param request The servlet request object.
	 * @param response The servlet response object.
	 * @param next A ModelAndView object that has been initialized to the view
	 *  specified by the <code>displayJSP</code> member variable.
	 * @param pageInfo A WikiPageInfo object that will hold output parameters
	 *  to be passed to the output JSP.
	 * @return A ModelAndView object corresponding to the information to be
	 *  rendered, or <code>null</code> if the method directly handles its own
	 *  output, for example by writing directly to the output response.
	 */
	protected abstract ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception;

	/**
	 * Implement the handleRequestInternal method specified by the
	 * Spring AbstractController class.
	 *
	 * @param request The servlet request object.
	 * @param response The servlet response object.
	 * @return A ModelAndView object corresponding to the information to be
	 *  rendered, or <code>null</code> if the method directly handles its own
	 *  output, for example by writing directly to the output response.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		long start = System.currentTimeMillis();
		initParams();
		ModelAndView next = new ModelAndView(this.displayJSP);
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			next = this.handleJAMWikiRequest(request, response, next, pageInfo);
			if (next != null && this.layout) {
				ServletUtil.loadDefaults(request, next, pageInfo);
			}
		} catch (Throwable t) {
			return ServletUtil.viewError(request, t);
		}
		long execution = System.currentTimeMillis() - start;
		if (execution > JAMWikiServlet.SLOW_PAGE_LIMIT) {
			logger.warning("Slow page loading time: " + request.getRequestURI() + " (" + (execution / 1000.000) + " s.)");
		}
		logger.info("Loaded page " + request.getRequestURI() + " (" + (execution / 1000.000) + " s.)");
		return next;
	}

	/**
	 * If any special servlet initialization needs to be performed it can be done
	 * by overriding this method.  In particular, this method can be used to
	 * override the defaults for the <code>layout</code> member variable, which
	 * determines whether or not the output JSP should include the left navigation
	 * and other layout values, and the <code>displayJSP</code> member variable,
	 * which determine the JSP file used to render output.
	 */
	protected void initParams() {
	}
}
