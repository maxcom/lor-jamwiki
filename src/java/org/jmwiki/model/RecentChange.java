/**
 *
 */
package org.jmwiki.model;

import java.sql.Timestamp;
import org.apache.log4j.Logger;

/**
 *
 */
public class RecentChange {

	private static Logger logger = Logger.getLogger(RecentChange.class);
	private String editComment = null;
	private Timestamp editDate = null;
	private int editType = -1;
	private int topicId = -1;
	private String topicName = null;
	private int topicVersionId = -1;

	/**
	 *
	 */
	public RecentChange() {
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
	public int getTopicVersionId() {
		return this.topicVersionId;
	}

	/**
	 *
	 */
	public void setTopicVersionId(int topicVersionId) {
		this.topicVersionId = topicVersionId;
	}
}
