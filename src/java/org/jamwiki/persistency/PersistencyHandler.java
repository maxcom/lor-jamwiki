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
package org.jamwiki.persistency;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.db.DBDate;

/**
 *
 */
public interface PersistencyHandler {

	/**
	 *
	 */
	void addTopic(Topic topic) throws Exception;

	/**
	 *
	 */
	void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception;

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception;

	/**
	 * Returns all versions of the given topic in reverse chronological order
	 * @param virtualWiki
	 * @param topicName
	 * @return
	 * @throws Exception
	 */
	public List getAllVersions(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception;

	/**
	 *
	 */
	public void write(Topic topic, TopicVersion topicVersion) throws Exception;

	/**
	 *
	 */
	public boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception;

	/**
	 *
	 */
	public boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception;

	/**
	 *
	 */
	public void unlockTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public boolean exists(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception;

	/**
	 *
	 */
	public Collection purgeDeletes(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception;

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int revision1, int revision2, boolean useHtml) throws Exception;

	/**
	 *
	 */
	public java.util.Date lastRevisionDate(String virtualWiki, String topicName) throws Exception;
}
