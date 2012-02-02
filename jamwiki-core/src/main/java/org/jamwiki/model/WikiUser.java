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

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing Wiki-specific information about a user of
 * the Wiki.
 */
public class WikiUser implements Serializable {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUser.class.getName());
	private Timestamp createDate = new Timestamp(System.currentTimeMillis());
	private String createIpAddress = "0.0.0.0";
	private String defaultLocale = null;
	private String displayName = null;
	/** The user's preferred editor (if any). */
	private String editor = Environment.getValue(Environment.PROP_TOPIC_EDITOR);
	private String email = null;
	private Timestamp lastLoginDate = new Timestamp(System.currentTimeMillis());
	private String lastLoginIpAddress = "0.0.0.0";
	/** The user's custom signature (if any). */
	private String signature = null;
	private String username = null;
	private int userId = -1;
  private String style = "tango";

	/**
	 *
	 */
	public WikiUser() {
	}

	/**
	 *
	 */
	public WikiUser(String username) {
		this.username = username;
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
	public String getDefaultLocale() {
		return this.defaultLocale;
	}

	/**
	 *
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
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
	public String getEditor() {
		return (StringUtils.isBlank(this.editor)) ? Environment.getValue(Environment.PROP_TOPIC_EDITOR) : this.editor;
	}

	/**
	 *
	 */
	public void setEditor(String editor) {
		this.editor = editor;
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
	public String getSignature() {
		return this.signature;
	}

	/**
	 *
	 */
	public void setSignature(String signature) {
		this.signature = signature;
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
		return username;
	}

  public String getStyle() {
    return style;
  }

  public void setStyle(String style1) {
    style = style1;
  }
}
