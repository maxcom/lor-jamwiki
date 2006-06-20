/**
 *
 */
package org.jmwiki.persistency.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.persistency.TopicVersion;
import org.jmwiki.VersionManager;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiMember;
import org.jmwiki.WikiMembers;
import org.jmwiki.WikiVersion;
import org.jmwiki.persistency.file.FileHandler;
import org.jmwiki.persistency.file.FileNotify;
import org.jmwiki.persistency.file.FileVersionManager;
import org.jmwiki.persistency.file.FileWikiMembers;
import org.jmwiki.utils.Encryption;
import org.jmwiki.utils.Utilities;

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
	protected static final String DEFAULT_PASSWORD = "password";
	protected static final String DEFAULT_AUTHOR_LOGIN = "unknown_author";
	protected static final String DEFAULT_AUTHOR_NAME = "Unknown Author";
	protected static final int TOPIC_TYPE_DEFAULT = 1;
	protected static final int EDIT_TYPE_DEFAULT = 1;

	/**
	 * FIXME: temporary
	 */
	public void cleanup() throws Exception {
		Connection conn = null;
		String sql = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			// FIXME - temporary during testing
			try {
				sql = "DROP TABLE vqw_recent_change";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP TABLE vqw_notification";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP TABLE vqw_topic_version";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP TABLE vqw_topic";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP TABLE vqw_author";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP TABLE vqw_virtual_wiki";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP SEQUENCE vqw_virtual_wiki_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP SEQUENCE vqw_author_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP SEQUENCE vqw_topic_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP SEQUENCE vqw_topic_version_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP SEQUENCE vqw_notification_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
			}
			try {
				sql = "DROP SEQUENCE vqw_recent_change_seq";
				st.executeUpdate(sql);
			} catch (Exception e) {
				logger.warn("Failure while executing " + sql);
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
	public void convertToFile() throws Exception {
		// language does not matter here
		FileHandler fileHandler = new FileHandler();
		DatabaseHandler databaseHandler = new DatabaseHandler();
		DatabaseSearchEngine databaseSearchEngine = DatabaseSearchEngine.getInstance();
		VersionManager fileVersionManager = FileVersionManager.getInstance();
		VersionManager databaseVersionManager = DatabaseVersionManager.getInstance();
		Collection virtualWikis = databaseHandler.getVirtualWikiList();
		for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
			String virtualWiki = (String) virtualWikiIterator.next();
			try {
				fileHandler.addVirtualWiki(virtualWiki);
			} catch (Exception e) {
				logger.error("Unable to convert virtual wiki to file: " + virtualWiki);
			}
			// Versions
			Collection topics = databaseSearchEngine.getAllTopicNames(virtualWiki);
			for (Iterator topicIterator = topics.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				List versions = databaseVersionManager.getAllVersions(virtualWiki, topicName);
				for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
					TopicVersion topicVersion = (TopicVersion) topicVersionIterator.next();
					String contents = topicVersion.getRawContents();
					if (contents == null) contents = "";
					try {
						fileVersionManager.addVersion(
							virtualWiki,
							topicVersion.getTopicName(),
							contents,
							topicVersion.getRevisionDate()
						);
					} catch (Exception e) {
						logger.error("Unable to convert topic version to file: " + topicVersion.getTopicName() + " / " + virtualWiki);
					}
				}
			}
			// Topics
			for (Iterator topicIterator = topics.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				try {
					fileHandler.write(virtualWiki, databaseHandler.read(virtualWiki, topicName), topicName);
				} catch (Exception e) {
					logger.error("Unable to convert topic to file: " + topicName + " / " + virtualWiki);
				}
			}
			// Read-only topics
			Collection readOnlys = databaseHandler.getReadOnlyTopics(virtualWiki);
			for (Iterator readOnlyIterator = readOnlys.iterator(); readOnlyIterator.hasNext();) {
				String topicName = (String) readOnlyIterator.next();
				try {
					fileHandler.addReadOnlyTopic(virtualWiki, topicName);
				} catch (Exception e) {
					logger.error("Unable to convert read-only topic to file: " + topicName + " / " + virtualWiki);
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
					logger.error("Unable to convert wiki member to file: " + wikiMember.getUserName());
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
						logger.error("Unable to convert notify member to file: " + memberName);
					}
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
	 * Run the create tables script
	 * Ignore SQL exceptions as these may just be the result of existing tables getting in the
	 * way of create table calls
	 *
	 * @throws java.lang.Exception
	 */
	protected static void initialize() throws Exception {
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
	 * If the JMWiki version has changed update the database as needed.
	 */
	public void upgrade() throws Exception {
		if (WikiVersion.getCurrentVersion().before(0, 0, 1)) {
			this.initialize();
		}
	}
}
