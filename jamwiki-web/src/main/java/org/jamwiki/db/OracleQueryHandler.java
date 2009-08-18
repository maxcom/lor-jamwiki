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

import java.sql.SQLException;
import java.util.Properties;
import org.jamwiki.Environment;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Oracle-specific implementation of the QueryHandler interface.  This class implements
 * Oracle-specific methods for instances where Oracle does not support the default
 * ASCII SQL syntax.
 */
public class OracleQueryHandler extends AnsiQueryHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(OracleQueryHandler.class.getName());
	private static final String SQL_PROPERTY_FILE_NAME = "sql.oracle.properties";

	/**
	 *
	 */
	protected OracleQueryHandler() {
		Properties defaults = Environment.loadProperties(AnsiQueryHandler.SQL_PROPERTY_FILE_NAME);
		Properties props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME, defaults);
		super.init(props);
	}

	/**
	 *
	 */
	public WikiResultSet getCategories(int virtualWikiId, Pagination pagination) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_CATEGORIES);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getLogItems(int virtualWikiId, int logType, Pagination pagination, boolean descending) throws SQLException {
		WikiPreparedStatement stmt = null;
		int index = 1;
		if (logType == -1) {
			stmt = new WikiPreparedStatement(STATEMENT_SELECT_LOG_ITEMS);
		} else {
			stmt = new WikiPreparedStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE);
			stmt.setInt(index++, logType);
		}
		stmt.setInt(index++, virtualWikiId);
		stmt.setInt(index++, pagination.getEnd());
		stmt.setInt(index++, pagination.getStart());
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_RECENT_CHANGES);
		stmt.setString(1, virtualWiki);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getTopicHistory(int topicId, Pagination pagination, boolean descending) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_HISTORY);
		stmt.setInt(1, topicId);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getTopicsAdmin(int virtualWikiId, Pagination pagination) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPICS_ADMIN);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributionsByLogin(String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, login);
		stmt.setInt(3, pagination.getEnd());
		stmt.setInt(4, pagination.getStart());
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributionsByUserDisplay(String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, userDisplay);
		stmt.setInt(3, pagination.getEnd());
		stmt.setInt(4, pagination.getStart());
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WATCHLIST_CHANGES);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, userId);
		stmt.setInt(3, pagination.getEnd());
		stmt.setInt(4, pagination.getStart());
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupTopicByType(int virtualWikiId, int topicType, Pagination pagination) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_BY_TYPE);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, topicType);
		stmt.setInt(3, pagination.getEnd());
		stmt.setInt(4, pagination.getStart());
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupWikiUsers(Pagination pagination) throws SQLException {
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USERS);
		stmt.setInt(1, pagination.getEnd());
		stmt.setInt(2, pagination.getStart());
		return stmt.executeQuery();
	}
}
