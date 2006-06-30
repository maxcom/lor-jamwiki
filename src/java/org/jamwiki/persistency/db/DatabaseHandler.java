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
package org.jamwiki.persistency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.WikiBase;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class DatabaseHandler extends PersistencyHandler {

	public static final String DB_TYPE_ORACLE = "oracle";
	public static final String DB_TYPE_MYSQL = "mysql";
	private static final Logger logger = Logger.getLogger(DatabaseHandler.class);
	private static Hashtable virtualWikiIdHash = null;
	private static Hashtable virtualWikiNameHash = null;

	private static final String STATEMENT_INSERT_TOPIC =
		"insert into jmw_topic ( "
		+   "topic_id, virtual_wiki_id, topic_name, topic_type, "
		+   "topic_locked_by, topic_lock_date, topic_read_only, topic_content, "
		+   "topic_lock_session_key "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ?, ?"
		+ ") ";
	private static final String STATEMENT_INSERT_TOPIC_VERSION =
		"insert into jmw_topic_version ("
		+   "topic_version_id, topic_id, edit_comment, version_content, "
		+   "author_id, edit_type, author_ip_address, edit_date "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ? "
		+ ") ";
	private static final String STATEMENT_INSERT_RECENT_CHANGE =
		"insert into jmw_recent_change ("
		+   "topic_version_id, previous_topic_version_id, topic_id, "
		+   "topic_name, edit_date, edit_comment, author_id, "
		+   "display_name, edit_type, virtual_wiki_id, virtual_wiki_name "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
		+ ") ";
	private static final String STATEMENT_INSERT_RECENT_CHANGES =
		"INSERT INTO jmw_recent_change ( "
		+   "topic_version_id, topic_id, "
		+   "topic_name, edit_date, author_id, display_name, "
		+   "edit_type, virtual_wiki_id, virtual_wiki_name, edit_comment "
		+ ") "
		+ "SELECT "
		+   "jmw_topic_version.topic_version_id, jmw_topic.topic_id, "
		+   "jmw_topic.topic_name, jmw_topic_version.edit_date, "
		+   "jmw_topic_version.author_id, jmw_author.display_name, "
		+   "jmw_topic_version.edit_type, jmw_virtual_wiki.virtual_wiki_id, "
		+   "jmw_virtual_wiki.virtual_wiki_name, jmw_topic_version.edit_comment "
		+ "FROM jmw_topic, jmw_topic_version, jmw_author, jmw_virtual_wiki "
		+ "WHERE jmw_topic.topic_id = jmw_topic_version.topic_id "
		+ "AND jmw_topic_version.author_id = jmw_author.author_id "
		+ "AND jmw_topic.virtual_wiki_id = jmw_virtual_wiki.virtual_wiki_id "
		+ "AND jmw_topic.topic_deleted = FALSE ";
	private static final String STATEMENT_INSERT_VIRTUAL_WIKI =
		"insert into jmw_virtual_wiki ("
		+   "virtual_wiki_id, virtual_wiki_name "
		+ ") values ( "
		+   "?, ? "
		+ ") ";
	private static final String STATEMENT_SELECT_RECENT_CHANGES =
		"select * from jmw_recent_change "
		+ "where virtual_wiki_name = ? "
		+ "order by edit_date desc "
		+ "limit ? ";
	private static final String STATEMENT_SELECT_TOPIC =
		"select * from jmw_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_name = ? ";
	private static final String STATEMENT_SELECT_TOPICS =
		"select * from jmw_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_deleted = FALSE ";
	private static final String STATEMENT_SELECT_TOPIC_READ_ONLY =
		"select * from jmw_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_read_only = ? "
		+ "and topic_deleted = FALSE ";
	private static final String STATEMENT_SELECT_TOPIC_LOCKED =
		"select * from jmw_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_lock_session_key is not null "
		+ "and topic_deleted = FALSE ";
	private static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
		"select nextval('jmw_topic_seq') as topic_id ";
	private static final String STATEMENT_SELECT_TOPIC_VERSION =
		"select * from jmw_topic_version "
		+ "where topic_version_id = ? ";
	private static final String STATEMENT_SELECT_TOPIC_VERSIONS =
		"select * from jmw_topic_version "
		+ "where topic_id = ? "
		+ "order by topic_version_id desc ";
	private static final String STATEMENT_SELECT_TOPIC_VERSION_LAST =
		"select max(topic_version_id) as topic_version_id from jmw_topic_version "
		+ "where topic_id = ? ";
	private static final String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE =
		"select nextval('jmw_topic_version_seq') as topic_version_id ";
	private static final String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE =
		"select nextval('jmw_virtual_wiki_seq') as virtual_wiki_id ";
	private static final String STATEMENT_UPDATE_TOPIC =
		"update jmw_topic set "
		+ "virtual_wiki_id = ?, "
		+ "topic_name = ?, "
		+ "topic_type = ?, "
		+ "topic_locked_by = ?, "
		+ "topic_lock_date = ?, "
		+ "topic_read_only = ?, "
		+ "topic_content = ?, "
		+ "topic_lock_session_key = ?, "
		+ "topic_deleted = ? "
		+ "where topic_id = ? ";

	/**
	 *
	 */
	public DatabaseHandler() {
	}

	/**
	 *
	 */
	protected void addRecentChange(RecentChange change) throws Exception {
		int virtualWikiId = lookupVirtualWikiId(change.getVirtualWiki());
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_INSERT_RECENT_CHANGE);
			stmt.setInt(1, change.getTopicVersionId());
			if (change.getPreviousTopicVersionId() > 0) {
				stmt.setInt(2, change.getPreviousTopicVersionId());
			} else {
				stmt.setNull(2, Types.INTEGER);
			}
			stmt.setInt(3, change.getTopicId());
			stmt.setString(4, change.getTopicName());
			stmt.setTimestamp(5, change.getEditDate());
			stmt.setString(6, change.getEditComment());
			if (change.getAuthorId() > 0) {
				stmt.setInt(7, change.getAuthorId());
			} else {
				stmt.setNull(7, Types.INTEGER);
			}
			stmt.setString(8, change.getAuthorName());
			stmt.setInt(9, change.getEditType());
			stmt.setInt(10, virtualWikiId);
			stmt.setString(11, change.getVirtualWiki());
			stmt.executeUpdate();
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt);
			}
		}
	}

	/**
	 *
	 */
	protected void addTopic(Topic topic) throws Exception {
		int virtualWikiId = lookupVirtualWikiId(topic.getVirtualWiki());
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DatabaseConnection.getConnection();
			if (topic.getTopicId() <= 0) {
				// add
				WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE, conn);
				topic.setTopicId(rs.getInt("topic_id"));
				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC);
				stmt.setInt(1, topic.getTopicId());
				stmt.setInt(2, virtualWikiId);
				stmt.setString(3, topic.getName());
				stmt.setInt(4, topic.getTopicType());
				if (topic.getLockedBy() > 0) {
					stmt.setInt(5, topic.getLockedBy());
				} else {
					stmt.setNull(5, Types.INTEGER);
				}
				stmt.setTimestamp(6, topic.getLockedDate());
				stmt.setBoolean(7, topic.getReadOnly());
				stmt.setString(8, topic.getTopicContent());
				stmt.setString(9, topic.getLockSessionKey());
				stmt.executeUpdate();
			} else {
				// update
				stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC);
				stmt.setInt(1, virtualWikiId);
				stmt.setString(2, topic.getName());
				stmt.setInt(3, topic.getTopicType());
				if (topic.getLockedBy() > 0) {
					stmt.setInt(4, topic.getLockedBy());
				} else {
					stmt.setNull(4, Types.INTEGER);
				}
				stmt.setTimestamp(5, topic.getLockedDate());
				stmt.setBoolean(6, topic.getReadOnly());
				stmt.setString(7, topic.getTopicContent());
				stmt.setString(8, topic.getLockSessionKey());
				stmt.setBoolean(9, topic.getDeleted());
				stmt.setInt(10, topic.getTopicId());
				stmt.executeUpdate();
			}
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt);
			}
		}
	}

	/**
	 *
	 */
	protected void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DatabaseConnection.getConnection();
			WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE, conn);
			topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
			Timestamp editDate = new Timestamp(System.currentTimeMillis());
			if (topicVersion.getEditDate() != null) {
				editDate = topicVersion.getEditDate();
			}
			stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_VERSION);
			stmt.setInt(1, topicVersion.getTopicVersionId());
			stmt.setInt(2, topicVersion.getTopicId());
			stmt.setString(3, topicVersion.getEditComment());
			stmt.setString(4, topicVersion.getVersionContent());
			stmt.setInt(5, topicVersion.getAuthorId());
			stmt.setInt(6, topicVersion.getEditType());
			stmt.setString(7, topicVersion.getAuthorIpAddress());
			stmt.setTimestamp(8, editDate);
			stmt.executeUpdate();
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt);
			}
		}
	}

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWikiName) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		int virtualWikiId;
		try {
			conn = DatabaseConnection.getConnection();
			WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE, conn);
			virtualWikiId = rs.getInt("virtual_wiki_id");
			stmt = conn.prepareStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
			stmt.setInt(1, virtualWikiId);
			stmt.setString(2, virtualWikiName);
			stmt.executeUpdate();
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt);
			}
		}
		if (virtualWikiNameHash != null) {
			virtualWikiNameHash.put(virtualWikiName, new Integer(virtualWikiId));
		}
		if (virtualWikiIdHash != null) {
			virtualWikiIdHash.put(new Integer(virtualWikiId), virtualWikiName);
		}
	}

	/**
	 *
	 */
	public List getAllTopicNames(String virtualWiki) throws Exception {
		List all = new ArrayList();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			int virtualWikiId = lookupVirtualWikiId(virtualWiki);
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPICS);
			stmt.setInt(1, virtualWikiId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				all.add(rs.getString("topic_name"));
			}
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllVersions(String virtualWiki, String topicName) throws Exception {
		List all = new ArrayList();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
		}
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSIONS);
			stmt.setInt(1, topic.getTopicId());
			rs = stmt.executeQuery();
			WikiResultSet wrs = new WikiResultSet(rs);
			while (wrs.next()) {
				all.add(initTopicVersion(wrs));
			}
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
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
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_LOCKED);
			stmt.setInt(1, virtualWikiId);
			rs = stmt.executeQuery();
			WikiResultSet wrs = new WikiResultSet(rs);
			while (wrs.next()) {
				Topic topic = initTopic(wrs);
				all.add(topic);
			}
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_READ_ONLY);
			stmt.setInt(1, virtualWikiId);
			stmt.setBoolean(2, true);
			rs = stmt.executeQuery();
			while (rs.next()) {
				all.add(rs.getString("topic_name"));
			}
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt, rs);
			}
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, int numChanges) throws Exception {
		ArrayList all = new ArrayList();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_RECENT_CHANGES);
			stmt.setString(1, virtualWiki);
			stmt.setInt(2, numChanges);
			rs = stmt.executeQuery();
			WikiResultSet wrs = new WikiResultSet(rs);
			while (wrs.next()) {
				RecentChange change = initRecentChange(wrs);
				all.add(change);
			}
			return all;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
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
	public void initialize(Locale locale) throws Exception {
		String sql = null;
		WikiResultSet rs = null;
		boolean tablesExist = false;
		sql = "select 1 from jmw_virtual_wiki ";
		try {
			rs = DatabaseConnection.executeQuery(sql);
			tablesExist = rs.next();
		} catch (Exception e) {
			// thrown if table doesn't exist, so safe to ignore
		}
		if (!tablesExist) {
			// set up tables
			DatabaseInit.initialize();
		}
		sql = "select * from jmw_virtual_wiki ";
		rs = DatabaseConnection.executeQuery(sql);
		if (rs.size() == 0) {
			addVirtualWiki(WikiBase.DEFAULT_VWIKI);
		}
		// add default author
		// FIXME - use prepared statement
		sql = "insert into jmw_author ( "
		    +   "login, virtual_wiki_id, display_name, encoded_password, "
		    +   "initial_ip_address, last_ip_address "
		    + ") values ( "
		    +   "'" + PersistencyHandler.DEFAULT_AUTHOR_LOGIN + "', "
		    +   "1, "
		    +   "'" + PersistencyHandler.DEFAULT_AUTHOR_NAME + "', "
		    +   "'" + Encryption.encrypt(PersistencyHandler.DEFAULT_PASSWORD) + "', "
		    +   "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "', "
		    +   "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "' "
		    + ") ";
		DatabaseConnection.executeUpdate(sql);
		super.initialize(locale);
	}

	/**
	 *
	 */
	protected static RecentChange initRecentChange(WikiResultSet rs) {
		try {
			RecentChange change = new RecentChange();
			change.setTopicVersionId(rs.getInt("topic_version_id"));
			change.setPreviousTopicVersionId(rs.getInt("previous_topic_version_id"));
			change.setTopicId(rs.getInt("topic_id"));
			change.setTopicName(rs.getString("topic_name"));
			change.setEditDate(rs.getTimestamp("edit_date"));
			change.setEditComment(rs.getString("edit_comment"));
			change.setAuthorId(rs.getInt("author_id"));
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
			topic.setAdminOnly(rs.getBoolean("topic_admin_only"));
			topic.setName(rs.getString("topic_name"));
			topic.setVirtualWiki(virtualWiki);
			topic.setTopicContent(rs.getString("topic_content"));
			topic.setTopicId(rs.getInt("topic_id"));
			topic.setLockedBy(rs.getInt("topic_locked_by"));
			topic.setLockedDate(rs.getTimestamp("topic_lock_date"));
			topic.setLockSessionKey(rs.getString("topic_lock_session_key"));
			topic.setReadOnly(rs.getBoolean("topic_read_only"));
			topic.setDeleted(rs.getBoolean("topic_deleted"));
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
			topicVersion.setAuthorId(rs.getInt("author_id"));
			topicVersion.setEditDate(rs.getTimestamp("edit_date"));
			topicVersion.setEditType(rs.getInt("edit_type"));
			topicVersion.setAuthorIpAddress(rs.getString("author_ip_address"));
			return topicVersion;
		} catch (Exception e) {
			logger.error("Failure while initializing topic version", e);
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
	public static boolean isOracle() {
		return Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE);
	}

	/**
	 *
	 */
	public static void loadRecentChanges() throws Exception {
		String sql;
		sql = "DELETE from jmw_recent_change";
		DatabaseConnection.executeUpdate(sql);
		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES);
		// FIXME - slow
		sql = "SELECT topic_id, topic_version_id from jmw_recent_change "
		    + "WHERE previous_topic_version_id is null ";
		WikiResultSet rs = DatabaseConnection.executeQuery(sql);
		while (rs.next()) {
			// FIXME - postgres specific
			sql = "UPDATE jmw_recent_change SET "
			    + "previous_topic_version_id = ( "
			    +   "select max(jmw_topic_version.topic_version_id) "
			    +   "from jmw_topic_version "
			    +   "where jmw_topic_version.topic_id = " + rs.getInt("topic_id") + " "
			    +   "and jmw_topic_version.topic_version_id < " + rs.getInt("topic_version_id") + " "
			    + ") "
			    + "where topic_id = " + rs.getInt("topic_id") + " "
			    + "and topic_version_id = " + rs.getInt("topic_version_id") + " ";
			DatabaseConnection.executeUpdate(sql);
		}
	}

	/**
	 *
	 */
	protected static void loadVirtualWikiHashes() throws Exception {
		virtualWikiNameHash = new Hashtable();
		virtualWikiIdHash = new Hashtable();
		String sql = "select * from jmw_virtual_wiki ";
		try {
			WikiResultSet rs = DatabaseConnection.executeQuery(sql);
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
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Topic topic = lookupTopic(virtualWiki, topicName);
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION_LAST);
			stmt.setInt(1, topic.getTopicId());
			rs = stmt.executeQuery();
			if (rs == null) return null;
			rs.next();
			int topicVersionId = rs.getInt("topic_version_id");
			return lookupTopicVersion(virtualWiki, topicName, topicVersionId);
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt, rs);
			}
		}
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC);
			stmt.setInt(1, virtualWikiId);
			stmt.setString(2, topicName);
			rs = stmt.executeQuery();
			WikiResultSet wrs = new WikiResultSet(rs);
			if (wrs.size() == 0) return null;
			return initTopic(wrs);
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt, rs);
			}
		}
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION);
			stmt.setInt(1, topicVersionId);
			rs = stmt.executeQuery();
			WikiResultSet wrs = new WikiResultSet(rs);
			if (wrs.size() == 0) return null;
			return initTopicVersion(wrs);
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt, rs);
			}
		}
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
}
