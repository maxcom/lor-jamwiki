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
package org.jmwiki;

import org.jmwiki.persistency.db.DBDate;

/**
 *
 */
public class TopicLock {

	private String virtualWiki;
	private String topicName;
	private DBDate time;
	private String sessionKey;

	/**
	 *
	 */
	public TopicLock(String virtualWiki, String topicName, DBDate time, String sessionKey) {
		this.virtualWiki = virtualWiki;
		this.topicName = topicName;
		this.time = time;
		this.sessionKey = sessionKey;
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
	public DBDate getTime() {
		return time;
	}

	/**
	 *
	 */
	public void setTime(DBDate time) {
		this.time = time;
	}

	/**
	 *
	 */
	public String getSessionKey() {
		return sessionKey;
	}

	/**
	 *
	 */
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
}
