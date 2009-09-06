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
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki recent change.
 */
public class RecentChange {

	private static final WikiLogger logger = WikiLogger.getLogger(RecentChange.class.getName());
	private Integer authorId = null;
	private String authorName = null;
	private Integer charactersChanged = null;
	private String changeComment = null;
	private Timestamp changeDate = null;
	private transient WikiMessage changeWikiMessage = null;
	private Integer editType = null;
	private LogItem logItem = null;
	private Integer previousTopicVersionId = null;
	private Integer topicId = null;
	private String topicName = null;
	private Integer topicVersionId = null;
	private String virtualWiki = null;

	/**
	 *
	 */
	public RecentChange() {
	}

	/**
	 *
	 */
	public static RecentChange initRecentChange(Topic topic, TopicVersion topicVersion, String authorName) {
		RecentChange recentChange = new RecentChange();
		recentChange.setTopicId(topic.getTopicId());
		recentChange.setTopicName(topic.getName());
		recentChange.setTopicVersionId(topicVersion.getTopicVersionId());
		recentChange.setPreviousTopicVersionId(topicVersion.getPreviousTopicVersionId());
		recentChange.setAuthorId(topicVersion.getAuthorId());
		recentChange.setAuthorName(authorName);
		recentChange.setCharactersChanged(topicVersion.getCharactersChanged());
		recentChange.setChangeComment(topicVersion.getEditComment());
		recentChange.setChangeDate(topicVersion.getEditDate());
		recentChange.setEditType(topicVersion.getEditType());
		recentChange.setVirtualWiki(topic.getVirtualWiki());
		recentChange.initChangeWikiMessage(topicVersion.getEditType(), topicVersion.getVersionParamString());
		return recentChange;
	}

	/**
	 *
	 */
	public static RecentChange initRecentChange(LogItem logItem) {
		RecentChange recentChange = new RecentChange();
		recentChange.setAuthorId(logItem.getUserId());
		recentChange.setAuthorName(logItem.getUserDisplayName());
		recentChange.setChangeComment(logItem.getLogComment());
		recentChange.setChangeDate(logItem.getLogDate());
		recentChange.setLogItem(logItem);
		recentChange.setVirtualWiki(logItem.getVirtualWiki());
		recentChange.initChangeWikiMessage(logItem);
		return recentChange;
	}

	/**
	 * Utility method for initializing the LogItem field of this object.
	 */
	public void initLogItem(int logType, String logParamString) {
		this.logItem = new LogItem();
		this.logItem.setLogComment(this.changeComment);
		this.logItem.setLogDate(this.changeDate);
		this.logItem.setLogParamString(logParamString);
		this.logItem.setLogType(logType);
		this.logItem.setUserDisplayName(this.authorName);
		this.logItem.setUserId(this.authorId);
		this.logItem.setVirtualWiki(this.virtualWiki);
		this.initChangeWikiMessage(this.logItem);
	}

	/**
	 *
	 */
	private void initChangeWikiMessage(LogItem logItem) {
		if (logItem.isDelete()) {
			this.setChangeWikiMessage(new WikiMessage("log.message.deletion", logItem.getLogParams()));
		} else if (logItem.isImport()) {
			this.setChangeWikiMessage(new WikiMessage("log.message.import", logItem.getLogParams()));
		} else if (logItem.isMove()) {
			this.setChangeWikiMessage(new WikiMessage("log.message.move", logItem.getLogParams()));
		} else if (logItem.isPermission()) {
			this.setChangeWikiMessage(new WikiMessage("log.message.permission", logItem.getLogParams()));
		} else if (logItem.isUpload()) {
			this.setChangeWikiMessage(new WikiMessage("log.message.upload", logItem.getLogParams()));
		} else if (logItem.isUser()) {
			this.setChangeWikiMessage(new WikiMessage("log.message.user"));
		}
	}

	/**
	 *
	 */
	public void initChangeWikiMessage(int editType, String logParamString) {
		if (StringUtils.isBlank(logParamString)) {
			// older versions of JAMWiki did not have this field, so it may not always be populated as expected
			return;
		}
		if (editType == TopicVersion.EDIT_MOVE) {
			this.setChangeWikiMessage(new WikiMessage("log.message.move", logParamString.split("\\|")));
		}
	}

	/**
	 *
	 */
	public Integer getAuthorId() {
		return this.authorId;
	}

	/**
	 *
	 */
	public void setAuthorId(Integer authorId) {
		this.authorId = authorId;
	}

	/**
	 *
	 */
	public String getAuthorName() {
		return this.authorName;
	}

	/**
	 *
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	/**
	 *
	 */
	public String getChangeComment() {
		return this.changeComment;
	}

