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
import java.text.MessageFormat;
import java.util.Properties;
import org.jamwiki.Environment;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * DB2/400-specific implementation of the QueryHandler interface.  This class implements
 * DB2/400-specific methods for instances where DB2/400 does not support the default
 * ASCII SQL syntax.
 */
public class DB2400QueryHandler extends AnsiQueryHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(DB2400QueryHandler.class.getName());
	private static final String SQL_PROPERTY_FILE_NAME = "sql.db2400.properties";

	/**
	 *
	 */
	protected DB2400QueryHandler() {
		Properties defaults = Environment.loadProperties(AnsiQueryHandler.SQL_PROPERTY_FILE_NAME);
		Properties props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME, defaults);
		super.init(props);
	}

	/**
	 * DB2/400 will not allow query parameters such as "fetch ? rows only", so
	 * this method provides a way of formatting the query limits without using
	 * query parameters.
	 *
	 * @param sql The SQL statement, with the last result parameter specified as
	 *  {0} and the total number of rows parameter specified as {1}.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A formatted SQL string.
	 */
	private static String formatStatement(String sql, Pagination pagination) {
		try {
			Object[] objects = {pagination.getEnd(), pagination.getNumResults()};
			return MessageFormat.format(sql, objects);
		} catch (Exception e) {
			logger.warning("Unable to format " + sql + " with values " + pagination.getEnd() + " / " + pagination.getNumResults(), e);
			return null;
		}
	}

	/**
	 *
	 */
	public WikiResultSet getCategories(int virtualWikiId, Pagination pagination) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_CATEGORIES, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, virtualWikiId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getLogItems(int virtualWikiId, int logType, Pagination pagination, boolean descending) throws SQLException {
		String sql = null;
		WikiPreparedStatement stmt = null;
		int index = 1;
		if (logType == -1) {
			sql = formatStatement(STATEMENT_SELECT_LOG_ITEMS, pagination);
			stmt = new WikiPreparedStatement(sql);
		} else {
			sql = formatStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE, pagination);
			stmt = new WikiPreparedStatement(sql);
			stmt.setInt(index++, logType);
		}
		stmt.setInt(index++, virtualWikiId);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_RECENT_CHANGES, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setString(1, virtualWiki);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(int topicId, Pagination pagination, boolean descending) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_RECENT_CHANGES_TOPIC, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, topicId);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getTopicsAdmin(int virtualWikiId, Pagination pagination) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_TOPICS_ADMIN, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, virtualWikiId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributionsByLogin(String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, login);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributionsByUserDisplay(String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, userDisplay);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_WATCHLIST_CHANGES, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, userId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupTopicByType(int virtualWikiId, int topicType, Pagination pagination) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_TOPIC_BY_TYPE, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, topicType);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupWikiUsers(Pagination pagination) throws SQLException {
		String sql = formatStatement(STATEMENT_SELECT_WIKI_USERS, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		return stmt.executeQuery();
	}
}
