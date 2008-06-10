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
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * 
 */
public class WikiUserAuth extends WikiUser implements UserDetails {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUserAuth.class.getName());
	private static final long serialVersionUID = -2818435399240684581L;
	/** Default roles for anonymous users */
	private static Role[] anonymousGroupRoles = null;
	/** Default roles for logged-in users */
	private static Role[] defaultGroupRoles = null;
	/**
	 * GrantedAuthority is used by Acegi Security to support several authorities
	 * (roles). A logged user always has ROLE_USER and may have other roles,
	 * e.g. ROLE_ADMIN.
	 */
	private GrantedAuthority[] authorities = {Role.ROLE_USER};

	/**
	 *
	 */
	private WikiUserAuth() {
		super();
	}

	/**
	 *
	 */
	public WikiUserAuth(WikiUser wikiUser) throws Exception {
		super(wikiUser.getUsername());
		this.init();
		this.setCreateDate(wikiUser.getCreateDate());
		this.setCreateIpAddress(wikiUser.getCreateIpAddress());
		this.setDefaultLocale(wikiUser.getDefaultLocale());
		this.setDisplayName(wikiUser.getDisplayName());
		this.setLastLoginDate(wikiUser.getLastLoginDate());
		this.setLastLoginIpAddress(wikiUser.getLastLoginIpAddress());
		this.setUserId(wikiUser.getUserId());
		this.setPassword(wikiUser.getPassword());
	}

	/**
	 *
	 */
	public WikiUserAuth(String username) throws Exception {
		super(username);
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
	public WikiUserAuth(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, GrantedAuthority[] authorities) {
		super(username);
		if (StringUtils.isBlank(username) || password == null) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}
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

	// Acegi Security: UserDetails contract

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
	private void init() throws Exception {
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
		try {
			if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
				// wiki is not yet setup
				return;
			}
			if (WikiUserAuth.anonymousGroupRoles == null) {
				Role[] tempRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_ANONYMOUS);
				WikiUserAuth.anonymousGroupRoles = new Role[tempRoles.length + 1];
				WikiUserAuth.anonymousGroupRoles[0] = Role.ROLE_ANONYMOUS;
				for (int i = 0; i < tempRoles.length; i++) {
					WikiUserAuth.anonymousGroupRoles[i + 1] = tempRoles[i];
				}
			}
		} catch (Exception e) {
			// FIXME - without default roles bad things happen, so should this throw the
			// error to the calling method?
			logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_ANONYMOUS, e);
			return;
		}
		this.setAuthorities(WikiUserAuth.anonymousGroupRoles);
	}

	/**
	 *
	 */
	private void addDefaultGroupRoles() {
		if (WikiUserAuth.defaultGroupRoles == null) {
			try {
				WikiUserAuth.defaultGroupRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_REGISTERED_USER);
			} catch (Exception e) {
				// FIXME - without default roles bad things happen, so should this throw the
				// error to the calling method?
				logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_REGISTERED_USER, e);
				return;
			}
		}
		this.addRoles(WikiUserAuth.defaultGroupRoles);
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
	public static WikiUserAuth initAnonymousWikiUserAuth() {
		WikiUserAuth user = new WikiUserAuth();
		user.addAnonymousGroupRoles();
		return user;
	}

	/**
	 * Utility method for converting an Acegi <code>Authentication</code>
	 * object into a <code>WikiUserAuth</code>.  If the user is logged-in then the
	 * <code>Authentication</code> object will have the <code>WikiUserAuth</code>
	 * as its principal.  If the user is not logged in then create an empty
	 * <code>WikiUserAuth</code> object and assign it the same authorities as the
	 * <code>Authentication</code> object.
	 *
	 * @param auth The Acegi <code>Authentication</code> object that is being
	 *  converted into a <code>WikiUserAuth</code> object.
	 * @return Returns a <code>WikiUserAuth</code> object that corresponds to the
	 *  Acegi <code>Authentication</code> object.  If the user is not currently
	 *  logged-in then an empty <code>WikiUserAuth</code> with the same authorities
	 *  as the <code>Authentication</code> object is returned.  This method
	 *  will never return <code>null</code>.
	 * @throws AuthenticationCredentialsNotFoundException If authentication
	 *  credentials are unavailable.
	 */
	public static WikiUserAuth initWikiUserAuth(Authentication auth) throws AuthenticationCredentialsNotFoundException {
		if (auth == null) {
			throw new AuthenticationCredentialsNotFoundException("No authentication credential available");
		}
		if (auth.getPrincipal() instanceof WikiUserAuth) {
			// logged-in user
			return (WikiUserAuth)auth.getPrincipal();
		}
		WikiUserAuth user = new WikiUserAuth();
		user.setAuthorities(auth.getAuthorities());
		return user;
	}

	/**
	 * Force a reset of the default role object.  This method should be called
	 * if the roles allowed to anonymous users are changed.
	 */
	public static void resetAnonymousGroupRoles() {
		WikiUserAuth.anonymousGroupRoles = null;
	}

	/**
	 * Force a reset of the default logged-in users roles.  This method should
	 * be called if the roles allowed to logged-in users are changed.
	 */
	public static void resetDefaultGroupRoles() {
		WikiUserAuth.defaultGroupRoles = null;
	}
}
