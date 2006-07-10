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

import java.sql.Timestamp;
import java.sql.Types;
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;

/**
 *
 */
public class MySqlQueryHandler extends AnsiQueryHandler {

	private static Logger logger = Logger.getLogger(MySqlQueryHandler.class.getName());

	private static final String STATEMENT_CREATE_VIRTUAL_WIKI_TABLE =
		"CREATE TABLE jam_virtual_wiki ( "
		+   "virtual_wiki_id INTEGER NOT NULL AUTO_INCREMENT, "
		+   "virtual_wiki_name VARCHAR(100) NOT NULL, "
		+   "create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "CONSTRAINT jam_pk_virtual_wiki PRIMARY KEY (virtual_wiki_id) "
		+ ") ";
	private static final String STATEMENT_CREATE_WIKI_USER_TABLE =
		"CREATE TABLE jam_wiki_user ( "
		+   "wiki_user_id INTEGER NOT NULL AUTO_INCREMENT, "
		+   "login VARCHAR(100) NOT NULL, "
		+   "display_name VARCHAR(100), "
		// FIXME - mysql only allows one column to use CURRENT_TIMESTAMP, but this should default also
		+   "create_date TIMESTAMP NOT NULL DEFAULT 0, "
		+   "last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "create_ip_address VARCHAR(15) NOT NULL, "
		+   "last_login_ip_address VARCHAR(15) NOT NULL, "
		+   "is_admin BOOLEAN NOT NULL DEFAULT FALSE, "
		+   "CONSTRAINT jam_pk_wiki_user PRIMARY KEY (wiki_user_id) "
		+ ") ";
	private static final String STATEMENT_CREATE_WIKI_USER_INFO_TABLE =
		"CREATE TABLE jam_wiki_user_info ( "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "login VARCHAR(100) NOT NULL, "
		+   "email VARCHAR(100), "
		+   "first_name VARCHAR(100), "
		+   "last_name VARCHAR(100), "
		+   "encoded_password VARCHAR(100) NOT NULL, "
		+   "CONSTRAINT jam_pk_wiki_user_info PRIMARY KEY (wiki_user_id), "
		+   "CONSTRAINT jam_fk_wiki_user_info_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id) "
		+ ") ";
	private static final String STATEMENT_CREATE_TOPIC_TABLE =
		"CREATE TABLE jam_topic ( "
		+   "topic_id INTEGER NOT NULL AUTO_INCREMENT, "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "topic_name VARCHAR(200) NOT NULL, "
		+   "topic_locked_by INTEGER, "
		+   "topic_lock_date TIMESTAMP, "
		+   "topic_lock_session_key VARCHAR(100), "
		+   "topic_deleted BOOLEAN DEFAULT FALSE, "
		+   "topic_read_only BOOLEAN DEFAULT FALSE, "
		+   "topic_admin_only BOOLEAN DEFAULT FALSE, "
		+   "topic_content TEXT, "
		+   "topic_type INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_topic PRIMARY KEY (topic_id), "
		+   "CONSTRAINT jam_fk_topic_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki(virtual_wiki_id), "
		+   "CONSTRAINT jam_fk_topic_locked_by FOREIGN KEY (topic_locked_by) REFERENCES jam_wiki_user(wiki_user_id) "
		+ ") ";
	private static final String STATEMENT_CREATE_TOPIC_VERSION_TABLE =
		"CREATE TABLE jam_topic_version ( "
		+   "topic_version_id INTEGER NOT NULL AUTO_INCREMENT, "
		+   "topic_id INTEGER NOT NULL, "
		+   "edit_comment VARCHAR(200), "
		+   "version_content TEXT, "
		+   "wiki_user_id INTEGER, "
		+   "wiki_user_ip_address VARCHAR(15) NOT NULL, "
		+   "edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "edit_type INTEGER NOT NULL, "
		+   "previous_topic_version_id INTEGER, "
		+   "CONSTRAINT jam_pk_topic_version PRIMARY KEY (topic_version_id), "
		+   "CONSTRAINT jam_fk_topic_version_topic FOREIGN KEY (topic_id) REFERENCES jam_topic(topic_id), "
		+   "CONSTRAINT jam_fk_topic_version_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_fk_topic_version_previous FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version(topic_version_id) "
		+ ") ";
	private static final String STATEMENT_CREATE_NOTIFICATION_TABLE =
		"CREATE TABLE jam_notification ( "
		+   "notification_id INTEGER NOT NULL AUTO_INCREMENT, "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "topic_id INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_notification PRIMARY KEY (notification_id), "
		+   "CONSTRAINT jam_fk_notification_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_fk_notification_topic FOREIGN KEY (topic_id) REFERENCES jam_topic(topic_id) "
		+ ") ";
	private static final String STATEMENT_CREATE_RECENT_CHANGE_TABLE =
		"CREATE TABLE jam_recent_change ( "
		+   "recent_change_id INTEGER NOT NULL AUTO_INCREMENT, "
		+   "topic_version_id INTEGER NOT NULL, "
		+   "previous_topic_version_id INTEGER, "
		+   "topic_id INTEGER NOT NULL, "
		+   "topic_name VARCHAR(200) NOT NULL, "
		+   "edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "edit_comment VARCHAR(200), "
		+   "wiki_user_id INTEGER, "
		+   "display_name VARCHAR(200) NOT NULL, "
		+   "edit_type INTEGER NOT NULL, "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "virtual_wiki_name VARCHAR(100) NOT NULL, "
		+   "CONSTRAINT jam_pk_recent_change PRIMARY KEY (recent_change_id), "
		+   "CONSTRAINT jam_fk_recent_change_topic_version FOREIGN KEY (topic_version_id) REFERENCES jam_topic_version(topic_version_id), "
		+   "CONSTRAINT jam_fk_recent_change_previous_topic_version FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version(topic_version_id), "
		+   "CONSTRAINT jam_fk_recent_change_topic FOREIGN KEY (topic_id) REFERENCES jam_topic(topic_id), "
		+   "CONSTRAINT jam_fk_recent_change_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_fk_recent_change_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki(virtual_wiki_id) "
		+ ") ";
	private static final String STATEMENT_INSERT_RECENT_CHANGES =
		"INSERT INTO jam_recent_change ( "
		+   "topic_version_id, topic_id, "
		+   "topic_name, edit_date, wiki_user_id, display_name, "
		+   "edit_type, virtual_wiki_id, virtual_wiki_name, edit_comment, "
		+   "previous_topic_version_id "
		+ ") "
		+ "SELECT "
		+   "jam_topic_version.topic_version_id, jam_topic.topic_id, "
		+   "jam_topic.topic_name, jam_topic_version.edit_date, "
		+   "jam_topic_version.wiki_user_id, "
		+   "IFNULL(jam_wiki_user.login, jam_topic_version.wiki_user_ip_address), "
		+   "jam_topic_version.edit_type, jam_virtual_wiki.virtual_wiki_id, "
		+   "jam_virtual_wiki.virtual_wiki_name, jam_topic_version.edit_comment, "
		+   "jam_topic_version.previous_topic_version_id "
		+ "FROM jam_topic, jam_virtual_wiki, jam_topic_version "
		+ "LEFT OUTER JOIN jam_wiki_user ON ( "
		+    "jam_wiki_user.wiki_user_id = jam_topic_version.wiki_user_id "
		+ ") "
		+ "WHERE jam_topic.topic_id = jam_topic_version.topic_id "
		+ "AND jam_topic.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id "
		+ "AND jam_topic.topic_deleted = FALSE ";
	private static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
		"select LAST_INSERT_ID() as topic_id from jam_topic ";
	private static final String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE =
		"select LAST_INSERT_ID() as topic_version_id from jam_topic_version ";
	private static final String STATEMENT_SELECT_WIKI_USER_SEQUENCE =
		"select LAST_INSERT_ID() as wiki_user_id from jam_wiki_user ";

