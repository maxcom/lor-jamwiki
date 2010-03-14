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
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki topic.
 */
public class Topic implements Serializable {

	/* Standard topic type. */
	public static final int TYPE_ARTICLE = 1;
	/* Topic redirects to another topic. */
	public static final int TYPE_REDIRECT = 2;
	/* Topic is an image. */
	public static final int TYPE_IMAGE = 4;
	/* Topic is a category. */
	public static final int TYPE_CATEGORY = 5;
	/* Topic is a non-image file. */
	public static final int TYPE_FILE = 6;
	/* Internal files, do not display on Special:Allpages */
	public static final int TYPE_SYSTEM_FILE = 7;
	/* Wiki templates. */
	public static final int TYPE_TEMPLATE = 8;
	// FIXME - consider making this an ACL (more flexible)
	private boolean adminOnly = false;
	private Integer currentVersionId = null;
	private Timestamp deleteDate = null;
	private String name = null;
	private boolean readOnly = false;
	private String redirectTo = null;
	private String topicContent = null;
	private int topicId = -1;
	private int topicType = TYPE_ARTICLE;
	private String virtualWiki = null;
	private Namespace namespace = Namespace.MAIN;
	private static final WikiLogger logger = WikiLogger.getLogger(Topic.class.getName());

	/**
	 *
	 */
	public Topic() {
	}

	/**
	 *
	 */
	public Topic(Topic topic) {
		this.adminOnly = topic.adminOnly;
		this.currentVersionId = topic.currentVersionId;
		this.deleteDate = topic.deleteDate;
		this.name = topic.name;
		this.readOnly = topic.readOnly;
		this.redirectTo = topic.redirectTo;
		this.topicContent = topic.topicContent;
		this.topicId = topic.topicId;
		this.topicType = topic.topicType;
		this.virtualWiki = topic.virtualWiki;
		this.namespace = topic.namespace;
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
	public Integer getCurrentVersionId() {
		return this.currentVersionId;
	}

	/**
	 *
	 */
	public void setCurrentVersionId(Integer currentVersionId) {
		this.currentVersionId = currentVersionId;
	}

	/**
	 *
	 */
	public boolean getDeleted() {
		return (this.deleteDate != null);
	}

	/**
	 *
	 */
	public Timestamp getDeleteDate() {
		return this.deleteDate;
	}

	/**
	 *
	 */
	public void setDeleteDate(Timestamp deleteDate) {
		this.deleteDate = deleteDate;
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
		WikiLink wikiLink = LinkUtil.parseWikiLink(this.virtualWiki, this.name);
		this.setNamespace(wikiLink.getNamespace());
	}

	/**
	 *
	 */
	public Namespace getNamespace() {
		return this.namespace;
	}

	/**
	 *
	 */
	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
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
	public String getRedirectTo() {
		return this.redirectTo;
	}

	/**
	 *
	 */
	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
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