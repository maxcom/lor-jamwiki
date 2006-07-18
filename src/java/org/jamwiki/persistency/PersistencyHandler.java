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
package org.jamwiki.persistency;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.persistency.file.FileHandler;
import org.jamwiki.utils.DiffUtil;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;

/**
 *
 */
public abstract class PersistencyHandler {

	private static Logger logger = Logger.getLogger(PersistencyHandler.class);
	/** For performance reasons, keep a (small) list of recently looked-up topics around in memory. */
	private static Vector cachedTopicsList = new Vector();
	/** For performance reasons, keep a (small) list of recently looked-up non-topics around in memory. */
	private static Vector cachedNonTopicsList = new Vector();
	/** For performance reasons, keep a (small) list of recently looked-up user logins and ids around in memory. */
	private static Hashtable cachedUserLoginHash = new Hashtable();
	// FIXME - possibly make this a property, or configurable based on number of topics in the system
	private static int MAX_CACHED_LIST_SIZE = 2000;

	/**
	 *
	 */
	protected abstract void addRecentChange(RecentChange change) throws Exception;

	/**
	 *
	 */
	protected abstract void addTopic(Topic topic) throws Exception;

	/**
	 *
	 */
	protected abstract void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception;

	/**
	 *
	 */
	protected abstract void addVirtualWiki(String virtualWiki) throws Exception;

	/**
	 *
	 */
	protected abstract void addWikiFile(String topicName, WikiFile wikiFile) throws Exception;

	/**
	 *
	 */
	protected abstract void addWikiFileVersion(String virtualWiki, String topicName, WikiFileVersion wikiFileVersion) throws Exception;

	/**
	 *
	 */
	protected abstract void addWikiUser(WikiUser user) throws Exception;

