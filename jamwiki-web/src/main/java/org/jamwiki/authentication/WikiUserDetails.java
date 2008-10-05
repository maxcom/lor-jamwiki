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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Role;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * 
 */
public class WikiUserDetails implements UserDetails {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUserDetails.class.getName());
	private static final long serialVersionUID = -2818435399240684581L;
	/** Default roles for anonymous users */
	private static Role[] anonymousGroupRoles = null;
	/** Default roles for logged-in users */
	private static Role[] defaultGroupRoles = null;
	private String username = null;
	private String password = null;

	/**
	 * GrantedAuthority is used by Spring Security to support several authorities
	 * (roles). A logged user always has ROLE_USER and may have other roles,
	 * e.g. ROLE_ADMIN.
	 */
	private GrantedAuthority[] authorities = {Role.ROLE_USER};

	/**
	 *
	 */
	private WikiUserDetails() {
	}

	/**
	 *
	 */
	public WikiUserDetails(String username, String password) {
		this.username = username;
		this.password = password;
		this.init();
	}

	/**
	 * Construct the <code>User</code> with the details required by
	 * {@link org.springframework.security.providers.dao.DaoAuthenticationProvider}.  This
	 * method should be used by systems that do NOT use the default JAMWiki
	 * user and group roles.  This method will NOT assign default roles to the
	 * user, and as a result the Special:Roles functionality will be ignored.
	 *
	 * @param username the username presented to the
	 *  <code>DaoAuthenticationProvider</code>
	 * @param password the password that should be presented to the
	 *  <code>DaoAuthenticationProvider</code>
	 * @param enabled set to <code>true</code> if the user is enabled
	 * @param accountNonExpired set to <code>true</code> if the account has not
	 *  expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials
	 *  have not expired
	 * @param accountNonLocked set to <code>true</code> if the account is not
	 *  locked
	 * @param authorities the authorities that should be granted to the caller
	 *  if they presented the correct username and password and the user
	 *  is enabled
	 * @throws IllegalArgumentException if a <code>null</code> value was passed
	 *  either as a parameter or as an element in the
	 *  <code>GrantedAuthority[]</code> array.
	 */
	public WikiUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, GrantedAuthority[] authorities) {
		if (StringUtils.isBlank(username) || password == null) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}
		this.setUsername(username);
		this.setPassword(password);
