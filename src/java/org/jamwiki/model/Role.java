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

import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki role.
 */
public class Role {

	private static WikiLogger logger = WikiLogger.getLogger(Role.class.getName());
	private String description = null;
	private String name = null;
	private int roleId = 0;

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_ADMIN_MANAGE = "ROLE_ADMIN_MANAGE";
	public static final String ROLE_EDIT_EXISTING = "ROLE_EDIT_EXISTING";
	public static final String ROLE_EDIT_NEW = "ROLE_EDIT_NEW";
	public static final String ROLE_MOVE = "ROLE_MOVE";
	public static final String ROLE_TRANSLATE = "ROLE_TRANSLATE";
	public static final String ROLE_UPLOAD = "ROLE_UPLOAD";
	public static final String ROLE_VIEW = "ROLE_VIEW";

	/**
	 *
	 */
	public Role() {
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
	 * Retrieve the role name.  Role names are returned in uppercase.
	 */
	public String getName() {
		return (this.name == null) ? null : this.name.toUpperCase();
	}

	/**
	 * Set the role name.  Role names will be forced to uppercase.
	 */
	public void setName(String name) {
		this.name = ((name == null) ? null : name.toUpperCase());
	}

	/**
	 *
	 */
	public int getRoleId() {
		return this.roleId;
	}

	/**
	 *
	 */
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
}