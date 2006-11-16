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
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiDatabase {

	public static final String DB_TYPE_ANSI = "ansi";
	public static final String DB_TYPE_DB2 = "db2";
	public static final String DB_TYPE_DB2_400 = "db2/400";
	public static final String DB_TYPE_HSQL = "hsql";
	public static final String DB_TYPE_MSSQL = "mssql";
	public static final String DB_TYPE_MYSQL = "mysql";
	public static final String DB_TYPE_ORACLE = "oracle";
	public static final String DB_TYPE_POSTGRES = "postgres";
	private static String CONNECTION_VALIDATION_QUERY = null;
	private static String EXISTENCE_VALIDATION_QUERY = null;
	private static final WikiLogger logger = WikiLogger.getLogger(WikiDatabase.class.getName());
	private static QueryHandler queryHandler = null;

	static {
		WikiDatabase.initialize();
	}

	/**
	 *
	 */
	protected static Connection getConnection() throws Exception {
		// add a connection to the conn array.  BE SURE TO RELEASE IT!
		Connection conn = DatabaseConnection.getConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	/**
	 *
	 */
	protected static String getConnectionValidationQuery() {
		return (StringUtils.hasText(CONNECTION_VALIDATION_QUERY)) ? CONNECTION_VALIDATION_QUERY : null;
	}

	/**
	 *
	 */
	protected static String getExistenceValidationQuery() {
		return (StringUtils.hasText(EXISTENCE_VALIDATION_QUERY)) ? EXISTENCE_VALIDATION_QUERY : null;
	}

	/**
	 *
	 */
	protected static QueryHandler getQueryHandler() {
		return WikiDatabase.queryHandler;
	}

	/**
	 *
	 */
	protected static void initialize() {
		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_DB2)) {
			WikiDatabase.queryHandler = new DB2QueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_DB2_400)) {
			WikiDatabase.queryHandler = new DB2400QueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MSSQL)) {
			WikiDatabase.queryHandler = new MSSqlQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_HSQL)) {
			WikiDatabase.queryHandler = new HSQLQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL)) {
			WikiDatabase.queryHandler = new MySqlQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE)) {
			WikiDatabase.queryHandler = new OracleQueryHandler();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_POSTGRES)) {
			WikiDatabase.queryHandler = new PostgresQueryHandler();
		} else {
			WikiDatabase.queryHandler = new DefaultQueryHandler();
		}
		WikiDatabase.CONNECTION_VALIDATION_QUERY = WikiDatabase.queryHandler.connectionValidationQuery();
		WikiDatabase.EXISTENCE_VALIDATION_QUERY = WikiDatabase.queryHandler.existenceValidationQuery();
		// initialize connection pool in its own try-catch to avoid an error
		// causing property values not to be saved.
		DatabaseConnection.setPoolInitialized(false);
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	protected static void purgeData(Connection conn) throws Exception {
		// BOOM!  Everything gone...
		WikiDatabase.queryHandler.dropTables(conn);
		try {
			// re-create empty tables
			WikiDatabase.queryHandler.createTables(conn);
		} catch (Exception e) {
			// creation failure, don't leave tables half-committed
			WikiDatabase.queryHandler.dropTables(conn);
		}
	}

	/**
	 *
	 */
	protected static void releaseParams(Connection conn) throws Exception {
		if (conn == null) return;
		try {
			conn.commit();
		} finally {
			if (conn != null) DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	protected static void setup(Locale locale, WikiUser user) throws Exception {
		Connection conn = null;
		try {
			try {
				conn = WikiDatabase.getConnection();
				// set up tables
				WikiDatabase.queryHandler.createTables(conn);
			} catch (Exception e) {
				logger.severe("Unable to set up database tables", e);
				// clean up anything that might have been created
				WikiDatabase.queryHandler.dropTables(conn);
				throw e;
			}
			try {
				WikiDatabase.setupDefaultVirtualWiki(conn);
				WikiDatabase.setupAdminUser(user, conn);
				WikiDatabase.setupSpecialPages(locale, user, conn);
			} catch (Exception e) {
				DatabaseConnection.handleErrors(conn);
				throw e;
			}
		} finally {
			WikiDatabase.releaseParams(conn);
		}
	}

	/**
	 *
	 */
	private static void setupAdminUser(WikiUser user, Connection conn) throws Exception {
		if (user == null) {
			throw new Exception("Admin user not specified");
		}
		if (WikiBase.getHandler().lookupWikiUser(user.getUserId(), conn) != null) {
			logger.warning("Admin user already exists");
		}
		WikiBase.getHandler().addWikiUser(user, conn);
		if (WikiBase.getUserHandler().canUpdate()) {
			WikiUserInfo userInfo = new WikiUserInfo();
			userInfo.setEncodedPassword(user.getRememberKey());
			userInfo.setLogin(user.getLogin());
			userInfo.setUserId(user.getUserId());
			WikiBase.getUserHandler().addWikiUserInfo(userInfo);
		}
	}

	/**
	 *
	 */
	public static void setupDefaultDatabase(Properties props) {
		props.setProperty(Environment.PROP_BASE_PERSISTENCE_TYPE, "INTERNAL");
		props.setProperty(Environment.PROP_DB_DRIVER, "org.hsqldb.jdbcDriver");
		props.setProperty(Environment.PROP_DB_TYPE, WikiDatabase.DB_TYPE_HSQL);
		props.setProperty(Environment.PROP_DB_USERNAME, "sa");
		props.setProperty(Environment.PROP_DB_PASSWORD, "");
		File file = new File(props.getProperty(Environment.PROP_BASE_FILE_DIR), "database");
		if (!file.exists()) {
			file.mkdirs();
		}
		String url = "jdbc:hsqldb:file:" + new File(file.getPath(), "jamwiki").getPath();
		props.setProperty(Environment.PROP_DB_URL, url);
	}

	/**
	 *
	 */
	private static void setupDefaultVirtualWiki(Connection conn) throws Exception {
		VirtualWiki virtualWiki = new VirtualWiki();
		virtualWiki.setName(WikiBase.DEFAULT_VWIKI);
		virtualWiki.setDefaultTopicName(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC));
		WikiBase.getHandler().writeVirtualWiki(virtualWiki, conn);
	}

	/**
	 *
	 */
	protected static void setupSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, boolean adminOnly, Connection conn) throws Exception {
		logger.info("Setting up special page " + virtualWiki + " / " + topicName);
		String contents = Utilities.readSpecialPage(locale, topicName);
		Topic topic = new Topic();
		topic.setName(topicName);
		topic.setVirtualWiki(virtualWiki);
		topic.setTopicContent(contents);
		topic.setAdminOnly(adminOnly);
		// FIXME - hard coding
		TopicVersion topicVersion = new TopicVersion(user, user.getLastLoginIpAddress(), "Automatically created by system setup", contents);
		WikiBase.getHandler().writeTopic(topic, topicVersion, Utilities.parserDocument(topic.getTopicContent(), virtualWiki, topicName), conn, true);
	}

	/**
	 *
	 */
	private static void setupSpecialPages(Locale locale, WikiUser user, Connection conn) throws Exception {
		Collection all = WikiBase.getHandler().getVirtualWikiList();
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
			// create the default topics
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false, conn);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true, conn);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true, conn);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true, conn);
		}
	}
}