	/**
	 *
	 */
	public void setChangeComment(String changeComment) {
		this.changeComment = changeComment;
	}

	/**
	 *
	 */
	public Timestamp getChangeDate() {
		return this.changeDate;
	}

	/**
	 *
	 */
	public void setChangeDate(Timestamp changeDate) {
		this.changeDate = changeDate;
	}

	/**
	 *
	 */
	public String getChangeTypeNotification() {
		StringBuffer changeTypeNotification = new StringBuffer();
		if (this.previousTopicVersionId == null) {
			changeTypeNotification.append('n');
		}
		if (this.editType == null) {
			return "";
		}
		if (this.editType == TopicVersion.EDIT_MINOR) {
			changeTypeNotification.append('m');
		}
		if (this.editType == TopicVersion.EDIT_DELETE) {
			changeTypeNotification.append('d');
		}
		if (this.editType == TopicVersion.EDIT_UNDELETE) {
			changeTypeNotification.append('u');
		}
		if (this.editType == TopicVersion.EDIT_IMPORT) {
			changeTypeNotification.append('i');
		}
		return changeTypeNotification.toString();
	}

	/**
	 * This field is a generated field used to return a <code>WikiMessage</code> object
	 * that represents any auto-generated message information for the recent change entry,
	 * such as "Topic A renamed to Topic B" when renaming a topic.
	 */
	public WikiMessage getChangeWikiMessage() {
		return this.changeWikiMessage;
	}

	/**
	 * This field is a generated field used to return a <code>WikiMessage</code> object
	 * that represents any auto-generated message information for the recent change entry,
	 * such as "Topic A renamed to Topic B" when renaming a topic.
	 */
	public void setChangeWikiMessage(WikiMessage changeWikiMessage) {
		this.changeWikiMessage = changeWikiMessage;
	}

	/**
	 *
	 */
	public Integer getCharactersChanged() {
		return this.charactersChanged;
	}

	/**
	 *
	 */
	public void setCharactersChanged(Integer charactersChanged) {
		this.charactersChanged = charactersChanged;
	}

	/**
	 *
	 */
	public boolean getDelete() {
		return (this.editType != null && this.editType == TopicVersion.EDIT_DELETE);
	}

	/**
	 *
	 */
	public Integer getEditType() {
		return this.editType;
	}

	/**
	 *
	 */
	public void setEditType(Integer editType) {
		this.editType = editType;
	}

	/**
	 *
	 */
	public boolean getImport() {
		return (this.editType != null && this.editType == TopicVersion.EDIT_IMPORT);
	}

	/**
	 *
	 */
	public LogItem getLogItem() {
		return this.logItem;
	}

	/**
	 *
	 */
	public void setLogItem(LogItem logItem) {
		this.logItem = logItem;
	}

	/**
	 *
	 */
	public List<String> getLogParams() {
		return (this.logItem == null) ? null : this.logItem.getLogParams();
	}

	/**
	 * Utility method for converting the log params to a pipe-delimited string.
	 */
	public String getLogParamString() {
		return (this.logItem == null) ? null : this.logItem.getLogParamString();
	}

	/**
	 *
	 */
	public Integer getLogType() {
		return (this.logItem == null) ? null : this.logItem.getLogType();
	}

	/**
	 *
	 */
	public boolean getMinor() {
		return (this.editType != null && this.editType == TopicVersion.EDIT_MINOR);
	}

	/**
	 *
	 */
	public boolean getMove() {
		return (this.editType != null && this.editType == TopicVersion.EDIT_MOVE);
	}

	/**
	 *
	 */
	public boolean getNormal() {
		return (this.editType != null && this.editType == TopicVersion.EDIT_NORMAL);
	}

	/**
	 *
	 */
	public Integer getPreviousTopicVersionId() {
		return this.previousTopicVersionId;
	}

	/**
	 *
	 */
	public void setPreviousTopicVersionId(Integer previousTopicVersionId) {
		this.previousTopicVersionId = previousTopicVersionId;
	}

	/**
	 *
	 */
	public Integer getTopicId() {
		return this.topicId;
	}

	/**
	 *
	 */
	public void setTopicId(Integer topicId) {
		this.topicId = topicId;
	}

	/**
	 *
	 */
	public String getTopicName() {
		return this.topicName;
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
	public Integer getTopicVersionId() {
		return this.topicVersionId;
	}

	/**
	 *
	 */
	public void setTopicVersionId(Integer topicVersionId) {
		this.topicVersionId = topicVersionId;
	}

	/**
	 *
	 */
	public boolean getUndelete() {
		return (this.editType != null && this.editType == TopicVersion.EDIT_UNDELETE);
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
