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
 * JAMWikiServlet is the base servlet which all other JAMWiki servlets extend.
 */
public abstract class JAMWikiServlet extends AbstractController {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiServlet.class.getName());
	public static final String PARAMETER_PAGE_INFO = "pageInfo";
	public static final String PARAMETER_TOPIC = "topic";
	public static final String PARAMETER_TOPIC_OBJECT = "topicObject";
	public static final String PARAMETER_USER = "user";
	public static final String PARAMETER_VIRTUAL_WIKI = "virtualWiki";
	public static final String PARAMETER_WATCHLIST = "watchlist";
	public static final String USER_COOKIE = "user-cookie";
	public static final String USER_COOKIE_DELIMITER = "|";
	// FIXME - make configurable
	public static final int USER_COOKIE_EXPIRES = 60 * 60 * 24 * 14; // 14 days

	/** Flag to indicate whether or not the servlet should load the nav bar and other layout elements. */
	protected boolean layout = true;
	/** The prefix of the JSP file used to display the servlet output. */
	protected String displayJSP = "wiki";

	/**
	 *
	 */
	protected abstract ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception;

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		initParams();
		ModelAndView next = new ModelAndView(this.displayJSP);
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			next = this.handleJAMWikiRequest(request, response, next, pageInfo);
			if (this.layout) {
				ServletUtil.loadDefaults(request, next, pageInfo);
			}
		} catch (Exception e) {
			return ServletUtil.viewError(request, e);
		}
		return next;
	}

	/**
	 * If any special servlet initialization needs to be performed it can be done
	 * by overriding this method.
	 */
	protected void initParams() {
	}
}
