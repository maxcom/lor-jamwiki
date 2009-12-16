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
public class RoleImpl extends GrantedAuthorityImpl implements Role {

	private static final WikiLogger logger = WikiLogger.getLogger(RoleImpl.class.getName());
	private String description = null;

	/**
	 * ROLE_ADMIN gives permission to perform wiki maintenance tasks not
	 * available to normal users.  It does not allow the ability to change
	 * system settings.
	 */
	public static final RoleImpl ROLE_ADMIN = new RoleImpl("ROLE_ADMIN");
	/**
	 * ROLE_ANONYMOUS is not stored in the database but is instead
	 * automatically assigned to all non-logged in users by Spring
	 * Security.
	 */
	public static final RoleImpl ROLE_ANONYMOUS = new RoleImpl("ROLE_ANONYMOUS");
	public static final RoleImpl ROLE_EDIT_EXISTING = new RoleImpl("ROLE_EDIT_EXISTING");
	public static final RoleImpl ROLE_EDIT_NEW = new RoleImpl("ROLE_EDIT_NEW");
	/**
	 * ROLE_EMBEDDED is meant for use with installations that perform
	 * authentication and user management in an external system, such as LDAP.
	 * This role is not created during JAMWiki setup, and is not available
	 * from the Special:Roles interface; instead it should be assigned by the
	 * LDAP or other system that performs user authentication.
	 */
	public static final RoleImpl ROLE_EMBEDDED = new RoleImpl("ROLE_EMBEDDED");
	/** Role used to control who can import new topics to the wiki using Special:Import. */
	public static final RoleImpl ROLE_IMPORT = new RoleImpl("ROLE_IMPORT");
	public static final RoleImpl ROLE_MOVE = new RoleImpl("ROLE_MOVE");
	/**
	 * ROLE_NO_ACCOUNT is meant for use with installations that do not allow
	 * user account management from within JAMWiki.  This role is not created
	 * during JAMWiki setup, and is not available from the Special:Roles
	 * interface; instead it should be assigned by the LDAP or other system
	 * that allows account management.
	 */
	public static final RoleImpl ROLE_NO_ACCOUNT = new RoleImpl("ROLE_NO_ACCOUNT");
	/** ROLE_SYSADMIN provides the ability to change system settings. */
	public static final RoleImpl ROLE_SYSADMIN = new RoleImpl("ROLE_SYSADMIN");
	public static final RoleImpl ROLE_TRANSLATE = new RoleImpl("ROLE_TRANSLATE");
	public static final RoleImpl ROLE_UPLOAD = new RoleImpl("ROLE_UPLOAD");
	public static final RoleImpl ROLE_VIEW = new RoleImpl("ROLE_VIEW");

	/**
	 *
	 */
	public RoleImpl(String RoleImpl) {
		super((RoleImpl == null) ? null : RoleImpl.toUpperCase());
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