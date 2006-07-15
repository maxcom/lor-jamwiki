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
package org.jamwiki.persistency.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.WikiBase;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class DatabaseHandler extends PersistencyHandler {

	public static final String DB_TYPE_ORACLE = "oracle";
	public static final String DB_TYPE_MYSQL = "mysql";
	public static final String DB_TYPE_POSTGRES = "postgres";
	private static final String INIT_SCRIPT_ANSI = "create_ansi.sql";
	private static final String INIT_SCRIPT_ORACLE = "create_oracle.sql";
	private static final Logger logger = Logger.getLogger(DatabaseHandler.class);
	private static Hashtable virtualWikiIdHash = null;
	private static Hashtable virtualWikiNameHash = null;
	private static QueryHandler queryHandler = null;

	static {
		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_POSTGRES)) {
			DatabaseHandler.queryHandler = new PostgresQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL)) {
			DatabaseHandler.queryHandler = new MySqlQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE)) {
			DatabaseHandler.queryHandler = new OracleQueryHandler();
		} else {
			DatabaseHandler.queryHandler = new DefaultQueryHandler();
		}
	}

	/**
	 *
	 */
	public DatabaseHandler() {
	}

	/**
	 *
	 */
	protected void addRecentChange(RecentChange change) throws Exception {
		DatabaseHandler.queryHandler.insertRecentChange(change);
	}

	/**
	 *
	 */
	protected void addTopic(Topic topic) throws Exception {
		if (topic.getTopicId() < 1) {
			int topicId = DatabaseHandler.queryHandler.nextTopicId();
			topic.setTopicId(topicId);
		}
		DatabaseHandler.queryHandler.insertTopic(topic);
	}

	/**
	 *
	 */
	protected void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
		if (topicVersion.getTopicVersionId() < 1) {
			int topicVersionId = DatabaseHandler.queryHandler.nextTopicVersionId();
			topicVersion.setTopicVersionId(topicVersionId);
		}
		if (topicVersion.getEditDate() == null) {
			Timestamp editDate = new Timestamp(System.currentTimeMillis());
			topicVersion.setEditDate(editDate);
		}
		DatabaseHandler.queryHandler.insertTopicVersion(topicVersion);
	}

	/**
	 *
	 */
	protected void addVirtualWiki(String virtualWikiName) throws Exception {
		int virtualWikiId = DatabaseHandler.queryHandler.nextVirtualWikiId();
		DatabaseHandler.queryHandler.insertVirtualWiki(virtualWikiId, virtualWikiName);
		DatabaseHandler.loadVirtualWikiHashes();
	}

	/**
	 *
	 */
	protected void addWikiFile(String topicName, WikiFile wikiFile) throws Exception {
		if (wikiFile.getFileId() < 1) {
			int fileId = DatabaseHandler.queryHandler.nextWikiFileId();
			wikiFile.setFileId(fileId);
		}
		DatabaseHandler.queryHandler.insertWikiFile(wikiFile);
	}

	/**
	 *
	 */
	protected void addWikiFileVersion(String virtualWiki, String wikiFileName, WikiFileVersion wikiFileVersion) throws Exception {
		if (wikiFileVersion.getFileVersionId() < 1) {
			int fileVersionId = DatabaseHandler.queryHandler.nextWikiFileVersionId();
			wikiFileVersion.setFileVersionId(fileVersionId);
		}
		if (wikiFileVersion.getUploadDate() == null) {
			Timestamp uploadDate = new Timestamp(System.currentTimeMillis());
			wikiFileVersion.setUploadDate(uploadDate);
		}
		DatabaseHandler.queryHandler.insertWikiFileVersion(wikiFileVersion);
	}

	/**
	 *
	 */
	protected void addWikiUser(WikiUser user) throws Exception {
		if (user.getUserId() < 1) {
			int nextUserId = DatabaseHandler.queryHandler.nextWikiUserId();
			user.setUserId(nextUserId);
		}
		DatabaseHandler.queryHandler.insertWikiUser(user);
		// FIXME - may be in LDAP
		DatabaseHandler.queryHandler.insertWikiUserInfo(user);
	}

	/**
	 *
	 */
	public List getAllTopicNames(String virtualWiki) throws Exception {
		List all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getAllTopicNames(virtualWiki);
		while (rs.next()) {
			all.add(rs.getString("topic_name"));
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllTopicVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
		List all = new ArrayList();
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
		}
		WikiResultSet rs = DatabaseHandler.queryHandler.getAllTopicVersions(topic, descending);
		while (rs.next()) {
			all.add(initTopicVersion(rs));
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllWikiFileTopicNames(String virtualWiki) throws Exception {
		List all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getAllWikiFileTopicNames(virtualWiki);
		while (rs.next()) {
			all.add(rs.getString("topic_name"));
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
		List all = new ArrayList();
		WikiFile wikiFile = lookupWikiFile(virtualWiki, topicName);
		if (wikiFile == null) {
			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
		}
		WikiResultSet rs = DatabaseHandler.queryHandler.getAllWikiFileVersions(wikiFile, descending);
		while (rs.next()) {
			all.add(initWikiFileVersion(rs));
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllWikiUserLogins() throws Exception {
		List all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getAllWikiUserLogins();
		while (rs.next()) {
			all.add(rs.getString("login"));
		}
		return all;
	}

	/**
	 *
	 */
	public static String getDatabaseType() {
		return Environment.getValue(Environment.PROP_DB_TYPE);
	}

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception {
		List all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getLockList(virtualWiki);
		while (rs.next()) {
			Topic topic = initTopic(rs);
			all.add(topic);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getReadOnlyTopics(virtualWiki);
		while (rs.next()) {
			all.add(rs.getString("topic_name"));
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, int num, boolean descending) throws Exception {
		ArrayList all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getRecentChanges(virtualWiki, num, descending);
		while (rs.next()) {
			RecentChange change = initRecentChange(rs);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getUserContributions(String virtualWiki, String userString, int num, boolean descending) throws Exception {
		Collection all = new ArrayList();
		WikiResultSet rs = DatabaseHandler.queryHandler.getUserContributions(virtualWiki, userString, num, descending);
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
		return virtualWikiNameHash.keySet();
	}

	/**
	 *
	 */
	public boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception {
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			// new topic
			return true;
		}
		if (topic.getLockSessionKey() == null) {
			return lockTopic(virtualWiki, topicName, key);
		}
		// FIXME - old code included a check to see if last version was made after the time
		// the lock was taken.  that should be impossible with the new code.
		return true;
	}

	/**
	 *
	 */
	public void initialize(Locale locale, WikiUser user) throws Exception {
		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_POSTGRES)) {
			DatabaseHandler.queryHandler = new PostgresQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL)) {
			DatabaseHandler.queryHandler = new MySqlQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE)) {
			DatabaseHandler.queryHandler = new OracleQueryHandler();
		} else {
			DatabaseHandler.queryHandler = new DefaultQueryHandler();
		}
		String sql = null;
		WikiResultSet rs = null;
		boolean tablesExist = false;
		sql = "select 1 from jam_virtual_wiki ";
		try {
			rs = DatabaseConnection.executeQuery(sql);
			tablesExist = rs.next();
		} catch (Exception e) {
			// thrown if table doesn't exist, so safe to ignore
		}
		if (!tablesExist) {
			// set up tables
			DatabaseHandler.queryHandler.createTables();
		}
		sql = "select * from jam_virtual_wiki ";
		rs = DatabaseConnection.executeQuery(sql);
		if (rs.size() == 0) {
			addVirtualWiki(WikiBase.DEFAULT_VWIKI);
		}
		super.initialize(locale, user);
	}

	/**
	 *
	 */
	protected static RecentChange initRecentChange(WikiResultSet rs) {
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
			logger.error("Failure while initializing recent change", e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static Topic initTopic(WikiResultSet rs) {
		try {
			int virtualWikiId = rs.getInt("virtual_wiki_id");
			String virtualWiki = lookupVirtualWikiName(virtualWikiId);
			Topic topic = new Topic();
			topic.setAdminOnly(rs.getChar("topic_admin_only") != '0');
			topic.setName(rs.getString("topic_name"));
			topic.setVirtualWiki(virtualWiki);
			topic.setTopicContent(rs.getString("topic_content"));
			topic.setTopicId(rs.getInt("topic_id"));
			int lockedBy = rs.getInt("topic_locked_by");
			if (lockedBy > 0) topic.setLockedBy(new Integer(lockedBy));
			topic.setLockedDate(rs.getTimestamp("topic_lock_date"));
			topic.setLockSessionKey(rs.getString("topic_lock_session_key"));
			topic.setReadOnly(rs.getChar("topic_read_only") != '0');
			topic.setDeleted(rs.getChar("topic_deleted") != '0');
			topic.setTopicType(rs.getInt("topic_type"));
			return topic;
		} catch (Exception e) {
			logger.error("Failure while initializing topic", e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static TopicVersion initTopicVersion(WikiResultSet rs) {
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
			logger.error("Failure while initializing topic version", e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static WikiFile initWikiFile(WikiResultSet rs) {
		try {
			int virtualWikiId = rs.getInt("virtual_wiki_id");
			String virtualWiki = lookupVirtualWikiName(virtualWikiId);
			WikiFile wikiFile = new WikiFile();
			wikiFile.setFileId(rs.getInt("file_id"));
			wikiFile.setAdminOnly(rs.getChar("file_admin_only") != '0');
			wikiFile.setFileName(rs.getString("file_name"));
			wikiFile.setVirtualWiki(virtualWiki);
			wikiFile.setUrl(rs.getString("file_url"));
			wikiFile.setTopicId(rs.getInt("topic_id"));
			wikiFile.setReadOnly(rs.getChar("file_read_only") != '0');
			wikiFile.setDeleted(rs.getChar("file_deleted") != '0');
			wikiFile.setMimeType(rs.getString("mime_type"));
			wikiFile.setFileSize(rs.getInt("file_size"));
			return wikiFile;
		} catch (Exception e) {
			logger.error("Failure while initializing file", e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static WikiFileVersion initWikiFileVersion(WikiResultSet rs) {
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
			logger.error("Failure while initializing wiki file version", e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static WikiUser initWikiUser(WikiResultSet rs) {
		try {
			WikiUser user = new WikiUser();
			user.setUserId(rs.getInt("wiki_user_id"));
			user.setLogin(rs.getString("login"));
			user.setDisplayName(rs.getString("display_name"));
			user.setCreateDate(rs.getTimestamp("create_date"));
			user.setLastLoginDate(rs.getTimestamp("last_login_date"));
			user.setCreateIpAddress(rs.getString("create_ip_address"));
			user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
			user.setAdmin(rs.getChar("is_admin") != '0');
			// FIXME - may be in LDAP
			user.setEmail(rs.getString("email"));
			user.setFirstName(rs.getString("first_name"));
			user.setLastName(rs.getString("last_name"));
			user.setEncodedPassword(rs.getString("encoded_password"));
			return user;
		} catch (Exception e) {
			logger.error("Failure while initializing user", e);
			return null;
		}
	}

	/**
	 *
	 */
	public static boolean isMySQL() {
		return Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL);
	}

	/**
	 *
	 */
	protected static void loadVirtualWikiHashes() throws Exception {
		virtualWikiNameHash = new Hashtable();
		virtualWikiIdHash = new Hashtable();
		try {
			WikiResultSet rs = DatabaseHandler.queryHandler.getVirtualWikis();
			while (rs.next()) {
				Integer value = new Integer(rs.getInt("virtual_wiki_id"));
				String key = rs.getString("virtual_wiki_name");
				virtualWikiNameHash.put(key, value);
				virtualWikiIdHash.put(value, key);
			}
		} catch (Exception e) {
			logger.error("Failure while loading virtual wiki hashtable ", e);
			// if there is an error make sure the hashtable is reset since it wasn't
			// properly initialized
			virtualWikiNameHash = null;
			virtualWikiIdHash = null;
			throw e;
		}
	}

	/**
	 *
	 */
	protected TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception {
		Topic topic = lookupTopic(virtualWiki, topicName);
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupLastTopicVersion(topic);
		if (rs.size() == 0) return null;
		int topicVersionId = rs.getInt("topic_version_id");
		return lookupTopicVersion(virtualWiki, topicName, topicVersionId);
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopic(virtualWiki, topicName);
		if (rs.size() == 0) return null;
		return initTopic(rs);
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicVersion(virtualWiki, topicName, topicVersionId);
		if (rs.size() == 0) return null;
		return initTopicVersion(rs);
	}

	/**
	 *
	 */
	protected static int lookupVirtualWikiId(String virtualWikiName) throws Exception {
		if (virtualWikiNameHash == null) {
			loadVirtualWikiHashes();
		}
		Integer virtualWikiId = (Integer)virtualWikiNameHash.get(virtualWikiName);
		if (virtualWikiId == null) {
			throw new Exception("Virtual wiki " + virtualWikiName + " not found");
		}
		return virtualWikiId.intValue();
	}

	/**
	 *
	 */
	protected static String lookupVirtualWikiName(int virtualWikiId) throws Exception {
		if (virtualWikiIdHash == null) {
			loadVirtualWikiHashes();
		}
		String virtualWikiName = (String)virtualWikiIdHash.get(new Integer(virtualWikiId));
		if (virtualWikiName == null) {
			throw new Exception("Virtual wiki " + virtualWikiId + " not found");
		}
		return virtualWikiName;
	}

	/**
	 *
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null) return null;
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiFile(virtualWiki, topic.getTopicId());
		if (rs.size() == 0) return null;
		return initWikiFile(rs);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(int userId) throws Exception {
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(userId);
		if (rs.size() == 0) return null;
		return initWikiUser(rs);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login) throws Exception {
		// FIXME - handle LDAP
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(login);
		if (rs.size() == 0) return null;
		int userId = rs.getInt("wiki_user_id");
		return lookupWikiUser(userId);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login, String password) throws Exception {
		// FIXME - handle LDAP
		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(login, password);
		if (rs.size() == 0) return null;
		int userId = rs.getInt("wiki_user_id");
		return lookupWikiUser(userId);
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	public void purgeData() throws Exception {
		// BOOM!  Everything gone...
		DatabaseHandler.queryHandler.dropTables();
		// re-create empty tables
		DatabaseHandler.queryHandler.createTables();
	}

	/**
	 *
	 */
	public static void reloadRecentChanges() throws Exception {
		DatabaseHandler.queryHandler.reloadRecentChanges();
	}

	/**
	 *
	 */
	public static boolean testDatabase() {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
		} catch (Exception e) {
			// database settings incorrect
			logger.error("Invalid database settings", e);
			return false;
		} finally {
			if (conn != null) DatabaseConnection.closeConnection(conn);
		}
		return true;
	}

	/**
	 *
	 */
	protected void updateTopic(Topic topic) throws Exception {
		DatabaseHandler.queryHandler.updateTopic(topic);
	}

	/**
	 *
	 */
	protected void updateWikiFile(String topicName, WikiFile wikiFile) throws Exception {
		DatabaseHandler.queryHandler.updateWikiFile(wikiFile);
	}

	/**
	 *
	 */
	protected void updateWikiUser(WikiUser user) throws Exception {
		DatabaseHandler.queryHandler.updateWikiUser(user);
		// FIXME - may be in LDAP
		DatabaseHandler.queryHandler.updateWikiUserInfo(user);
	}
}
