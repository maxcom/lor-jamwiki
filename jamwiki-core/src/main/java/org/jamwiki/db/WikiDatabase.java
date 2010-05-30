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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.DataHandler;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.transaction.TransactionStatus;

/**
 * This class contains general database utility methods that are useful for a
 * variety of JAMWiki database functions, including setup and upgrades.
 */
public class WikiDatabase {

	private static String CONNECTION_VALIDATION_QUERY = null;
	private static String EXISTENCE_VALIDATION_QUERY = null;
	private static final WikiLogger logger = WikiLogger.getLogger(WikiDatabase.class.getName());
	/** Root directory within the WAR distribution that contains the default topic pages. */
	public static final String SPECIAL_PAGE_DIR = "pages";
	private static final String[][] JAMWIKI_DB_TABLE_INFO = {
		{"jam_virtual_wiki", "virtual_wiki_id"},
		{"jam_users", null},
		{"jam_wiki_user", "wiki_user_id"},
		{"jam_topic", "topic_id"},
		{"jam_topic_version", "topic_version_id"},
		{"jam_file", "file_id"},
		{"jam_file_version", "file_version_id"},
		{"jam_category", null},
		{"jam_group", "group_id"},
		{"jam_group_members", "id"},
		{"jam_role", null},
		{"jam_authorities", null},
		{"jam_group_authorities", null},
		{"jam_recent_change", "topic_version_id"},
		{"jam_watchlist", null}
	};

	/**
	 *
	 */
	private WikiDatabase() {
	}

	/**
	 *
	 */
	private static DataHandler findNewDataHandler(Properties props) {
		// find the DataHandler appropriate to the NEW database
		String handlerClassName = props.getProperty(Environment.PROP_DB_TYPE);
		if (handlerClassName.equals(Environment.getValue(Environment.PROP_DB_TYPE))) {
			// use existing DataHandler
			return WikiBase.getDataHandler();
		}
		logger.fine("Using NEW data handler: " + handlerClassName);
		return (DataHandler)Utilities.instantiateClass(handlerClassName);
	}

