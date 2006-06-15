/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.vqwiki.persistency.db;

import org.apache.log4j.Logger;
import org.vqwiki.Environment;
import org.vqwiki.persistency.PersistencyHandler;
import org.vqwiki.TopicLock;
import org.vqwiki.VersionManager;
import org.vqwiki.WikiBase;
import org.vqwiki.WikiException;
import org.vqwiki.PseudoTopicHandler;
import org.vqwiki.utils.Utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

public class DatabaseHandler implements PersistencyHandler {

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
	protected final static String STATEMENT_EXISTS =
		"SELECT COUNT(*) FROM Topic WHERE name = ? AND virtualwiki = ?";
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
	protected static final String STATEMENT_GET_ALL_VIRTUAL_WIKIS =
		"SELECT name FROM VirtualWiki";
	protected static final String STATEMENT_GET_TEMPLATE_NAMES =
		"SELECT name FROM WikiTemplate WHERE virtualwiki = ?";
	protected static final String STATEMENT_GET_TEMPLATE =
		"SELECT contents FROM WikiTemplate WHERE virtualwiki = ? AND name = ?";
	protected static final String STATEMENT_ADD_VIRTUAL_WIKI =
		"INSERT INTO VirtualWiki VALUES(?)";
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
	protected final static String STATEMENT_ADD_TEMPLATE =
		"INSERT INTO WikiTemplate( virtualwiki, name, contents ) VALUES( ?, ?, ? )";
	protected final static String STATEMENT_ADD_TEMPLATE_ORACLE =
		"INSERT INTO WikiTemplate( virtualwiki, name, contents ) VALUES( ?, ?, EMPTY_CLOB() )";
	protected final static String STATEMENT_TEMPLATE_EXISTS =
		"SELECT COUNT(*) FROM WikiTemplate WHERE virtualwiki = ? AND name = ?";
	protected final static String STATEMENT_UPDATE_TEMPLATE =
		"UPDATE WikiTemplate SET contents = ? WHERE virtualwiki = ? AND name = ?";
	protected final static String STATEMENT_UPDATE_TEMPLATE_ORACLE1 =
		"UPDATE WikiTemplate SET contents = EMPTY_CLOB() WHERE virtualwiki = ? AND name = ?";
	protected final static String STATEMENT_UPDATE_TEMPLATE_ORACLE2 =
		"SELECT contents FROM WikiTemplate WHERE virtualwiki = ? AND name = ? FOR UPDATE";
	protected final static String STATEMENT_GET_LOCK_LIST =
		"SELECT * FROM TopicLock WHERE virtualwiki = ?";

