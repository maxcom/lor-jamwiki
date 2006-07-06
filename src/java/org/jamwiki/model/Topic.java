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

import java.sql.Timestamp;
import org.apache.log4j.Logger;

/**
 *
 */
public class Topic {

	public static final int TYPE_ARTICLE = 1;
	public static final int TYPE_REDIRECT = 2;
	public static final int TYPE_TALK_PAGE = 3;
	// FIXME - consider making this an ACL (more flexible)
	private boolean adminOnly = false;
	private boolean deleted = false;
	private Integer lockedBy = null;
	private Timestamp lockedDate = null;
	private String lockSessionKey = null;
	private String name = null;
	private boolean readOnly = false;
	private String topicContent = null;
	private int topicId = -1;
	private int topicType = TYPE_ARTICLE;
	private String virtualWiki = null;
	private static Logger logger = Logger.getLogger(Topic.class);

	/**
	 *
	 */
	public Topic() {
	}

	/**
	 *
	 */
	public boolean getAdminOnly() {
		return this.adminOnly;
	}

	/**
	 *
	 */
	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	/**
	 *
	 */
	public boolean getDeleted() {
		return this.deleted;
	}

	/**
	 *
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 *
	 */
	public Integer getLockedBy() {
		return this.lockedBy;
	}

	/**
	 *
	 */
	public void setLockedBy(Integer lockedBy) {
		this.lockedBy = lockedBy;
	}

	/**
	 *
	 */
	public Timestamp getLockedDate() {
		return this.lockedDate;
	}

	/**
	 *
	 */
	public void setLockedDate(Timestamp lockedDate) {
		this.lockedDate = lockedDate;
	}

	/**
	 *
	 */
	public String getLockSessionKey() {
		return this.lockSessionKey;
	}

	/**
	 *
	 */
	public void setLockSessionKey(String lockSessionKey) {
		this.lockSessionKey = lockSessionKey;
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public boolean getReadOnly() {
		return this.readOnly;
	}

	/**
	 *
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 *
	 */
	public String getTopicContent() {
		return this.topicContent;
	}

	/**
	 *
	 */
	public void setTopicContent(String topicContent) {
		this.topicContent = topicContent;
	}

	/**
	 *
	 */
	public int getTopicId() {
		return this.topicId;
	}

	/**
	 *
	 */
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	/**
	 *
	 */
	public int getTopicType() {
		return this.topicType;
	}

	/**
	 *
	 */
	public void setTopicType(int topicType) {
		this.topicType = topicType;
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