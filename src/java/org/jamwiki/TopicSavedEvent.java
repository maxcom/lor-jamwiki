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
package org.jamwiki;

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