	public static final String DB_TYPE_ORACLE = "oracle";
	public static final String DB_TYPE_MYSQL = "mysql";
	private static final Logger logger = Logger.getLogger(DatabaseHandler.class);

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
	public void setDefaults(Locale locale) throws Exception {
		logger.debug("Setting defaults");
		// resources for i18n
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
		//set up tables
		createTables();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_GET_ALL_VIRTUAL_WIKIS);
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				stmt.close();
				stmt = conn.prepareStatement(STATEMENT_ADD_VIRTUAL_WIKI);
				stmt.setString(1, WikiBase.DEFAULT_VWIKI);
				stmt.execute();
			}
			stmt.close();
			Statement st = conn.createStatement();
			rs = st.executeQuery(STATEMENT_GET_ALL_VIRTUAL_WIKIS);
			while (rs.next()) {
				String vWiki = rs.getString("name");
				// starting points
				setupSpecialPage(vWiki, messages.getString("specialpages.startingpoints"));
				// text formatting rules
				setupSpecialPage(vWiki, messages.getString("specialpages.textformattingrules"));
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
					write(vWiki, "", false, "SetUsername");
				}
			}
			rs.close();
			st.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	private void setupSpecialPage(String vWiki, String specialPage) throws Exception {
		if (!exists(vWiki, specialPage)) {
			logger.debug("Setting up " + specialPage);
			write(vWiki, WikiBase.readDefaultTopic(specialPage), true, specialPage);
		}
	}

	/**
	 * Run the create tables script
	 * Ignore SQL exceptions as these may just be the result of existing tables getting in the
	 * way of create table calls
	 *
	 * @throws java.lang.Exception
	 */
	private void createTables() throws Exception {
		String databaseType = DatabaseHandler.getDatabaseType();
		StringBuffer buffer = new StringBuffer();
		buffer.append("/create_");
		buffer.append(databaseType);
		buffer.append(".sql");
		String resourceName = buffer.toString();
		InputStream createScriptStream = getClass().getResourceAsStream(resourceName);
		if (createScriptStream == null) {
			logger.error("Can't find create script: " + resourceName);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(createScriptStream));
		buffer = new StringBuffer();
		while (true) {
			String line = in.readLine();
			if (line == null) {
				break;
			}
			buffer.append(line);
		}
		in.close();
		StringTokenizer tokens = new StringTokenizer(buffer.toString(), ";");
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				try {
					st.executeUpdate(token);
				} catch (SQLException e) {
					logger.warn(e);
				}
			}
			st.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
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
	public void write(String virtualWiki, String contents, boolean convertTabs, String topicName) throws Exception {
		if (convertTabs) {
			contents = Utilities.convertTabs(contents);
		}
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
		Connection conn = null;
		boolean result = false;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement existsStatement = conn.prepareStatement(STATEMENT_EXISTS);
			existsStatement.setString(1, topicName);
			existsStatement.setString(2, virtualWiki);
			ResultSet rs = existsStatement.executeQuery();
			rs.next();
			int count = rs.getInt(1);
			result = (count == 0) ? false : true;
			rs.close();
			existsStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return result;
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
	public void executeSQL(String sql) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			st.execute(sql);
			st.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public void initialise(Locale locale) throws Exception {
		this.setDefaults(locale);
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		Connection conn = null;
		Collection all = new ArrayList();
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(STATEMENT_GET_ALL_VIRTUAL_WIKIS);
			while (rs.next()) {
				all.add(rs.getString("name"));
			}
			rs.close();
			st.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		if (!all.contains(WikiBase.DEFAULT_VWIKI)) {
			all.add(WikiBase.DEFAULT_VWIKI);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getTemplateNames(String virtualWiki) throws Exception {
		logger.debug("Returning template names for " + virtualWiki);
		Collection all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_GET_TEMPLATE_NAMES);
			stmt.setString(1, virtualWiki);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				all.add(rs.getString("name"));
			}
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		logger.debug(all.size() + " templates exist");
		return all;
	}

	/**
	 *
	 */
	public String getTemplate(String virtualWiki, String templateName) throws Exception {
		Connection conn = null;
		String contents;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_GET_TEMPLATE);
			stmt.setString(1, virtualWiki);
			stmt.setString(2, templateName);
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				rs.close();
				stmt.close();
				throw new WikiException("Template not found: " + templateName);
			}
			if (DatabaseHandler.isOracle()) {
				contents = OracleClobHelper.getClobValue(rs.getClob("contents"));
			} else {
				contents = rs.getString("contents");
			}
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return contents;
	}

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception {
		if (getVirtualWikiList().contains(virtualWiki)) {
			return;
		}
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_ADD_VIRTUAL_WIKI);
			stmt.setString(1, virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
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
	public void saveAsTemplate(String virtualWiki, String templateName, String contents) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(STATEMENT_TEMPLATE_EXISTS);
			stmt.setString(1, virtualWiki);
			stmt.setString(2, templateName);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int count = rs.getInt(1);
			stmt.close();
			if (count < 1) {
				if (DatabaseHandler.isOracle()) {
					boolean savedAutoCommit = conn.getAutoCommit();
					conn.setAutoCommit(false);
					stmt = conn.prepareStatement(STATEMENT_ADD_TEMPLATE_ORACLE);
					stmt.setString(1, virtualWiki);
					stmt.setString(2, templateName);
					stmt.execute();
					stmt.close();
					conn.commit();
					stmt = conn.prepareStatement(
						STATEMENT_UPDATE_TEMPLATE_ORACLE2,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE
					);
					stmt.setString(1, virtualWiki);
					stmt.setString(2, templateName);
					rs = stmt.executeQuery();
					rs.next();
					OracleClobHelper.setClobValue(rs.getClob(1), contents);
					rs.close();
					stmt.close();
					conn.commit();
					conn.setAutoCommit(savedAutoCommit);
				} else {
					stmt = conn.prepareStatement(STATEMENT_ADD_TEMPLATE);
					stmt.setString(1, virtualWiki);
					stmt.setString(2, templateName);
					stmt.setString(3, contents);
					stmt.execute();
					stmt.close();
				}
			} else {
				if (DatabaseHandler.isOracle()) {
					boolean savedAutoCommit = conn.getAutoCommit();
					conn.setAutoCommit(false);
					stmt = conn.prepareStatement(STATEMENT_UPDATE_TEMPLATE_ORACLE1);
					stmt.setString(1, virtualWiki);
					stmt.setString(2, templateName);
					stmt.execute();
					stmt.close();
					conn.commit();
					stmt = conn.prepareStatement(
						STATEMENT_UPDATE_TEMPLATE_ORACLE2,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE
					);
					stmt.setString(1, virtualWiki);
					stmt.setString(2, templateName);
					rs = stmt.executeQuery();
					rs.next();
					OracleClobHelper.setClobValue(rs.getClob(1), contents);
					rs.close();
					stmt.close();
					conn.commit();
					conn.setAutoCommit(savedAutoCommit);
				} else {
					stmt = conn.prepareStatement(STATEMENT_UPDATE_TEMPLATE);
					stmt.setString(1, contents);
					stmt.setString(2, virtualWiki);
					stmt.setString(3, templateName);
					stmt.execute();
					stmt.close();
				}
			}
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
