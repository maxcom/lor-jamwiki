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

/**
 * Provides an object representing a Wiki role that can be extended by the specific security
 * implementation.
 *
 * @see org.springframework.security.core.authority.GrantedAuthorityImpl#getAuthority
 */
public class Role {

	/**
	 * ROLE_ADMIN gives permission to perform wiki maintenance tasks not
	 * available to normal users.  It does not allow the ability to change
	 * system settings.
	 */
	public static final Role ROLE_ADMIN = new Role("ROLE_ADMIN");
	/**
	 * ROLE_ANONYMOUS is not stored in the database but is instead
	 * automatically assigned to all non-logged in users by Spring
	 * Security.
	 */
	public static final Role ROLE_ANONYMOUS = new Role("ROLE_ANONYMOUS");
	public static final Role ROLE_EDIT_EXISTING = new Role("ROLE_EDIT_EXISTING");
	public static final Role ROLE_EDIT_NEW = new Role("ROLE_EDIT_NEW");
	/**
	 * ROLE_EMBEDDED is meant for use with installations that perform
	 * authentication and user management in an external system, such as LDAP.
	 * This role is not created during JAMWiki setup, and is not available
	 * from the Special:Roles interface; instead it should be assigned by the
	 * LDAP or other system that performs user authentication.
	 */
	public static final Role ROLE_EMBEDDED = new Role("ROLE_EMBEDDED");
	/** Role used to control who can import new topics to the wiki using Special:Import. */
	public static final Role ROLE_IMPORT = new Role("ROLE_IMPORT");
	public static final Role ROLE_MOVE = new Role("ROLE_MOVE");
	/**
	 * ROLE_NO_ACCOUNT is meant for use with installations that do not allow
	 * user account management from within JAMWiki.  This role is not created
	 * during JAMWiki setup, and is not available from the Special:Roles
	 * interface; instead it should be assigned by the LDAP or other system
	 * that allows account management.
	 */
	public static final Role ROLE_NO_ACCOUNT = new Role("ROLE_NO_ACCOUNT");
	/** ROLE_SYSADMIN provides the ability to change system settings. */
	public static final Role ROLE_SYSADMIN = new Role("ROLE_SYSADMIN");
	public static final Role ROLE_TRANSLATE = new Role("ROLE_TRANSLATE");
	public static final Role ROLE_UPLOAD = new Role("ROLE_UPLOAD");
	public static final Role ROLE_VIEW = new Role("ROLE_VIEW");

	private String authority;
	private String description;

	/**
	 * Constructor for role.
	 *
	 * @param authority The role name.
	 */
	public Role(String authority) {
		this.authority = authority;
	}

	/**
	 * 
	 */
	public String getAuthority() {
		return this.authority;
	}

	/**
	 * Provide a description of this role.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Set a description for this role.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}