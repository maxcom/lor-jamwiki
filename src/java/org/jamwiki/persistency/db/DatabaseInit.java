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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMember;
import org.jamwiki.WikiMembers;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.file.FileHandler;
import org.jamwiki.persistency.file.FileNotify;
import org.jamwiki.persistency.file.FileWikiMembers;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class DatabaseInit {

	private static final Logger logger = Logger.getLogger(DatabaseInit.class);

	private static final String DATABASE_DB2 = "db2";
	private static final String DATABASE_MYSQL = "mysql";
	private static final String DATABASE_ORACLE = "oracle";
	private static final String DATABASE_POSTGRES = "postgres";
	private static final String INIT_SCRIPT_ANSI = "create_ansi.sql";
	private static final String INIT_SCRIPT_ORACLE = "create_oracle.sql";

	// FIXME - move to more logical location
	public static final String DEFAULT_PASSWORD = "password";
	public static final String DEFAULT_AUTHOR_LOGIN = "unknown_author";
	public static final String DEFAULT_AUTHOR_NAME = "Unknown Author";
	public static final String DEFAULT_AUTHOR_IP_ADDRESS = "0.0.0.0";
	public static final int TOPIC_TYPE_DEFAULT = 1;
	public static final int EDIT_TYPE_DEFAULT = 1;

	/**
	 * FIXME: temporary
	 */
	public static void cleanup() throws Exception {
		Connection conn = null;
		String sql = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			// FIXME - temporary during testing
			try {
				sql = "DROP TABLE jmw_recent_change";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP TABLE jmw_notification";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP TABLE jmw_topic_version";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP TABLE jmw_topic";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP TABLE jmw_author";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP TABLE jmw_virtual_wiki";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP SEQUENCE jmw_virtual_wiki_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP SEQUENCE jmw_author_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP SEQUENCE jmw_topic_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP SEQUENCE jmw_topic_version_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP SEQUENCE jmw_notification_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			try {
				sql = "DROP SEQUENCE jmw_recent_change_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
			}
			st.close();
		} catch (Exception e) {
			if (sql != null) {
				throw new Exception("Failure while executing SQL: " + sql, e);
			}
			throw e;
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public static void convertToFile() throws Exception {
		// language does not matter here
		FileHandler fileHandler = new FileHandler();
		DatabaseHandler databaseHandler = new DatabaseHandler();
		Collection virtualWikis = databaseHandler.getVirtualWikiList();
		for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
			String virtualWiki = (String) virtualWikiIterator.next();
			try {
				fileHandler.addVirtualWiki(virtualWiki);
			} catch (Exception e) {
				logger.error("Unable to convert virtual wiki to file: " + virtualWiki + ": " + e.getMessage());
			}
			// Topics
			Collection topicNames = databaseHandler.getAllTopicNames(virtualWiki);
			for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				try {
					Topic topic = databaseHandler.lookupTopic(virtualWiki, topicName);
					fileHandler.addTopic(topic);
				} catch (Exception e) {
					logger.error("Unable to convert topic to file: " + topicName + " / " + virtualWiki, e);
				}
			}
			// Versions
			for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				List versions = databaseHandler.getAllVersions(virtualWiki, topicName);
				for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
					TopicVersion topicVersion = (TopicVersion) topicVersionIterator.next();
					try {
						fileHandler.addTopicVersion(virtualWiki, topicName, topicVersion);
					} catch (Exception e) {
						logger.error("Unable to convert topic version to file: " + topicName + " / " + virtualWiki + ": " + e.getMessage());
					}
				}
			}
			// Read-only topics
			Collection readOnlys = databaseHandler.getReadOnlyTopics(virtualWiki);
			for (Iterator readOnlyIterator = readOnlys.iterator(); readOnlyIterator.hasNext();) {
				String topicName = (String) readOnlyIterator.next();
				try {
					fileHandler.addReadOnlyTopic(virtualWiki, topicName);
				} catch (Exception e) {
					logger.error("Unable to convert read-only topic to file: " + topicName + " / " + virtualWiki + ": " + e.getMessage());
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
			Collection changes = databaseHandler.getRecentChanges(virtualWiki, 1000);
			for (Iterator changeIterator = changes.iterator(); changeIterator.hasNext();) {
				RecentChange change = (RecentChange)changeIterator.next();
				try {
					fileHandler.addRecentChange(change);
				} catch (Exception e) {
					logger.error("Unable to convert recent change to file: " + virtualWiki + ": " + e.getMessage());
				}
			}
		}
	}

	/**
	 *
	 */
	private static String createScript() {
		String databaseType = DatabaseHandler.getDatabaseType();
		if (databaseType.equals(DATABASE_ORACLE)) {
			return INIT_SCRIPT_ORACLE;
		}
		return INIT_SCRIPT_ANSI;
	}

	/**
	 *
	 */
	// FIXME - temporary
	public static void convert() throws Exception {
		ConvertVQWiki.convertFromDatabase();
		DatabaseHandler.loadVirtualWikiHashes();
	}

	/**
	 * Run the create tables script
	 * Ignore SQL exceptions as these may just be the result of existing tables getting in the
	 * way of create table calls
	 *
	 * @throws java.lang.Exception
	 */
	public static void initialize() throws Exception {
		String script = DatabaseInit.createScript();
		String contents = Utilities.readFile(script);
		StringTokenizer tokens = new StringTokenizer(contents, ";");
		String sql = null;
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			while (tokens.hasMoreTokens()) {
				sql = tokens.nextToken();
				st.executeUpdate(sql);
			}
			st.close();
		} catch (Exception e) {
			if (sql != null) {
				throw new Exception("Failure while executing SQL: " + sql, e);
			}
			throw e;
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 * Optionally purge obsolete data from the database.
	 */
	private void purgeVQWiki278() throws Exception {
		Connection conn = null;
		String sql = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			sql = "DROP TABLE WikiMember";
			st.executeUpdate(sql);
			sql = "DROP TABLE Notification";
			st.executeUpdate(sql);
			sql = "DROP TABLE VirtualWiki";
			st.executeUpdate(sql);
			sql = "DROP TABLE TopicReadOnly";
			st.executeUpdate(sql);
			sql = "DROP TABLE TopicLock";
			st.executeUpdate(sql);
			sql = "DROP TABLE TopicChange";
			st.executeUpdate(sql);
			sql = "DROP TABLE TopicVersion";
			st.executeUpdate(sql);
			sql = "DROP TABLE Topic";
			st.executeUpdate(sql);
			st.close();
		} catch (Exception e) {
			if (sql != null) {
				throw new Exception("Failure while executing SQL: " + sql, e);
			}
			throw e;
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 * If the JAMWiki version has changed update the database as needed.
	 */
	public void upgrade() throws Exception {
		if (WikiVersion.getCurrentVersion().before(0, 0, 1)) {
			this.initialize();
		}
	}
}
