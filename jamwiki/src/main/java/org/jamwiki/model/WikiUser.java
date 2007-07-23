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
package org.jamwiki.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Provides an object representing Wiki-specific information about a user of
 * the Wiki.
 */
public class WikiUser implements UserDetails {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUser.class.getName());
	private static final long serialVersionUID = -2818435399240684581L;

	private Timestamp createDate = new Timestamp(System.currentTimeMillis());
	private String createIpAddress = "0.0.0.0";
	private String defaultLocale = null;
	private String displayName = null;
	private Timestamp lastLoginDate = new Timestamp(System.currentTimeMillis());
	private String lastLoginIpAddress = "0.0.0.0";
	private String username = null;
	private int userId = -1;

	/**
	 * GrantedAuthority is used by Acegi Security to support several authorities
	 * (roles). A logged user always has ROLE_USER and may have other roles,
	 * e.g. ROLE_ADMIN.
	 */
	private GrantedAuthority[] authorities = {Role.ROLE_USER};
	private String password = null;

	/**
	 *
	 */
	public WikiUser(String username) throws Exception {
		this.username = username;
		if (Utilities.isFirstUse() || Utilities.isUpgrade()) {
			return;
		}
		this.addGroupRoles();
		this.addUserRoles();
	}

	/**
	 * Construct the <code>User</code> with the details required by {@link org.acegisecurity.providers.dao.DaoAuthenticationProvider}.
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
	public WikiUser(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked, GrantedAuthority[] authorities)
			throws IllegalArgumentException {
		if (!StringUtils.hasText(username) || password == null) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}
		this.username = username;
		this.password = password;
//		this.enabled = enabled;
//		this.accountNonExpired = accountNonExpired;
//		this.credentialsNonExpired = credentialsNonExpired;
//		this.accountNonLocked = accountNonLocked;
		if (authorities == null) {
			authorities = new GrantedAuthority[0];
		}
		this.addRoles(authorities);
		this.addGroupRoles();
		this.addUserRoles();
	}

	/**
	 *
	 */
	public Timestamp getCreateDate() {
		return this.createDate;
	}

	/**
	 *
	 */
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	/**
	 *
	 */
	public String getCreateIpAddress() {
		return this.createIpAddress;
	}

	/**
	 *
	 */
	public void setCreateIpAddress(String createIpAddress) {
		this.createIpAddress = createIpAddress;
	}

	/**
	 *
	 */
	public String getDefaultLocale() {
		return this.defaultLocale;
	}

	/**
	 *
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	/**
	 *
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 *
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 *
	 */
	public Timestamp getLastLoginDate() {
		return this.lastLoginDate;
	}

	/**
	 *
	 */
	public void setLastLoginDate(Timestamp lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	/**
	 *
	 */
	public String getLastLoginIpAddress() {
		return this.lastLoginIpAddress;
	}

	/**
	 *
	 */
	public void setLastLoginIpAddress(String lastLoginIpAddress) {
		this.lastLoginIpAddress = lastLoginIpAddress;
	}

	/**
	 *
	 */
	public int getUserId() {
		return this.userId;
	}

	/**
	 *
	 */
	public void setUserId(int userId) {
		this.userId = userId;
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
		Assert.notNull(authorities, "Cannot pass a null GrantedAuthority array");
		for (int i = 0; i < authorities.length; i++) {
			Assert.notNull(authorities[i], "Granted authority element " + i + " is null - GrantedAuthority[] cannot contain any null elements");
		}
		this.authorities = authorities;
	}

	/**
	 *
	 */
	public String getPassword() {
		return password;
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
		return username;
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
		for (int i=0; i < roles.length; i++) {
			if (!authoritiesSet.contains(roles[i])) {
				authoritiesSet.add(roles[i]);
			}
		}
		this.setAuthorities((GrantedAuthority[])authoritiesSet.toArray(authorities));
	}

	/**
	 *
	 */
	private void addGroupRoles() {
		Role[] groupRoles = new Role[0];
		try {
			groupRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_REGISTERED_USER);
		} catch (Exception e) {
			// FIXME - without default roles bad things happen, so should this throw the
			// error to the calling method?
			logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_REGISTERED_USER, e);
		}
		this.addRoles(groupRoles);
	}

	/**
	 *
	 */
	private void addUserRoles() {
		if (!StringUtils.hasText(this.username)) {
			// FIXME - log error?  RegisterServlet will trigger this.
			return;
		}
		Role[] userRoles = new Role[0];
		try {
			userRoles = WikiBase.getDataHandler().getRoleMapUser(this.username);
		} catch (Exception e) {
			// FIXME - without default roles bad things happen, so should this throw the
			// error to the calling method?
			logger.severe("Unable to retrieve default roles for " + username, e);
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
			logger.warning("No roles assigned for user " + this.username);
			return false;
		}
		return Arrays.asList(authorities).contains(role);
	}
}
