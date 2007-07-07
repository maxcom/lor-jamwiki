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

import java.util.Collection;
import java.util.Vector;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a mapping of a user or group to a set of
 * roles.  This class exists primarily as a helper when adding or modifying
 * roles using a form interface.
 */
public class RoleMap {

	private static WikiLogger logger = WikiLogger.getLogger(RoleMap.class.getName());
	private Integer groupId = null;
	private Collection roleNames = null;
	private Integer userId = null;

	/**
	 *
	 */
	public RoleMap() {
	}

	/**
	 *
	 */
	public Integer groupId() {
		return this.groupId;
	}

	/**
	 *
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	/**
	 *
	 */
	public void addRole(String roleName) {
		if (this.roleNames == null) {
			this.roleNames = new Vector();
		}
		this.roleNames.add(roleName);
	}

	/**
	 *
	 */
	 public Collection getRoleNames() {
		return this.roleNames;
	}

	/**
	 *
	 */
	public void setRoleNames(Collection roleNames) {
		this.roleNames = roleNames;
	}

	/**
	 *
	 */
	public Integer getUserId() {
		return this.userId;
	}

	/**
	 *
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}