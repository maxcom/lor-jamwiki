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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMember;
import org.jamwiki.WikiMembers;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.db.DBDate;
// FIXME - get rid of these imports
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.persistency.db.DatabaseNotify;
import org.jamwiki.persistency.db.DatabaseWikiMembers;
import org.jamwiki.persistency.file.FileHandler;
import org.jamwiki.persistency.file.FileNotify;
import org.jamwiki.persistency.file.FileWikiMembers;
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
	public static void convert(PersistencyHandler fromHandler, PersistencyHandler toHandler) throws Exception {
		Collection virtualWikis = fromHandler.getVirtualWikiList();
		for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
			String virtualWiki = (String) virtualWikiIterator.next();
			try {
				toHandler.addVirtualWiki(virtualWiki);
			} catch (Exception e) {
				logger.error("Unable to convert virtual wiki to file: " + virtualWiki + ": " + e.getMessage());
			}
			// Topics
			Collection topicNames = fromHandler.getAllTopicNames(virtualWiki);
			for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				try {
					Topic topic = fromHandler.lookupTopic(virtualWiki, topicName);
					toHandler.addTopic(topic);
				} catch (Exception e) {
					logger.error("Unable to convert topic to file: " + topicName + " / " + virtualWiki, e);
				}
			}
			// Versions
			for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				List versions = fromHandler.getAllVersions(virtualWiki, topicName);
				for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
					TopicVersion topicVersion = (TopicVersion) topicVersionIterator.next();
					try {
						toHandler.addTopicVersion(virtualWiki, topicName, topicVersion);
					} catch (Exception e) {
						logger.error("Unable to convert topic version to file: " + topicName + " / " + virtualWiki + ": " + e.getMessage());
					}
				}
			}
			// Read-only topics
			Collection readOnlys = fromHandler.getReadOnlyTopics(virtualWiki);
			for (Iterator readOnlyIterator = readOnlys.iterator(); readOnlyIterator.hasNext();) {
				String topicName = (String) readOnlyIterator.next();
				try {
					toHandler.addReadOnlyTopic(virtualWiki, topicName);
				} catch (Exception e) {
					logger.error("Unable to convert read-only topic to file: " + topicName + " / " + virtualWiki + ": " + e.getMessage());
				}
			}
			// Locks
			Collection locks = fromHandler.getLockList(virtualWiki);
			for (Iterator lockIterator = locks.iterator(); lockIterator.hasNext();) {
				Topic topic = (Topic)lockIterator.next();
				String topicName = topic.getName();
				try {
					toHandler.lockTopic(virtualWiki, topicName, topic.getLockSessionKey());
				} catch (Exception e) {
					logger.error("Unable to write lock file: " + topicName + " / " + virtualWiki + ": " + e.getMessage());
				}
			}
			// Members
			WikiMembers fileMembers = new FileWikiMembers(virtualWiki);
			WikiMembers databaseMembers = new DatabaseWikiMembers(virtualWiki);
			Collection members = databaseMembers.getAllMembers();
			for (Iterator memberIterator = members.iterator(); memberIterator.hasNext();) {
				WikiMember wikiMember = (WikiMember) memberIterator.next();
				try {
					fileMembers.addMember(
						wikiMember.getUserName(),
						wikiMember.getEmail(),
						wikiMember.getKey()
					);
				} catch (Exception e) {
					logger.error("Unable to convert wiki member to file: " + wikiMember.getUserName() + ": " + e.getMessage());
				}
			}
			// Notifications
			Collection databaseNotifications = DatabaseNotify.getAllNotifications(virtualWiki);
			for (Iterator iterator = databaseNotifications.iterator(); iterator.hasNext();) {
				DatabaseNotify databaseNotify = (DatabaseNotify) iterator.next();
				FileNotify fileNotify = new FileNotify(virtualWiki, databaseNotify.getTopicName());
				Collection notifyMembers = databaseNotify.getMembers();
				for (Iterator notifyMemberIterator = notifyMembers.iterator(); notifyMemberIterator.hasNext();) {
					String memberName = (String) notifyMemberIterator.next();
					try {
						fileNotify.addMember(memberName);
					} catch (Exception e) {
						logger.error("Unable to convert notify member to file: " + memberName + ": " + e.getMessage());
					}
				}
			}
			// recent changes
			Collection changes = fromHandler.getRecentChanges(virtualWiki, 1000);
			for (Iterator changeIterator = changes.iterator(); changeIterator.hasNext();) {
				RecentChange change = (RecentChange)changeIterator.next();
				try {
					toHandler.addRecentChange(change);
				} catch (Exception e) {
					logger.error("Unable to convert recent change to file: " + virtualWiki + ": " + e.getMessage());
				}
			}
		}
	}

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
	public boolean exists(String virtualWiki, String topicName) throws Exception {
		return (lookupTopic(virtualWiki, topicName) != null);
	}

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
	public boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception {
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null) return true;
		if (topic.getLockSessionKey() != null) {
			// a lock still exists, see if it was taken by the current user
			if (topic.getLockSessionKey().equals(key)) {
				// same user still has the lock, return true
				return true;
			}
			// see if the existing lock has expired
			Timestamp expireDate = new Timestamp(topic.getLockedDate().getTime() + (60000 * Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT)));
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if (now.before(expireDate)) {
				// lock is still valid, return false
				return false;
			}
		}
		topic.setLockSessionKey(key);
		topic.setLockedDate(new Timestamp(System.currentTimeMillis()));
		// FIXME - save author
		//topic.setLockedBy(authorId);
		addTopic(topic);
		return true;
	}

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
	public void unlockTopic(Topic topic) throws Exception {
		topic.setLockSessionKey(null);
		topic.setLockedDate(null);
		topic.setLockedBy(-1);
		addTopic(topic);
	}


	/**
	 *
	 */
	public synchronized void write(Topic topic, TopicVersion topicVersion) throws Exception {
		int previousTopicVersionId = 0;
		if (topic.getTopicId() > 0) {
			TopicVersion oldVersion = lookupLastTopicVersion(topic.getVirtualWiki(), topic.getName());
			if (oldVersion != null) previousTopicVersionId = oldVersion.getTopicVersionId();
		}
		// release any lock that is held by setting lock fields null
		topic.setLockedBy(-1);
		topic.setLockedDate(null);
		topic.setLockSessionKey(null);
		addTopic(topic);
		topicVersion.setTopicId(topic.getTopicId());
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			// write version
			addTopicVersion(topic.getVirtualWiki(), topic.getName(), topicVersion);
		}
		RecentChange change = new RecentChange();
		change.setTopicId(topic.getTopicId());
		change.setTopicName(topic.getName());
		change.setTopicVersionId(topicVersion.getTopicVersionId());
		change.setPreviousTopicVersionId(previousTopicVersionId);
		change.setAuthorId(topicVersion.getAuthorId());
		// FIXME - should be the actual author name
		change.setAuthorName(topicVersion.getAuthorIpAddress());
		change.setEditComment(topicVersion.getEditComment());
		change.setEditDate(topicVersion.getEditDate());
		change.setEditType(topicVersion.getEditType());
		change.setVirtualWiki(topic.getVirtualWiki());
		addRecentChange(change);
	}
}
