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
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class AnsiQueryHandler implements QueryHandler {

	private static Logger logger = Logger.getLogger(AnsiQueryHandler.class.getName());

	protected static final String STATEMENT_CREATE_VIRTUAL_WIKI_SEQUENCE =
		"CREATE SEQUENCE jam_virtual_wiki_seq ";
	protected static final String STATEMENT_CREATE_VIRTUAL_WIKI_TABLE =
		"CREATE TABLE jam_virtual_wiki ( "
		+   "virtual_wiki_id INTEGER NOT NULL DEFAULT NEXTVAL('jam_virtual_wiki_seq'), "
		+   "virtual_wiki_name VARCHAR(100) NOT NULL, "
		+   "create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "CONSTRAINT jam_pk_virtual_wiki PRIMARY KEY (virtual_wiki_id) "
		+ ") ";
	protected static final String STATEMENT_CREATE_WIKI_USER_SEQUENCE =
		"CREATE SEQUENCE jam_wiki_user_seq ";
	protected static final String STATEMENT_CREATE_WIKI_USER_TABLE =
		"CREATE TABLE jam_wiki_user ( "
		+   "wiki_user_id INTEGER NOT NULL DEFAULT NEXTVAL('jam_wiki_user_seq'), "
		+   "login VARCHAR(100) NOT NULL, "
		+   "display_name VARCHAR(100), "
		+   "create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "create_ip_address VARCHAR(15) NOT NULL, "
		+   "last_login_ip_address VARCHAR(15) NOT NULL, "
		+   "is_admin CHAR NOT NULL DEFAULT '0', "
		+   "CONSTRAINT jam_pk_wiki_user PRIMARY KEY (wiki_user_id) "
		+ ") ";
	protected static final String STATEMENT_CREATE_WIKI_USER_INFO_TABLE =
		"CREATE TABLE jam_wiki_user_info ( "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "login VARCHAR(100) NOT NULL, "
		+   "email VARCHAR(100), "
		+   "first_name VARCHAR(100), "
		+   "last_name VARCHAR(100), "
		+   "encoded_password VARCHAR(100) NOT NULL, "
		+   "CONSTRAINT jam_pk_wiki_user_info PRIMARY KEY (wiki_user_id), "
		+   "CONSTRAINT jam_fk_wiki_user_info_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user "
		+ ") ";
	protected static final String STATEMENT_CREATE_TOPIC_SEQUENCE =
		"CREATE SEQUENCE jam_topic_seq ";
	protected static final String STATEMENT_CREATE_TOPIC_TABLE =
		"CREATE TABLE jam_topic ( "
		+   "topic_id INTEGER NOT NULL DEFAULT NEXTVAL('jam_topic_seq'), "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "topic_name VARCHAR(200) NOT NULL, "
		+   "topic_locked_by INTEGER, "
		+   "topic_lock_date TIMESTAMP, "
		+   "topic_lock_session_key VARCHAR(100), "
		+   "topic_deleted CHAR NOT NULL DEFAULT '0', "
		+   "topic_read_only CHAR NOT NULL DEFAULT '0', "
		+   "topic_admin_only CHAR NOT NULL DEFAULT '0', "
		+   "topic_content TEXT, "
		+   "topic_type INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_topic PRIMARY KEY (topic_id), "
		+   "CONSTRAINT jam_fk_topic_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki, "
		+   "CONSTRAINT jam_fk_topic_locked_by FOREIGN KEY (topic_locked_by) REFERENCES jam_wiki_user "
		+ ") ";
	protected static final String STATEMENT_CREATE_TOPIC_VERSION_SEQUENCE =
		"CREATE SEQUENCE jam_topic_version_seq ";
	protected static final String STATEMENT_CREATE_TOPIC_VERSION_TABLE =
		"CREATE TABLE jam_topic_version ( "
		+   "topic_version_id INTEGER NOT NULL DEFAULT NEXTVAL('jam_topic_version_seq'), "
		+   "topic_id INTEGER NOT NULL, "
		+   "edit_comment VARCHAR(200), "
		+   "version_content TEXT, "
		+   "wiki_user_id INTEGER, "
		+   "wiki_user_ip_address VARCHAR(15) NOT NULL, "
		+   "edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "edit_type INTEGER NOT NULL, "
		+   "previous_topic_version_id INTEGER, "
		+   "CONSTRAINT jam_pk_topic_version PRIMARY KEY (topic_version_id), "
		+   "CONSTRAINT jam_fk_topic_version_topic FOREIGN KEY (topic_id) REFERENCES jam_topic, "
		+   "CONSTRAINT jam_fk_topic_version_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user, "
		+   "CONSTRAINT jam_fk_topic_version_previous FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version "
		+ ") ";
	protected static final String STATEMENT_CREATE_NOTIFICATION_SEQUENCE =
		"CREATE SEQUENCE jam_notification_seq ";
	protected static final String STATEMENT_CREATE_NOTIFICATION_TABLE =
		"CREATE TABLE jam_notification ( "
		+   "notification_id INTEGER NOT NULL DEFAULT NEXTVAL('jam_notification_seq'), "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "topic_id INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_notification PRIMARY KEY (notification_id), "
		+   "CONSTRAINT jam_fk_notification_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user, "
		+   "CONSTRAINT jam_fk_notification_topic FOREIGN KEY (topic_id) REFERENCES jam_topic "
		+ ") ";
	protected static final String STATEMENT_CREATE_RECENT_CHANGE_SEQUENCE =
		"CREATE SEQUENCE jam_recent_change_seq ";
	protected static final String STATEMENT_CREATE_RECENT_CHANGE_TABLE =
		"CREATE TABLE jam_recent_change ( "
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
		+   "CONSTRAINT jam_pk_recent_change PRIMARY KEY (topic_version_id), "
		+   "CONSTRAINT jam_fk_recent_change_topic_version FOREIGN KEY (topic_version_id) REFERENCES jam_topic_version, "
		+   "CONSTRAINT jam_fk_recent_change_previous_topic_version FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version, "
		+   "CONSTRAINT jam_fk_recent_change_topic FOREIGN KEY (topic_id) REFERENCES jam_topic, "
		+   "CONSTRAINT jam_fk_recent_change_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user, "
		+   "CONSTRAINT jam_fk_recent_change_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki "
		+ ") ";
	protected static final String STATEMENT_DELETE_RECENT_CHANGES =
	    "delete from jam_recent_change ";
	protected static final String STATEMENT_INSERT_TOPIC =
		"insert into jam_topic ( "
		+   "topic_id, virtual_wiki_id, topic_name, topic_type, "
		+   "topic_locked_by, topic_lock_date, topic_read_only, topic_content, "
		+   "topic_lock_session_key "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ?, ?"
		+ ") ";
	protected static final String STATEMENT_INSERT_TOPIC_VERSION =
		"insert into jam_topic_version ("
		+   "topic_version_id, topic_id, edit_comment, version_content, "
		+   "wiki_user_id, edit_type, wiki_user_ip_address, edit_date, "
		+   "previous_topic_version_id "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ?, ? "
		+ ") ";
	protected static final String STATEMENT_INSERT_RECENT_CHANGE =
		"insert into jam_recent_change ("
		+   "topic_version_id, previous_topic_version_id, topic_id, "
		+   "topic_name, edit_date, edit_comment, wiki_user_id, "
		+   "display_name, edit_type, virtual_wiki_id, virtual_wiki_name "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
		+ ") ";
	protected static final String STATEMENT_INSERT_RECENT_CHANGES =
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
		// FIXME - need the equivalent of "coalesce(login, ip_address)"
		+   "jam_topic_version.wiki_user_ip_address, "
		+   "jam_topic_version.edit_type, jam_virtual_wiki.virtual_wiki_id, "
		+   "jam_virtual_wiki.virtual_wiki_name, jam_topic_version.edit_comment, "
		+   "jam_topic_version.previous_topic_version_id "
		+ "FROM jam_topic, jam_virtual_wiki, jam_topic_version "
		+ "LEFT OUTER JOIN jam_wiki_user ON ( "
		+    "jam_wiki_user.wiki_user_id = jam_topic_version.wiki_user_id "
		+ ") "
		+ "WHERE jam_topic.topic_id = jam_topic_version.topic_id "
		+ "AND jam_topic.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id "
		+ "AND jam_topic.topic_deleted = '0' ";
	protected static final String STATEMENT_INSERT_VIRTUAL_WIKI =
		"insert into jam_virtual_wiki ("
		+   "virtual_wiki_id, virtual_wiki_name "
		+ ") values ( "
		+   "?, ? "
		+ ") ";
	protected static final String STATEMENT_INSERT_WIKI_USER =
		"insert into jam_wiki_user ("
		+   "wiki_user_id, login, display_name, create_date, "
		+   "last_login_date, create_ip_address, last_login_ip_address, "
		+   "is_admin "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ? "
		+ ") ";
	protected static final String STATEMENT_INSERT_WIKI_USER_INFO =
		"insert into jam_wiki_user_info ("
		+   "wiki_user_id, login, email, first_name, last_name, encoded_password "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ? "
		+ ") ";
	protected static final String STATEMENT_SELECT_RECENT_CHANGES =
		"select * from jam_recent_change "
		+ "where virtual_wiki_name = ? "
		+ "order by edit_date desc "
		+ "limit ? ";
	protected static final String STATEMENT_SELECT_TOPIC =
		"select * from jam_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_name = ? ";
	protected static final String STATEMENT_SELECT_TOPICS =
		"select * from jam_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_deleted = '0' ";
	protected static final String STATEMENT_SELECT_TOPIC_READ_ONLY =
		"select * from jam_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_read_only = ? "
		+ "and topic_deleted = '0' ";
	protected static final String STATEMENT_SELECT_TOPIC_LOCKED =
		"select * from jam_topic "
		+ "where virtual_wiki_id = ? "
		+ "and topic_lock_session_key is not null "
		+ "and topic_deleted = '0' ";
	protected static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
		"select nextval('jam_topic_seq') as topic_id ";
	protected static final String STATEMENT_SELECT_TOPIC_VERSION =
		"select * from jam_topic_version "
		+ "where topic_version_id = ? ";
	protected static final String STATEMENT_SELECT_TOPIC_VERSIONS =
		"select * from jam_topic_version "
		+ "where topic_id = ? "
		+ "order by topic_version_id desc ";
	protected static final String STATEMENT_SELECT_TOPIC_VERSION_LAST =
		"select max(topic_version_id) as topic_version_id from jam_topic_version "
		+ "where topic_id = ? ";
	protected static final String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE =
		"select nextval('jam_topic_version_seq') as topic_version_id ";
	protected static final String STATEMENT_SELECT_VIRTUAL_WIKIS =
		"select * from jam_virtual_wiki ";
	protected static final String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE =
		"select nextval('jam_virtual_wiki_seq') as virtual_wiki_id ";
	protected static final String STATEMENT_SELECT_WIKI_USER_SEQUENCE =
		"select nextval('jam_wiki_user_seq') as wiki_user_id ";
	protected static final String STATEMENT_SELECT_WIKI_USER =
	    "select jam_wiki_user.wiki_user_id, jam_wiki_user.login, "
	    +   "jam_wiki_user.display_name, jam_wiki_user.create_date, "
	    +   "jam_wiki_user.last_login_date, jam_wiki_user.create_ip_address, "
	    +   "jam_wiki_user.last_login_ip_address, jam_wiki_user.is_admin, "
	    +   "jam_wiki_user_info.email, jam_wiki_user_info.first_name, "
	    +   "jam_wiki_user_info.last_name, jam_wiki_user_info.encoded_password "
	    + "from jam_wiki_user "
	    + "left outer join jam_wiki_user_info "
	    + "on (jam_wiki_user.wiki_user_id = jam_wiki_user_info.wiki_user_id) "
	    + "where jam_wiki_user.wiki_user_id = ? ";
	protected static final String STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS =
		"select "
		+   "jam_topic_version.topic_version_id, jam_topic_version.topic_id, "
		+   "jam_topic_version.previous_topic_version_id, jam_topic.topic_name, "
		+   "jam_topic_version.edit_date, jam_topic_version.edit_comment, "
		+   "jam_topic_version.wiki_user_id, jam_topic_version.edit_type, "
		+   "jam_topic_version.wiki_user_ip_address as display_name, "
		+   "jam_topic.virtual_wiki_id, jam_virtual_wiki.virtual_wiki_name "
		+ "from jam_topic, jam_virtual_wiki, jam_topic_version "
		+ "where jam_virtual_wiki.virtual_wiki_id = jam_topic.virtual_wiki_id "
		+ "and jam_topic.topic_id = jam_topic_version.topic_id "
		+ "and jam_virtual_wiki.virtual_wiki_name = ? "
		+ "and jam_topic_version.wiki_user_ip_address = ? "
		+ "and jam_topic_version.wiki_user_id is null "
		+ "and jam_topic.topic_deleted = '0' "
		+ "order by edit_date desc "
		+ "limit ? ";
	protected static final String STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN =
		"select "
		+   "jam_topic_version.topic_version_id, jam_topic_version.topic_id, "
		+   "jam_topic_version.previous_topic_version_id, jam_topic.topic_name, "
		+   "jam_topic_version.edit_date, jam_topic_version.edit_comment, "
		+   "jam_topic_version.wiki_user_id, jam_topic_version.edit_type, "
		+   "jam_wiki_user.login as display_name, jam_topic.virtual_wiki_id, "
		+   "jam_virtual_wiki.virtual_wiki_name "
		+ "from jam_topic, jam_virtual_wiki, jam_topic_version, jam_wiki_user "
		+ "where jam_virtual_wiki.virtual_wiki_id = jam_topic.virtual_wiki_id "
		+ "and jam_wiki_user.wiki_user_id = jam_topic_version.wiki_user_id "
		+ "and jam_topic.topic_id = jam_topic_version.topic_id "
		+ "and jam_virtual_wiki.virtual_wiki_name = ? "
		+ "and jam_wiki_user.login = ? "
		+ "and jam_topic.topic_deleted = '0' "
		+ "order by edit_date desc "
		+ "limit ? ";
	protected static final String STATEMENT_SELECT_WIKI_USER_PASSWORD =
	    "select wiki_user_id from jam_wiki_user_info "
	    + "where login = ? "
	    + "and encoded_password = ? ";
	protected static final String STATEMENT_SELECT_WIKI_USER_LOGIN =
	    "select wiki_user_id from jam_wiki_user_info "
	    + "where login = ? ";
	protected static final String STATEMENT_SELECT_WIKI_USER_LOGINS =
	    "select login from jam_wiki_user_info ";
	protected static final String STATEMENT_UPDATE_TOPIC =
		"update jam_topic set "
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
	protected static final String STATEMENT_UPDATE_WIKI_USER =
		"update jam_wiki_user set "
		+ "login = ?, "
		+ "display_name = ?, "
		+ "last_login_date = ?, "
		+ "last_login_ip_address = ?, "
		+ "is_admin = ? "
		+ "where wiki_user_id = ? ";
	protected static final String STATEMENT_UPDATE_WIKI_USER_INFO =
		"update jam_wiki_user_info set "
		+ "login = ?, "
		+ "email = ?, "
		+ "first_name = ?, "
		+ "last_name = ?, "
		+ "encoded_password = ? "
		+ "where wiki_user_id = ? ";

	/**
	 *
	 */
	protected AnsiQueryHandler() {
	}

	/**
	 *
	 */
	public void createTables() throws Exception {
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_SEQUENCE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_SEQUENCE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_INFO_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_SEQUENCE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_SEQUENCE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_NOTIFICATION_SEQUENCE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_NOTIFICATION_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_SEQUENCE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_TABLE);
	}

	/**
	 *
	 */
	public WikiResultSet getAllTopicNames(String virtualWiki) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPICS);
		stmt.setInt(1, virtualWikiId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getAllTopicVersions(Topic topic) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSIONS);
		stmt.setInt(1, topic.getTopicId());
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getAllWikiUserLogins() throws Exception {
		return DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_LOGINS);
	}

	/**
	 *
	 */
	public WikiResultSet getLockList(String virtualWiki) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_LOCKED);
		stmt.setInt(1, virtualWikiId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getReadOnlyTopics(String virtualWiki) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_READ_ONLY);
		stmt.setInt(1, virtualWikiId);
		stmt.setChar(2, '1');
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, int num) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_RECENT_CHANGES);
		stmt.setString(1, virtualWiki);
		stmt.setInt(2, num);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributions(String virtualWiki, String userString, int num) throws Exception {
		WikiPreparedStatement stmt = null;
		if (Utilities.isIpAddress(userString)) {
			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
		} else {
			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
		}
		stmt.setString(1, virtualWiki);
		stmt.setString(2, userString);
		stmt.setInt(3, num);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getVirtualWikis() throws Exception {
		return DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKIS);
	}

	/**
	 *
	 */
	public void insertRecentChange(RecentChange change) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(change.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_RECENT_CHANGE);
		stmt.setInt(1, change.getTopicVersionId());
		if (change.getPreviousTopicVersionId() != null) {
			stmt.setInt(2, change.getPreviousTopicVersionId().intValue());
		} else {
			stmt.setNull(2, Types.INTEGER);
		}
		stmt.setInt(3, change.getTopicId());
		stmt.setString(4, change.getTopicName());
		stmt.setTimestamp(5, change.getEditDate());
		stmt.setString(6, change.getEditComment());
		if (change.getAuthorId() != null) {
			stmt.setInt(7, change.getAuthorId().intValue());
		} else {
			stmt.setNull(7, Types.INTEGER);
		}
		stmt.setString(8, change.getAuthorName());
		stmt.setInt(9, change.getEditType());
		stmt.setInt(10, virtualWikiId);
		stmt.setString(11, change.getVirtualWiki());
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void insertTopic(Topic topic) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(topic.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_TOPIC);
		stmt.setInt(1, topic.getTopicId());
		stmt.setInt(2, virtualWikiId);
		stmt.setString(3, topic.getName());
		stmt.setInt(4, topic.getTopicType());
		if (topic.getLockedBy() != null) {
			stmt.setInt(5, topic.getLockedBy().intValue());
		} else {
			stmt.setNull(5, Types.INTEGER);
		}
		stmt.setTimestamp(6, topic.getLockedDate());
		stmt.setChar(7, (topic.getReadOnly() ? '1' : '0'));
		stmt.setString(8, topic.getTopicContent());
		stmt.setString(9, topic.getLockSessionKey());
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void insertTopicVersion(TopicVersion topicVersion) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_TOPIC_VERSION);
		stmt.setInt(1, topicVersion.getTopicVersionId());
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
		stmt.setTimestamp(8, topicVersion.getEditDate());
		if (topicVersion.getPreviousTopicVersionId() != null) {
			stmt.setInt(9, topicVersion.getPreviousTopicVersionId().intValue());
		} else {
			stmt.setNull(9, Types.INTEGER);
		}
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void insertVirtualWiki(int virtualWikiId, String virtualWikiName) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
		stmt.setInt(1, virtualWikiId);
		stmt.setString(2, virtualWikiName);
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void insertWikiUser(WikiUser user) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER);
		stmt.setInt(1, user.getUserId());
		stmt.setString(2, user.getLogin());
		stmt.setString(3, user.getDisplayName());
		stmt.setTimestamp(4, user.getCreateDate());
		stmt.setTimestamp(5, user.getLastLoginDate());
		stmt.setString(6, user.getCreateIpAddress());
		stmt.setString(7, user.getLastLoginIpAddress());
		stmt.setChar(8, (user.getAdmin() ? '1' : '0'));
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void insertWikiUserInfo(WikiUser user) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER_INFO);
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
	public WikiResultSet lookupLastTopicVersion(Topic topic) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION_LAST);
		stmt.setInt(1, topic.getTopicId());
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupTopic(String virtualWiki, String topicName) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC);
		stmt.setInt(1, virtualWikiId);
		stmt.setString(2, topicName);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION);
		stmt.setInt(1, topicVersionId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupWikiUser(int userId) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER);
		stmt.setInt(1, userId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupWikiUser(String login) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_LOGIN);
		stmt.setString(1, login);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupWikiUser(String login, String password) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_PASSWORD);
		stmt.setString(1, login);
		stmt.setString(2, Encryption.encrypt(password));
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public int nextTopicId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE);
		return rs.getInt("topic_id");
	}

	/**
	 *
	 */
	public int nextTopicVersionId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE);
		return rs.getInt("topic_version_id");
	}

	/**
	 *
	 */
	public int nextVirtualWikiId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE);
		return rs.getInt("virtual_wiki_id");
	}

	/**
	 *
	 */
	public int nextWikiUserId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE);
		return rs.getInt("wiki_user_id");
	}

	/**
	 *
	 */
	public void reloadRecentChanges() throws Exception {
		DatabaseConnection.executeUpdate(STATEMENT_DELETE_RECENT_CHANGES);
		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES);
	}

	/**
	 *
	 */
	public void updateTopic(Topic topic) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(topic.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_TOPIC);
		stmt.setInt(1, virtualWikiId);
		stmt.setString(2, topic.getName());
		stmt.setInt(3, topic.getTopicType());
		if (topic.getLockedBy() != null) {
			stmt.setInt(4, topic.getLockedBy().intValue());
		} else {
			stmt.setNull(4, Types.INTEGER);
		}
		stmt.setTimestamp(5, topic.getLockedDate());
		stmt.setChar(6, (topic.getReadOnly() ? '1' : '0'));
		stmt.setString(7, topic.getTopicContent());
		stmt.setString(8, topic.getLockSessionKey());
		stmt.setChar(9, (topic.getDeleted() ? '1' : '0'));
		stmt.setInt(10, topic.getTopicId());
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void updateWikiUser(WikiUser user) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_USER);
		stmt.setString(1, user.getLogin());
		stmt.setString(2, user.getDisplayName());
		stmt.setTimestamp(3, user.getLastLoginDate());
		stmt.setString(4, user.getLastLoginIpAddress());
		stmt.setChar(5, (user.getAdmin() ? '1' : '0'));
		stmt.setInt(6, user.getUserId());
		stmt.executeUpdate();
	}

	/**
	 *
	 */
	public void updateWikiUserInfo(WikiUser user) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_USER_INFO);
		stmt.setString(1, user.getLogin());
		stmt.setString(2, user.getEmail());
		stmt.setString(3, user.getFirstName());
		stmt.setString(4, user.getLastName());
		stmt.setString(5, user.getEncodedPassword());
		stmt.setInt(6, user.getUserId());
		stmt.executeUpdate();
	}
}
