/**
 *
 */
package org.jmwiki.persistency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.persistency.PersistencyHandler;
import org.jmwiki.TopicLock;
import org.jmwiki.VersionManager;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiException;
import org.jmwiki.PseudoTopicHandler;
import org.jmwiki.model.Topic;
import org.jmwiki.model.TopicVersion;
import org.jmwiki.utils.Utilities;

/**
 *
 */
public class DatabaseHandler implements PersistencyHandler {

	public static final String DB_TYPE_ORACLE = "oracle";
	public static final String DB_TYPE_MYSQL = "mysql";
	private static final Logger logger = Logger.getLogger(DatabaseHandler.class);
	private static Hashtable virtualWikiHash = null;

	/**
	 *
	 */
	public void addTopic(Topic topic) throws Exception {
		int virtualWikiId = lookupVirtualWiki(topic.getVirtualWiki());
		// FIXME - switch this to a PreparedStatement
		String sql = "select nextval('vqw_topic_seq') as topic_id ";
		WikiResultSet rs = DatabaseHandler.executeQuery(sql);
		topic.setTopicId(rs.getInt("topic_id"));
		sql = "insert into vqw_topic ( "
		    +   "topic_id, virtual_wiki_id, topic_name, topic_type, "
		    +   "topic_locked_by, topic_lock_date, topic_read_only "
		    + ") values ( "
		    +   topic.getTopicId() + ", "
		    +   virtualWikiId + ", "
		    +   escapeSQL(topic.getName()) + ", "
		    +   topic.getTopicType() + ", "
		    +   topic.getLockedBy() + ", "
		    +   topic.getLockedDate() + ", "
		    +   topic.getReadOnly() + " "
		    + ") ";
		DatabaseHandler.executeUpdate(sql);
	}

