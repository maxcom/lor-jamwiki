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
package org.jmwiki.model;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Timestamp;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;
import org.jmwiki.persistency.db.DBDate;
// FIXME - remove this dependency
import org.jmwiki.servlets.JMController;

/**
 *
 */
public class TopicVersion {

	private static final int EDIT_NORMAL = 1;
	private static final int EDIT_MINOR = 2;
	private static final int EDIT_REVERT = 3;
	private static final int EDIT_MOVE = 4;
	// FIXME - change back to -1 once author handling works
	private int authorId = 1;
	private String editComment = null;
	private Timestamp editDate = new Timestamp(System.currentTimeMillis());
	private int editType = EDIT_NORMAL;
	private int topicId = -1;
	private int topicVersionId = -1;
	private String versionContent = null;
	private static Logger logger = Logger.getLogger(TopicVersion.class);

	/**
	 *
	 */
	public TopicVersion() {
	}

	/**
	 *
	 */
	public int getAuthorId() {
		return this.authorId;
	}

	/**
	 *
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	/**
	 *
	 */
	public String getEditComment() {
		return this.editComment;
	}

	/**
	 *
	 */
	public void setEditComment(String editComment) {
		this.editComment = editComment;
	}

	/**
	 *
	 */
	public Timestamp getEditDate() {
		return this.editDate;
	}

	/**
	 *
	 */
	public void setEditDate(Timestamp editDate) {
		this.editDate = editDate;
	}

	/**
	 *
	 */
	public int getEditType() {
		return this.editType;
	}

	/**
	 *
	 */
	public void setEditType(int editType) {
		this.editType = editType;
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
	public int getTopicVersionId() {
		return this.topicVersionId;
	}

	/**
	 *
	 */
	public void setTopicVersionId(int topicVersionId) {
		this.topicVersionId = topicVersionId;
	}

	/**
	 *
	 */
	public String getVersionContent() {
		return this.versionContent;
	}

	/**
	 *
	 */
	public void setVersionContent(String versionContent) {
		this.versionContent = versionContent;
	}

	// ======================================
	// DELETE THE CODE BELOW
	// ======================================

	private String virtualWiki;
	private String topicName;
	private DBDate revisionDate;
	private int versionNumber;
	private String cookedContents;

	/**
	 *
	 */
	public TopicVersion(String virtualWiki, String topicName, DBDate revisionDate, int versionNumber) {
		if (virtualWiki == null) virtualWiki = "";
		this.virtualWiki = virtualWiki;
		this.topicName = topicName;
		this.revisionDate = revisionDate;
		this.versionNumber = versionNumber;
		this.cookedContents = null;
	}

	/**
	 *
	 */
	public int getVersionNumber() {
		return versionNumber;
	}

	/**
	 *
	 */
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	/**
	 *
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 *
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 *
	 */
	public DBDate getRevisionDate() {
		return revisionDate;
	}

	/**
	 *
	 */
	public void setRevisionDate(DBDate revisionDate) {
		this.revisionDate = revisionDate;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}

	/**
	 *
	 */
	public String getRawContents() throws Exception {
		return WikiBase.getInstance().getVersionManagerInstance().getVersionContents(
			this.virtualWiki,
			this.topicName,
			this.versionNumber
		);
	}

	/**
	 *
	 */
	public String getCookedContents() {
		return this.cookedContents;
	 }

	/**
	 *
	 */
	public void setCookedContents(String cookedContents) {
		this.cookedContents = cookedContents;
	 }

	/**
	 *
	 */
	 public String toString() {
		 StringBuffer buffer = new StringBuffer();
		 buffer.append(this.versionNumber);
		 buffer.append(":");
		 buffer.append(this.revisionDate);
		 return buffer.toString();
	 }
}
