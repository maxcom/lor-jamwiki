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
package org.jamwiki.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.file.FileHandler;
import org.jamwiki.model.Category;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.DiffUtil;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class DatabaseHandler {

	private static Hashtable virtualWikiIdHash = null;
	private static Hashtable virtualWikiNameHash = null;
	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseHandler.class.getName());

	/**
	 *
	 */
	private void addCategory(Category category, Connection conn) throws Exception {
		Topic childTopic = lookupTopic(category.getVirtualWiki(), category.getChildTopicName(), false, conn);
		int childTopicId = childTopic.getTopicId();
		WikiDatabase.getQueryHandler().insertCategory(childTopicId, category.getName(), category.getSortKey(), conn);
	}

	/**
	 *
	 */
	private void addRecentChange(RecentChange change, Connection conn) throws Exception {
		int virtualWikiId = this.lookupVirtualWikiId(change.getVirtualWiki());
		WikiDatabase.getQueryHandler().insertRecentChange(change, virtualWikiId, conn);
	}

	/**
	 *
	 */
	private void addTopic(Topic topic, Connection conn) throws Exception {
		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
		if (topic.getTopicId() < 1) {
			int topicId = WikiDatabase.getQueryHandler().nextTopicId(conn);
			topic.setTopicId(topicId);
		}
		WikiDatabase.getQueryHandler().insertTopic(topic, virtualWikiId, conn);
	}

	/**
	 *
	 */
	private void addTopicVersion(String topicName, TopicVersion topicVersion, Connection conn) throws Exception {
		if (topicVersion.getTopicVersionId() < 1) {
			int topicVersionId = WikiDatabase.getQueryHandler().nextTopicVersionId(conn);
			topicVersion.setTopicVersionId(topicVersionId);
		}
		if (topicVersion.getEditDate() == null) {
			Timestamp editDate = new Timestamp(System.currentTimeMillis());
			topicVersion.setEditDate(editDate);
		}
		WikiDatabase.getQueryHandler().insertTopicVersion(topicVersion, conn);
	}

	/**
	 *
	 */
	private void addVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
		if (virtualWiki.getVirtualWikiId() < 1) {
			int virtualWikiId = WikiDatabase.getQueryHandler().nextVirtualWikiId(conn);
			virtualWiki.setVirtualWikiId(virtualWikiId);
		}
		WikiDatabase.getQueryHandler().insertVirtualWiki(virtualWiki, conn);
	}

	/**
	 *
	 */
	private void addWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws Exception {
		WikiDatabase.getQueryHandler().insertWatchlistEntry(virtualWikiId, topicName, userId, conn);
	}

	/**
	 *
	 */
	private void addWikiFile(String topicName, WikiFile wikiFile, Connection conn) throws Exception {
		if (wikiFile.getFileId() < 1) {
			int fileId = WikiDatabase.getQueryHandler().nextWikiFileId(conn);
			wikiFile.setFileId(fileId);
		}
		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
		WikiDatabase.getQueryHandler().insertWikiFile(wikiFile, virtualWikiId, conn);
	}

	/**
	 *
	 */
	private void addWikiFileVersion(String topicName, WikiFileVersion wikiFileVersion, Connection conn) throws Exception {
		if (wikiFileVersion.getFileVersionId() < 1) {
			int fileVersionId = WikiDatabase.getQueryHandler().nextWikiFileVersionId(conn);
			wikiFileVersion.setFileVersionId(fileVersionId);
		}
		if (wikiFileVersion.getUploadDate() == null) {
			Timestamp uploadDate = new Timestamp(System.currentTimeMillis());
			wikiFileVersion.setUploadDate(uploadDate);
		}
		WikiDatabase.getQueryHandler().insertWikiFileVersion(wikiFileVersion, conn);
	}

	/**
	 *
	 */
	protected void addWikiUser(WikiUser user, WikiUserInfo userInfo, Connection conn) throws Exception {
		if (user.getUserId() < 1) {
			int nextUserId = WikiDatabase.getQueryHandler().nextWikiUserId(conn);
			user.setUserId(nextUserId);
		}
		userInfo.setUserId(user.getUserId());
		WikiDatabase.getQueryHandler().insertWikiUser(user, conn);
		// FIXME - may be in LDAP
		WikiDatabase.getQueryHandler().insertWikiUserInfo(userInfo, conn);
	}

	/**
	 *
	 */
	public boolean canMoveTopic(Topic fromTopic, String destination) throws Exception {
		Topic toTopic = WikiBase.getHandler().lookupTopic(fromTopic.getVirtualWiki(), destination);
		if (toTopic == null || toTopic.getDeleteDate() != null) {
			// destination doesn't exist or is deleted, so move is OK
			return true;
		}
		if (toTopic.getRedirectTo() != null && toTopic.getRedirectTo().equals(fromTopic.getName())) {
			// source redirects to destination, so move is OK
			return true;
		}
		return false;
	}

	/**
	 * @deprecated This method exists solely to allow upgrades to JAMWiki 0.4.0 or
	 *  greater and will be replaced during the JAMWiki 0.5.x or JAMWiki 0.6.x series.
	 */
	public static Vector convertFromFile(WikiUser user, Locale locale, FileHandler fromHandler, DatabaseHandler toHandler) throws Exception {
		Connection conn = null;
		try {
			toHandler.setup(locale, user);
			DatabaseHandler.virtualWikiNameHash = new Hashtable();
			DatabaseHandler.virtualWikiIdHash = new Hashtable();
			conn = WikiDatabase.getConnection();
			// FIXME - hard coding of messages
			Vector messages = new Vector();
			// purge EVERYTHING from the destination handler
			WikiDatabase.purgeData(conn);
			// users
			Collection userNames = fromHandler.getAllWikiUserLogins();
			int success = 0;
			int failed = 0;
			for (Iterator userIterator = userNames.iterator(); userIterator.hasNext();) {
				String userName = (String)userIterator.next();
				try {
					WikiUser wikiUser = fromHandler.lookupWikiUser(userName);
					WikiUserInfo wikiUserInfo = fromHandler.lookupWikiUserInfo(userName);
					toHandler.addWikiUser(wikiUser, wikiUserInfo, conn);
					success++;
				} catch (Exception e) {
					String msg = "Unable to convert user: " + userName;
					logger.severe(msg, e);
					messages.add(msg + ": " + e.getMessage());
					failed++;
				}
			}
			messages.add("Converted " + success + " users successfully, " + failed + " conversions failed");
			success = 0;
			failed = 0;
			Collection virtualWikis = fromHandler.getVirtualWikiList();
			for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
				VirtualWiki virtualWiki = (VirtualWiki)virtualWikiIterator.next();
				try {
					toHandler.addVirtualWiki(virtualWiki, conn);
					messages.add("Added virtual wiki " + virtualWiki.getName());
				} catch (Exception e) {
					String msg = "Unable to convert virtual wiki " + virtualWiki.getName();
					logger.severe(msg, e);
					messages.add(msg + ": " + e.getMessage());
				}
				DatabaseHandler.virtualWikiNameHash.put(virtualWiki.getName(), virtualWiki);
				DatabaseHandler.virtualWikiIdHash.put(new Integer(virtualWiki.getVirtualWikiId()), virtualWiki);
				success = 0;
				failed = 0;
				// topics
				Collection topicNames = fromHandler.getAllTopicNames(virtualWiki.getName());
				for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
					String topicName = (String)topicIterator.next();
					try {
						Topic topic = fromHandler.lookupTopic(virtualWiki.getName(), topicName);
						toHandler.addTopic(topic, conn);
						success++;
					} catch (Exception e) {
						String msg = "Unable to convert topic: " + virtualWiki.getName() + " / " + topicName;
						logger.severe(msg, e);
						messages.add(msg + ": " + e.getMessage());
						failed++;
					}
				}
				messages.add("Converted " + success + " topics in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
				success = 0;
				failed = 0;
				// topic versions - must be added numerically due to previousTopicVersionId constraint
				TreeMap versionsMap = new TreeMap();
				Hashtable topicNameMap = new Hashtable();
				for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
					String topicName = (String)topicIterator.next();
					Collection versions = fromHandler.getAllTopicVersions(virtualWiki.getName(), topicName, false);
					for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
						TopicVersion topicVersion = (TopicVersion)topicVersionIterator.next();
						Integer key = new Integer(topicVersion.getTopicVersionId());
						topicNameMap.put(key, topicName);
						versionsMap.put(key, topicVersion);
					}
				}
				for (Iterator topicVersionIterator = versionsMap.keySet().iterator(); topicVersionIterator.hasNext();) {
					Integer key = (Integer)topicVersionIterator.next();
					TopicVersion topicVersion = (TopicVersion)versionsMap.get(key);
					String topicName = (String)topicNameMap.get(key);
					try {
						toHandler.addTopicVersion(topicName, topicVersion, conn);
						success++;
					} catch (Exception e) {
						String msg = "Unable to convert topic version: " + virtualWiki.getName() + " / " + topicName + " / " + topicVersion.getTopicVersionId();
						logger.severe(msg, e);
						messages.add(msg + ": " + e.getMessage());
						failed++;
					}
				}
				messages.add("Converted " + success + " topic versions in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
				success = 0;
				failed = 0;
				// wiki files
				Collection wikiFileNames = fromHandler.getAllWikiFileTopicNames(virtualWiki.getName());
				for (Iterator wikiFileIterator = wikiFileNames.iterator(); wikiFileIterator.hasNext();) {
					String topicName = (String)wikiFileIterator.next();
					try {
						WikiFile wikiFile = fromHandler.lookupWikiFile(virtualWiki.getName(), topicName);
						toHandler.addWikiFile(topicName, wikiFile, conn);
						success++;
					} catch (Exception e) {
						String msg = "Unable to convert wiki file: " + virtualWiki.getName() + " / " + topicName;
						logger.severe(msg, e);
						messages.add(msg + ": " + e.getMessage());
						failed++;
					}
				}
				messages.add("Converted " + success + " wiki files in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
				success = 0;
				failed = 0;
				// wiki file versions
				for (Iterator topicIterator = wikiFileNames.iterator(); topicIterator.hasNext();) {
					String topicName = (String)topicIterator.next();
					Collection versions = fromHandler.getAllWikiFileVersions(virtualWiki.getName(), topicName, false);
					for (Iterator wikiFileVersionIterator = versions.iterator(); wikiFileVersionIterator.hasNext();) {
						WikiFileVersion wikiFileVersion = (WikiFileVersion)wikiFileVersionIterator.next();
						try {
							toHandler.addWikiFileVersion(topicName, wikiFileVersion, conn);
							success++;
						} catch (Exception e) {
							String msg = "Unable to convert wiki file version: " + virtualWiki.getName() + " / " + topicName;
							logger.severe(msg, e);
							messages.add(msg + ": " + e.getMessage());
							failed++;
						}
					}
				}
				messages.add("Converted " + success + " wiki file versions in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
				toHandler.reloadRecentChanges();
			}
			// FIXME - since search index info is in the same directory it gets deleted
			WikiBase.getSearchEngine().refreshIndex();
			return messages;
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
			WikiBase.reset(locale, user);
		}
	}

	/**
	 *
	 */
	private void deleteRecentChanges(Topic topic, Connection conn) throws Exception {
		WikiDatabase.getQueryHandler().deleteRecentChanges(topic.getTopicId(), conn);
	}

	/**
	 *
	 */
	public void deleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			this.deleteTopic(topic, topicVersion, userVisible, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private void deleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Connection conn) throws Exception {
		if (userVisible) {
			// delete old recent changes
			deleteRecentChanges(topic, conn);
		}
		// update topic to indicate deleted, add delete topic version.  parser output
		// should be empty since nothing to add to search engine.
		ParserDocument parserDocument = new ParserDocument();
		topic.setDeleteDate(new Timestamp(System.currentTimeMillis()));
		writeTopic(topic, topicVersion, parserDocument, conn, userVisible);
	}

	/**
	 *
	 */
	private void deleteTopicCategories(Topic topic, Connection conn) throws Exception {
		WikiDatabase.getQueryHandler().deleteTopicCategories(topic.getTopicId(), conn);
	}

	/**
	 *
	 */
	private void deleteWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws Exception {
		WikiDatabase.getQueryHandler().deleteWatchlistEntry(virtualWikiId, topicName, userId, conn);
	}

	/**
	 *
	 */
	public Vector diff(String topicName, int topicVersionId1, int topicVersionId2) throws Exception {
		TopicVersion version1 = lookupTopicVersion(topicName, topicVersionId1);
		TopicVersion version2 = lookupTopicVersion(topicName, topicVersionId2);
		if (version1 == null && version2 == null) {
			String msg = "Versions " + topicVersionId1 + " and " + topicVersionId2 + " not found for " + topicName;
			logger.severe(msg);
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
			logger.severe(msg);
			throw new Exception(msg);
		}
		return DiffUtil.diff(contents1, contents2);
	}

	/**
	 * See if a topic exists and if it has not been deleted.
	 *
	 * @param virtualWiki The virtual wiki for the topic being checked.
	 * @param topicName The name of the topic that is being checked.
	 * @return <code>true</code> if the topic exists.
	 * @throws Exception Thrown if any error occurs during lookup.
	 */
	public boolean exists(String virtualWiki, String topicName) throws Exception {
		if (!StringUtils.hasText(virtualWiki) || !StringUtils.hasText(topicName)) {
			return false;
		}
		// first check a cache of recently looked-up topics for performance reasons
		String key = virtualWiki + "/" + topicName;
		if (WikiCache.isCached(WikiCache.CACHE_TOPIC_NAME, virtualWiki, topicName)) {
			return (WikiCache.retrieveFromCache(WikiCache.CACHE_TOPIC_NAME, virtualWiki, topicName) != null);
		}
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null || topic.getDeleteDate() != null) {
			WikiCache.addToCache(WikiCache.CACHE_TOPIC_NAME, virtualWiki, topicName, null);
			return false;
		}
		WikiCache.addToCache(WikiCache.CACHE_TOPIC_NAME, virtualWiki, topicName, topicName);
		return true;
	}

	/**
	 *
	 */
	public Collection getAllCategories(String virtualWiki, Pagination pagination) throws Exception {
		Collection results = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().getCategories(virtualWikiId, pagination);
		while (rs.next()) {
			Category category = new Category();
			category.setName(rs.getString("category_name"));
			// FIXME - child topic name not initialized
			category.setVirtualWiki(virtualWiki);
			category.setSortKey(rs.getString("sort_key"));
			results.add(category);
		}
		return results;
	}

	/**
	 *
	 */
	public Collection getAllTopicNames(String virtualWiki) throws Exception {
		Vector all = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().getAllTopicNames(virtualWikiId);
		while (rs.next()) {
			all.add(rs.getString("topic_name"));
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
		Vector all = new Vector();
		WikiFile wikiFile = lookupWikiFile(virtualWiki, topicName);
		if (wikiFile == null) {
			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
		}
		WikiResultSet rs = WikiDatabase.getQueryHandler().getAllWikiFileVersions(wikiFile, descending);
		while (rs.next()) {
			all.add(initWikiFileVersion(rs));
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception {
		Vector all = new Vector();
		WikiResultSet rs = WikiDatabase.getQueryHandler().getRecentChanges(virtualWiki, pagination, descending);
		while (rs.next()) {
			RecentChange change = initRecentChange(rs);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, String topicName, Pagination pagination, boolean descending) throws Exception {
		Vector all = new Vector();
		Topic topic = lookupTopic(virtualWiki, topicName, true);
		if (topic == null) return all;
		WikiResultSet rs = WikiDatabase.getQueryHandler().getRecentChanges(topic.getTopicId(), pagination, descending);
		while (rs.next()) {
			RecentChange change = initRecentChange(rs);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getTopicsAdmin(String virtualWiki, Pagination pagination) throws Exception {
		Collection all = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().getTopicsAdmin(virtualWikiId, pagination);
		while (rs.next()) {
			String topicName = rs.getString("topic_name");
			all.add(topicName);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception {
		Collection all = new Vector();
		WikiResultSet rs = WikiDatabase.getQueryHandler().getUserContributions(virtualWiki, userString, pagination, descending);
		while (rs.next()) {
			RecentChange change = initRecentChange(rs);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		if (virtualWikiNameHash == null) {
			loadVirtualWikiHashes();
		}
		return virtualWikiNameHash.values();
	}

	/**
	 * Retrieve a watchlist containing a Collection of topic ids and topic
	 * names that can be used to determine if a topic is in a user's current
	 * watchlist.
	 */
	public Watchlist getWatchlist(String virtualWiki, int userId) throws Exception {
		Collection all = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().getWatchlist(virtualWikiId, userId);
		while (rs.next()) {
			String topicName = rs.getString("topic_name");
			all.add(topicName);
		}
		return new Watchlist(virtualWiki, all);
	}

	/**
	 * Retrieve a watchlist containing a Collection of RecentChanges objects
	 * that can be used for display on the Special:Watchlist page.
	 */
	public Collection getWatchlist(String virtualWiki, int userId, Pagination pagination) throws Exception {
		Collection all = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().getWatchlist(virtualWikiId, userId, pagination);
		while (rs.next()) {
			RecentChange change = initRecentChange(rs);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	private RecentChange initRecentChange(WikiResultSet rs) {
		try {
			RecentChange change = new RecentChange();
			change.setTopicVersionId(rs.getInt("topic_version_id"));
			int previousTopicVersionId = rs.getInt("previous_topic_version_id");
			if (previousTopicVersionId > 0) change.setPreviousTopicVersionId(new Integer(previousTopicVersionId));
			change.setTopicId(rs.getInt("topic_id"));
			change.setTopicName(rs.getString("topic_name"));
			change.setEditDate(rs.getTimestamp("edit_date"));
			change.setEditComment(rs.getString("edit_comment"));
			int userId = rs.getInt("wiki_user_id");
			if (userId > 0) change.setAuthorId(new Integer(userId));
			change.setAuthorName(rs.getString("display_name"));
			change.setEditType(rs.getInt("edit_type"));
			change.setVirtualWiki(rs.getString("virtual_wiki_name"));
			return change;
		} catch (Exception e) {
			logger.severe("Failure while initializing recent change", e);
			return null;
		}
	}

	/**
	 *
	 */
	private Topic initTopic(WikiResultSet rs) {
		try {
			int virtualWikiId = rs.getInt("virtual_wiki_id");
			String virtualWiki = this.lookupVirtualWikiName(virtualWikiId);
			Topic topic = new Topic();
			topic.setAdminOnly(rs.getInt("topic_admin_only") != 0);
			topic.setName(rs.getString("topic_name"));
			topic.setVirtualWiki(virtualWiki);
			int currentVersionId = rs.getInt("current_version_id");
			if (currentVersionId > 0) topic.setCurrentVersionId(new Integer(currentVersionId));
			topic.setTopicContent(rs.getString("version_content"));
			topic.setTopicId(rs.getInt("topic_id"));
			topic.setReadOnly(rs.getInt("topic_read_only") != 0);
			topic.setDeleteDate(rs.getTimestamp("delete_date"));
			topic.setTopicType(rs.getInt("topic_type"));
			topic.setRedirectTo(rs.getString("redirect_to"));
			return topic;
		} catch (Exception e) {
			logger.severe("Failure while initializing topic", e);
			return null;
		}
	}

	/**
	 *
	 */
	private TopicVersion initTopicVersion(WikiResultSet rs) {
		try {
			TopicVersion topicVersion = new TopicVersion();
			topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
			topicVersion.setTopicId(rs.getInt("topic_id"));
			topicVersion.setEditComment(rs.getString("edit_comment"));
			topicVersion.setVersionContent(rs.getString("version_content"));
			int previousTopicVersionId = rs.getInt("previous_topic_version_id");
			if (previousTopicVersionId > 0) topicVersion.setPreviousTopicVersionId(new Integer(previousTopicVersionId));
			int userId = rs.getInt("wiki_user_id");
			if (userId > 0) topicVersion.setAuthorId(new Integer(userId));
			topicVersion.setEditDate(rs.getTimestamp("edit_date"));
			topicVersion.setEditType(rs.getInt("edit_type"));
			topicVersion.setAuthorIpAddress(rs.getString("wiki_user_ip_address"));
			return topicVersion;
		} catch (Exception e) {
			logger.severe("Failure while initializing topic version", e);
			return null;
		}
	}

	/**
	 *
	 */
	private VirtualWiki initVirtualWiki(WikiResultSet rs) {
		try {
			VirtualWiki virtualWiki = new VirtualWiki();
			virtualWiki.setVirtualWikiId(rs.getInt("virtual_wiki_id"));
			virtualWiki.setName(rs.getString("virtual_wiki_name"));
			virtualWiki.setDefaultTopicName(rs.getString("default_topic_name"));
			return virtualWiki;
		} catch (Exception e) {
			logger.severe("Failure while initializing virtual wiki", e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiFile initWikiFile(WikiResultSet rs) {
		try {
			int virtualWikiId = rs.getInt("virtual_wiki_id");
			String virtualWiki = this.lookupVirtualWikiName(virtualWikiId);
			WikiFile wikiFile = new WikiFile();
			wikiFile.setFileId(rs.getInt("file_id"));
			wikiFile.setAdminOnly(rs.getInt("file_admin_only") != 0);
			wikiFile.setFileName(rs.getString("file_name"));
			wikiFile.setVirtualWiki(virtualWiki);
			wikiFile.setUrl(rs.getString("file_url"));
			wikiFile.setTopicId(rs.getInt("topic_id"));
			wikiFile.setReadOnly(rs.getInt("file_read_only") != 0);
			wikiFile.setDeleteDate(rs.getTimestamp("delete_date"));
			wikiFile.setMimeType(rs.getString("mime_type"));
			wikiFile.setFileSize(rs.getInt("file_size"));
			return wikiFile;
		} catch (Exception e) {
			logger.severe("Failure while initializing file", e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiFileVersion initWikiFileVersion(WikiResultSet rs) {
		try {
			WikiFileVersion wikiFileVersion = new WikiFileVersion();
			wikiFileVersion.setFileVersionId(rs.getInt("file_version_id"));
			wikiFileVersion.setFileId(rs.getInt("file_id"));
			wikiFileVersion.setUploadComment(rs.getString("upload_comment"));
			wikiFileVersion.setUrl(rs.getString("file_url"));
			int userId = rs.getInt("wiki_user_id");
			if (userId > 0) wikiFileVersion.setAuthorId(new Integer(userId));
			wikiFileVersion.setUploadDate(rs.getTimestamp("upload_date"));
			wikiFileVersion.setMimeType(rs.getString("mime_type"));
			wikiFileVersion.setAuthorIpAddress(rs.getString("wiki_user_ip_address"));
			wikiFileVersion.setFileSize(rs.getInt("file_size"));
			return wikiFileVersion;
		} catch (Exception e) {
			logger.severe("Failure while initializing wiki file version", e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiUser initWikiUser(WikiResultSet rs) {
		try {
			WikiUser user = new WikiUser();
			user.setUserId(rs.getInt("wiki_user_id"));
			user.setLogin(rs.getString("login"));
			user.setDisplayName(rs.getString("display_name"));
			user.setCreateDate(rs.getTimestamp("create_date"));
			user.setLastLoginDate(rs.getTimestamp("last_login_date"));
			user.setCreateIpAddress(rs.getString("create_ip_address"));
			user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
			user.setAdmin(rs.getInt("is_admin") != 0);
			user.setRememberKey(rs.getString("remember_key"));
			return user;
		} catch (Exception e) {
			logger.severe("Failure while initializing user", e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiUserInfo initWikiUserInfo(WikiResultSet rs) {
		try {
			WikiUserInfo userInfo = new WikiUserInfo();
			userInfo.setUserId(rs.getInt("wiki_user_id"));
			userInfo.setLogin(rs.getString("login"));
			userInfo.setEmail(rs.getString("email"));
			userInfo.setFirstName(rs.getString("first_name"));
			userInfo.setLastName(rs.getString("last_name"));
			userInfo.setEncodedPassword(rs.getString("encoded_password"));
			return userInfo;
		} catch (Exception e) {
			logger.severe("Failure while initializing user info", e);
			return null;
		}
	}

	/**
	 *
	 */
	private void loadVirtualWikiHashes() throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			this.loadVirtualWikiHashes(conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private void loadVirtualWikiHashes(Connection conn) throws Exception {
		DatabaseHandler.virtualWikiNameHash = new Hashtable();
		DatabaseHandler.virtualWikiIdHash = new Hashtable();
		try {
			WikiResultSet rs = WikiDatabase.getQueryHandler().getVirtualWikis(conn);
			while (rs.next()) {
				VirtualWiki virtualWiki = initVirtualWiki(rs);
				DatabaseHandler.virtualWikiNameHash.put(virtualWiki.getName(), virtualWiki);
				DatabaseHandler.virtualWikiIdHash.put(new Integer(virtualWiki.getVirtualWikiId()), virtualWiki);
			}
		} catch (Exception e) {
			logger.severe("Failure while loading virtual wiki hashtable ", e);
			// if there is an error make sure the hashtable is reset since it wasn't
			// properly initialized
			DatabaseHandler.virtualWikiNameHash = null;
			DatabaseHandler.virtualWikiIdHash = null;
			throw e;
		}
	}

	/**
	 *
	 */
	public Collection lookupCategoryTopics(String virtualWiki, String categoryName, int topicType) throws Exception {
		Vector results = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupCategoryTopics(virtualWikiId, categoryName, topicType);
		while (rs.next()) {
			Category category = new Category();
			category.setName(categoryName);
			category.setVirtualWiki(virtualWiki);
			category.setChildTopicName(rs.getString("topic_name"));
			category.setSortKey(rs.getString("sort_key"));
			results.add(category);
		}
		return results;
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
		return lookupTopic(virtualWiki, topicName, false);
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			return this.lookupTopic(virtualWiki, topicName, deleteOK, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Connection conn) throws Exception {
		WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
		String namespace = wikiLink.getNamespace();
		boolean caseSensitive = true;
		if (namespace != null) {
			if (namespace.equals(NamespaceHandler.NAMESPACE_SPECIAL)) {
				// invalid namespace
				return null;
			}
			if (namespace.equals(NamespaceHandler.NAMESPACE_TEMPLATE) || namespace.equals(NamespaceHandler.NAMESPACE_USER)) {
				// user/template namespaces are case-insensitive
				caseSensitive = false;
			}
		}
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupTopic(virtualWikiId, topicName, caseSensitive, deleteOK, conn);
		if (rs.size() == 0) return null;
		return initTopic(rs);
	}

	/**
	 * Return a count of all topics, including redirects, comments pages and templates,
	 * currently available on the Wiki.  This method excludes deleted topics.
	 *
	 * @param virtualWiki The virtual wiki for which the total topic count is being returned
	 *  for.
	 */
	public int lookupTopicCount(String virtualWiki) throws Exception {
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupTopicCount(virtualWikiId);
		return rs.getInt("topic_count");
	}

	/**
	 *
	 */
	public Collection lookupTopicByType(String virtualWiki, int topicType, Pagination pagination) throws Exception {
		Vector results = new Vector();
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupTopicByType(virtualWikiId, topicType, pagination);
		while (rs.next()) {
			results.add(rs.getString("topic_name"));
		}
		return results;
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String topicName, int topicVersionId) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			return this.lookupTopicVersion(topicName, topicVersionId, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private TopicVersion lookupTopicVersion(String topicName, int topicVersionId, Connection conn) throws Exception {
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupTopicVersion(topicVersionId, conn);
		if (rs.size() == 0) return null;
		return initTopicVersion(rs);
	}

	/**
	 *
	 */
	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws Exception {
		if (virtualWikiNameHash == null) {
			loadVirtualWikiHashes();
		}
		return (VirtualWiki)virtualWikiNameHash.get(virtualWikiName);
	}

	/**
	 *
	 */
	public int lookupVirtualWikiId(String virtualWikiName) throws Exception {
		if (virtualWikiNameHash == null) {
			this.loadVirtualWikiHashes();
		}
		VirtualWiki virtualWiki = (VirtualWiki)virtualWikiNameHash.get(virtualWikiName);
		return (virtualWiki != null) ? virtualWiki.getVirtualWikiId() : -1;
	}

	/**
	 *
	 */
	public String lookupVirtualWikiName(int virtualWikiId) throws Exception {
		if (virtualWikiIdHash == null) {
			this.loadVirtualWikiHashes();
		}
		VirtualWiki virtualWiki = (VirtualWiki)virtualWikiIdHash.get(new Integer(virtualWikiId));
		return (virtualWiki != null) ? virtualWiki.getName() : null;
	}

	/**
	 *
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null) return null;
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiFile(virtualWikiId, topic.getTopicId());
		if (rs.size() == 0) return null;
		return initWikiFile(rs);
	}

	/**
	 * Return a count of all wiki files currently available on the Wiki.  This
	 * method excludes deleted files.
	 *
	 * @param virtualWiki The virtual wiki of the files being retrieved.
	 */
	public int lookupWikiFileCount(String virtualWiki) throws Exception {
		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiFileCount(virtualWikiId);
		return rs.getInt("file_count");
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(int userId) throws Exception {
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiUser(userId);
		if (rs.size() == 0) return null;
		return initWikiUser(rs);
	}

	/**
	 *
	 */
	protected WikiUser lookupWikiUser(int userId, Connection conn) throws Exception {
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiUser(userId, conn);
		if (rs.size() == 0) return null;
		return initWikiUser(rs);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login) throws Exception {
		// FIXME - handle LDAP
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiUser(login);
		if (rs.size() == 0) return null;
		int userId = rs.getInt("wiki_user_id");
		return lookupWikiUser(userId);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login, String password, boolean encrypted) throws Exception {
		// FIXME - handle LDAP
		String encryptedPassword = password;
		if (!encrypted) {
			encryptedPassword = Encryption.encrypt(password);
		}
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiUser(login, encryptedPassword);
		if (rs.size() == 0) return null;
		int userId = rs.getInt("wiki_user_id");
		return lookupWikiUser(userId);
	}

	/**
	 * Return a count of all wiki users.
	 */
	public int lookupWikiUserCount() throws Exception {
		// FIXME - handle LDAP
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiUserCount();
		return rs.getInt("user_count");
	}

	/**
	 *
	 */
	public WikiUserInfo lookupWikiUserInfo(String login) throws Exception {
		WikiResultSet rs = WikiDatabase.getQueryHandler().lookupWikiUserInfo(login);
		if (rs.size() == 0) return null;
		return initWikiUserInfo(rs);
	}

	/**
	 *
	 */
	public void moveTopic(Topic fromTopic, TopicVersion fromVersion, String destination) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			if (!this.canMoveTopic(fromTopic, destination)) {
				throw new WikiException(new WikiMessage("move.exception.destinationexists", destination));
			}
			Topic toTopic = WikiBase.getHandler().lookupTopic(fromTopic.getVirtualWiki(), destination);
			boolean detinationExistsFlag = (toTopic != null && toTopic.getDeleteDate() == null);
			if (detinationExistsFlag) {
				// if the target topic is a redirect to the source topic then the
				// target must first be deleted.
				this.deleteTopic(toTopic, null, false, conn);
			}
			String fromTopicName = fromTopic.getName();
			fromTopic.setName(destination);
			writeTopic(fromTopic, fromVersion, Utilities.parserDocument(fromTopic.getTopicContent(), fromTopic.getVirtualWiki(), fromTopicName), conn, true);
			if (detinationExistsFlag) {
				// target topic was deleted, so rename and undelete
				toTopic.setName(fromTopicName);
				writeTopic(toTopic, null, null, conn, false);
				this.undeleteTopic(toTopic, null, false, conn);
			} else {
				// create a new topic that redirects to the destination
				toTopic = fromTopic;
				toTopic.setTopicId(-1);
				toTopic.setName(fromTopicName);
			}
			String content = Utilities.parserRedirectContent(destination);
			toTopic.setRedirectTo(destination);
			toTopic.setTopicType(Topic.TYPE_REDIRECT);
			toTopic.setTopicContent(content);
			TopicVersion toVersion = fromVersion;
			toVersion.setTopicVersionId(-1);
			toVersion.setVersionContent(content);
			writeTopic(toTopic, toVersion, Utilities.parserDocument(content, toTopic.getVirtualWiki(), toTopic.getName()), conn, true);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	public void reloadRecentChanges() throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			WikiDatabase.getQueryHandler().reloadRecentChanges(conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private void resetCache() {
		DatabaseHandler.virtualWikiIdHash = null;
		DatabaseHandler.virtualWikiNameHash = null;
	}

	/**
	 *
	 */
	public void setup(Locale locale, WikiUser user) throws Exception {
		WikiDatabase.initialize();
		// determine if database exists
		try {
			DatabaseConnection.executeQuery(WikiDatabase.getExistenceValidationQuery());
			return;
		} catch (Exception e) {
			// database not yet set up
		}
		WikiDatabase.setup(locale, user);
	}

	/**
	 *
	 */
	public static void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			// create the default topics
			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false, conn);
			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true, conn);
			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true, conn);
			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	public void undeleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			this.undeleteTopic(topic, topicVersion, userVisible, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private void undeleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Connection conn) throws Exception {
		// update topic to indicate deleted, add delete topic version.  parser output
		// should be empty since nothing to add to search engine.
		ParserDocument parserDocument = new ParserDocument();
		topic.setDeleteDate(null);
		writeTopic(topic, topicVersion, parserDocument, conn, userVisible);
	}

	/**
	 *
	 */
	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, String ipAddress) throws Exception {
		logger.info("Updating special page " + virtualWiki + " / " + topicName);
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			String contents = Utilities.readSpecialPage(locale, topicName);
			Topic topic = this.lookupTopic(virtualWiki, topicName, false, conn);
			topic.setTopicContent(contents);
			// FIXME - hard coding
			TopicVersion topicVersion = new TopicVersion(user, ipAddress, "Automatically updated by system upgrade", contents);
			writeTopic(topic, topicVersion, Utilities.parserDocument(topic.getTopicContent(), virtualWiki, topicName), conn, true);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private void updateTopic(Topic topic, Connection conn) throws Exception {
		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
		WikiDatabase.getQueryHandler().updateTopic(topic, virtualWikiId, conn);
	}

	/**
	 *
	 */
	private void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
		WikiDatabase.getQueryHandler().updateVirtualWiki(virtualWiki, conn);
	}

	/**
	 *
	 */
	private void updateWikiFile(String topicName, WikiFile wikiFile, Connection conn) throws Exception {
		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
		WikiDatabase.getQueryHandler().updateWikiFile(wikiFile, virtualWikiId, conn);
	}

	/**
	 *
	 */
	private void updateWikiUser(WikiUser user, WikiUserInfo userInfo, Connection conn) throws Exception {
		WikiDatabase.getQueryHandler().updateWikiUser(user, conn);
		// FIXME - may be in LDAP
		WikiDatabase.getQueryHandler().updateWikiUserInfo(userInfo, conn);
	}

	/**
	 *
	 */
	public void writeFile(String topicName, WikiFile wikiFile, WikiFileVersion wikiFileVersion) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			if (wikiFile.getFileId() <= 0) {
				addWikiFile(topicName, wikiFile, conn);
			} else {
				updateWikiFile(topicName, wikiFile, conn);
			}
			wikiFileVersion.setFileId(wikiFile.getFileId());
			// write version
			addWikiFileVersion(topicName, wikiFileVersion, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	public void writeTopic(Topic topic, TopicVersion topicVersion, ParserDocument parserDocument) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			WikiUser user = null;
			if (topicVersion.getAuthorId() != null) {
				user = lookupWikiUser(topicVersion.getAuthorId().intValue(), conn);
			}
			this.writeTopic(topic, topicVersion, parserDocument, conn, true);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 * Commit changes to a topic (and its version) to the database or filesystem.
	 *
	 * @param topic The topic object that is to be committed.  If the topic id is
	 *  empty or less than zero then the topic is added, otherwise an update is performed.
	 * @param topicVersion The version associated with the topic that is being added.
	 *  This parameter should never be null UNLESS the change is not user visible, such as
	 *  when deleting a topic temporarily during page moves.
	 * @param parserDocument The parserDocument object that contains a list of links in the
	 *  topic content, categories, etc.  This parameter may be set with the
	 *  Utilities.getParserDocument() method.
	 * @param conn Database connection or other parameters required for updates.
	 * @param userVisible A flag indicating whether or not this change should be visible
	 *  to Wiki users.  This flag should be true except in rare cases, such as when
	 *  temporarily deleting a topic during page moves.
	 */
	protected void writeTopic(Topic topic, TopicVersion topicVersion, ParserDocument parserDocument, Connection conn, boolean userVisible) throws Exception {
		Utilities.validateTopicName(topic.getName());
		if (topic.getTopicId() <= 0) {
			addTopic(topic, conn);
		} else {
			updateTopic(topic, conn);
		}
		if (userVisible) {
			if (topicVersion.getPreviousTopicVersionId() == null) {
				if (topic.getCurrentVersionId() != null) topicVersion.setPreviousTopicVersionId(topic.getCurrentVersionId());
			}
			topicVersion.setTopicId(topic.getTopicId());
			// write version
			addTopicVersion(topic.getName(), topicVersion, conn);
			String authorName = topicVersion.getAuthorIpAddress();
			Integer authorId = topicVersion.getAuthorId();
			if (authorId != null) {
				WikiUser user = lookupWikiUser(topicVersion.getAuthorId().intValue(), conn);
				authorName = user.getLogin();
			}
			RecentChange change = new RecentChange(topic, topicVersion, authorName);
			addRecentChange(change, conn);
		}
		if (parserDocument != null) {
			// add / remove categories associated with the topic
			this.deleteTopicCategories(topic, conn);
			LinkedHashMap categories = parserDocument.getCategories();
			for (Iterator iterator = categories.keySet().iterator(); iterator.hasNext();) {
				String categoryName = (String)iterator.next();
				Category category = new Category();
				category.setName(categoryName);
				category.setSortKey((String)categories.get(categoryName));
				category.setVirtualWiki(topic.getVirtualWiki());
				category.setChildTopicName(topic.getName());
				this.addCategory(category, conn);
			}
		}
		if (parserDocument != null) {
			WikiBase.getSearchEngine().deleteFromIndex(topic);
			WikiBase.getSearchEngine().addToIndex(topic, parserDocument.getLinks());
		}
		WikiCache.removeFromCache(WikiCache.CACHE_TOPIC_CONTENT, topic.getVirtualWiki(), topic.getName());
		WikiCache.removeFromCache(WikiCache.CACHE_TOPIC_NAME, topic.getVirtualWiki(), topic.getName());
	}

	/**
	 *
	 */
	public void writeVirtualWiki(VirtualWiki virtualWiki) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			this.writeVirtualWiki(virtualWiki, conn);
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	protected void writeVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
		Utilities.validateTopicName(virtualWiki.getName());
		if (virtualWiki.getVirtualWikiId() <= 0) {
			this.addVirtualWiki(virtualWiki, conn);
		} else {
			this.updateVirtualWiki(virtualWiki, conn);
		}
		// update the hashtable AFTER the commit
		this.loadVirtualWikiHashes(conn);
	}

	/**
	 *
	 */
	public void writeWatchlistEntry(Watchlist watchlist, String virtualWiki, String topicName, int userId) throws Exception {
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
			String article = Utilities.extractTopicLink(topicName);
			String comments = Utilities.extractCommentsLink(topicName);
			if (watchlist.containsTopic(topicName)) {
				// remove from watchlist
				this.deleteWatchlistEntry(virtualWikiId, article, userId, conn);
				this.deleteWatchlistEntry(virtualWikiId, comments, userId, conn);
				watchlist.remove(article);
				watchlist.remove(comments);
			} else {
				// add to watchlist
				this.addWatchlistEntry(virtualWikiId, article, userId, conn);
				this.addWatchlistEntry(virtualWikiId, comments, userId, conn);
				watchlist.add(article);
				watchlist.add(comments);
			}
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	public void writeWikiUser(WikiUser user, WikiUserInfo userInfo) throws Exception {
		Utilities.validateUserName(user.getLogin());
		Connection conn = null;
		try {
			conn = WikiDatabase.getConnection();
			if (user.getUserId() <= 0) {
				this.addWikiUser(user, userInfo, conn);
			} else {
				this.updateWikiUser(user, userInfo, conn);
			}
		} catch (Exception e) {
			DatabaseConnection.handleErrors(conn);
			throw e;
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}
}