	/**
	 * Convert all data from one handler to another.  Note: this method will
	 * delete all data in the destination handler.  Thus, when converting from
	 * file to database, the database is completely re-initialized and all data
	 * erased.  Do not use this method unless you know what you are doing.
	 */
	public static Vector convert(PersistencyHandler fromHandler, PersistencyHandler toHandler) throws Exception {
		// FIXME - hard coding of messages
		Vector messages = new Vector();
		// purge EVERYTHING from the destination handler
		toHandler.purgeData();
		if (toHandler instanceof DatabaseHandler) {
			messages.add("Deleting all database data.");
		} else {
			messages.add("Deleting all files from " + Environment.getValue(Environment.PROP_BASE_FILE_DIR));
		}
		// users
		Collection userNames = fromHandler.getAllWikiUserLogins();
		for (Iterator userIterator = userNames.iterator(); userIterator.hasNext();) {
			String userName = (String)userIterator.next();
			try {
				WikiUser user = fromHandler.lookupWikiUser(userName);
				toHandler.addWikiUser(user);
				messages.add("Added user " + userName);
			} catch (Exception e) {
				String msg = "Unable to convert user: " + userName;
				logger.error(msg, e);
				messages.add(msg + ": " + e.getMessage());
			}
		}
		Collection virtualWikis = fromHandler.getVirtualWikiList();
		for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
			String virtualWiki = (String) virtualWikiIterator.next();
			try {
				toHandler.addVirtualWiki(virtualWiki);
				messages.add("Added virtual wiki " + virtualWiki);
			} catch (Exception e) {
				String msg = "Unable to convert virtual wiki " + virtualWiki;
				logger.error(msg, e);
				messages.add(msg + ": " + e.getMessage());
			}
			// topics
			Collection topicNames = fromHandler.getAllTopicNames(virtualWiki);
			for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String)topicIterator.next();
				try {
					Topic topic = fromHandler.lookupTopic(virtualWiki, topicName);
					toHandler.addTopic(topic);
					messages.add("Added topic " + virtualWiki + " / " + topicName);
				} catch (Exception e) {
					String msg = "Unable to convert topic: " + virtualWiki + " / " + topicName;
					logger.error(msg, e);
					messages.add(msg + ": " + e.getMessage());
				}
			}
			// topic versions
			for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String)topicIterator.next();
				List versions = fromHandler.getAllTopicVersions(virtualWiki, topicName, false);
				for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
					TopicVersion topicVersion = (TopicVersion)topicVersionIterator.next();
					try {
						toHandler.addTopicVersion(virtualWiki, topicName, topicVersion);
						messages.add("Added topic version " + virtualWiki + " / " + topicName + " / " + topicVersion.getTopicVersionId());
					} catch (Exception e) {
						String msg = "Unable to convert topic version: " + virtualWiki + " / " + topicName + " / " + topicVersion.getTopicVersionId();
						logger.error(msg, e);
						messages.add(msg + ": " + e.getMessage());
					}
				}
			}
			// read-only topics
			Collection readOnlys = fromHandler.getReadOnlyTopics(virtualWiki);
			for (Iterator readOnlyIterator = readOnlys.iterator(); readOnlyIterator.hasNext();) {
				String topicName = (String)readOnlyIterator.next();
				try {
					toHandler.writeReadOnlyTopic(virtualWiki, topicName);
					messages.add("Added read-only topic " + virtualWiki + " / " + topicName);
				} catch (Exception e) {
					String msg = "Unable to convert read-only topic: " + virtualWiki + " / " + topicName;
					logger.error(msg, e);
					messages.add(msg + ": " + e.getMessage());
				}
			}
			// wiki files
			Collection wikiFileNames = fromHandler.getAllWikiFileTopicNames(virtualWiki);
			for (Iterator wikiFileIterator = wikiFileNames.iterator(); wikiFileIterator.hasNext();) {
				String topicName = (String)wikiFileIterator.next();
				try {
					WikiFile wikiFile = fromHandler.lookupWikiFile(virtualWiki, topicName);
					toHandler.addWikiFile(topicName, wikiFile);
					messages.add("Added wiki file " + virtualWiki + " / " + topicName);
				} catch (Exception e) {
					String msg = "Unable to convert wiki file: " + virtualWiki + " / " + topicName;
					logger.error(msg, e);
					messages.add(msg + ": " + e.getMessage());
				}
			}
			// wiki file versions
			for (Iterator topicIterator = wikiFileNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String)topicIterator.next();
				List versions = fromHandler.getAllWikiFileVersions(virtualWiki, topicName, false);
				for (Iterator wikiFileVersionIterator = versions.iterator(); wikiFileVersionIterator.hasNext();) {
					WikiFileVersion wikiFileVersion = (WikiFileVersion)wikiFileVersionIterator.next();
					try {
						toHandler.addWikiFileVersion(virtualWiki, topicName, wikiFileVersion);
						messages.add("Added wiki file version " + virtualWiki + " / " + topicName + " / " + wikiFileVersion.getFileVersionId());
					} catch (Exception e) {
						String msg = "Unable to convert wiki file version: " + virtualWiki + " / " + topicName;
						logger.error(msg, e);
						messages.add(msg + ": " + e.getMessage());
					}
				}
			}
			// recent changes
			Collection changes = fromHandler.getRecentChanges(virtualWiki, 1000, false);
			for (Iterator changeIterator = changes.iterator(); changeIterator.hasNext();) {
				RecentChange change = (RecentChange)changeIterator.next();
				try {
					toHandler.addRecentChange(change);
					messages.add("Added recent change " + virtualWiki + " / " + change.getTopicName());
				} catch (Exception e) {
					String msg = "Unable to convert recent change: " + virtualWiki + " / " + change.getTopicName();
					logger.error(msg, e);
					messages.add(msg + ": " + e.getMessage());
				}
			}
		}
		return messages;
	}

	/**
	 *
	 */
	public void deleteReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		Topic topic = lookupTopic(virtualWiki, topicName);
		topic.setReadOnly(false);
		updateTopic(topic);
	}

	/**
	 *
	 */
	public void deleteTopic(Topic topic) throws Exception {
		topic.setDeleted(true);
		// update recent changes
		updateTopic(topic);
		// reset topic existence vector
		cachedTopicsList = new Vector();
	}

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int topicVersionId1, int topicVersionId2, boolean useHtml, Locale locale) throws Exception {
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
		return DiffUtil.diff(contents1, contents2, useHtml, locale);
	}

	/**
	 * See if a topic exists and if it has not been deleted.
	 */
	public boolean exists(String virtualWiki, String topicName) throws Exception {
		if (!StringUtils.hasText(virtualWiki) || !StringUtils.hasText(topicName)) {
			return false;
		}
		// first check a cache of recently looked-up topics for performance reasons
		String key = virtualWiki + "/" + topicName;
		if (cachedTopicsList.contains(key)) {
			return true;
		}
		if (cachedNonTopicsList.contains(key)) {
			return false;
		}
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null || topic.getDeleted()) {
			cachedNonTopicsList.add(key);
			while (cachedNonTopicsList.size() > MAX_CACHED_LIST_SIZE) {
				cachedNonTopicsList.removeElementAt(0);
			}
			return false;
		}
		cachedTopicsList.add(key);
		while (cachedTopicsList.size() > MAX_CACHED_LIST_SIZE) {
			cachedTopicsList.removeElementAt(0);
		}
		return true;
	}

	/**
	 *
	 */
	public abstract List getAllTopicVersions(String virtualWiki, String topicName, boolean descending) throws Exception;

	/**
	 *
	 */
	public abstract List getAllTopicNames(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract List getAllWikiFileTopicNames(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract List getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception;

	/**
	 *
	 */
	public abstract List getAllWikiUserLogins() throws Exception;

	/**
	 *
	 */
	public abstract Collection getReadOnlyTopics(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public abstract Collection getRecentChanges(String virtualWiki, int numChanges, boolean descending) throws Exception;

	/**
	 *
	 */
	public Vector getRecentChanges(String virtualWiki, String topicName, boolean descending) throws Exception {
		Vector results = new Vector();
		Collection versions = getAllTopicVersions(virtualWiki, topicName, descending);
		for (Iterator iterator = versions.iterator(); iterator.hasNext();) {
			TopicVersion version = (TopicVersion)iterator.next();
			RecentChange change = new RecentChange();
			Integer authorId = version.getAuthorId();
			change.setAuthorId(authorId);
			if (authorId != null) {
				String login = lookupWikiUserLogin(authorId);
				change.setAuthorName(login);
			} else {
				change.setAuthorName(version.getAuthorIpAddress());
			}
			change.setEditComment(version.getEditComment());
			change.setEditDate(version.getEditDate());
			change.setEditType(version.getEditType());
			// NOTE! previous topic version not set!
			change.setTopicId(version.getTopicId());
			// NOTE! topic name not set!
			change.setTopicVersionId(version.getTopicVersionId());
			change.setVirtualWiki(virtualWiki);
			results.add(change);
		}
		return results;
	}

	/**
	 *
	 */
	public abstract Collection getUserContributions(String virtualWiki, String userString, int num, boolean descending) throws Exception;

	/**
	 *
	 */
	public abstract Collection getVirtualWikiList() throws Exception;

	/**
	 * Set up defaults if necessary
	 */
	public void initialize(Locale locale, WikiUser user) throws Exception {
		if (!Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
			return;
		}
		Collection all = getVirtualWikiList();
		if (user != null && user.getUserId() < 1) addWikiUser(user);
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			String virtualWiki = (String)iterator.next();
			logger.info("Creating defaults for " + virtualWiki);
			// create the default topics
			setupSpecialPage(virtualWiki, Utilities.getMessage("specialpages.startingpoints", locale), user);
			setupSpecialPage(virtualWiki, Utilities.getMessage("specialpages.leftMenu", locale), user);
			setupSpecialPage(virtualWiki, Utilities.getMessage("specialpages.topArea", locale), user);
			setupSpecialPage(virtualWiki, Utilities.getMessage("specialpages.bottomArea", locale), user);
			setupSpecialPage(virtualWiki, Utilities.getMessage("specialpages.stylesheet", locale), user);
			setupSpecialPage(virtualWiki, Utilities.getMessage("specialpages.adminonlytopics", locale), user);
		}
	}

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
	public abstract WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public abstract WikiUser lookupWikiUser(int userId) throws Exception;

	/**
	 *
	 */
	public abstract WikiUser lookupWikiUser(String login, String password) throws Exception;

	/**
	 *
	 */
	public abstract WikiUser lookupWikiUser(String login) throws Exception;

	/**
	 *
	 */
	public String lookupWikiUserLogin(Integer authorId) throws Exception {
		String login = (String)cachedUserLoginHash.get(authorId);
		if (login != null) {
			return login;
		}
		WikiUser user = lookupWikiUser(authorId.intValue());
		login = user.getLogin();
		if (login != null) {
			cachedUserLoginHash.put(authorId, login);
		}
		if (cachedUserLoginHash.size() > MAX_CACHED_LIST_SIZE) {
			// FIXME - need a has that can drop oldest elements, this is inefficient
			cachedUserLoginHash = new Hashtable();
		}
		return login;
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	public abstract void purgeData() throws Exception;

	/**
	 * Utility method for reading default topic values from files and returning
	 * the file contents.
	 */
	private static String readSpecialPage(String topicName) throws Exception {
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
	protected void setupSpecialPage(String virtualWiki, String topicName, WikiUser user) throws Exception {
		if (exists(virtualWiki, topicName)) {
			return;
		}
		String baseFileDir = Environment.getValue(Environment.PROP_BASE_FILE_DIR);
		if (!StringUtils.hasText(baseFileDir)) {
			// do not set up special pages until initial property values set
			return;
		}
		String contents = PersistencyHandler.readSpecialPage(topicName);
		Topic topic = new Topic();
		topic.setName(topicName);
		topic.setVirtualWiki(virtualWiki);
		topic.setTopicContent(contents);
		TopicVersion topicVersion = new TopicVersion();
		topicVersion.setVersionContent(contents);
		topicVersion.setAuthorIpAddress(user.getLastLoginIpAddress());
		topicVersion.setAuthorId(new Integer(user.getUserId()));
		// FIXME - hard coding
		topicVersion.setEditComment("Automatically created by system setup");
		writeTopic(topic, topicVersion);
	}

	/**
	 *
	 */
	protected abstract void updateTopic(Topic topic) throws Exception;

	/**
	 *
	 */
	protected abstract void updateWikiFile(String topicName, WikiFile wikiFile) throws Exception;

	/**
	 *
	 */
	protected abstract void updateWikiUser(WikiUser user) throws Exception;

	/**
	 *
	 */
	public synchronized void writeFile(String topicName, WikiFile wikiFile, WikiFileVersion wikiFileVersion) throws Exception {
		if (wikiFile.getFileId() <= 0) {
			addWikiFile(topicName, wikiFile);
		} else {
			updateWikiFile(topicName, wikiFile);
		}
		wikiFileVersion.setFileId(wikiFile.getFileId());
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			// write version
			addWikiFileVersion(wikiFile.getVirtualWiki(), wikiFile.getFileName(), wikiFileVersion);
		}
	}

	/**
	 *
	 */
	public void writeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		Topic topic = lookupTopic(virtualWiki, topicName);
		topic.setReadOnly(true);
		updateTopic(topic);
	}

	/**
	 *
	 */
	public synchronized void writeTopic(Topic topic, TopicVersion topicVersion) throws Exception {
		if (topic.getTopicId() <= 0) {
			addTopic(topic);
		} else {
			updateTopic(topic);
		}
		if (topicVersion.getPreviousTopicVersionId() == null) {
			TopicVersion tmp = lookupLastTopicVersion(topic.getVirtualWiki(), topic.getName());
			if (tmp != null) topicVersion.setPreviousTopicVersionId(new Integer(tmp.getTopicVersionId()));
		}
		// reset topic non-existence vector
		cachedNonTopicsList = new Vector();
		topicVersion.setTopicId(topic.getTopicId());
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			// write version
			addTopicVersion(topic.getVirtualWiki(), topic.getName(), topicVersion);
		}
		String authorName = topicVersion.getAuthorIpAddress();
		if (topicVersion.getAuthorId() != null) {
			WikiUser user = lookupWikiUser(topicVersion.getAuthorId().intValue());
			authorName = user.getLogin();
			if (!StringUtils.hasText(authorName)) authorName = user.getLogin();
		}
		RecentChange change = new RecentChange();
		change.setTopicId(topic.getTopicId());
		change.setTopicName(topic.getName());
		change.setTopicVersionId(topicVersion.getTopicVersionId());
		change.setPreviousTopicVersionId(topicVersion.getPreviousTopicVersionId());
		change.setAuthorId(topicVersion.getAuthorId());
		change.setAuthorName(authorName);
		change.setEditComment(topicVersion.getEditComment());
		change.setEditDate(topicVersion.getEditDate());
		change.setEditType(topicVersion.getEditType());
		change.setVirtualWiki(topic.getVirtualWiki());
		addRecentChange(change);
	}

	/**
	 *
	 */
	public void writeVirtualWiki(String virtualWiki) throws Exception {
		this.addVirtualWiki(virtualWiki);
	}

	/**
	 *
	 */
	public void writeWikiUser(WikiUser user) throws Exception {
		if (user.getUserId() <= 0) {
			this.addWikiUser(user);
		} else {
			this.updateWikiUser(user);
		}
	}
}
