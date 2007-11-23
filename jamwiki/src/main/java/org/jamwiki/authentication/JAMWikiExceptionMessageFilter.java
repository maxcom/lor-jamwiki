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
package org.jamwiki.authentication;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.AcegiSecurityException;
import org.acegisecurity.AuthenticationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * This class provides an additional filter that is added to the Acegi
 * configuration for adding messages to the session about why a login is
 * required.
 */
public class JAMWikiExceptionMessageFilter implements Filter, InitializingBean {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiExceptionMessageFilter.class.getName());
	public static final String JAMWIKI_ACCESS_DENIED_ERROR_KEY = "JAMWIKI_403_ERROR_KEY";
	public static final String JAMWIKI_ACCESS_DENIED_URI_KEY = "JAMWIKI_403_URI_KEY";
	public static final String JAMWIKI_AUTHENTICATION_REQUIRED_KEY = "JAMWIKI_AUTHENTICATION_REQUIRED_KEY";
	public static final String JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY = "JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY";
	private JAMWikiErrorMessageProvider errorMessageProvider;

	/**
	 *
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(errorMessageProvider, "errorMessageProvider must be specified");
    }

	/**
	 *
	 */
	public void destroy() {
	}

	/**
	 *
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest)) {
			throw new ServletException("HttpServletRequest required");
		}
		try {
			chain.doFilter(request, response);
		} catch (AcegiSecurityException ex) {
			handleException(request, ex);
			throw ex;
		} catch (ServletException ex) {
			if (ex.getRootCause() instanceof AcegiSecurityException) {
				handleException(request, (AcegiSecurityException)ex.getRootCause());
			}
			throw ex;
		}
	}

	/**
	 *
	 */
	public JAMWikiErrorMessageProvider getErrorMessageProvider() {
		return this.errorMessageProvider;
	}

	/**
	 *
	 */
	private void handleException(ServletRequest servletRequest, AcegiSecurityException exception) {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		if (exception instanceof AccessDeniedException) {
			request.getSession().setAttribute(JAMWIKI_ACCESS_DENIED_ERROR_KEY, this.getErrorMessageProvider().getErrorMessageKey(request));
			request.getSession().setAttribute(JAMWIKI_ACCESS_DENIED_URI_KEY, WikiUtil.getTopicFromURI(request));
		} else if (exception instanceof AuthenticationException) {
			request.getSession().setAttribute(JAMWIKI_AUTHENTICATION_REQUIRED_KEY, this.getErrorMessageProvider().getErrorMessageKey(request));
			request.getSession().setAttribute(JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY, WikiUtil.getTopicFromURI(request));
		}
	}

	/**
	 *
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 *
	 */
	public void setErrorMessageProvider(JAMWikiErrorMessageProvider errorMessageProvider) {
		this.errorMessageProvider = errorMessageProvider;
	}
}
