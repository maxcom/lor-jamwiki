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

/**
 * For systems using LDAP or other external authentication systems, once authenticated
 * a user may not have records in the jam_wiki_user or similar tables, so this method
 * will create the records necessary to track editing history and similar information.
 *
 * For a standard JAMWiki setup this filter is disabled by default since the user
 * registration process creates the required records.
 */
public class JAMWikiPostAuthenticationFilter implements Filter {

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
		if (jamwikiPostAuthenticationFilterEnabled && validCredentials() && !validateJamwikiUserExists()) {
			// if there is a valid security credential && no JAMWiki record for the user, create one
			setupJamwikiUser();
		}
		chain.doFilter(request, response);
	}

	/**
	 *
	 */
	private void setupJamwikiUser() {
		// TODO - implement
	}

	/**
	 *
	 */
	private boolean validCredentials() {
		// TODO - implement
		return true;
	}

	/**
	 *
	 */
	private boolean validateJamwikiUserExists() {
		// TODO - implement
		return true;
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
