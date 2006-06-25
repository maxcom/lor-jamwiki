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
package org.jmwiki;

import org.apache.log4j.Logger;

/**
 * Represents a single user account in a JMWiki members list.
 */
public class WikiMember implements java.io.Serializable {

	private static final Logger logger = Logger.getLogger(WikiMember.class);
	private String userName;
	private String email;
	private String key;
	private String password;

	/**
	 *
	 */
	public WikiMember() {
		this.key = NULL_KEY();
	}

	/**
	 *
	 */
	public WikiMember(String newUserName) {
		this.userName = newUserName;
		this.key = NULL_KEY();
	}

	/**
	 *
	 */
	public WikiMember(String newUserName, String newEmail) {
		this.userName = newUserName;
		this.email = newEmail;
		this.key = NULL_KEY();
	}

	/**
	 *
	 */
	private String NULL_KEY() {
		return "NULL";
	}

	/**
	 *
	 */
	private String CONFIRM_KEY() {
		return "";
	}

	/**
	 *
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 *
	 */
	public void setUserName(String newUserName) {
		this.userName = newUserName;
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
	public void setEmail(String newEmail) {
		this.email = newEmail;
	}

	/**
	 *
	 */
	public void confirm() {
		this.key = CONFIRM_KEY();
	}

	/**
	 *
	 */
	public boolean isConfirmed() {
		if (this.key == null) return false;
		return (this.key.equals(CONFIRM_KEY()));
	}

	/**
	 *
	 */
	public boolean isPending() {
		return (!CONFIRM_KEY().equals(this.key) && !this.NULL_KEY().equals(this.key));
	}

	/**
	 *
	 */
	public boolean checkKey(String keyToCheck) {
		logger.debug("Checking that key '" + keyToCheck.trim() + "'=='" + this.key.trim() + "' (" + keyToCheck.equals(this.key) + ")");
		return keyToCheck.trim().equals(this.key.trim());
	}

	/**
	 *
	 */
	public void setKey(String newKey) {
		this.key = newKey;
	}

	/**
	 *
	 */
	public String getKey() {
		return key;
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
	public boolean isValidPassword(String password) {
		return this.password.equals(password);
	}

	/**
	 * Returns a string representation of the object.
	 * @return  a string representation of the object.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getClass().getName());
		buffer.append("[");
		buffer.append("username=");
		buffer.append(this.userName);
		buffer.append(",email=");
		buffer.append(this.email);
		buffer.append(",key=");
		buffer.append(this.key);
		buffer.append(",confirmed=");
		buffer.append(this.isConfirmed());
		buffer.append("]");
		return buffer.toString();
	}
}
