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
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.providers.anonymous.AnonymousProcessingFilter;
import org.springframework.security.userdetails.memory.UserAttribute;
import org.jamwiki.utils.WikiLogger;

/**
 * This class allows anonymous users to be provided default roles from the
 * JAMWiki database.
 */
public class JAMWikiAnonymousProcessingFilter extends AnonymousProcessingFilter {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAnonymousProcessingFilter.class.getName());

	/**
	 * Override the parent method to ensure that default roles for anonymous
	 * users have been retrieved.
	 */
	public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		WikiUserAuth user = WikiUserAuth.initAnonymousWikiUserAuth();
		UserAttribute userAttribute = this.getUserAttribute();
		if (userAttribute == null) {
			logger.warning("No user attribute available in JAMWikiAnonymousProcessingFilter.  Please verify the Acegi configuration settings.");
		} else {
			ArrayList userAuthorities = new ArrayList();
			for (int i = 0; i < user.getAuthorities().length; i++) {
				userAuthorities.add(user.getAuthorities()[i]);
			}
			userAttribute.setAuthorities(userAuthorities);
		}
		super.doFilterHttp(request, response, chain);
	}
}
