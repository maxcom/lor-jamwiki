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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * Loads user data from JAMWiki database.
 *
 * @author Rainer Schmitz
 * @version $Id: $
 * @since 28.11.2006
 */
public class JAMWikiDaoImpl implements UserDetailsService {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		if (StringUtils.isBlank(username)) {
			throw new UsernameNotFoundException("Cannot retrieve user without a valid username");
		}
		WikiUser user = this.retrieveUser(username);
		if (user == null) {
			throw new UsernameNotFoundException("Failure retrieving user information for " + username);
		}
		GrantedAuthority[] authorities = this.retrieveUserAuthorities(username);
		return new WikiUserDetails(username, user.getPassword(), true, true, true, true, authorities);
	}

	/**
	 *
	 */
	private WikiUser retrieveUser(String username) throws DataAccessException {
		try {
			return WikiBase.getDataHandler().lookupWikiUser(username, null);
		} catch (Exception e) {
			throw new DataAccessResourceFailureException("Unable to retrieve authorities for user: " + username, e);
		}
	}

	/**
	 *
	 */
	private GrantedAuthority[] retrieveUserAuthorities(String username) throws DataAccessException {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			return new GrantedAuthority[0];
		}
		// add authorities given to all users
		GrantedAuthority[] groupAuthorities = new GrantedAuthority[0];
		if (JAMWikiAuthenticationConfiguration.getDefaultGroupRoles() != null) {
			groupAuthorities = JAMWikiAuthenticationConfiguration.getDefaultGroupRoles();
		}
		// add authorities specific to this user
		GrantedAuthority[] userAuthorities = new GrantedAuthority[0];
		if (!StringUtils.isBlank(username)) {
			// FIXME - log error for blank username?  RegisterServlet will trigger that.
			try {
				userAuthorities = WikiBase.getDataHandler().getRoleMapUser(username);
			} catch (Exception e) {
				throw new DataAccessResourceFailureException("Unable to retrieve authorities for user: " + username, e);
			}
		}
		return (GrantedAuthority[])ArrayUtils.addAll(groupAuthorities, userAuthorities);
	}
}
