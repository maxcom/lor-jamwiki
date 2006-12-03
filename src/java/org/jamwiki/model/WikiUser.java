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
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class WikiUser implements UserDetails {

	private static final long serialVersionUID = 8081925283274124743L;

	private final GrantedAuthority ROLE_USER = new GrantedAuthorityImpl("ROLE_USER");
	private final GrantedAuthority ROLE_ADMIN = new GrantedAuthorityImpl("ROLE_ADMIN");

	private Timestamp createDate = new Timestamp(System.currentTimeMillis());
	private String createIpAddress = null;
	private String displayName = null;
	private Timestamp lastLoginDate = new Timestamp(System.currentTimeMillis());
	private String lastLoginIpAddress = null;
	private String username = null;
	private int userId = -1;

	/** GrantedAuthority is used by Acegi Security to support several authorities (roles).  A logged user always has ROLE_USER and may have ROLE_ADMIN. */
	private GrantedAuthority[] grantedAuthorities;
	private String password = null;

	private static WikiLogger logger = WikiLogger.getLogger(WikiUser.class.getName());

	/**
	 *
	 */
	public WikiUser() {
	}

	/**
	 *
	 */
	public boolean getAdmin() {
		return (grantedAuthorities != null) && (Arrays.asList(grantedAuthorities).contains(ROLE_ADMIN));
	}

	/**
	 *
	 */
	public void setAdmin(boolean admin) {
		// A user's roles are fixed so far, so it's safe to create or destroy grantedAuthorities as required.
		// If more roles are to be supported this method must be refactored.
		if (admin) {
			grantedAuthorities = new GrantedAuthority[] {ROLE_USER, ROLE_ADMIN};
		} else {
			grantedAuthorities = new GrantedAuthority[] {ROLE_USER};
		}
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
	 *
	 */
	public GrantedAuthority[] getAuthorities() {
		return grantedAuthorities;
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
	public void setUsername(String username) {
		this.username = username;
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
}