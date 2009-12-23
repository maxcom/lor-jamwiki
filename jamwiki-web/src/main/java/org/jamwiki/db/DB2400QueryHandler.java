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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jamwiki.Environment;
import org.jamwiki.model.Category;
import org.jamwiki.model.LogItem;
import org.jamwiki.model.RecentChange;
import org.jamwiki.utils.Pagination;
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
	public List<Category> getCategories(int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_CATEGORIES, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, virtualWikiId);
			rs = stmt.executeQuery();
			List<Category> results = new ArrayList<Category>();
			while (rs.next()) {
				Category category = new Category();
				category.setName(rs.getString("category_name"));
				// child topic name not initialized since it is not needed
				category.setVirtualWiki(virtualWikiName);
				category.setSortKey(rs.getString("sort_key"));
				// topic type not initialized since it is not needed
				results.add(category);
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<LogItem> getLogItems(int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int index = 1;
		List<LogItem> logItems = new ArrayList<LogItem>();
		String sql = null;
		try {
			conn = DatabaseConnection.getConnection();
			if (logType == -1) {
				sql = formatStatement(STATEMENT_SELECT_LOG_ITEMS, pagination);
				stmt = conn.prepareStatement(sql);
			} else {
				sql = formatStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE, pagination);
				stmt = conn.prepareStatement(sql);
				stmt.setInt(index++, logType);
			}
			stmt.setInt(index++, virtualWikiId);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			while (rs.next()) {
				logItems.add(this.initLogItem(rs, virtualWikiName));
			}
			return logItems;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_RECENT_CHANGES, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, virtualWiki);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<RecentChange> getTopicHistory(int topicId, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_TOPIC_HISTORY, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, topicId);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<String> getTopicsAdmin(int virtualWikiId, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_TOPICS_ADMIN, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, virtualWikiId);
			rs = stmt.executeQuery();
			List<String> results = new ArrayList<String>();
			while (rs.next()) {
				results.add(rs.getString("topic_name"));
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<RecentChange> getUserContributionsByLogin(String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, virtualWiki);
			stmt.setString(2, login);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<RecentChange> getUserContributionsByUserDisplay(String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, virtualWiki);
			stmt.setString(2, userDisplay);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<RecentChange> getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_WATCHLIST_CHANGES, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, virtualWikiId);
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<String> lookupTopicByType(int virtualWikiId, int topicType1, int topicType2, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_TOPIC_BY_TYPE, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, virtualWikiId);
			stmt.setInt(2, topicType1);
			stmt.setInt(3, topicType2);
			rs = stmt.executeQuery();
			List<String> results = new ArrayList<String>();
			while (rs.next()) {
				results.add(rs.getString("topic_name"));
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<String> lookupWikiUsers(Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = formatStatement(STATEMENT_SELECT_WIKI_USERS, pagination);
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			List<String> results = new ArrayList<String>();
			while (rs.next()) {
				results.add(rs.getString("login"));
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}
}
