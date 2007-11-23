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

import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing basic information about a user that is not
 * Wiki-specific.
 */
public class WikiUserInfo {

	private String email = null;
	private String encodedPassword = null;
	private String firstName = null;
	private String lastName = null;
	private int userId = -1;
	private String username = null;
	private static final WikiLogger logger = WikiLogger.getLogger(WikiUserInfo.class.getName());

	/**
	 *
	 */
	public WikiUserInfo() {
	}

	/**
	 *
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 *
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 *
	 */
	public String getEncodedPassword() {
		return this.encodedPassword;
	}

	/**
	 *
	 */
	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	/**
	 *
	 */
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 *
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 *
	 */
	public String getLastName() {
		return this.lastName;
	}

	/**
	 *
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	/**
	 *
	 */
	public String getUsername() {
		return this.username;
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
	public boolean isWriteable() {
		return WikiBase.getUserHandler().isWriteable();
	}
}