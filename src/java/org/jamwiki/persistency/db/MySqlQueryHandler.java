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

	private static final String STATEMENT_CREATE_WIKI_USER_TABLE =
		"CREATE TABLE jam_wiki_user ( "
		+   "wiki_user_id INTEGER NOT NULL, "
		+   "login VARCHAR(100) NOT NULL, "
		+   "display_name VARCHAR(100), "
		// FIXME - mysql only allows one column to use CURRENT_TIMESTAMP, but this should default also
		+   "create_date TIMESTAMP NOT NULL DEFAULT 0, "
		+   "last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
		+   "create_ip_address VARCHAR(15) NOT NULL, "
		+   "last_login_ip_address VARCHAR(15) NOT NULL, "
		+   "is_admin CHAR NOT NULL DEFAULT '0', "
		+   "CONSTRAINT jam_pk_wiki_user PRIMARY KEY (wiki_user_id) "
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
		+ "AND jam_topic.topic_deleted = '0' ";
	private static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
		"select max(topic_id) as topic_id from jam_topic ";
	private static final String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE =
		"select max(topic_version_id) as topic_version_id from jam_topic_version ";
	private static final String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE =
		"select max(virtual_wiki_id) as virtual_wiki_id from jam_virtual_wiki ";
	private static final String STATEMENT_SELECT_WIKI_USER_SEQUENCE =
		"select max(wiki_user_id) as wiki_user_id from jam_wiki_user ";

	/**
	 *
	 */
	protected MySqlQueryHandler() {
	}

	/**
	 *
	 */
	public void createTables() throws Exception {
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_VIRTUAL_WIKI_TABLE);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE);
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_WIKI_USER_INFO_TABLE);
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_TOPIC_TABLE);
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_TOPIC_VERSION_TABLE);
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_NOTIFICATION_TABLE);
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_RECENT_CHANGE_TABLE);
	}

	/**
	 *
	 */
	public int nextTopicId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("topic_id");
		// note - for MySql this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextTopicVersionId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("topic_version_id");
		// note - for MySql this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextVirtualWikiId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("virtual_wiki_id");
		// note - for MySql this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public int nextWikiUserId() throws Exception {
		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE);
		int nextId = 0;
		if (rs.size() > 0) nextId = rs.getInt("wiki_user_id");
		// note - for MySql this returns the last id in the system, so add one
		return nextId + 1;
	}

	/**
	 *
	 */
	public void reloadRecentChanges() throws Exception {
		DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DELETE_RECENT_CHANGES);
		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES);
	}
}
