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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.db.DBDate;
import org.jamwiki.utils.DiffUtil;
import org.jamwiki.utils.Utilities;
import org.apache.log4j.Logger;

/**
 *
 */
public abstract class PersistencyHandler {

	private static Logger logger = Logger.getLogger(PersistencyHandler.class);

	// FIXME - move to more logical location
	public static final String DEFAULT_PASSWORD = "password";
	public static final String DEFAULT_AUTHOR_LOGIN = "unknown_author";
	public static final String DEFAULT_AUTHOR_NAME = "Unknown Author";
	public static final String DEFAULT_AUTHOR_IP_ADDRESS = "0.0.0.0";

	/**
	 *
	 */
	public abstract void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract void addRecentChange(RecentChange change) throws Exception;

	/**
	 *
	 */
	public abstract void addTopic(Topic topic) throws Exception;

	/**
	 *
	 */
	public abstract void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception;

	/**
	 *
	 */
	public abstract void addVirtualWiki(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int topicVersionId1, int topicVersionId2, boolean useHtml) throws Exception {
		TopicVersion version1 = lookupTopicVersion(virtualWiki, topicName, topicVersionId1);
		TopicVersion version2 = lookupTopicVersion(virtualWiki, topicName, topicVersionId2);
		if (version1 == null && version2 == null) {
			String msg = "Versions " + topicVersionId1 + " and " + topicVersionId2 + " not found for " + topicName + " / " + virtualWiki;
			logger.error(msg);
			throw new Exception(msg);
		}
		String contents1 = null;
		if (version1 != null) {
			contents1 = version1.getVersionContent();
		}
		String contents2 = null;
		if (version2 != null) {
			contents2 = version2.getVersionContent();
		}
		if (contents1 == null && contents2 == null) {
			String msg = "No versions found for " + topicVersionId1 + " against " + topicVersionId2;
			logger.error(msg);
			throw new Exception(msg);
		}
		return DiffUtil.diff(contents1, contents2, useHtml);
	}

	/**
	 *
	 */
	public abstract boolean exists(String virtualWiki, String topicName) throws Exception;

	/**
	 * Returns all versions of the given topic in reverse chronological order
	 * @param virtualWiki
	 * @param topicName
	 * @return
	 * @throws Exception
	 */
	public abstract List getAllVersions(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract List getAllTopicNames(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract List getLockList(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract int getNumberOfVersions(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract Collection getReadOnlyTopics(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract Collection getRecentChanges(String virtualWiki, int numChanges) throws Exception;

	/**
	 *
	 */
	public abstract Collection getVirtualWikiList() throws Exception;

	/**
	 *
	 */
	public abstract boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception;

	/**
	 *
	 */
	public Date lastRevisionDate(String virtualWiki, String topicName) throws Exception {
		if (!Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			return null;
		}
		TopicVersion version = lookupLastTopicVersion(virtualWiki, topicName);
		return version.getEditDate();
	}

	/**
	 *
	 */
	public abstract boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception;

	/**
	 *
	 */
	public abstract TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract Topic lookupTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception;

	/**
	 *
	 */
	public abstract Collection purgeDeletes(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception;

	/**
	 * Finds a default topic file and returns the contents
	 *
	 * FIXME - this doesn't belong here
	 */
	public static String readDefaultTopic(String topicName) throws Exception {
		String filename = Utilities.encodeURL(topicName) + ".txt";
		String contents = null;
		try {
			contents = Utilities.readFile(filename);
		} catch (Exception e) {
			logger.error("Failure while reading from file " + filename, e);
			throw new Exception("Failure while reading from file " + filename + " " + e.getMessage());
		}
		return contents;
	}

	/**
	 *
	 */
	public abstract void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	protected void setupSpecialPage(String virtualWiki, String topicName) throws Exception {
		if (exists(virtualWiki, topicName)) {
			return;
		}
		String baseFileDir = Environment.getValue(Environment.PROP_BASE_FILE_DIR);
		if (baseFileDir == null || baseFileDir.length() == 0) {
			// do not set up special pages until initial property values set
			return;
		}
		String contents = PersistencyHandler.readDefaultTopic(topicName);
		Topic topic = new Topic();
		topic.setName(topicName);
		topic.setVirtualWiki(virtualWiki);
		topic.setTopicContent(contents);
		TopicVersion topicVersion = new TopicVersion();
		topicVersion.setVersionContent(contents);
		topicVersion.setAuthorIpAddress(PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS);
		write(topic, topicVersion);
	}

	/**
	 *
	 */
	public abstract void unlockTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract void write(Topic topic, TopicVersion topicVersion) throws Exception;
}
