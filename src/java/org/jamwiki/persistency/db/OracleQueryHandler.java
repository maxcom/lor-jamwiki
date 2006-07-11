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

import java.sql.Types;
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class OracleQueryHandler extends DefaultQueryHandler {

	private static Logger logger = Logger.getLogger(OracleQueryHandler.class.getName());

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
		+   "nvl(jam_wiki_user.login, jam_topic_version.wiki_user_ip_address), "
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
	private static final String STATEMENT_SELECT_RECENT_CHANGES =
		"select * from ( "
		+   "select * from jam_recent_change "
		+     "where virtual_wiki_name = ? "
		+     "order by edit_date desc "
		+ ") where rownum <= ? ";
	private static final String STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS =
		"select * from ("
		+   "select "
		+     "jam_topic_version.topic_version_id, jam_topic_version.topic_id, "
		+     "jam_topic_version.previous_topic_version_id, jam_topic.topic_name, "
		+     "jam_topic_version.edit_date, jam_topic_version.edit_comment, "
		+     "jam_topic_version.wiki_user_id, jam_topic_version.edit_type, "
		+     "jam_topic_version.wiki_user_ip_address as display_name, "
		+     "jam_topic.virtual_wiki_id, jam_virtual_wiki.virtual_wiki_name "
		+   "from jam_topic, jam_virtual_wiki, jam_topic_version "
		+   "where jam_virtual_wiki.virtual_wiki_id = jam_topic.virtual_wiki_id "
		+   "and jam_topic.topic_id = jam_topic_version.topic_id "
		+   "and jam_virtual_wiki.virtual_wiki_name = ? "
		+   "and jam_topic_version.wiki_user_ip_address = ? "
		+   "and jam_topic_version.wiki_user_id is null "
		+   "and jam_topic.topic_deleted = '0' "
		+   "order by edit_date desc "
		+ ") where rownum <= ? ";
	private static final String STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN =
		"select * from ("
		+   "select "
		+     "jam_topic_version.topic_version_id, jam_topic_version.topic_id, "
		+     "jam_topic_version.previous_topic_version_id, jam_topic.topic_name, "
		+     "jam_topic_version.edit_date, jam_topic_version.edit_comment, "
		+     "jam_topic_version.wiki_user_id, jam_topic_version.edit_type, "
		+     "jam_wiki_user.login as display_name, jam_topic.virtual_wiki_id, "
		+     "jam_virtual_wiki.virtual_wiki_name "
		+   "from jam_topic, jam_virtual_wiki, jam_topic_version, jam_wiki_user "
		+   "where jam_virtual_wiki.virtual_wiki_id = jam_topic.virtual_wiki_id "
		+   "and jam_wiki_user.wiki_user_id = jam_topic_version.wiki_user_id "
		+   "and jam_topic.topic_id = jam_topic_version.topic_id "
		+   "and jam_virtual_wiki.virtual_wiki_name = ? "
		+   "and jam_wiki_user.login = ? "
		+   "and jam_topic.topic_deleted = '0' "
		+   "order by edit_date desc "
		+ ") where rownum <= ? ";
	private static final String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE =
		"select jam_virtual_wiki_seq.nextval as virtual_wiki_id from dual ";
	private static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
		"select jam_topic_seq.nextval as topic_id from dual ";
	private static final String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE =
		"select jam_topic_version_seq.nextval as topic_version_id from dual ";
	private static final String STATEMENT_SELECT_WIKI_USER_SEQUENCE =
		"select jam_wiki_user_seq.nextval as wiki_user_id from dual ";

	/**
	 *
	 */
	protected OracleQueryHandler() {
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
		DatabaseConnection.executeUpdate(DefaultQueryHandler.STATEMENT_DELETE_RECENT_CHANGES);
		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES);
	}
}
