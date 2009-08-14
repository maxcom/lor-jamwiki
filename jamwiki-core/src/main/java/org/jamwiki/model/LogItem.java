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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki log entry.
 */
public class LogItem {

	private static final WikiLogger logger = WikiLogger.getLogger(LogItem.class.getName());
	public static final int LOG_TYPE_ALL = -1;
	public static final int LOG_TYPE_DELETION = 1;
	public static final int LOG_TYPE_IMPORT = 2;
	public static final int LOG_TYPE_MOVE = 3;
	public static final int LOG_TYPE_UPLOAD = 4;
	public static final int LOG_TYPE_USER_CREATION = 5;
	public static Map<Integer, String> LOG_TYPES = new LinkedHashMap<Integer, String>();
	static {
		LOG_TYPES.put(LOG_TYPE_ALL, "logs.caption.logs.all");
		LOG_TYPES.put(LOG_TYPE_DELETION, "logs.caption.logs.deletion");
		LOG_TYPES.put(LOG_TYPE_IMPORT, "logs.caption.logs.import");
		LOG_TYPES.put(LOG_TYPE_MOVE, "logs.caption.logs.move");
		LOG_TYPES.put(LOG_TYPE_UPLOAD, "logs.caption.logs.upload");
		LOG_TYPES.put(LOG_TYPE_USER_CREATION, "logs.caption.logs.user");
	}

	private String logComment = null;
	private Timestamp logDate = null;
	private List<String> logParams = null;
	private int logType = -1;
	private String userDisplayName = null;
	private Integer userId = null;
	private String virtualWiki = null;

	/**
	 *
	 */
	public LogItem() {
	}

	/**
	 * Create a log item from a topic, topic version and author name.  If the topic
	 * version is not valid for logging this method will return <code>null</code>.
	 */
	public static LogItem initLogItem(Topic topic, TopicVersion topicVersion, String authorName) {
		LogItem logItem = new LogItem();
		switch (topicVersion.getEditType()) {
			case TopicVersion.EDIT_MOVE:
				logItem.setLogType(LOG_TYPE_MOVE);
				break;
			case TopicVersion.EDIT_DELETE:
			case TopicVersion.EDIT_UNDELETE:
				logItem.setLogType(LOG_TYPE_DELETION);
				break;
			case TopicVersion.EDIT_PERMISSION:
				// FIXME - implement
				break;
			case TopicVersion.EDIT_IMPORT:
				logItem.setLogType(LOG_TYPE_IMPORT);
				break;
			default:
				// not valid for logging
				return null;
		}
		logItem.setLogComment(topicVersion.getEditComment());
		logItem.setLogDate(topicVersion.getEditDate());
		logItem.setUserDisplayName(authorName);
		logItem.setUserId(topicVersion.getAuthorId());
		logItem.setVirtualWiki(topic.getVirtualWiki());
		return logItem;
	}

	/**
	 * Create a log item from a file, file version and author name.
	 */
	public static LogItem initLogItem(WikiFile wikiFile, WikiFileVersion wikiFileVersion, String authorName) {
		LogItem logItem = new LogItem();
		logItem.setLogType(LOG_TYPE_UPLOAD);
		logItem.setLogComment(wikiFileVersion.getUploadComment());
		logItem.setLogDate(wikiFileVersion.getUploadDate());
		logItem.setUserDisplayName(authorName);
		logItem.setUserId(wikiFileVersion.getAuthorId());
		logItem.setVirtualWiki(wikiFile.getVirtualWiki());
		return logItem;
	}

	/**
	 * Create a log item from a wiki user.
	 */
	public static LogItem initLogItem(WikiUser wikiUser, String virtualWiki) {
		LogItem logItem = new LogItem();
		logItem.setLogType(LOG_TYPE_USER_CREATION);
		logItem.setLogDate(wikiUser.getCreateDate());
		logItem.setUserDisplayName(wikiUser.getUsername());
		logItem.setUserId(wikiUser.getUserId());
		logItem.setVirtualWiki(virtualWiki);
		return logItem;
	}

	/**
	 *
	 */
	public String getLogComment() {
		return this.logComment;
	}

	/**
	 *
	 */
	public void setLogComment(String logComment) {
		this.logComment = logComment;
	}

	/**
	 *
	 */
	public Timestamp getLogDate() {
		return this.logDate;
	}

	/**
	 *
	 */
	public void setLogDate(Timestamp logDate) {
		this.logDate = logDate;
	}

	/**
	 *
	 */
	public List<String> getLogParams() {
		return this.logParams;
	}

	/**
	 *
	 */
	public void setLogParams(List<String> logParams) {
		this.logParams = logParams;
	}

	/**
	 * Utility method for converting the log params to a pipe-delimited string.
	 */
	public String getLogParamString() {
		if (this.logParams == null || this.logParams.isEmpty()) {
			return null;
		}
		String result = "";
		for (String logParam : this.logParams) {
			if (result.length() > 0) {
				result += "|";
			}
			result += logParam;
		}
		return result;
	}

	/**
	 *
	 */
	public int getLogType() {
		return this.logType;
	}

	/**
	 *
	 */
	public void setLogType(int logType) {
		this.logType = logType;
	}

	/**
	 *
	 */
	public String getUserDisplayName() {
		return this.userDisplayName;
	}

	/**
	 *
	 */
	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
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
