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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.RequestKey;
import org.springframework.security.web.util.AntUrlPathMatcher;
import org.springframework.security.web.util.UrlMatcher;
import org.jamwiki.utils.WikiLogger;

/**
 * This class provides a configurable bean object that can be used with the
 * JAMWikiAccessDeniedHandler to retrieve URL-specific error messages to
 * present to the user in the case of authorization or authentication failures.
 */
public class JAMWikiErrorMessageProvider {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiErrorMessageProvider.class.getName());
	private DefaultFilterInvocationSecurityMetadataSource defaultFilterInvocationSecurityMetadataSource = null;
	private LinkedHashMap<String, String> urlPatterns;

	/**
	 *
	 */
	public String getErrorMessageKey(HttpServletRequest request) {
		return this.retrieveErrorKey(request);
	}

	/**
	 *
	 */
	public LinkedHashMap<String, String> getUrlPatterns() {
		return this.urlPatterns;
	}

	/**
	 *
	 */
	private Collection<ConfigAttribute> retrieveConfigAttribute(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null) {
			return null;
		}
		if (this.defaultFilterInvocationSecurityMetadataSource == null) {
			UrlMatcher matcher = new AntUrlPathMatcher();
			LinkedHashMap<RequestKey, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestKey, Collection<ConfigAttribute>>();
			for (String key : this.getUrlPatterns().keySet()) {
				String value = this.getUrlPatterns().get(key);
				Collection<ConfigAttribute> elements = new ArrayList<ConfigAttribute>();
				elements.add(new SecurityConfig(value));
				requestMap.put(new RequestKey(key), elements);
			}
			this.defaultFilterInvocationSecurityMetadataSource = new DefaultFilterInvocationSecurityMetadataSource(matcher, requestMap);
		}
		return this.defaultFilterInvocationSecurityMetadataSource.lookupAttributes(uri, null);
	}

	/**
	 *
	 */
	private String retrieveErrorKey(HttpServletRequest request) {
		Collection<ConfigAttribute> attrs = this.retrieveConfigAttribute(request);
		if (attrs != null) {
			for (ConfigAttribute attr : attrs) {
				return attr.getAttribute();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public void setUrlPatterns(LinkedHashMap<String, String> urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
}
