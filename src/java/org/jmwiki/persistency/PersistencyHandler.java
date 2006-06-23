/**
 *
 */
package org.jmwiki.persistency;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.jmwiki.model.Topic;
import org.jmwiki.model.TopicVersion;
import org.jmwiki.persistency.db.DBDate;

/**
 *
 */
public interface PersistencyHandler {

	/**
	 *
	 */
	void addTopicVersion(String virtualWiki, String topicName, String contents, Date at, String ipAddress) throws Exception;

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public String read(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public void write(String virtualWiki, String contents, String topicName, String ipAddress) throws Exception;

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
	public boolean isTopicReadOnly(String virtualWiki, String topicName) throws Exception;

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
}
