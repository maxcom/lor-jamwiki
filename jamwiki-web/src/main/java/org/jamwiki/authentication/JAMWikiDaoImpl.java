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

import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.springframework.dao.DataAccessException;

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
		try {
			WikiUser user = WikiBase.getDataHandler().lookupWikiUser(username, null);
			if (user == null) {
				throw new UsernameNotFoundException("User with name '" + username + "' not found in JAMWiki database.");
			}
			return new WikiUserDetails(username, user.getPassword());
		} catch (Exception e) {
			// FIXME - for now throw an exception that Spring Security can handle, but
			// this should be handled with the Spring Security ExceptionTranslationFilter
//			throw new DataAccessResourceFailureException(e.getMessage(), e);
			throw new UsernameNotFoundException("Failure retrieving user information for " + username, e);
		}
	}
}
