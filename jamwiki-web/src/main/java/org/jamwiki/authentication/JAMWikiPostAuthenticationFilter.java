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
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;

/**
 * For systems using LDAP or other external authentication systems, once authenticated
 * a user may not have records in the jam_wiki_user or similar tables, so this method
 * will create the records necessary to track editing history and similar information.
 *
 * For a standard JAMWiki setup this filter is disabled by default since the user
 * registration process creates the required records.
 */
public class JAMWikiPostAuthenticationFilter implements Filter {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiPostAuthenticationFilter.class.getName());
	/** Property indicating whether or not this filter is enabled. */
	private boolean jamwikiPostAuthenticationFilterEnabled = false;

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
	 *
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (jamwikiPostAuthenticationFilterEnabled) {
			String username = retrieveUserCredentials();
			try {
				if (!StringUtils.isBlank(username) && !jamwikiUserExists(username)) {
					// if there is a valid security credential & no JAMWiki record for the user, create one
					setupJamwikiUser(username);
				}
			} catch (Exception e) {
				logger.severe("Failure while processing user credentials for " + username, e);
				throw new ServletException(e);
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 *
	 */
	private boolean jamwikiUserExists(String username) throws Exception {
		return (WikiBase.getDataHandler().lookupWikiUser(username) != null);
	}

	/**
	 * Determine the user name from the Spring Security authentication object.  Returns
	 * <code>null</code> if the user has not been validated or is anonymous.
	 */
	private String retrieveUserCredentials() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			// no valid credentials
			return null;
		}
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof UserDetails)) {
			logger.warning("Unknown principal type: " + principal);
			return null;
		}
		return ((UserDetails)principal).getUsername();
	}

	/**
	 *
	 */
	private void setupJamwikiUser(String username) throws Exception {
		WikiUser user = new WikiUser(username);
		// default the password empty so that the user cannot login directly
		String encryptedPassword = "";
		WikiBase.getDataHandler().writeWikiUser(user, username, encryptedPassword);
	}

	/**
	 * Bean property indicating whether or not this filter is enabled.
	 */
	public boolean getJamwikiPostAuthenticationFilterEnabled() {
		return this.jamwikiPostAuthenticationFilterEnabled;
	}

	/**
	 * Bean property indicating whether or not this filter is enabled.
	 */
	public void setJamwikiPostAuthenticationFilterEnabled(boolean jamwikiPostAuthenticationFilterEnabled) {
		this.jamwikiPostAuthenticationFilterEnabled = jamwikiPostAuthenticationFilterEnabled;
	}
}