	/**
	 *
	 */
	public void addTopicVersion(TopicVersion topicVersion) throws Exception {
		// FIXME - use PreparedStatement
		String sql;
		sql = "select nextval('vqw_topic_version_seq') as topic_version_id ";
		WikiResultSet rs = DatabaseHandler.executeQuery(sql);
		topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
		sql = "insert into vqw_topic_version ("
		    +   "topic_version_id, topic_id, edit_comment, version_content, "
		    +   "author_id, edit_date, edit_type "
		    + ") values ( "
		    +   topicVersion.getTopicVersionId() + ", "
		    +   topicVersion.getTopicId() + ", "
		    +   escapeSQL(topicVersion.getEditComment()) + ", "
		    +   escapeSQL(topicVersion.getVersionContent()) + ", "
		    +   topicVersion.getAuthorId() + ", "
		    // FIXME - CURRENT_TIME is postgres specific, also, what if it has been set already?
		    +   "CURRENT_TIMESTAMP, "
//		    +   topicVersion.getEditDate() + ", "
		    +   topicVersion.getEditType() + " "
		    + ") ";
		DatabaseHandler.executeUpdate(sql);
	}

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWikiName) throws Exception {
		// FIXME - use PreparedStatement
		String sql;
		sql = "select nextval('vqw_virtual_wiki_seq') as virtual_wiki_id ";
		WikiResultSet rs = DatabaseHandler.executeQuery(sql);
		int virtualWikiId = rs.getInt("virtual_wiki_id");
		sql = "insert into vqw_virtual_wiki ("
		    +   "virtual_wiki_id, virtual_wiki_name "
		    + ") values ( "
		    +   virtualWikiId + ", "
		    +   escapeSQL(virtualWikiName) + " "
		    + ") ";
		DatabaseHandler.executeUpdate(sql);
		if (virtualWikiHash != null) {
			virtualWikiHash.put(virtualWikiName, new Integer(virtualWikiId));
		}
	}

	/**
	 *
	 */
	public static String escapeSQL(String sql) {
		// FIXME - hard coding of "NULL" should be database-specific
		if (sql == null) return "NULL";
		return "'" + Utilities.replaceString(sql, "'", "''") + "'";
	}

	/**
	 *
	 */
	protected static WikiResultSet executeQuery(String sql) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.createStatement();
			logger.info("Executing SQL: " + sql);
			rs = stmt.executeQuery(sql);
			return new WikiResultSet(rs);
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt, rs);
			}
		}
	}

	/**
	 *
	 */
	protected static void executeUpdate(String sql) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.createStatement();
			logger.info("Executing SQL: " + sql);
			stmt.executeUpdate(sql);
		} finally {
			if (conn != null) {
				DatabaseConnection.closeConnection(conn, stmt);
			}
		}
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, int num) throws Exception {
		// FIXME - implement this
		return new Vector();
	}

	/**
	 *
	 */
	public int lookupTopic(String virtualWiki, String topic) throws Exception {
		int virtualWikiId = lookupVirtualWiki(virtualWiki);
		String sql = "select topic_id from vqw_topic "
		           + "where virtual_wiki_id = " + virtualWikiId + " "
		           + "and topic_name = " + escapeSQL(topic) + " ";
		WikiResultSet rs = executeQuery(sql);
		return (rs.next() ? rs.getInt("topic_id") : -1);
	}

	/**
	 *
	 */
	protected static int lookupVirtualWiki(String virtualWiki) throws Exception {
		if (virtualWikiHash == null) {
			loadVirtualWikiHash();
		}
		Integer virtualWikiId = (Integer)virtualWikiHash.get(virtualWiki);
		if (virtualWikiId == null) {
			throw new Exception("Virtual wiki " + virtualWiki + " not found");
		}
		return virtualWikiId.intValue();
	}

	/**
	 *
	 */
	protected static void loadVirtualWikiHash() throws Exception {
		virtualWikiHash = new Hashtable();
		String sql = "select * from vqw_virtual_wiki ";
		try {
			WikiResultSet rs = DatabaseHandler.executeQuery(sql);
			while (rs.next()) {
				Integer value = new Integer(rs.getInt("virtual_wiki_id"));
				String key = rs.getString("virtual_wiki_name");
				virtualWikiHash.put(key, value);
			}
		} catch (Exception e) {
			logger.error("Failure while loading virtual wiki hashtable ", e);
			// if there is an error make sure the hashtable is reset since it wasn't
			// properly initialized
			virtualWikiHash = null;
			throw e;
		}
	}

	/**
	 *
	 */
	public void updateTopic(Topic topic) throws Exception {
		int virtualWikiId = lookupVirtualWiki(topic.getVirtualWiki());
		String sql = null;
		// FIXME - switch this to a PreparedStatement
		sql = "update vqw_topic set "
		    + "virtual_wiki_id = " + virtualWikiId + ", "
		    + "topic_name = " + escapeSQL(topic.getName()) + ", "
		    + "topic_type = " + topic.getTopicType() + ", "
		    + "topic_locked_by = " + topic.getLockedBy() + ", "
		    + "topic_lock_date = " + topic.getLockedDate() + ", "
		    + "topic_read_only = " + topic.getReadOnly() + " "
		    + "where topic_id = " + topic.getTopicId() + " ";
		DatabaseHandler.executeUpdate(sql);
	}

	// ======================================
	// DELETE THE CODE BELOW
	// ======================================

	protected final static String STATEMENT_READ =
		"SELECT contents FROM Topic WHERE name = ? AND virtualwiki = ?";
	protected final static String STATEMENT_UPDATE =
		"UPDATE Topic SET contents = ? WHERE name = ? AND virtualwiki = ?";
	protected final static String STATEMENT_UPDATE_ORACLE1 =
		"UPDATE Topic SET contents = EMPTY_CLOB() WHERE name = ? AND virtualwiki = ?";
	protected final static String STATEMENT_UPDATE_ORACLE2 =
		"SELECT contents FROM Topic WHERE name = ? AND virtualwiki = ? FOR UPDATE";
	protected final static String STATEMENT_INSERT =
		"INSERT INTO Topic( name, contents, virtualwiki ) VALUES ( ?, ?, ? )";
	protected final static String STATEMENT_INSERT_ORACLE =
		"INSERT INTO Topic( name, contents, virtualwiki ) VALUES ( ?, EMPTY_CLOB(), ? )";
	protected final static String STATEMENT_SET_LOCK =
		"INSERT INTO TopicLock( topic, sessionkey, lockat, virtualwiki ) VALUES( ?, ?, ?, ? )";
	protected final static String STATEMENT_CHECK_LOCK =
		"SELECT lockat, sessionkey FROM TopicLock WHERE topic = ? AND virtualwiki = ?";
	protected final static String STATEMENT_CHECK_SPECIFIC_LOCK =
		"SELECT lockat, sessionkey FROM TopicLock WHERE topic = ? AND virtualwiki = ? AND sessionkey = ?";
	protected final static String STATEMENT_REMOVE_LOCK =
		"DELETE FROM TopicLock WHERE topic = ? AND virtualwiki = ?";
	protected final static String STATEMENT_REMOVE_ANY_LOCK =
		"DELETE FROM TopicLock WHERE topic = ? AND virtualwiki = ?";
	protected final static String STATEMENT_READONLY_INSERT =
		"INSERT INTO TopicReadOnly( topic, virtualwiki ) VALUES ( ?, ? )";
	protected final static String STATEMENT_READONLY_DELETE =
		"DELETE FROM TopicReadOnly WHERE topic = ? AND virtualwiki = ?";
	protected final static String STATEMENT_READONLY_ALL =
		"SELECT topic FROM TopicReadOnly";
	protected final static String STATEMENT_READONLY_FIND =
		"SELECT COUNT(*) FROM TopicReadOnly WHERE topic = ? AND virtualwiki = ?";
	protected static final String STATEMENT_PURGE_DELETES =
		"DELETE FROM Topic WHERE virtualwiki = ? AND (contents = 'delete\n' or contents = '\n' or contents = '')";
	protected static final String STATEMENT_PURGE_TOPIC =
		"DELETE FROM Topic WHERE virtualwiki = ? AND name = ?";
	protected static final String STATEMENT_TOPICS_TO_PURGE =
		"SELECT name FROM Topic WHERE virtualwiki = ? AND (contents = 'delete\n' or contents = '\n' or contents = '')";
	protected static final String STATEMENT_ALL_TOPICS =
		"SELECT name, contents FROM Topic WHERE virtualwiki = ?";
	protected static final String STATEMENT_ALL_OLDER_TOPICS =
		"SELECT name, contents FROM Topic WHERE virtualwiki = ? AND versionat < ?";
	protected final static String STATEMENT_PURGE_VERSIONS =
		"DELETE FROM TopicVersion WHERE versionat < ? AND virtualwiki = ?";
	protected final static String STATEMENT_GET_LOCK_LIST =
		"SELECT * FROM TopicLock WHERE virtualwiki = ?";

	/**
	 *
	 */
	public DatabaseHandler() throws Exception {
		setDefaults(Locale.ENGLISH);
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
	public static String getDatabaseType() {
		return Environment.getValue(Environment.PROP_DB_TYPE);
	}

	/**
	 *
	 */
	private static boolean dbInitialized() {
		String sql = "select 1 from vqw_virtual_wiki ";
		try {
			WikiResultSet rs = executeQuery(sql);
			return rs.next();
		} catch (Exception e) {
			// thrown if table doesn't exist, so safe to ignore
		}
		return false;
	}

	/**
	 *
	 */
	public void setDefaults(Locale locale) throws Exception {
		logger.debug("Setting defaults");
		// resources for i18n
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
		if (!DatabaseHandler.dbInitialized()) {
			//set up tables
			DatabaseInit.initialize();
		}
		String sql = null;
		WikiResultSet rs = null;
		sql = "select 1 from vqw_virtual_wiki ";
		rs = executeQuery(sql);
		if (!rs.next()) {
			addVirtualWiki(WikiBase.DEFAULT_VWIKI);
		}
		sql = "select * from vqw_virtual_wiki ";
		rs = executeQuery(sql);
		while (rs.next()) {
			String vWiki = rs.getString("virtual_wiki_name");
			// starting points
			setupSpecialPage(vWiki, messages.getString("specialpages.startingpoints"));
			// leftMenu
			setupSpecialPage(vWiki, messages.getString("specialpages.leftMenu"));
			// topArea
			setupSpecialPage(vWiki, messages.getString("specialpages.topArea"));
			// bottomArea
			setupSpecialPage(vWiki, messages.getString("specialpages.bottomArea"));
			// stylesheet
			setupSpecialPage(vWiki, messages.getString("specialpages.stylesheet"));
			// list of topics that only admin is allowed to edit/view by themselves
			setupSpecialPage(vWiki, messages.getString("specialpages.adminonlytopics"));
			if (!exists(vWiki, "SetUsername")) {
				write(vWiki, "", "SetUsername");
			}
		}
	}

	/**
	 *
	 */
	private void setupSpecialPage(String vWiki, String specialPage) throws Exception {
		if (!exists(vWiki, specialPage)) {
			logger.debug("Setting up " + specialPage);
			write(vWiki, WikiBase.readDefaultTopic(specialPage), specialPage);
		}
	}

	/**
	 *
	 */
	public String read(String virtualWiki, String topicName) throws Exception {
		if (virtualWiki == null || virtualWiki.length() == 0) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		String contents = null;
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement readStatement = conn.prepareStatement(STATEMENT_READ);
			readStatement.setString(1, topicName);
			readStatement.setString(2, virtualWiki);
			ResultSet rs = readStatement.executeQuery();
			if (!rs.next()) {
				return "This is a new topic";
			}
			if (DatabaseHandler.isOracle()) {
				contents = OracleClobHelper.getClobValue(rs.getClob("contents"));
			} else {
				contents = rs.getString("contents");
			}
			rs.close();
			readStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return (contents);
	}

	/**
	 *
	 */
	public void write(String virtualWiki, String contents, String topicName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			if (!exists(virtualWiki, topicName)) {
				logger.debug("Inserting into topic " + topicName + ", " + contents);
				PreparedStatement insertStatement;
				if (DatabaseHandler.isOracle()) {
					boolean savedAutoCommit = conn.getAutoCommit();
					conn.setAutoCommit(false);
					insertStatement = conn.prepareStatement(STATEMENT_INSERT_ORACLE);
					insertStatement.setString(1, topicName);
					insertStatement.setString(2, virtualWiki);
					insertStatement.execute();
					insertStatement.close();
					conn.commit();
					insertStatement = conn.prepareStatement(
						STATEMENT_UPDATE_ORACLE2,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE
					);
					insertStatement.setString(1, topicName);
					insertStatement.setString(2, virtualWiki);
					ResultSet rs = insertStatement.executeQuery();
					rs.next();
					OracleClobHelper.setClobValue(rs.getClob(1), contents);
					rs.close();
					insertStatement.close();
					conn.setAutoCommit(savedAutoCommit);
				} else {
					insertStatement = conn.prepareStatement(STATEMENT_INSERT);
					insertStatement.setString(1, topicName);
					insertStatement.setString(2, contents);
					insertStatement.setString(3, virtualWiki);
					insertStatement.execute();
					insertStatement.close();
				}
			} else {
				logger.debug("Updating topic " + topicName + " to " + contents);
				PreparedStatement updateStatement;
				if (DatabaseHandler.isOracle()) {
					boolean savedAutoCommit = conn.getAutoCommit();
					conn.setAutoCommit(false);
					updateStatement = conn.prepareStatement(STATEMENT_UPDATE_ORACLE1);
					updateStatement.setString(1, topicName);
					updateStatement.setString(2, virtualWiki);
					updateStatement.execute();
					updateStatement.close();
					conn.commit();
					updateStatement = conn.prepareStatement(
						STATEMENT_UPDATE_ORACLE2,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE
					);
					updateStatement.setString(1, topicName);
					updateStatement.setString(2, virtualWiki);
					ResultSet rs = updateStatement.executeQuery();
					rs.next();
					OracleClobHelper.setClobValue(rs.getClob(1), contents);
					rs.close();
					updateStatement.close();
					conn.commit();
					conn.setAutoCommit(savedAutoCommit);
				} else {
					updateStatement = conn.prepareStatement(STATEMENT_UPDATE);
					updateStatement.setString(2, topicName);
					updateStatement.setString(1, contents);
					updateStatement.setString(3, virtualWiki);
					updateStatement.execute();
					updateStatement.close();
				}
			}
			Topic topic = new Topic();
			topic.setName(topicName);
			topic.setVirtualWiki(virtualWiki);
			int topicId = lookupTopic(topic.getVirtualWiki(), topic.getName());
			topic.setTopicId(topicId);
			if (topicId == -1) {
				this.addTopic(topic);
			} else {
				this.updateTopic(topic);
			}
			TopicVersion version = new TopicVersion();
			version.setTopicId(topic.getTopicId());
			version.setVersionContent(contents);
			this.addTopicVersion(version);
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			// write version
			DatabaseVersionManager.getInstance().addVersion(virtualWiki, topicName, contents, new DBDate());
		}
	}

	/**
	 *
	 */
	public boolean exists(String virtualWiki, String topicName) throws Exception {
		return (lookupTopic(virtualWiki, topicName) != -1);
	}

	/**
	 *
	 */
	public boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement checkLockStatement = conn.prepareStatement(STATEMENT_CHECK_SPECIFIC_LOCK);
			checkLockStatement.setString(1, topicName);
			checkLockStatement.setString(2, virtualWiki);
			checkLockStatement.setString(3, key);
			ResultSet rs = checkLockStatement.executeQuery();
			if (!rs.next()) {
				rs.close();
				checkLockStatement.close();
				// Since the topic is not locked, there's no problem in allowing someone to save the topic.
				// Create a lock and return the outcome.
				return lockTopic(virtualWiki, topicName, key);
			}
			java.util.Date lockedAt = new DBDate(rs.getTimestamp("lockat"));
			VersionManager versionManager = WikiBase.getInstance().getVersionManagerInstance();
			java.util.Date lastRevision = versionManager.lastRevisionDate(virtualWiki, topicName);
			logger.debug("Checking for lock possession: locked at " + lockedAt + " last changed at " + lastRevision);
			if (lastRevision != null) {
				if (lastRevision.after(lockedAt)) {
					return false;
				}
			}
			rs.close();
			checkLockStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return true;
	}

	/**
	 *
	 */
	public boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement checkLockStatement = conn.prepareStatement(STATEMENT_CHECK_LOCK);
			checkLockStatement.setString(1, topicName);
			checkLockStatement.setString(2, virtualWiki);
			ResultSet rs = checkLockStatement.executeQuery();
			if (rs.next()) {
				DBDate date = new DBDate(rs.getTimestamp("lockat"));
				DBDate now = new DBDate();
				logger.debug("Already locked at " + date);
				long fiveMinutesAgo = now.getTime() - 60000 *
				Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT);
				if (date.getTime() < fiveMinutesAgo) {
					logger.debug("Lock expired");
					PreparedStatement removeLockStatement = conn.prepareStatement(STATEMENT_REMOVE_LOCK);
					removeLockStatement.setString(1, topicName);
					removeLockStatement.setString(2, virtualWiki);
					removeLockStatement.execute();
					removeLockStatement.close();
				} else {
					String existingKey = rs.getString("sessionkey");
					rs.close();
					checkLockStatement.close();
					// if the key being locked with is the existing lock, then the user still has
					// the lock, otherwise, it must be locked by someone else
					boolean sameKey = existingKey.equals(key);
					logger.debug("Same key: " + sameKey);
					return sameKey;
				}
			}
			logger.debug("Setting lock");
			PreparedStatement setLockStatement = conn.prepareStatement(STATEMENT_SET_LOCK);
			setLockStatement.setString(1, topicName);
			setLockStatement.setString(2, key);
			setLockStatement.setTimestamp(3, (new DBDate()).asTimestamp());
			setLockStatement.setString(4, virtualWiki);
			setLockStatement.execute();
			setLockStatement.close();
			rs.close();
			checkLockStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return true;
	}

	/**
	 *
	 */
	public void unlockTopic(String virtualWiki, String topicName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement removeAnyLockStatement = conn.prepareStatement(STATEMENT_REMOVE_ANY_LOCK);
			removeAnyLockStatement.setString(1, topicName);
			removeAnyLockStatement.setString(2, virtualWiki);
			removeAnyLockStatement.execute();
			removeAnyLockStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public boolean isTopicReadOnly(String virtualWiki, String topicName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement findReadOnlyStatement = conn.prepareStatement(STATEMENT_READONLY_FIND);
			findReadOnlyStatement.setString(1, topicName);
			findReadOnlyStatement.setString(2, virtualWiki);
			ResultSet rs = findReadOnlyStatement.executeQuery();
			rs.next();
			if (rs.getInt(1) > 0) {
				rs.close();
				findReadOnlyStatement.close();
				return true;
			}
			rs.close();
			findReadOnlyStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return false;
	}

	/**
	 *
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement getReadOnlyStatement = conn.prepareStatement(STATEMENT_READONLY_ALL);
			ResultSet rs = getReadOnlyStatement.executeQuery();
			while (rs.next()) {
				all.add(rs.getString("topic"));
			}
			rs.close();
			getReadOnlyStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}

	/**
	 *
	 */
	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement addReadOnlyStatement = conn.prepareStatement(STATEMENT_READONLY_INSERT);
			addReadOnlyStatement.setString(1, topicName);
			addReadOnlyStatement.setString(2, virtualWiki);
			addReadOnlyStatement.execute();
			addReadOnlyStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement deleteReadOnlyStatement = conn.prepareStatement(STATEMENT_READONLY_DELETE);
			deleteReadOnlyStatement.setString(1, topicName);
			deleteReadOnlyStatement.setString(2, virtualWiki);
			deleteReadOnlyStatement.execute();
			deleteReadOnlyStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		if (virtualWikiHash == null) {
			loadVirtualWikiHash();
		}
		return virtualWikiHash.keySet();
	}

	/**
	 *
	 */
	public Collection purgeDeletesOracle(String virtualWiki) throws Exception {
		PreparedStatement stmt;
		ResultSet rs;
		Vector names = new Vector();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_ALL_TOPICS);
			stmt.setString(1, virtualWiki);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String contents = OracleClobHelper.getClobValue(rs.getClob(2));
				if (contents.length() == 0 || contents.equals("delete\n") || contents.equals("\n")) {
					names.add(rs.getString(1));
				}
			}
			rs.close();
			stmt.close();
			stmt = conn.prepareStatement(STATEMENT_PURGE_TOPIC);
			Iterator i = names.iterator();
			while (i.hasNext()) {
				String name = (String) i.next();
				stmt.setString(1, virtualWiki);
				stmt.setString(2, name);
				stmt.execute();
			}
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return names;
	}

	/**
	 *
	 */
	public Collection purgeDeletes(String virtualWiki) throws Exception {
		if (DatabaseHandler.isOracle()) {
			return purgeDeletesOracle(virtualWiki);
		}
		Collection all = new ArrayList();
		// get list of stuff to be purged
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_TOPICS_TO_PURGE);
			stmt.setString(1, virtualWiki);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String topicName = rs.getString("name");
				if (!PseudoTopicHandler.getInstance().isPseudoTopic(topicName)) {
					all.add(topicName);
				}
			}
			stmt.close();
			stmt = conn.prepareStatement(STATEMENT_PURGE_DELETES);
			stmt.setString(1, virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}

	/**
	 *
	 */
	public void purgeVersionsOlderThanOracle(String virtualWiki, DBDate date) throws Exception {
		PreparedStatement stmt;
		ResultSet rs;
		Vector names = new Vector();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_ALL_OLDER_TOPICS);
			stmt.setString(1, virtualWiki);
			stmt.setTimestamp(2, date.asTimestamp());
			rs = stmt.executeQuery();
			while (rs.next()) {
				String contents = OracleClobHelper.getClobValue(rs.getClob(2));
				if (contents.length() == 0 || contents.equals("delete\n") || contents.equals("\n")) {
					names.add(rs.getString(1));
				}
			}
			rs.close();
			stmt.close();
			stmt = conn.prepareStatement(STATEMENT_PURGE_TOPIC);
			Iterator i = names.iterator();
			while (i.hasNext()) {
				String name = (String) i.next();
				stmt.setString(1, virtualWiki);
				stmt.setString(2, name);
				stmt.execute();
			}
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception {
		if (DatabaseHandler.isOracle()) {
			purgeVersionsOlderThanOracle(virtualWiki, date);
		}
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_PURGE_VERSIONS);
			stmt.setTimestamp(1, date.asTimestamp());
			stmt.setString(2, virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception {
		List all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_GET_LOCK_LIST);
			stmt.setString(1, virtualWiki);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				TopicLock lock = new TopicLock(
					rs.getString("virtualwiki"),
					rs.getString("topic"),
					new DBDate(rs.getTimestamp("lockat")),
					rs.getString("sessionkey")
				);
				all.add(lock);
			}
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}
}