	/**
	 * Utility method for validating that all topics are currently pointing to the correct
	 * namespace ID.  This method is required for updating data when upgrading to JAMWiki 0.9.0,
	 * and is also available for use to resolve data issues after creating or updating
	 * namespace names.
	 */
	public static int fixIncorrectTopicNamespaces() throws DataAccessException {
		int count = 0;
		Map<Integer, String> topicNames;
		List<Topic> topics;
		List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			for (VirtualWiki virtualWiki : virtualWikis) {
				topicNames = WikiDatabase.queryHandler().lookupTopicNames(virtualWiki.getVirtualWikiId(), conn);
				if (topicNames.isEmpty()) {
					continue;
				}
				topics = new ArrayList<Topic>();
				for (int topicId : topicNames.keySet()) {
					Topic topic = new Topic(virtualWiki.getName(), topicNames.get(topicId));
					topic.setTopicId(topicId);
					topics.add(topic);
				}
				WikiDatabase.queryHandler().updateTopicNamespaces(topics, conn);
				count += topicNames.size();
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return count;
	}

	/**
	 * Migrate from the current database to a new database.
	 * Tables are created in the new database, and then the contents
	 * of the existing database are transferred across.
	 *
	 * @param props Properties object containing the new database properties
	 * @param errors List to add error messages to
	 */
	public static void migrateDatabase(Properties props, List<WikiMessage> errors) throws DataAccessException {
		// verify that new database is different from the old database
		if (StringUtils.equalsIgnoreCase(Environment.getValue(Environment.PROP_DB_URL), props.getProperty(Environment.PROP_DB_URL))) {
			errors.add(new WikiMessage("error.databaseconnection", "Cannot migrate to the same database"));
			return;
		}
		// find the DataHandler appropriate to the NEW database
		DataHandler newDataHandler = WikiDatabase.findNewDataHandler(props);
		// the QueryHandler appropriate for the NEW database
		QueryHandler newQueryHandler = null;
		// FIXME - this is ugly
		if (newDataHandler instanceof AnsiDataHandler) {
			AnsiDataHandler dataHandler = (AnsiDataHandler)newDataHandler;
			newQueryHandler = dataHandler.queryHandler();
			logger.fine("Using NEW query handler: " + newQueryHandler.getClass().getName());
		} else {
			newQueryHandler = queryHandler();
		}
		Connection conn = null;
		Connection from = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// create the tables in the NEW database
			conn = WikiDatabase.initializeNewDatabase(props, errors, newQueryHandler);
			if (conn == null) {
				return;
			}
			// since this is a new database setting autocommit to true is ok.  in addition,
			// since a potentially huge amount of data might be getting committed it prevents
			// locking issues when loading the database.
			conn.setAutoCommit(true);
			// copy the existing table content from the CURRENT database across to the NEW database
			from = DatabaseConnection.getConnection();
			from.setReadOnly(true);
			from.setAutoCommit(true);
			// used to track current_version_id for each jam_topic row inserted
			Map<Integer, Integer> topicVersions = new HashMap<Integer, Integer>();
			for (int i = 0; i < JAMWIKI_DB_TABLE_INFO.length; i++) {
				// these 3 variables are for special handling of the jam_topic.current_version_id field
				// which cannot be loaded on initial insert due to the jam_f_topic_topicv constraint
				boolean isTopicTable = "jam_topic".equals(JAMWIKI_DB_TABLE_INFO[i][0]);
				int topicIdColumn = 0;
				int currentVersionColumn = 0;
				int maxIndex = WikiDatabase.retrieveMaximumTableId(JAMWIKI_DB_TABLE_INFO[i][0], JAMWIKI_DB_TABLE_INFO[i][1]);
				StringBuilder insert;
				ResultSetMetaData md;
				StringBuilder values;
				StringBuilder select;
				String columnName;
				Integer topicId;
				Integer currentVersionId;
				Object o;
				// cycle through at most RECORDS_PER_CYCLE records at a time to avoid blowing up the system
				int RECORDS_PER_CYCLE = 25;
				for (int j = 0; j <= maxIndex; j += RECORDS_PER_CYCLE) {
					select = new StringBuilder("SELECT * FROM ").append(JAMWIKI_DB_TABLE_INFO[i][0]);
					if (!StringUtils.isBlank(JAMWIKI_DB_TABLE_INFO[i][1])) {
						select.append(" WHERE ").append(JAMWIKI_DB_TABLE_INFO[i][1]).append(" > ").append(j);
						select.append(" AND ").append(JAMWIKI_DB_TABLE_INFO[i][1]).append(" <= ").append(j + RECORDS_PER_CYCLE);
						select.append(" ORDER BY ").append(JAMWIKI_DB_TABLE_INFO[i][1]);
					}
					insert = new StringBuilder();
					stmt = from.createStatement();
					logger.info(select.toString());
					rs = stmt.executeQuery(select.toString());
					md = rs.getMetaData();
					insert.append("INSERT INTO ").append(JAMWIKI_DB_TABLE_INFO[i][0]).append('(');
					values = new StringBuilder();
					for (int k = 1; k <= md.getColumnCount(); k++) {
						if (k > 1) {
							insert.append(',');
							values.append(',');
						}
						columnName = md.getColumnLabel(k);
						if (isTopicTable) {
							if ("topic_id".equalsIgnoreCase(columnName)) {
								topicIdColumn = k;
							} else if ("current_version_id".equalsIgnoreCase(columnName)) {
								currentVersionColumn = k;
							}
						}
						// special handling for Sybase ASA, which requires the "login" column name to be quoted
						if (newQueryHandler instanceof org.jamwiki.db.SybaseASAQueryHandler && "login".equalsIgnoreCase(columnName)) {
							columnName = "\"" + columnName + "\"";
						}
						insert.append(columnName);
						values.append('?');
					}
					insert.append(") VALUES (").append(values).append(')');
					logger.info(insert.toString());
					PreparedStatement insertStmt = conn.prepareStatement(insert.toString());
					while (rs.next()) {
						topicId = null;
						currentVersionId = null;
						for (int k = 1; k <= md.getColumnCount(); k++) {
							o = rs.getObject(k);
							if (isTopicTable) {
								if (k == topicIdColumn) {
									topicId = (Integer)o;
								} else if (k == currentVersionColumn) {
									currentVersionId = (Integer)o;
								}
							}
							if (rs.wasNull() || (isTopicTable && k == currentVersionColumn)) {
								insertStmt.setNull(k, md.getColumnType(k));
							} else {
								insertStmt.setObject(k, rs.getObject(k));
							}
						}
						insertStmt.executeUpdate();
						if (topicId != null && currentVersionId != null) {
							// store current topic version for later update.  since topic id is the
							// map key, any older (obsolete) topic version IDs will be overwritten
							// as later records are processed.
							topicVersions.put(topicId, currentVersionId);
						}
					}
					rs.close();
					DatabaseConnection.closeStatement(stmt);
					DatabaseConnection.closeStatement(insertStmt);
				}
			}
			// update the jam_topic.current_version_id field that we had to leave blank on initial insert
			String updateSql = "UPDATE jam_topic SET current_version_id = ? WHERE topic_id = ?";
			logger.info(updateSql);
			PreparedStatement update = conn.prepareStatement(updateSql);
			for (Integer topicId : topicVersions.keySet()) {
				Integer topicVersionId = topicVersions.get(topicId);
				update.setObject(1, topicVersionId);
				update.setObject(2, topicId);
				update.executeUpdate();
			}
		} catch (Exception e) {
			logger.severe("Error attempting to migrate the database", e);
			errors.add(new WikiMessage("error.unknown", e.getMessage()));
			try {
				newQueryHandler.dropTables(conn);
			} catch (Exception ex) {
				logger.warning("Unable to drop tables in NEW database following failed migration", ex);
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
			if (from != null) {
				DatabaseConnection.closeConnection(from, stmt, rs);
			}
		}
	}

	/**
	 *
	 */
	protected static String getConnectionValidationQuery() {
		return (!StringUtils.isBlank(CONNECTION_VALIDATION_QUERY)) ? CONNECTION_VALIDATION_QUERY : null;
	}

	/**
	 *
	 */
	protected static String getExistenceValidationQuery() {
		return (!StringUtils.isBlank(EXISTENCE_VALIDATION_QUERY)) ? EXISTENCE_VALIDATION_QUERY : null;
	}

	/**
	 *
	 */
	public synchronized static void initialize() {
		try {
			WikiDatabase.CONNECTION_VALIDATION_QUERY = WikiDatabase.queryHandler().connectionValidationQuery();
			WikiDatabase.EXISTENCE_VALIDATION_QUERY = WikiDatabase.queryHandler().existenceValidationQuery();
			// initialize connection pool in its own try-catch to avoid an error
			// causing property values not to be saved.
			// this clears out any existing connection pool, so that a new one will be created on first access
			DatabaseConnection.closeConnectionPool();
		} catch (Exception e) {
			logger.severe("Unable to initialize database", e);
		}
	}

	/**
	 *
	 */
	private static Connection initializeNewDatabase(Properties props, List<WikiMessage> errors, QueryHandler newQueryHandler) {
		String driver = props.getProperty(Environment.PROP_DB_DRIVER);
		String url = props.getProperty(Environment.PROP_DB_URL);
		String userName = props.getProperty(Environment.PROP_DB_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
		Connection conn = null;
		try {
			// test to see if we can connect to the new database
			conn = DatabaseConnection.getTestConnection(driver, url, userName, password);
			conn.setAutoCommit(true);
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
			return null;
		}
		// test to see if JAMWiki tables already exist (if they do, we can't continue this migration process
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(newQueryHandler.existenceValidationQuery());
			errors.add(new WikiMessage("setup.error.migrate"));
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
			return null;
		} catch (Exception ex) {
			// we expect this exception as the JAMWiki tables don't exist
			logger.fine("NEW Database does not contain any JAMWiki instance");
		} finally {
			DatabaseConnection.closeStatement(stmt);
		}
		try {
			newQueryHandler.createTables(conn);
		} catch (Exception e) {
			logger.severe("Error attempting to migrate the database", e);
			errors.add(new WikiMessage("error.unknown", e.getMessage()));
			try {
				newQueryHandler.dropTables(conn);
			} catch (Exception ex) {
				logger.warning("Unable to drop tables in NEW database following failed migration", ex);
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
		}
		return conn;
	}

	public synchronized static void shutdown() {
		try {
			DatabaseConnection.closeConnectionPool();
		} catch (Exception e) {
			logger.severe("Unable to close the connection pool on shutdown", e);
		}
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	protected static void purgeData(Connection conn) throws DataAccessException {
		// BOOM!  Everything gone...
		WikiDatabase.queryHandler().dropTables(conn);
		try {
			// re-create empty tables
			WikiDatabase.queryHandler().createTables(conn);
		} catch (Exception e) {
			// creation failure, don't leave tables half-committed
			WikiDatabase.queryHandler().dropTables(conn);
		}
	}

	/**
	 *
	 */
	protected static QueryHandler queryHandler() throws DataAccessException {
		// FIXME - this is ugly
		if (WikiBase.getDataHandler() instanceof AnsiDataHandler) {
			AnsiDataHandler dataHandler = (AnsiDataHandler)WikiBase.getDataHandler();
			return dataHandler.queryHandler();
		}
		throw new DataAccessException("Unable to determine query handler");
	}

	/**
	 * Utility method for reading special topic values from files and returning
	 * the file contents.
	 *
	 * @param locale The locale for the user viewing the special page.
	 * @param pageName The name of the special page being retrieved.
	 */
	protected static String readSpecialPage(Locale locale, String pageName) throws IOException {
		String contents = null;
		String filename = null;
		String language = null;
		String country = null;
		if (locale != null) {
			language = locale.getLanguage();
			country = locale.getCountry();
		}
		String subdirectory = "";
		if (!StringUtils.isBlank(language) && !StringUtils.isBlank(country)) {
			try {
				subdirectory = WikiDatabase.SPECIAL_PAGE_DIR + File.separator + language + "_" + country;
				filename = subdirectory + File.separator + WikiUtil.encodeForFilename(pageName) + ".txt";
				contents = Utilities.readFile(filename);
			} catch (IOException e) {
				logger.info("No locale-specific file is available for " + filename + ", checking for a language-specific version.");
			}
		}
		if (contents == null && !StringUtils.isBlank(language)) {
			try {
				subdirectory = WikiDatabase.SPECIAL_PAGE_DIR + File.separator + language;
				filename = subdirectory + File.separator + WikiUtil.encodeForFilename(pageName) + ".txt";
				contents = Utilities.readFile(filename);
			} catch (IOException e) {
				logger.info("No language-specific file is available for " + filename + " so the default will be used.");
			}
		}
		if (contents == null) {
			try {
				subdirectory = WikiDatabase.SPECIAL_PAGE_DIR;
				filename = subdirectory + File.separator + WikiUtil.encodeForFilename(pageName) + ".txt";
				contents = Utilities.readFile(filename);
			} catch (IOException e) {
				logger.warning("Default topic initialization file " + filename + " could not be read", e);
				throw e;
			}
		}
		return contents;
	}

	/**
	 *
	 */
	protected static void releaseConnection(Connection conn, Object transactionObject) throws SQLException {
		if (transactionObject instanceof Connection) {
			// transaction objects will be released elsewhere
			return;
		}
		WikiDatabase.releaseConnection(conn);
	}

	/**
	 *
	 */
	private static void releaseConnection(Connection conn) throws SQLException {
		if (conn == null) {
			return;
		}
		try {
			conn.commit();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 * Return the largest primary key ID for the specified table, or 1 if the table does
	 * not have a numeric primary key value.
	 */
	private static int retrieveMaximumTableId(String tableName, String primaryIdColumnName) throws SQLException {
		if (StringUtils.isBlank(tableName) || StringUtils.isBlank(primaryIdColumnName)) {
			return 1;
		}
		String sql = "select max(" + primaryIdColumnName + ") as max_table_id from " + tableName;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			return (rs.next()) ? rs.getInt("max_table_id") : 0;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected static void setup(Locale locale, WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction();
			Connection conn = DatabaseConnection.getConnection();
			// set up tables
			WikiDatabase.queryHandler().createTables(conn);
			WikiDatabase.setupDefaultVirtualWiki();
			WikiDatabase.setupDefaultNamespaces();
			WikiDatabase.setupRoles();
			WikiDatabase.setupGroups();
			WikiDatabase.setupAdminUser(user, username, encryptedPassword);
			WikiDatabase.setupSpecialPages(locale, user);
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.severe("Unable to set up database tables", e);
			// clean up anything that might have been created
			try {
				Connection conn = DatabaseConnection.getConnection();
				WikiDatabase.queryHandler().dropTables(conn);
			} catch (Exception e2) {}
			throw new DataAccessException(e);
		} catch (DataAccessException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.severe("Unable to set up database tables", e);
			// clean up anything that might have been created
			try {
				Connection conn = DatabaseConnection.getConnection();
				WikiDatabase.queryHandler().dropTables(conn);
			} catch (Exception e2) {}
			throw e;
		} catch (WikiException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.severe("Unable to set up database tables", e);
			// clean up anything that might have been created
			try {
				Connection conn = DatabaseConnection.getConnection();
				WikiDatabase.queryHandler().dropTables(conn);
			} catch (Exception e2) {}
			throw e;
		}
		DatabaseConnection.commit(status);
	}

	/**
	 *
	 */
	private static void setupAdminUser(WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
		logger.info("Creating wiki admin user");
		if (user == null) {
			throw new IllegalArgumentException("Cannot pass null or anonymous WikiUser object to setupAdminUser");
		}
		if (WikiBase.getDataHandler().lookupWikiUser(user.getUserId()) != null) {
			logger.warning("Admin user already exists");
		}
		WikiBase.getDataHandler().writeWikiUser(user, username, encryptedPassword);
		List<String> roles = new ArrayList<String>();
		roles.add(Role.ROLE_ADMIN.getAuthority());
		roles.add(Role.ROLE_IMPORT.getAuthority());
		roles.add(Role.ROLE_SYSADMIN.getAuthority());
		roles.add(Role.ROLE_TRANSLATE.getAuthority());
		WikiBase.getDataHandler().writeRoleMapUser(user.getUsername(), roles);
	}

	/**
	 *
	 */
	public static void setupDefaultDatabase(Properties props) {
		props.setProperty(Environment.PROP_DB_DRIVER, "org.hsqldb.jdbcDriver");
		props.setProperty(Environment.PROP_DB_TYPE, DataHandler.DATA_HANDLER_HSQL);
		props.setProperty(Environment.PROP_DB_USERNAME, "sa");
		props.setProperty(Environment.PROP_DB_PASSWORD, "");
		File file = new File(props.getProperty(Environment.PROP_BASE_FILE_DIR), "database");
		if (!file.exists()) {
			file.mkdirs();
		}
		String url = "jdbc:hsqldb:file:" + new File(file.getPath(), "jamwiki").getPath() + ";shutdown=true";
		props.setProperty(Environment.PROP_DB_URL, url);
	}

	/**
	 *
	 */
	// FIXME - make this private once the ability to upgrade to 0.9.0 is removed
	protected static void setupDefaultNamespaces() throws DataAccessException, WikiException {
		logger.info("Creating default wiki namespaces");
		List<Namespace> defaultNamespaces = new ArrayList<Namespace>(Namespace.DEFAULT_NAMESPACES.values());
		Namespace commentsNamespace, mainNamespace;
		// namespaces are ordered with main first, then comments, so loop through and get each
		for (int i = 0; i < defaultNamespaces.size(); i++) {
			mainNamespace = defaultNamespaces.get(i);
			// some namespaces do not have a comments namespace, so verify one is present
			commentsNamespace = null;
			if (defaultNamespaces.size() > (i + 1)) {
				commentsNamespace = defaultNamespaces.get(i + 1);
				if (mainNamespace.equals(commentsNamespace.getMainNamespace())) {
					i++;
				} else {
					commentsNamespace = null;
				}
			}
			WikiBase.getDataHandler().writeNamespace(mainNamespace, commentsNamespace);
		}
	}

	/**
	 *
	 */
	private static void setupDefaultVirtualWiki() throws DataAccessException, WikiException {
		logger.info("Creating default virtual wiki");
		VirtualWiki virtualWiki = new VirtualWiki();
		virtualWiki.setName(Environment.getValue(Environment.PROP_VIRTUAL_WIKI_DEFAULT));
		virtualWiki.setDefaultTopicName(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC));
		WikiBase.getDataHandler().writeVirtualWiki(virtualWiki);
	}

	/**
	 *
	 */
	protected static void setupGroups() throws DataAccessException, WikiException {
		logger.info("Creating default wiki groups");
		WikiGroup group = new WikiGroup();
		group.setName(WikiGroup.GROUP_ANONYMOUS);
		// FIXME - use message key
		group.setDescription("All non-logged in users are automatically assigned to the anonymous group.");
		WikiBase.getDataHandler().writeWikiGroup(group);
		List<String> anonymousRoles = new ArrayList<String>();
		anonymousRoles.add(Role.ROLE_EDIT_EXISTING.getAuthority());
		anonymousRoles.add(Role.ROLE_EDIT_NEW.getAuthority());
		anonymousRoles.add(Role.ROLE_UPLOAD.getAuthority());
		anonymousRoles.add(Role.ROLE_VIEW.getAuthority());
		WikiBase.getDataHandler().writeRoleMapGroup(group.getGroupId(), anonymousRoles);
		group = new WikiGroup();
		group.setName(WikiGroup.GROUP_REGISTERED_USER);
		// FIXME - use message key
		group.setDescription("All logged in users are automatically assigned to the registered user group.");
		WikiBase.getDataHandler().writeWikiGroup(group);
		List<String> userRoles = new ArrayList<String>();
		userRoles.add(Role.ROLE_EDIT_EXISTING.getAuthority());
		userRoles.add(Role.ROLE_EDIT_NEW.getAuthority());
		userRoles.add(Role.ROLE_MOVE.getAuthority());
		userRoles.add(Role.ROLE_UPLOAD.getAuthority());
		userRoles.add(Role.ROLE_VIEW.getAuthority());
		WikiBase.getDataHandler().writeRoleMapGroup(group.getGroupId(), userRoles);
	}

	/**
	 *
	 */
	protected static void setupRoles() throws DataAccessException, WikiException {
		logger.info("Creating default wiki roles");
		Role role = Role.ROLE_ADMIN;
		// FIXME - use message key
		role.setDescription("Provides the ability to perform wiki maintenance tasks not available to normal users.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_EDIT_EXISTING;
		// FIXME - use message key
		role.setDescription("Allows a user to edit an existing topic.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_EDIT_NEW;
		// FIXME - use message key
		role.setDescription("Allows a user to create a new topic.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_IMPORT;
		// FIXME - use message key
		role.setDescription("Allows a user to import data from a file.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_MOVE;
		// FIXME - use message key
		role.setDescription("Allows a user to move a topic to a different name.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_SYSADMIN;
		// FIXME - use message key
		role.setDescription("Allows access to set database parameters, modify parser settings, and set other wiki system settings.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_TRANSLATE;
		// FIXME - use message key
		role.setDescription("Allows access to the translation tool used for modifying the values of message keys used to display text on the wiki.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_UPLOAD;
		// FIXME - use message key
		role.setDescription("Allows a user to upload a file to the wiki.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_VIEW;
		// FIXME - use message key
		role.setDescription("Allows a user to view topics on the wiki.");
		WikiBase.getDataHandler().writeRole(role, false);
	}

	/**
	 *
	 */
	protected static void setupSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, boolean adminOnly) throws DataAccessException, WikiException {
		logger.info("Setting up special page " + virtualWiki + " / " + topicName);
		if (user == null) {
			throw new IllegalArgumentException("Cannot pass null WikiUser object to setupSpecialPage");
		}
		String contents = null;
		try {
			contents = WikiDatabase.readSpecialPage(locale, topicName);
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
		Topic topic = new Topic(virtualWiki, topicName);
		topic.setTopicContent(contents);
		topic.setAdminOnly(adminOnly);
		int charactersChanged = StringUtils.length(contents);
		// FIXME - hard coding
		TopicVersion topicVersion = new TopicVersion(user, user.getLastLoginIpAddress(), "Automatically created by system setup", contents, charactersChanged);
		// FIXME - it is not connection-safe to parse for metadata since we are already holding a connection
		// ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), virtualWiki, topicName);
		// WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
		WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null);
	}

	/**
	 *
	 */
	private static void setupSpecialPages(Locale locale, WikiUser user) throws DataAccessException, WikiException {
		List<VirtualWiki> all = WikiBase.getDataHandler().getVirtualWikiList();
		for (VirtualWiki virtualWiki : all) {
			// create the default topics
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true);
		}
	}
}
