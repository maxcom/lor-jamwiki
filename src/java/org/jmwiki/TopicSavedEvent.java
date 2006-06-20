/**
 *
 */
package org.jmwiki;

import java.util.Date;

/**
 * Event fired when a topic has been saved
 */
public class TopicSavedEvent {

	/** Name of the virtual wiki */
	private String virtualWiki;
	/** Name of the topic */
	private String topicName;
	/** The contents saved */
	private String contents;
	/** User */
	private String user;
	/** Time */
	private Date time;

	/**
	 * New event
	 *
	 * @param virtualWiki
	 * @param topicName
	 * @param contents
	 */
	public TopicSavedEvent(String virtualWiki, String topicName, String contents, String user, Date time) {
		this.virtualWiki = virtualWiki;
		this.topicName = topicName;
		this.contents = contents;
		this.user = user;
		this.time = time;
	}

	/**
	 * Name of the virtual wiki
	 * @return
	 */
	public String getVirtualWiki() {
		return virtualWiki;
	}

	/**
	 * Name of the topic
	 * @return
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * Name of the contents
	 * @return
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * User who saved
	 * @return user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Time save was made
	 * @return time
	 */
	public Date getTime() {
		return time;
	}
}