//		this.enabled = enabled;
//		this.accountNonExpired = accountNonExpired;
//		this.credentialsNonExpired = credentialsNonExpired;
//		this.accountNonLocked = accountNonLocked;
		if (authorities == null) {
			authorities = new GrantedAuthority[0];
		}
		this.addRoles(authorities);
	}

	// Spring Security: UserDetails contract

	/**
	 * Returns granted authorites.
	 *
	 * @return authorites, never null.
	 */
	public GrantedAuthority[] getAuthorities() {
		return authorities;
	}

	protected void setAuthorities(GrantedAuthority[] authorities) {
		if (authorities == null) {
			throw new IllegalArgumentException("Cannot pass a null GrantedAuthority array");
		}
		for (int i = 0; i < authorities.length; i++) {
			if (authorities[i] == null) {
				throw new IllegalArgumentException("Granted authority element " + i + " is null - GrantedAuthority[] cannot contain any null elements");
			}
		}
		this.authorities = authorities;
	}

	/**
	 *
	 */
	private void init() {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			return;
		}
		this.addDefaultGroupRoles();
		this.addUserRoles();
	}

	/**
	 *
	 */
	public boolean isAccountNonExpired() {
		// TODO Not yet implemented
		return true;
	}

	/**
	 *
	 */
	public boolean isAccountNonLocked() {
		// TODO Not yet implemented
		return true;
	}

	/**
	 *
	 */
	public boolean isCredentialsNonExpired() {
		// TODO Not yet implemented
		return true;
	}

	/**
	 *
	 */
	public boolean isEnabled() {
		// TODO Not yet implemented
		return true;
	}

	/**
	 *
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 *
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 *
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 *
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 *
	 */
	private void addRoles(GrantedAuthority[] roles) {
		if (this.authorities == null) {
			this.authorities = new GrantedAuthority[0];
		}
		Set authoritiesSet = new HashSet(Arrays.asList(this.authorities));
		for (int i = 0; i < roles.length; i++) {
			if (!authoritiesSet.contains(roles[i])) {
				authoritiesSet.add(roles[i]);
			}
		}
		this.setAuthorities((GrantedAuthority[])authoritiesSet.toArray(authorities));
	}

	/**
	 *
	 */
	private void addAnonymousGroupRoles() {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			// wiki is not yet setup
			return;
		}
		this.setAuthorities(WikiUserDetails.getAnonymousGroupRoles());
	}

	/**
	 *
	 */
	private void addDefaultGroupRoles() {
		if (WikiUserDetails.defaultGroupRoles == null) {
			try {
				WikiUserDetails.defaultGroupRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_REGISTERED_USER);
			} catch (Exception e) {
				// FIXME - without default roles bad things happen, so should this throw the
				// error to the calling method?
				logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_REGISTERED_USER, e);
				return;
			}
		}
		this.addRoles(WikiUserDetails.defaultGroupRoles);
	}

	/**
	 *
	 */
	private void addUserRoles() {
		if (StringUtils.isBlank(this.getUsername())) {
			// FIXME - log error?  RegisterServlet will trigger this.
			return;
		}
		Role[] userRoles = new Role[0];
		try {
			userRoles = WikiBase.getDataHandler().getRoleMapUser(this.getUsername());
		} catch (Exception e) {
			// FIXME - without default roles bad things happen, so should this throw the
			// error to the calling method?
			logger.severe("Unable to retrieve default roles for " + this.getUsername(), e);
		}
		this.addRoles(userRoles);
	}

	/**
	 *
	 */
	public static Role[] getAnonymousGroupRoles() {
		if (WikiUserDetails.anonymousGroupRoles == null) {
			try {
				Role[] tempRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_ANONYMOUS);
				WikiUserDetails.anonymousGroupRoles = new Role[tempRoles.length + 1];
				WikiUserDetails.anonymousGroupRoles[0] = Role.ROLE_ANONYMOUS;
				for (int i = 0; i < tempRoles.length; i++) {
					WikiUserDetails.anonymousGroupRoles[i + 1] = tempRoles[i];
				}
			} catch (Exception e) {
				// FIXME - without default roles bad things happen, so should this throw the
				// error to the calling method?
				logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_ANONYMOUS, e);
			}
		}
		return WikiUserDetails.anonymousGroupRoles;
	}

	/**
	 * Convenience method for determining if a user has been assigned a role
	 * without the need to examine an array of Role objects.
	 *
	 * @param role If the user has been assigned this role then the method will
	 *  return <code>true</code>.
	 * @return <code>true</code> if the user has been assigned the specified
	 *  role, <code>false</code> otherwise.
	 */
	public boolean hasRole(Role role) {
		if (this.authorities == null) {
			logger.warning("No roles assigned for user " + this.getUsername());
			return false;
		}
		return Arrays.asList(authorities).contains(role);
	}

	/**
	 * This method is a (hopefully) temporary workaround for an annoying issue where a user
	 * can be auto-logged in without a user name.
	 */
	public static WikiUserDetails initAnonymousWikiUserDetails() {
		WikiUserDetails user = new WikiUserDetails();
		user.addAnonymousGroupRoles();
		return user;
	}

	/**
	 * Utility method for converting a Spring Security <code>Authentication</code>
	 * object into a <code>WikiUserDetails</code>.  If the user is logged-in then the
	 * <code>Authentication</code> object will have the <code>WikiUserDetails</code>
	 * as its principal.  If the user is not logged in then create an empty
	 * <code>WikiUserDetails</code> object and assign it the same authorities as the
	 * <code>Authentication</code> object.
	 *
	 * @param auth The Spring Security <code>Authentication</code> object that is being
	 *  converted into a <code>WikiUserDetails</code> object.
	 * @return Returns a <code>WikiUserDetails</code> object that corresponds to the
	 *  Spring Security <code>Authentication</code> object.  If the user is not currently
	 *  logged-in then an empty <code>WikiUserDetails</code> with the same authorities
	 *  as the <code>Authentication</code> object is returned.  This method
	 *  will never return <code>null</code>.
	 * @throws AuthenticationCredentialsNotFoundException If authentication
	 *  credentials are unavailable.
	 */
	public static WikiUserDetails initWikiUserDetails(Authentication auth) throws AuthenticationCredentialsNotFoundException {
		if (auth == null) {
			throw new AuthenticationCredentialsNotFoundException("No authentication credential available");
		}
		if (auth.getPrincipal() instanceof WikiUserDetails) {
			// logged-in user
			return (WikiUserDetails)auth.getPrincipal();
		}
		WikiUserDetails user = new WikiUserDetails();
		user.setAuthorities(auth.getAuthorities());
		return user;
	}

	/**
	 * Force a reset of the default role object.  This method should be called
	 * if the roles allowed to anonymous users are changed.
	 */
	public static void resetAnonymousGroupRoles() {
		WikiUserDetails.anonymousGroupRoles = null;
	}

	/**
	 * Force a reset of the default logged-in users roles.  This method should
	 * be called if the roles allowed to logged-in users are changed.
	 */
	public static void resetDefaultGroupRoles() {
		WikiUserDetails.defaultGroupRoles = null;
	}
}