	/**
	 *
	 */
	protected MySqlQueryHandler() {
	}

	/**
	 *
	 */
	public void createTables() throws Exception {
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_INFO_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_NOTIFICATION_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_TABLE);
	}

	/**
	 *
	 */
	public void insertTopic(Topic topic) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(topic.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(AnsiQueryHandler.STATEMENT_INSERT_TOPIC);
		stmt.setNull(1, Types.INTEGER);
		stmt.setInt(2, virtualWikiId);
		stmt.setString(3, topic.getName());
		stmt.setInt(4, topic.getTopicType());
		if (topic.getLockedBy() != null) {
			stmt.setInt(5, topic.getLockedBy().intValue());
		} else {
			stmt.setNull(5, Types.INTEGER);
		}
		stmt.setTimestamp(6, topic.getLockedDate());
		stmt.setBoolean(7, topic.getReadOnly());
		stmt.setString(8, topic.getTopicContent());
		stmt.setString(9, topic.getLockSessionKey());
		stmt.executeUpdate();
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE);
		topic.setTopicId(rs.getInt("topic_id"));
	}

	/**
	 *
	 */
	public void insertTopicVersion(TopicVersion topicVersion) throws Exception {
		Timestamp editDate = new Timestamp(System.currentTimeMillis());
		if (topicVersion.getEditDate() != null) {
			editDate = topicVersion.getEditDate();
		}
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_TOPIC_VERSION);
		if (topicVersion.getTopicVersionId() < 1) {
			stmt.setNull(1, Types.INTEGER);
		} else {
			stmt.setInt(1, topicVersion.getTopicVersionId());
		}
		stmt.setInt(2, topicVersion.getTopicId());
		stmt.setString(3, topicVersion.getEditComment());
		stmt.setString(4, topicVersion.getVersionContent());
		if (topicVersion.getAuthorId() != null) {
			stmt.setInt(5, topicVersion.getAuthorId().intValue());
		} else {
			stmt.setNull(5, Types.INTEGER);
		}
		stmt.setInt(6, topicVersion.getEditType());
		stmt.setString(7, topicVersion.getAuthorIpAddress());
		stmt.setTimestamp(8, editDate);
		if (topicVersion.getPreviousTopicVersionId() != null) {
			stmt.setInt(9, topicVersion.getPreviousTopicVersionId().intValue());
		} else {
			stmt.setNull(9, Types.INTEGER);
		}
		stmt.executeUpdate();
		if (topicVersion.getTopicVersionId() < 1) {
			WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE);
			topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
		}
	}

	/**
	 *
	 */
	public void insertVirtualWiki(String virtualWikiName) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
		stmt.setNull(1, Types.INTEGER);
		stmt.setString(2, virtualWikiName);
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void insertWikiUser(WikiUser user) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER);
		stmt.setNull(1, Types.INTEGER);
		stmt.setString(2, user.getLogin());
		stmt.setString(3, user.getDisplayName());
		stmt.setTimestamp(4, user.getCreateDate());
		stmt.setTimestamp(5, user.getLastLoginDate());
		stmt.setString(6, user.getCreateIpAddress());
		stmt.setString(7, user.getLastLoginIpAddress());
		stmt.setBoolean(8, user.getAdmin());
		stmt.executeUpdate();
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE);
		user.setUserId(rs.getInt("wiki_user_id"));
		// FIXME - may be in LDAP
		stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER_INFO);
		stmt.setInt(1, user.getUserId());
		stmt.setString(2, user.getLogin());
		stmt.setString(3, user.getEmail());
		stmt.setString(4, user.getFirstName());
		stmt.setString(5, user.getLastName());
		stmt.setString(6, user.getEncodedPassword());
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void reloadRecentChanges() throws Exception {
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DELETE_RECENT_CHANGES);
		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES);
	}
}
