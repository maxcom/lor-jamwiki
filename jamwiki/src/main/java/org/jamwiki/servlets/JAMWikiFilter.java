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

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * Perform filtering of all Wiki page requests, including setting the
 * character encoding to UTF-8 and verifying that no setup or upgrade is
 * required.
 */
public class JAMWikiFilter implements Filter {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiFilter.class.getName());
	private String encoding = "UTF-8";
	private FilterConfig config = null;

	/**
	 *
	 */
	public void destroy() {
	}

	/**
	 * Set request encoding to UTF-8.  See
	 * http://wiki.apache.org/tomcat/Tomcat/UTF-8 for a further discussion.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		if (redirectNeeded(request, response)) {
			return;
		}
		chain.doFilter(request, response);
	}

	/**
	 *
	 */
	public void init(FilterConfig config) throws ServletException {
		this.encoding = config.getInitParameter("encoding");
		this.config = config;
	}

	/**
	 *
	 */
	private void redirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException, ServletException {
		response.sendRedirect(url);
	}

	/**
	 *
	 */
	private boolean redirectNeeded(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
		if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
			return false;
		}
		try {
			HttpServletRequest request = (HttpServletRequest)servletRequest;
			HttpServletResponse response = (HttpServletResponse)servletResponse;
			if (redirectSetup(request)) {
				// redirect to setup page
				String url = request.getContextPath() + "/" + WikiBase.DEFAULT_VWIKI + "/Special:Setup";
				redirect(request, response, url);
				return true;
			}
			if (redirectUpgrade(request)) {
				// redirect to upgrade page
				String url = request.getContextPath() + "/" + WikiBase.DEFAULT_VWIKI + "/Special:Upgrade";
				redirect(request, response, url);
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Determine whether or not to redirect to the setup page.
	 */
	private boolean redirectSetup(HttpServletRequest request) throws Exception {
		if (!WikiUtil.isFirstUse()) {
			return false;
		}
		if (request.getRequestURI().toLowerCase().endsWith(".css")) {
			return false;
		}
		if (ServletUtil.isTopic(request, "Special:Setup")) {
			return false;
		}
		if (ServletUtil.isTopic(request, "jsp/setup.jsp")) {
			return false;
		}
		return true;
	}

	/**
	 * Determine whether or not to redirect to the upgrade page.
	 */
	private boolean redirectUpgrade(HttpServletRequest request) throws Exception {
		if (!WikiUtil.isUpgrade()) {
			return false;
		}
		if (request.getRequestURI().toLowerCase().endsWith(".css")) {
			return false;
		}
		if (ServletUtil.isTopic(request, "Special:Upgrade")) {
			return false;
		}
		if (ServletUtil.isTopic(request, "Special:Login")) {
			return false;
		}
		if (ServletUtil.isTopic(request, "jsp/upgrade.jsp")) {
			return false;
		}
		return true;
	}
}