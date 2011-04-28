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

import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.jamwiki.model.Role;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki role and implementing the Spring Security
 * <code>GrantedAuthority</code> interface.
 */
public class RoleImpl extends GrantedAuthorityImpl {

	private static final WikiLogger logger = WikiLogger.getLogger(RoleImpl.class.getName());
	private String description = null;

	/**
	 *
	 */
	public RoleImpl(Role role) {
		// it is invalid to call with an empty role authority, so no null check is needed
		super(role.getAuthority().toUpperCase());
	}

	/**
	 *
	 */
	public RoleImpl(String authority) {
		// it is invalid to call with an empty role authority, so no null check is needed
		super(authority.toUpperCase());
	}

	/**
	 *
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 *
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Two roles are equal if the role names are the same.
	 */
	public boolean equals(RoleImpl RoleImpl) {
		if (this.getAuthority() == null && RoleImpl != null && RoleImpl.getAuthority() == null) {
			return true;
		}
		return (this.getAuthority() != null && RoleImpl != null && this.getAuthority().equals(RoleImpl.getAuthority()));
	}
}