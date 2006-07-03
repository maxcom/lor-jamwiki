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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.model;

import java.sql.Timestamp;
import org.apache.log4j.Logger;

/**
 *
 */
public class WikiUser {

	// FIXME - consider making this an ACL (more flexible)
	private boolean admin = false;
	private Timestamp createDate = new Timestamp(System.currentTimeMillis());
	private String createIpAddress = null;
	private String displayName = null;
	private String email = null;
	private String encodedPassword = null;
	private String firstName = null;
	private Timestamp lastLoginDate = new Timestamp(System.currentTimeMillis());
	private String lastLoginIpAddress = null;
	private String lastName = null;
	private String login = null;
	private int userId = -1;
	private String virtualWiki = null;
	private static Logger logger = Logger.getLogger(WikiUser.class);

	/**
	 *
	 */
	public WikiUser() {
	}

	/**
	 *
	 */
	public boolean getAdmin() {
		return this.admin;
	}

	/**
	 *
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
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
	public String getLogin() {
		return this.login;
	}

	/**
	 *
	 */
	public void setLogin(String login) {
		this.login = login;
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
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}
}