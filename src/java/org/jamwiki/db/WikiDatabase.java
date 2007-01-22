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
 * This class contains general database utility methods that are useful for a
 * variety of JAMWiki database functions, including setup and upgrades.
 */
public class WikiDatabase {

	private static String CONNECTION_VALIDATION_QUERY = null;
	private static String EXISTENCE_VALIDATION_QUERY = null;
	private static final WikiLogger logger = WikiLogger.getLogger(WikiDatabase.class.getName());

	static {
		WikiDatabase.initialize();
	}

	/**
	 *
	 */
	private static Connection getConnection() throws Exception {
		// add a connection to the conn array.  BE SURE TO RELEASE IT!
		Connection conn = DatabaseConnection.getConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	/**
	 *
	 */
	protected static Connection getConnection(Object transactionObject) throws Exception {
		if (transactionObject instanceof Connection) {
			return (Connection)transactionObject;
		}
		return WikiDatabase.getConnection();
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
	protected static void initialize() {
		try {
			WikiDatabase.CONNECTION_VALIDATION_QUERY = WikiDatabase.queryHandler().connectionValidationQuery();
			WikiDatabase.EXISTENCE_VALIDATION_QUERY = WikiDatabase.queryHandler().existenceValidationQuery();
			// initialize connection pool in its own try-catch to avoid an error
			// causing property values not to be saved.
			DatabaseConnection.setPoolInitialized(false);
		} catch (Exception e) {
			logger.severe("Unable to initialize database", e);
		}
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	protected static void purgeData(Connection conn) throws Exception {
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
	protected static QueryHandler queryHandler() throws Exception {
		// FIXME - this is ugly
		if (WikiBase.getDataHandler() instanceof AnsiDataHandler) {
			AnsiDataHandler dataHandler = (AnsiDataHandler)WikiBase.getDataHandler();
			return dataHandler.queryHandler();
		}
		throw new Exception("Unable to determine query handler");
	}

	/**
	 *
	 */
	protected static void releaseConnection(Connection conn, Object transactionObject) throws Exception {
		if (transactionObject instanceof Connection) {
			// transaction objects will be released elsewhere
			return;
		}
		WikiDatabase.releaseConnection(conn);
	}

	/**
	 *
	 */
	private static void releaseConnection(Connection conn) throws Exception {
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
				WikiDatabase.queryHandler().createTables(conn);
			} catch (Exception e) {
				logger.severe("Unable to set up database tables", e);
				// clean up anything that might have been created
				WikiDatabase.queryHandler().dropTables(conn);
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
			WikiDatabase.releaseConnection(conn);
		}
	}

	/**
	 *
	 */
	private static void setupAdminUser(WikiUser user, Connection conn) throws Exception {
		if (user == null) {
			throw new Exception("Admin user not specified");
		}
		if (WikiBase.getDataHandler().lookupWikiUser(user.getUserId(), conn) != null) {
			logger.warning("Admin user already exists");
		}
		WikiUserInfo userInfo = null;
		if (WikiBase.getUserHandler().isWriteable()) {
			userInfo = new WikiUserInfo();
			userInfo.setEncodedPassword(user.getPassword());
			userInfo.setUsername(user.getUsername());
			userInfo.setUserId(user.getUserId());
		}
		WikiBase.getDataHandler().writeWikiUser(user, userInfo, conn);
	}

	/**
	 *
	 */
	public static void setupDefaultDatabase(Properties props) {
		props.setProperty(Environment.PROP_DB_DRIVER, "org.hsqldb.jdbcDriver");
		props.setProperty(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_HSQL);
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
		WikiBase.getDataHandler().writeVirtualWiki(virtualWiki, conn);
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
		WikiBase.getDataHandler().writeTopic(topic, topicVersion, Utilities.parserDocument(topic.getTopicContent(), virtualWiki, topicName), true, conn);
	}

	/**
	 *
	 */
	private static void setupSpecialPages(Locale locale, WikiUser user, Connection conn) throws Exception {
		Collection all = WikiBase.getDataHandler().getVirtualWikiList(conn);
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
