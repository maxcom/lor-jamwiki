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

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class DefaultQueryHandler implements QueryHandler {

	private static Logger logger = Logger.getLogger(DefaultQueryHandler.class.getName());

	protected static final String STATEMENT_CREATE_VIRTUAL_WIKI_TABLE =
		"CREATE TABLE jam_virtual_wiki ( "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "virtual_wiki_name VARCHAR(100) NOT NULL, "
		+   "default_topic_name VARCHAR(200) NOT NULL, "
		+   "create_date TIMESTAMP DEFAULT " + now() + " NOT NULL, "
		+   "CONSTRAINT jam_pk_vwiki PRIMARY KEY (virtual_wiki_id), "
		+   "CONSTRAINT jam_unique_vwiki_name UNIQUE (virtual_wiki_name) "
		+ ") ";
	protected static final String STATEMENT_CREATE_WIKI_USER_TABLE =
		"CREATE TABLE jam_wiki_user ( "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "login VARCHAR(100) NOT NULL, "
		+   "display_name VARCHAR(100), "
		+   "create_date TIMESTAMP DEFAULT " + now() + " NOT NULL, "
		+   "last_login_date TIMESTAMP DEFAULT " + now() + " NOT NULL, "
		+   "create_ip_address VARCHAR(15) NOT NULL, "
		+   "last_login_ip_address VARCHAR(15) NOT NULL, "
		+   "is_admin INTEGER DEFAULT 0 NOT NULL, "
		+   "CONSTRAINT jam_pk_wiki_user PRIMARY KEY (wiki_user_id), "
		+   "CONSTRAINT jam_unique_wiki_user_login UNIQUE (login) "
		+ ") ";
	protected static final String STATEMENT_CREATE_WIKI_USER_INFO_TABLE =
		"CREATE TABLE jam_wiki_user_info ( "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "login VARCHAR(100) NOT NULL, "
		+   "email VARCHAR(100), "
		+   "first_name VARCHAR(100), "
		+   "last_name VARCHAR(100), "
		+   "encoded_password VARCHAR(100) NOT NULL, "
		+   "CONSTRAINT jam_pk_wiki_uinfo PRIMARY KEY (wiki_user_id), "
		+   "CONSTRAINT jam_fk_wiki_uinfo_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_unique_wiki_uinfo_login UNIQUE (login) "
		+ ") ";
	protected static final String STATEMENT_CREATE_TOPIC_TABLE =
		"CREATE TABLE jam_topic ( "
		+   "topic_id INTEGER NOT NULL, "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "topic_name VARCHAR(200) NOT NULL, "
		+   "topic_deleted INTEGER DEFAULT 0 NOT NULL, "
		+   "topic_read_only INTEGER DEFAULT 0 NOT NULL, "
		+   "topic_admin_only INTEGER DEFAULT 0 NOT NULL, "
		+   "topic_content " + text() + ", "
		+   "topic_type INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_topic PRIMARY KEY (topic_id), "
		+   "CONSTRAINT jam_fk_topic_vwiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki(virtual_wiki_id), "
		+   "CONSTRAINT jam_unique_topic_name_vwiki UNIQUE (topic_name, virtual_wiki_id) "
		+ ") ";
	protected static final String STATEMENT_CREATE_TOPIC_VERSION_TABLE =
		"CREATE TABLE jam_topic_version ( "
		+   "topic_version_id INTEGER NOT NULL, "
		+   "topic_id INTEGER NOT NULL, "
		+   "edit_comment VARCHAR(200), "
		+   "version_content " + text() + ", "
		+   "wiki_user_id INTEGER, "
		+   "wiki_user_ip_address VARCHAR(15) NOT NULL, "
		+   "edit_date TIMESTAMP DEFAULT " + now() + " NOT NULL, "
		+   "edit_type INTEGER NOT NULL, "
		+   "previous_topic_version_id INTEGER, "
		+   "CONSTRAINT jam_pk_topic_ver PRIMARY KEY (topic_version_id), "
		+   "CONSTRAINT jam_fk_topic_ver_topic FOREIGN KEY (topic_id) REFERENCES jam_topic(topic_id), "
		+   "CONSTRAINT jam_fk_topic_ver_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_fk_topic_ver_prv_topic_ver FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version(topic_version_id) "
		+ ") ";
	protected static final String STATEMENT_CREATE_WIKI_FILE_TABLE =
		"CREATE TABLE jam_file ( "
		+   "file_id INTEGER NOT NULL, "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "file_name VARCHAR(200) NOT NULL, "
		+   "file_deleted INTEGER DEFAULT 0 NOT NULL, "
		+   "file_read_only INTEGER DEFAULT 0 NOT NULL, "
		+   "file_admin_only INTEGER DEFAULT 0 NOT NULL, "
		+   "file_url VARCHAR(200) NOT NULL, "
		+   "mime_type VARCHAR(100) NOT NULL, "
		+   "topic_id INTEGER NOT NULL, "
		+   "file_size INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_file PRIMARY KEY (file_id), "
		+   "CONSTRAINT jam_fk_file_vwiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki(virtual_wiki_id), "
		+   "CONSTRAINT jam_fk_file_topic FOREIGN KEY (topic_id) REFERENCES jam_topic(topic_id), "
		+   "CONSTRAINT jam_unique_file_url UNIQUE (file_url), "
		+   "CONSTRAINT jam_unique_file_topic_vwiki UNIQUE (virtual_wiki_id, topic_id) "
		+ ") ";
	protected static final String STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE =
		"CREATE TABLE jam_file_version ( "
		+   "file_version_id INTEGER NOT NULL, "
		+   "file_id INTEGER NOT NULL, "
		+   "upload_comment VARCHAR(200), "
		+   "file_url VARCHAR(200) NOT NULL, "
		+   "wiki_user_id INTEGER, "
		+   "wiki_user_ip_address VARCHAR(15) NOT NULL, "
		+   "upload_date TIMESTAMP DEFAULT " + now() + " NOT NULL, "
		+   "mime_type VARCHAR(100) NOT NULL, "
		+   "file_size INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_file_ver PRIMARY KEY (file_version_id), "
		+   "CONSTRAINT jam_fk_file_ver_file FOREIGN KEY (file_id) REFERENCES jam_file(file_id), "
		+   "CONSTRAINT jam_fk_file_ver_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_unique_file_ver_url UNIQUE (file_url) "
		+ ") ";
	protected static final String STATEMENT_CREATE_IMAGE_TABLE =
		"CREATE TABLE jam_image ( "
		+   "file_version_id INTEGER NOT NULL, "
		+   "width INTEGER NOT NULL, "
		+   "height INTEGER NOT NULL, "
		+   "CONSTRAINT jam_pk_image PRIMARY KEY (file_version_id), "
		+   "CONSTRAINT jam_fk_image_file_ver FOREIGN KEY (file_version_id) REFERENCES jam_file_version(file_version_id) "
		+ ") ";
	protected static final String STATEMENT_CREATE_RECENT_CHANGE_TABLE =
		"CREATE TABLE jam_recent_change ( "
		+   "topic_version_id INTEGER NOT NULL, "
		+   "previous_topic_version_id INTEGER, "
		+   "topic_id INTEGER NOT NULL, "
		+   "topic_name VARCHAR(200) NOT NULL, "
		+   "edit_date TIMESTAMP DEFAULT " + now() + " NOT NULL, "
		+   "edit_comment VARCHAR(200), "
		+   "wiki_user_id INTEGER, "
		+   "display_name VARCHAR(200) NOT NULL, "
		+   "edit_type INTEGER NOT NULL, "
		+   "virtual_wiki_id INTEGER NOT NULL, "
		+   "virtual_wiki_name VARCHAR(100) NOT NULL, "
		+   "CONSTRAINT jam_pk_rchange PRIMARY KEY (topic_version_id), "
		+   "CONSTRAINT jam_fk_rchange_topic_ver FOREIGN KEY (topic_version_id) REFERENCES jam_topic_version(topic_version_id), "
		+   "CONSTRAINT jam_fk_rchange_prv_topic_ver FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version(topic_version_id), "
		+   "CONSTRAINT jam_fk_rchange_topic FOREIGN KEY (topic_id) REFERENCES jam_topic(topic_id), "
		+   "CONSTRAINT jam_fk_rchange_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user(wiki_user_id), "
		+   "CONSTRAINT jam_fk_rchange_vwiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki(virtual_wiki_id) "
		+ ") ";
	protected static final String STATEMENT_DELETE_RECENT_CHANGES =
	    "delete from jam_recent_change ";
	protected static final String STATEMENT_DROP_VIRTUAL_WIKI_TABLE =
		"DROP TABLE jam_virtual_wiki ";
	protected static final String STATEMENT_DROP_WIKI_USER_TABLE =
		"DROP TABLE jam_wiki_user ";
	protected static final String STATEMENT_DROP_WIKI_USER_INFO_TABLE =
		"DROP TABLE jam_wiki_user_info ";
	protected static final String STATEMENT_DROP_TOPIC_TABLE =
		"DROP TABLE jam_topic ";
	protected static final String STATEMENT_DROP_TOPIC_VERSION_TABLE =
		"DROP TABLE jam_topic_version ";
	protected static final String STATEMENT_DROP_WIKI_FILE_TABLE =
		"DROP TABLE jam_file ";
	protected static final String STATEMENT_DROP_WIKI_FILE_VERSION_TABLE =
		"DROP TABLE jam_file_version ";
	protected static final String STATEMENT_DROP_IMAGE_TABLE =
		"DROP TABLE jam_image ";
	protected static final String STATEMENT_DROP_RECENT_CHANGE_TABLE =
		"DROP TABLE jam_recent_change ";
	protected static final String STATEMENT_INSERT_TOPIC =
		"insert into jam_topic ( "
		+   "topic_id, virtual_wiki_id, topic_name, topic_type, "
		+   "topic_read_only, topic_content, topic_deleted, "
		+   "topic_admin_only  "
		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ? "
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
		+   "virtual_wiki_id, virtual_wiki_name, default_topic_name "
		+ ") values ( "
		+   "?, ?, ? "
		+ ") ";
	protected static final String STATEMENT_INSERT_WIKI_FILE =
	    "insert into jam_file ( "
	    +   "file_id, virtual_wiki_id, file_name, "
	    +   "file_url, mime_type, topic_id, "
	    +   "file_deleted, file_read_only, file_admin_only, "
	    +   "file_size "
	    + ") values ( "
	    +   "?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
	    + ") ";
	protected static final String STATEMENT_INSERT_WIKI_FILE_VERSION =
	    "insert into jam_file_version ( "
	    +   "file_version_id, file_id, upload_comment, "
	    +   "file_url, wiki_user_id, wiki_user_ip_address, "
	    +   "upload_date, mime_type, file_size "
	    + ") values ( "
	    +   "?, ?, ?, ?, ?, ?, ?, ?, ? "
	    + ") ";
	protected static final String STATEMENT_INSERT_WIKI_USER =
		"insert into jam_wiki_user ( "
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
	protected static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
		"select max(topic_id) as topic_id from jam_topic ";
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
		"select max(topic_version_id) as topic_version_id from jam_topic_version ";
	protected static final String STATEMENT_SELECT_VIRTUAL_WIKIS =
		"select * from jam_virtual_wiki ";
	protected static final String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE =
		"select max(virtual_wiki_id) as virtual_wiki_id from jam_virtual_wiki ";
	protected static final String STATEMENT_SELECT_WIKI_FILE =
		"select * from jam_file "
		+ "where virtual_wiki_id = ? "
		+ "and topic_id = ? ";
	protected static final String STATEMENT_SELECT_WIKI_FILE_SEQUENCE =
		"select max(file_id) as file_id from jam_file ";
	protected static final String STATEMENT_SELECT_WIKI_FILE_TOPIC_NAMES =
		"select jam_topic.topic_name "
		+ "from jam_topic, jam_file "
		+ "where jam_topic.topic_id = jam_file.topic_id "
		+ "and jam_file.virtual_wiki_id = ? ";
	protected static final String STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE =
		"select max(file_version_id) as file_version_id from jam_file_version ";
	protected static final String STATEMENT_SELECT_WIKI_FILE_VERSIONS =
		"select * from jam_file_version "
		+ "where file_id = ? "
		+ "order by file_version_id desc ";
	protected static final String STATEMENT_SELECT_WIKI_USER_SEQUENCE =
		"select max(wiki_user_id) as wiki_user_id from jam_wiki_user ";
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
		+ "topic_read_only = ?, "
		+ "topic_content = ?, "
		+ "topic_deleted = ? "
		+ "where topic_id = ? ";
	protected static final String STATEMENT_UPDATE_VIRTUAL_WIKI =
		"update jam_virtual_wiki set "
		+ "default_topic_name = ? "
		+ "where virtual_wiki_id = ? ";
	protected static final String STATEMENT_UPDATE_WIKI_FILE =
		"update jam_file set "
		+ "virtual_wiki_id = ?, "
		+ "file_name = ?, "
		+ "file_url = ?, "
		+ "mime_type = ?, "
		+ "topic_id = ?, "
		+ "file_deleted = ?, "
		+ "file_read_only = ?, "
		+ "file_admin_only = ?, "
		+ "file_size = ? "
		+ "where file_id = ? ";
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
	protected DefaultQueryHandler() {
	}

	/**
	 *
	 */
	public void createTables(Connection conn) throws Exception {
		WikiPreparedStatement stmt = null;
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_INFO_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_IMAGE_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_TABLE, conn);
	}

	/**
	 *
	 */
	public void dropTables(Connection conn) {
		// note that this method is called during creation failures, so be careful to
		// catch errors that might result from a partial failure during install.  also
		// note that the coding style violation here is intentional since it makes the
		// actual work of the method more obvious.
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_RECENT_CHANGE_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_IMAGE_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_VERSION_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_VERSION_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_INFO_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DROP_VIRTUAL_WIKI_TABLE, conn);
		} catch (Exception e) { logger.error(e.getMessage()); }
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
	public WikiResultSet getAllTopicVersions(Topic topic, boolean descending) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSIONS);
		stmt.setInt(1, topic.getTopicId());
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getAllWikiFileTopicNames(String virtualWiki) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_FILE_TOPIC_NAMES);
		stmt.setInt(1, virtualWikiId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getAllWikiFileVersions(WikiFile wikiFile, boolean descending) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_FILE_VERSIONS);
		// FIXME - sort order ignored
		stmt.setInt(1, wikiFile.getFileId());
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
	public WikiResultSet getReadOnlyTopics(String virtualWiki) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_READ_ONLY);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, 1);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, int num, boolean descending) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_RECENT_CHANGES);
		stmt.setString(1, virtualWiki);
		stmt.setInt(2, num);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributions(String virtualWiki, String userString, int num, boolean descending) throws Exception {
		WikiPreparedStatement stmt = null;
		if (Utilities.isIpAddress(userString)) {
			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
		} else {
			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
		}
		stmt.setString(1, virtualWiki);
		stmt.setString(2, userString);
		stmt.setInt(3, num);
		// FIXME - sort order ignored
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
	public void insertRecentChange(RecentChange change, Connection conn) throws Exception {
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
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertTopic(Topic topic, Connection conn) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(topic.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_TOPIC);
		stmt.setInt(1, topic.getTopicId());
		stmt.setInt(2, virtualWikiId);
		stmt.setString(3, topic.getName());
		stmt.setInt(4, topic.getTopicType());
		stmt.setInt(5, (topic.getReadOnly() ? 1 : 0));
		stmt.setString(6, topic.getTopicContent());
		stmt.setInt(7, (topic.getDeleted() ? 1 : 0));
		stmt.setInt(8, (topic.getAdminOnly() ? 1 : 0));
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertTopicVersion(TopicVersion topicVersion, Connection conn) throws Exception {
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
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
		stmt.setInt(1, virtualWiki.getVirtualWikiId());
		stmt.setString(2, virtualWiki.getName());
		stmt.setString(3, virtualWiki.getDefaultTopicName());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertWikiFile(WikiFile wikiFile, Connection conn) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(wikiFile.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_FILE);
		stmt.setInt(1, wikiFile.getFileId());
		stmt.setInt(2, virtualWikiId);
		stmt.setString(3, wikiFile.getFileName());
		stmt.setString(4, wikiFile.getUrl());
		stmt.setString(5, wikiFile.getMimeType());
		stmt.setInt(6, wikiFile.getTopicId());
		stmt.setInt(7, (wikiFile.getDeleted() ? 1 : 0));
		stmt.setInt(8, (wikiFile.getReadOnly() ? 1 : 0));
		stmt.setInt(9, (wikiFile.getAdminOnly() ? 1 : 0));
		stmt.setInt(10, wikiFile.getFileSize());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_FILE_VERSION);
		stmt.setInt(1, wikiFileVersion.getFileVersionId());
		stmt.setInt(2, wikiFileVersion.getFileId());
		stmt.setString(3, wikiFileVersion.getUploadComment());
		stmt.setString(4, wikiFileVersion.getUrl());
		if (wikiFileVersion.getAuthorId() != null) {
			stmt.setInt(5, wikiFileVersion.getAuthorId().intValue());
		} else {
			stmt.setNull(5, Types.INTEGER);
		}
		stmt.setString(6, wikiFileVersion.getAuthorIpAddress());
		stmt.setTimestamp(7, wikiFileVersion.getUploadDate());
		stmt.setString(8, wikiFileVersion.getMimeType());
		stmt.setInt(9, wikiFileVersion.getFileSize());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertWikiUser(WikiUser user, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER);
		stmt.setInt(1, user.getUserId());
		stmt.setString(2, user.getLogin());
		stmt.setString(3, user.getDisplayName());
		stmt.setTimestamp(4, user.getCreateDate());
		stmt.setTimestamp(5, user.getLastLoginDate());
		stmt.setString(6, user.getCreateIpAddress());
		stmt.setString(7, user.getLastLoginIpAddress());
		stmt.setInt(8, (user.getAdmin() ? 1 : 0));
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void insertWikiUserInfo(WikiUser user, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER_INFO);
		stmt.setInt(1, user.getUserId());
		stmt.setString(2, user.getLogin());
		stmt.setString(3, user.getEmail());
		stmt.setString(4, user.getFirstName());
		stmt.setString(5, user.getLastName());
		stmt.setString(6, user.getEncodedPassword());
		stmt.executeUpdate(conn);
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
	public WikiResultSet lookupLastTopicVersion(Topic topic, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION_LAST);
		stmt.setInt(1, topic.getTopicId());
		return stmt.executeQuery(conn);
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
	public WikiResultSet lookupTopic(String virtualWiki, String topicName, Connection conn) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC);
		stmt.setInt(1, virtualWikiId);
		stmt.setString(2, topicName);
		return stmt.executeQuery(conn);
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
	public WikiResultSet lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION);
		stmt.setInt(1, topicVersionId);
		return stmt.executeQuery(conn);
	}

	/**
	 *
	 */
	public WikiResultSet lookupWikiFile(String virtualWiki, int topicId) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(virtualWiki);
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_FILE);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, topicId);
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
	public WikiResultSet lookupWikiUser(int userId, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER);
		stmt.setInt(1, userId);
		return stmt.executeQuery(conn);
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
	public WikiResultSet lookupWikiUser(String login, String encryptedPassword) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_PASSWORD);
		stmt.setString(1, login);
		stmt.setString(2, encryptedPassword);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public int nextTopicId(Connection conn) throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE, conn);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("topic_id");
		// note - this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextTopicVersionId(Connection conn) throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE, conn);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("topic_version_id");
		// note - this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextVirtualWikiId(Connection conn) throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE, conn);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("virtual_wiki_id");
		// note - this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextWikiFileId(Connection conn) throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_FILE_SEQUENCE, conn);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("file_id");
		// note - this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextWikiFileVersionId(Connection conn) throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE, conn);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("file_version_id");
		// note - this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextWikiUserId(Connection conn) throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE, conn);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("wiki_user_id");
		// note - this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 * Utility method for returning a database-appropriate value that corresponds
	 * to the SQL function indicating the current time.
	 */
	private static String now() {
		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_POSTGRES)) {
			return "CURRENT_TIMESTAMP";
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_MYSQL)) {
			return "CURRENT_TIMESTAMP";
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE)) {
			return "SYSTIMESTAMP";
		} else {
			return "CURRENT_TIMESTAMP";
		}
	}

	/**
	 *
	 */
	public void reloadRecentChanges(Connection conn) throws Exception {
		DatabaseConnection.executeUpdate(STATEMENT_DELETE_RECENT_CHANGES, conn);
		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES, conn);
	}

	/**
	 * Utility method for returning a database-appropriate value that corresponds
	 * to the SQL type for text values.
	 */
	private static String text() {
		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_POSTGRES)) {
			return "TEXT";
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_MYSQL)) {
			return "TEXT";
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE)) {
			return "CLOB";
		} else {
			return "TEXT";
		}
	}

	/**
	 *
	 */
	public void updateTopic(Topic topic, Connection conn) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(topic.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_TOPIC);
		stmt.setInt(1, virtualWikiId);
		stmt.setString(2, topic.getName());
		stmt.setInt(3, topic.getTopicType());
		stmt.setInt(4, (topic.getReadOnly() ? 1 : 0));
		stmt.setString(5, topic.getTopicContent());
		stmt.setInt(6, (topic.getDeleted() ? 1 : 0));
		stmt.setInt(7, topic.getTopicId());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_VIRTUAL_WIKI);
		stmt.setString(1, virtualWiki.getDefaultTopicName());
		stmt.setInt(2, virtualWiki.getVirtualWikiId());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void updateWikiFile(WikiFile wikiFile, Connection conn) throws Exception {
		int virtualWikiId = DatabaseHandler.lookupVirtualWikiId(wikiFile.getVirtualWiki());
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_FILE);
		stmt.setInt(1, virtualWikiId);
		stmt.setString(2, wikiFile.getFileName());
		stmt.setString(3, wikiFile.getUrl());
		stmt.setString(4, wikiFile.getMimeType());
		stmt.setInt(5, wikiFile.getTopicId());
		stmt.setInt(6, (wikiFile.getDeleted() ? 1 : 0));
		stmt.setInt(7, (wikiFile.getReadOnly() ? 1 : 0));
		stmt.setInt(8, (wikiFile.getAdminOnly() ? 1 : 0));
		stmt.setInt(9, wikiFile.getFileSize());
		stmt.setInt(10, wikiFile.getFileId());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void updateWikiUser(WikiUser user, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_USER);
		stmt.setString(1, user.getLogin());
		stmt.setString(2, user.getDisplayName());
		stmt.setTimestamp(3, user.getLastLoginDate());
		stmt.setString(4, user.getLastLoginIpAddress());
		stmt.setInt(5, (user.getAdmin() ? 1 : 0));
		stmt.setInt(6, user.getUserId());
		stmt.executeUpdate(conn);
	}

	/**
	 *
	 */
	public void updateWikiUserInfo(WikiUser user, Connection conn) throws Exception {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_USER_INFO);
		stmt.setString(1, user.getLogin());
		stmt.setString(2, user.getEmail());
		stmt.setString(3, user.getFirstName());
		stmt.setString(4, user.getLastName());
		stmt.setString(5, user.getEncodedPassword());
		stmt.setInt(6, user.getUserId());
		stmt.executeUpdate(conn);
	}
}
