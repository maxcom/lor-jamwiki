package org.jmwiki.servlets;

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
 *
 * @author Martin Kuba makub@ics.muni.cz
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
