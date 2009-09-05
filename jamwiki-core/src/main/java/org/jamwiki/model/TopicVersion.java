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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a version of a Wiki topic.
 */
public class TopicVersion implements Serializable {

	public static final int EDIT_NORMAL = 1;
	public static final int EDIT_MINOR = 2;
	public static final int EDIT_REVERT = 3;
	public static final int EDIT_MOVE = 4;
	public static final int EDIT_DELETE = 5;
	public static final int EDIT_PERMISSION = 6;
	public static final int EDIT_UNDELETE = 7;
	public static final int EDIT_IMPORT = 8;
	private Integer authorId = null;
	private String authorDisplay = null;
	private int charactersChanged = 0;
	private String editComment = null;
	/**
	 * This field is seldom used but may occasionally be populated when a version is created as a result
	 * of something like a page move.  In such a case a system message (Page moved from A to B) will be
	 * stored in this field and appended to the editComment prior to saving the topic version.
	 */
	private String editCommentAuto = null;
	private Timestamp editDate = new Timestamp(System.currentTimeMillis());
	private int editType = EDIT_NORMAL;
	/** This field is not persisted and is simply used when writing versions to indicate whether the version can be logged. */
	private boolean loggable = true;
	private Integer previousTopicVersionId = null;
	/** Some versions should be created without creating a recent change entry.  This field is not persisted. */
	private boolean recentChangeAllowed = true;
	private int topicId = -1;
	private int topicVersionId = -1;
	private String versionContent = null;
	private List<String> versionParams = null;
	private static final WikiLogger logger = WikiLogger.getLogger(TopicVersion.class.getName());

	/**
	 *
	 */
	public TopicVersion() {
	}

	/**
	 *
	 */
	public TopicVersion(WikiUser user, String authorDisplay, String editComment, String versionContent, int charactersChanged) {
		if (user != null && user.getUserId() > 0) {
			this.authorId = user.getUserId();
		}
		this.authorDisplay = authorDisplay;
		this.editComment = editComment;
		this.versionContent = versionContent;
		this.charactersChanged = charactersChanged;
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
	public String getAuthorDisplay() {
		return this.authorDisplay;
	}

	/**
	 *
	 */
	public void setAuthorDisplay(String authorDisplay) {
		this.authorDisplay = authorDisplay;
	}

	/**
	 *
	 */
	public int getCharactersChanged() {
		return this.charactersChanged;
	}

	/**
	 *
	 */
	public void setCharactersChanged(int charactersChanged) {
		this.charactersChanged = charactersChanged;
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
	public String getEditCommentAuto() {
		return this.editCommentAuto;
	}

	/**
	 *
	 */
	public void setEditCommentAuto(String editCommentAuto) {
		this.editCommentAuto = editCommentAuto;
	}

	/**
	 * Utility method for concatenating the auto edit comment and the edit comment.
	 */
	public String getEditCommentFull() {
		String result = null;
		if (!StringUtils.isBlank(this.editCommentAuto)) {
			result = this.editCommentAuto;
		}
		if (!StringUtils.isBlank(this.editComment)) {
			if (StringUtils.isBlank(this.editCommentAuto)) {
				result = this.editComment;
			} else {
				result += " (" + this.editComment + ")";
			}
		}
		return result;
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
	public boolean isLoggable() {
		return this.loggable;
	}

	/**
	 *
	 */
	public void setLoggable(boolean loggable) {
		this.loggable = loggable;
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
	public boolean isRecentChangeAllowed() {
		return this.recentChangeAllowed;
	}

	/**
	 *
	 */
	public void setRecentChangeAllowed(boolean recentChangeAllowed) {
		this.recentChangeAllowed = recentChangeAllowed;
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

	/**
	 *
	 */
	public List<String> getVersionParams() {
		return this.versionParams;
	}

	/**
	 *
	 */
	public void setVersionParams(List<String> versionParams) {
		this.versionParams = versionParams;
	}

	/**
	 * Utility method for converting the version params to a pipe-delimited string.
	 */
	public String getVersionParamString() {
		if (this.versionParams == null || this.versionParams.isEmpty()) {
			return null;
		}
		String result = "";
		for (String versionParam : this.versionParams) {
			if (result.length() > 0) {
				result += "|";
			}
			result += versionParam;
		}
		return result;
	}

	/**
	 * Utility method for converting a version params pipe-delimited string to a list.
	 */
	public void setVersionParamString(String versionParamsString) {
		if (!StringUtils.isBlank(versionParamsString)) {
			List<String> versionParams = Arrays.asList(versionParamsString.split("\\|"));
			this.setVersionParams(versionParams);
		}
	}
}
