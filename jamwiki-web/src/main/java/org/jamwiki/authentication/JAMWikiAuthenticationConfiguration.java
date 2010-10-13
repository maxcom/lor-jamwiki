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

import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.security.GrantedAuthority;

/**
 * This class acts as a utility class for holding information used by the authentication
 * and authorization code.
 */
public class JAMWikiAuthenticationConfiguration {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAuthenticationConfiguration.class.getName());
	/** Default roles for anonymous users */
	private static GrantedAuthority[] jamwikiAnonymousAuthorities = null;
	/** Default roles for logged-in and remembered users */
	private static GrantedAuthority[] defaultGroupRoles = null;

	/**
	 *
	 */
	public static GrantedAuthority[] getDefaultGroupRoles() {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			// only query for authorities if wiki is fully setup
			return null;
		}
		if (JAMWikiAuthenticationConfiguration.defaultGroupRoles == null) {
			try {
				JAMWikiAuthenticationConfiguration.defaultGroupRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_REGISTERED_USER);
			} catch (DataAccessException e) {
				// FIXME - without default roles bad things happen, so should this throw the
				// error to the calling method?
				logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_REGISTERED_USER, e);
			}
		}
		return JAMWikiAuthenticationConfiguration.defaultGroupRoles;
	}

	/**
	 *
	 */
	public static GrantedAuthority[] getJamwikiAnonymousAuthorities() {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			// only query for authorities if wiki is fully setup
			return null;
		}
		if (JAMWikiAuthenticationConfiguration.jamwikiAnonymousAuthorities == null) {
			try {
				JAMWikiAuthenticationConfiguration.jamwikiAnonymousAuthorities = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_ANONYMOUS);
			} catch (DataAccessException e) {
				logger.severe("Failure while initializing JAMWiki anonymous user authorities", e);
			}
		}
		return JAMWikiAuthenticationConfiguration.jamwikiAnonymousAuthorities;
	}

	/**
	 * Force a reset of the default logged-in users roles.  This method should
	 * be called if the roles allowed to logged-in users are changed.
	 */
	public static void resetDefaultGroupRoles() {
		JAMWikiAuthenticationConfiguration.defaultGroupRoles = null;
	}

	/**
	 * Force a reset of the default anonymous users roles.  This method should
	 * be called if the roles allowed to anonymous users are changed.
	 */
	public static void resetJamwikiAnonymousAuthorities() {
		JAMWikiAuthenticationConfiguration.jamwikiAnonymousAuthorities = null;
	}
}
