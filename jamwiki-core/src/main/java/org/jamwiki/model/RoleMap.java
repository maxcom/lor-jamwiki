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
import java.util.HashMap;
import java.util.Vector;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a mapping of a user or group to a set of
 * roles.  This class exists primarily as a helper when adding or modifying
 * roles using a form interface.
 */
public class RoleMap {

	private static final WikiLogger logger = WikiLogger.getLogger(RoleMap.class.getName());
	private Integer groupId = null;
	private String groupName = null;
	private Collection<String> roleNames = null;
	private Integer userId = null;
	private String userLogin = null;

	/**
	 *
	 */
	public RoleMap() {
	}

	/**
	 *
	 */
	public Integer getGroupId() {
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
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 *
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 *
	 */
	public void addRole(String roleName) {
		if (this.roleNames == null) {
			this.roleNames = new Vector<String>();
		}
		this.roleNames.add(roleName);
	}

	/**
	 *
	 */
	 public Collection<String> getRoleNames() {
		return this.roleNames;
	}

	/**
	 *
	 */
	public void setRoleNames(Collection<String> roleNames) {
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

	/**
	 *
	 */
	public String getUserLogin() {
		return this.userLogin;
	}

	/**
	 *
	 */
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	/**
	 * This method is simply a utility method to be used with JSTL for
	 * determining if the current list of roles contains a specific role.
	 */
	 public HashMap<String, String> getRoleNamesMap() {
		HashMap<String, String> results = new HashMap<String, String>();
		if (this.roleNames == null) {
			return results;
		}
		for (String key : this.roleNames) {
			String value = this.getUserGroup() + "|" + key;
			results.put(key, value);
		}
		return results;
	}

	/**
	 * This is a utility method for building a concatenated version of the
	 * user and group id values for use with JSTL.
	 */
	public String getUserGroup() {
		String result = "";
		if (this.userId != null) {
			result += this.userId;
		} else {
			result += "0";
		}
		result += "|";
		if (this.groupId != null) {
			result += this.groupId;
		} else {
			result += "0";
		}
		return result;
	}
}