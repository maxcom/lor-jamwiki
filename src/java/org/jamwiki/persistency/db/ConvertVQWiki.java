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
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.utils.Encryption;

/**
 * Convert a VQWiki 2.7.8 database to JAMWiki format.
 */
public class ConvertVQWiki {

	private static final Logger logger = Logger.getLogger(ConvertVQWiki.class);

	private static final String DATABASE_DB2 = "db2";
	private static final String DATABASE_MYSQL = "mysql";
	private static final String DATABASE_ORACLE = "oracle";
	private static final String DATABASE_POSTGRES = "postgres";
	private static final String INIT_SCRIPT_ANSI = "create_ansi.sql";
	private static final String INIT_SCRIPT_ORACLE = "create_oracle.sql";

	/**
	 *
	 */
	protected static void convertFromDatabase() throws Exception {
		Connection conn = null;
		String sql = null;
		try {
			conn = DatabaseConnection.getConnection();
			Statement st = conn.createStatement();
			Statement results = conn.createStatement();
			ResultSet rs = null;
			// jam_virtual_wiki
			sql = "INSERT INTO jam_virtual_wiki ("
				+   "virtual_wiki_name "
				+ ") "
				+ "SELECT name FROM VirtualWiki ";
			st.executeUpdate(sql);
			// jam_wiki_user
			sql = "INSERT INTO jam_wiki_user ( "
				+   "login, virtual_wiki_id, confirmation_key, email, display_name, "
				+   "encoded_password, initial_ip_address, last_ip_address "
				+ ") "
				+ "SELECT WikiMember.wikiuser, jam_virtual_wiki.virtual_wiki_id, "
				+ "WikiMember.userkey, WikiMember.email, WikiMember.wikiuser, "
				+ "'" + Encryption.encrypt(PersistencyHandler.DEFAULT_PASSWORD) + "', "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "', "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "' "
				+ "FROM WikiMember, jam_virtual_wiki "
				+ "WHERE WikiMember.virtualwiki = jam_virtual_wiki.virtual_wiki_name ";
			st.executeUpdate(sql);
			sql = "INSERT INTO jam_wiki_user ( "
				+   "login, virtual_wiki_id, display_name, "
				+   "encoded_password, initial_ip_address, last_ip_address "
				+ ") "
				+ "SELECT DISTINCT TopicChange.username, jam_virtual_wiki.virtual_wiki_id, "
				+ "TopicChange.username, "
				+ "'" + Encryption.encrypt(PersistencyHandler.DEFAULT_PASSWORD) + "', "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "', "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "' "
				+ "FROM TopicChange, jam_virtual_wiki "
				+ "WHERE TopicChange.virtualwiki = jam_virtual_wiki.virtual_wiki_name "
				+ "AND NOT EXISTS ( "
				+   "SELECT * FROM WikiMember "
				+   "WHERE TopicChange.username = WikiMember.wikiuser "
				+   "AND TopicChange.virtualwiki = WikiMember.virtualwiki "
				+ ") ";
			st.executeUpdate(sql);
			// jam_topic
			sql = "insert into jam_topic ( "
				+   "virtual_wiki_id, topic_name, topic_type, topic_content "
				+ ") "
				+ "SELECT jam_virtual_wiki.virtual_wiki_id, Topic.name, "
				+ Topic.TYPE_ARTICLE + ", "
				+ "Topic.contents "
				+ "FROM Topic, jam_virtual_wiki "
				+ "WHERE Topic.virtualwiki = jam_virtual_wiki.virtual_wiki_name ";
			st.executeUpdate(sql);
			sql = "SELECT topic, virtualwiki FROM TopicReadOnly ";
			rs = results.executeQuery(sql);
			while (rs.next()) {
				sql = "UPDATE jam_topic set topic_read_only = TRUE "
					+ "WHERE topic_name = '" + rs.getString("topic") + "' "
					+ "AND virtual_wiki_id = ( "
					+   "SELECT virtual_wiki_id FROM jam_virtual_wiki "
					+   "WHERE virtual_wiki_name = '" + rs.getString("virtualwiki") + "' "
					+ ") ";
				st.executeUpdate(sql);
			}
			sql = "SELECT topic, sessionkey, lockat, virtualwiki FROM TopicLock ";
			rs = results.executeQuery(sql);
			while (rs.next()) {
				sql = "UPDATE jam_topic set "
				    + "topic_lock_session_key = '" + rs.getString("sessionkey") + "', "
				    + "topic_lock_date = '" + rs.getTimestamp("lockat") + "' "
					+ "WHERE topic_name = '" + rs.getString("topic") + "' "
					+ "AND virtual_wiki_id = ( "
					+   "SELECT virtual_wiki_id FROM jam_virtual_wiki "
					+   "WHERE virtual_wiki_name = '" + rs.getString("virtualwiki") + "' "
					+ ") ";
				st.executeUpdate(sql);
			}
			// jam_topic_version
			// create default author for edits with no author
			sql = "INSERT INTO jam_wiki_user ( "
				+   "login, virtual_wiki_id, display_name, "
				+   "encoded_password, initial_ip_address, last_ip_address "
				+ ") "
				+ "SELECT '" + PersistencyHandler.DEFAULT_AUTHOR_LOGIN + "', "
				+ "virtual_wiki_id, '" + PersistencyHandler.DEFAULT_AUTHOR_NAME + "', "
				+ "'" + Encryption.encrypt(PersistencyHandler.DEFAULT_PASSWORD) + "', "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "', "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "' "
				+ "FROM jam_virtual_wiki ";
			st.executeUpdate(sql);
			// get default author (will update later)
			sql = "SELECT wiki_user_id FROM jam_wiki_user "
				+ "WHERE login = '" + PersistencyHandler.DEFAULT_AUTHOR_LOGIN + "' ";
			rs  = results.executeQuery(sql);
			int authorId = 0;
			while (rs.next()) {
				authorId = rs.getInt("wiki_user_id");
			}
			sql = "INSERT INTO jam_topic_version ( "
				+   "topic_id, version_content, wiki_user_id, "
				+   "edit_date, edit_type, wiki_user_ip_address "
				+ ") "
				+ "SELECT jam_topic.topic_id, TopicVersion.contents, "
				+ authorId + ", "
				+ "TopicVersion.versionat, "
				+ TopicVersion.EDIT_NORMAL + ", "
				+ "'" + PersistencyHandler.DEFAULT_AUTHOR_IP_ADDRESS + "' "
				+ "FROM TopicVersion, jam_topic, jam_virtual_wiki "
				+ "WHERE jam_topic.topic_name = TopicVersion.name "
				+ "AND jam_topic.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id "
				+ "AND TopicVersion.virtualwiki = jam_virtual_wiki.virtual_wiki_name ";
			st.executeUpdate(sql);
			// add links to authors
			sql = "SELECT jam_wiki_user.wiki_user_id, jam_topic_version.topic_version_id "
				+ "FROM jam_wiki_user, TopicChange, jam_topic_version, "
				+ "jam_virtual_wiki, jam_topic "
				+ "WHERE jam_wiki_user.login = TopicChange.username "
				+ "AND jam_wiki_user.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id "
				+ "AND jam_virtual_wiki.virtual_wiki_name = TopicChange.virtualwiki "
				+ "AND (TopicChange.changeat - jam_topic_version.edit_date) < INTERVAL '5 seconds' "
				+ "AND jam_topic_version.topic_id = jam_topic.topic_id "
				+ "AND jam_topic.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id "
				+ "AND jam_topic.topic_name = TopicChange.topic ";
			rs  = results.executeQuery(sql);
			while (rs.next()) {
				sql = "UPDATE jam_topic_version "
					+ "SET wiki_user_id = " + rs.getInt("wiki_user_id") + " "
					+ "WHERE topic_version_id = " + rs.getInt("topic_version_id") + " ";
				st.executeUpdate(sql);
			}
			// update articles still using default author
			sql = "SELECT wiki_user_id, topic_id "
				+ "FROM jam_wiki_user, jam_topic "
				+ "WHERE jam_wiki_user.login = '" + PersistencyHandler.DEFAULT_AUTHOR_LOGIN + "' "
				+ "AND jam_wiki_user.virtual_wiki_id = jam_topic.virtual_wiki_id ";
			rs  = results.executeQuery(sql);
			while (rs.next()) {
				// FIXME - not all topics need to be updated, so this is inefficient
				sql = "UPDATE jam_topic_version "
					+ "SET wiki_user_id = " + rs.getInt("wiki_user_id") + " "
					+ "WHERE wiki_user_id = " + authorId + " "
					+ "AND topic_id = " + rs.getInt("topic_id") + " ";
				st.executeUpdate(sql);
			}
			// jam_notification
			sql = "INSERT INTO jam_notification ( "
				+   "wiki_user_id, topic_id "
				+ ") "
				+ "SELECT jam_wiki_user.wiki_user_id, jam_topic.topic_id "
				+ "FROM jam_wiki_user, jam_topic, jam_virtual_wiki, Notification "
				+ "WHERE jam_topic.topic_name = Notification.topic "
				+ "AND jam_virtual_wiki.virtual_wiki_name = Notification.virtualwiki "
				+ "AND jam_virtual_wiki.virtual_wiki_id = jam_topic.virtual_wiki_id "
				+ "AND jam_wiki_user.login = Notification.wikiuser "
				+ "AND jam_wiki_user.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id ";
			st.executeUpdate(sql);
			// jam_recent_change
			sql = "INSERT INTO jam_recent_change ( "
				+   "topic_version_id, topic_id, "
				+   "topic_name, edit_date, wiki_user_id, display_name, "
				+   "edit_type, virtual_wiki_id, virtual_wiki_name, edit_comment "
				+ ") "
				+ "SELECT "
				+   "jam_topic_version.topic_version_id, jam_topic.topic_id, "
				+   "jam_topic.topic_name, jam_topic_version.edit_date, "
				+   "jam_topic_version.wiki_user_id, jam_wiki_user.display_name, "
				+   "jam_topic_version.edit_type, jam_virtual_wiki.virtual_wiki_id, "
				+   "jam_virtual_wiki.virtual_wiki_name, jam_topic_version.edit_comment "
				+ "FROM jam_topic, jam_topic_version, jam_wiki_user, jam_virtual_wiki "
				+ "WHERE jam_topic.topic_id = jam_topic_version.topic_id "
				+ "AND jam_topic_version.wiki_user_id = jam_wiki_user.wiki_user_id "
				+ "AND jam_topic.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id ";
			st.executeUpdate(sql);
			st.close();
		} catch (Exception e) {
			if (sql != null) {
				e = new Exception("Failure while executing SQL: " + sql, e);
			}
			throw e;
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 * Optionally purge obsolete data from the database.
	 */
	private void purgeDatabase() throws Exception {
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
}
