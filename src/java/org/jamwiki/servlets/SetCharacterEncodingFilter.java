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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

/**
 * Sets request encoding for servlets to encoding used by JSTL formating tags.
 * JSTL formating tags used for displaying localized messages
 * are setting session-scoped variable
 * <code>javax.servlet.jsp.jstl.fmt.request.charset</code>.
 * This filter uses the variable to set request encoding
 * for servlets, as they cannot use &lt;fmt:requestEncoding/&gt;
 * as JSP pages.
 */
public final class SetCharacterEncodingFilter implements Filter {

	/**
	 *
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 *
	 */
	public void destroy() {
	}

	/**
	 * Sets request encoding.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request.getCharacterEncoding() == null) {
			//set response locale according to request locale
			response.setLocale(request.getLocale());
			//if fmt tags specified output encoding already, use it
			if (request instanceof HttpServletRequest) {
				HttpSession ses = ((HttpServletRequest) request).getSession(false);
				if (ses != null) {
					String encoding = (String) ses.getAttribute("javax.servlet.jsp.jstl.fmt.request.charset");
					if (encoding != null) {
						request.setCharacterEncoding(encoding);
					}
				}
			}
		}
		chain.doFilter(request, response);
	}
}
