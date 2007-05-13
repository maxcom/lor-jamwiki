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
import java.util.Iterator;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.intercept.web.FilterInvocationDefinitionSourceEditor;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.acegisecurity.ui.AccessDeniedHandler;
import org.jamwiki.utils.WikiLogger;

/**
 * Handle AccessDeniedExceptions thrown by the Acegi security framework.  This
 * class is based on the org.acegisecurity.ui.AccessDeniedHandler class.
 */
public class JAMWikiAccessDeniedHandler implements AccessDeniedHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAccessDeniedHandler.class.getName());
	public static final String JAMWIKI_ACCESS_DENIED_ERROR_KEY = "JAMWIKI_403_ERROR_KEY";
	public static final String JAMWIKI_ACCESS_DENIED_URI_KEY = "JAMWIKI_403_URI_KEY";
	private String errorPage;
	private String urlPatterns;

	/**
	 *
	 */
	public void handle(ServletRequest servletRequest, ServletResponse servletResponse, AccessDeniedException accessDeniedException) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		if (this.errorPage != null) {
			request.setAttribute(JAMWIKI_ACCESS_DENIED_ERROR_KEY, this.retrieveErrorKey(request));
			request.setAttribute(JAMWIKI_ACCESS_DENIED_URI_KEY, this.retrieveSanitizedRequest(request));
			RequestDispatcher rd = request.getRequestDispatcher(this.errorPage);
			rd.forward(request, response);
		}
		if (!response.isCommitted()) {
			// send 403 after response has been written
			response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
		}
	}

	/**
	 * The error page to use. Must begin with a "/" and is interpreted relative to
	 * the current context root.
	 *
	 * @param errorPage the dispatcher path to display
	 *
	 * @throws IllegalArgumentException if the argument doesn't comply with the above
	 *  limitations
	 */
	public void setErrorPage(String errorPage) {
		if (errorPage != null && !errorPage.startsWith("/")) {
			throw new IllegalArgumentException("ErrorPage must begin with '/'");
		}
		this.errorPage = errorPage;
	}

	/**
	 *
	 */
	public String getUrlPatterns() {
		return this.urlPatterns;
	}

	/**
	 *
	 */
	private ConfigAttributeDefinition retrieveConfigAttributeDefinition(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null) {
			return null;
		}
		FilterInvocationDefinitionSourceEditor editor = new FilterInvocationDefinitionSourceEditor();
		editor.setAsText(this.getUrlPatterns());
		PathBasedFilterInvocationDefinitionMap map = (PathBasedFilterInvocationDefinitionMap)editor.getValue();
		return map.lookupAttributes(uri);
    }

	/**
	 *
	 */
	private String retrieveErrorKey(HttpServletRequest request) {
        ConfigAttributeDefinition attrs = this.retrieveConfigAttributeDefinition(request);
        if (attrs != null) {
			Iterator configIterator = attrs.getConfigAttributes();
			if (configIterator.hasNext()) {
				ConfigAttribute attr = (ConfigAttribute)configIterator.next();
				return attr.getAttribute();
			}
 		}
 		return null;
	}

	/**
	 *
	 */
	private String retrieveSanitizedRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null) {
			return null;
		}
		if (uri.indexOf('?') > 0) {
			// strip everything after and including '?' unless the uri is only one character
			uri = uri.substring(0, uri.indexOf('?'));
		}
		if (uri.indexOf('#') > 0) {
			// strip everything after and including '#' unless the uri is only one character
			uri = uri.substring(0, uri.indexOf('#'));
		}
		while (uri.endsWith("/") && uri.length() > 1) {
			// if the uri ends with '/' but is more than one character strip the last '/'
			uri = uri.substring(0, uri.length() - 1);
		}
		if (uri.indexOf('/') != -1 && uri.length() > 1) {
			// strip everything prior to the final '/'
			uri = uri.substring(uri.lastIndexOf('/') + 1);
		}
		return uri;
	}

	/**
	 *
	 */
	public void setUrlPatterns(String urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
}
