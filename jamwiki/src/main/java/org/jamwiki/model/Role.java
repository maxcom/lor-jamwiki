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

import org.acegisecurity.GrantedAuthorityImpl;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki role and implementing the Acegi
 * <code>GrantedAuthority</code> interface.
 */
public class Role extends GrantedAuthorityImpl {

	private static final WikiLogger logger = WikiLogger.getLogger(Role.class.getName());
	private String description = null;

	public static final Role ROLE_ADMIN = new Role("ROLE_ADMIN");
	/** ROLE_ANONYMOUS is not store in the database but is instead automatically assigned to all non-logged in users. */
	public static final Role ROLE_ANONYMOUS = new Role("ROLE_ANONYMOUS");
	public static final Role ROLE_DELETE = new Role("ROLE_DELETE");
	public static final Role ROLE_EDIT_EXISTING = new Role("ROLE_EDIT_EXISTING");
	public static final Role ROLE_EDIT_NEW = new Role("ROLE_EDIT_NEW");
	public static final Role ROLE_MOVE = new Role("ROLE_MOVE");
	public static final Role ROLE_TRANSLATE = new Role("ROLE_TRANSLATE");
	public static final Role ROLE_UPLOAD = new Role("ROLE_UPLOAD");
	/** ROLE_USER is not store in the database but is instead automatically assigned to all logged in users. */
	public static final Role ROLE_USER = new Role("ROLE_USER");
	public static final Role ROLE_VIEW = new Role("ROLE_VIEW");

	/**
	 *
	 */
	public Role(String role) {
		super((role == null) ? null : role.toUpperCase());
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
	public boolean equals(Role role) {
		if (this.getAuthority() == null && role != null && role.getAuthority() == null) {
			return true;
		}
		return (this.getAuthority() != null && role != null && this.getAuthority().equals(role.getAuthority()));
	}
}