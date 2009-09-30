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

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;
import org.jamwiki.WikiBase;

/**
 * AuthenticationProvider to use with JAMWiki database.
 *
 * Extends DaoAuthenticationProvider to use JAMWiki password authentication.
 * It's not possible to use {@link org.acegisecurity.providers.dao.DaoAuthenticationProvider}
 * with a {@link org.acegisecurity.providers.encoding.PasswordEncoder} because
 * JAMWiki stores passwords encoded and not only hashed.
 *
 * @author Rainer Schmitz
 * @version $Id: $
 * @since 28.11.2006
 */
public class JAMWikiDaoAuthenticationProvider extends DaoAuthenticationProvider {

	/**
	 *
	 */
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		try {
			if (!WikiBase.getUserHandler().authenticate(userDetails.getUsername(),
					authentication.getCredentials().toString())) {
				throw new BadCredentialsException(messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"),
						isIncludeDetailsObject() ? userDetails : null);
			}
		} catch (Exception e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

}
