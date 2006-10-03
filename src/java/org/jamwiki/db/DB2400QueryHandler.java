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
public class DB2400QueryHandler extends DefaultQueryHandler {

	private static WikiLogger logger = WikiLogger.getLogger(DB2400QueryHandler.class.getName());
	private static final String SQL_PROPERTY_FILE_NAME = "sql.db2400.properties";
	private static Properties props = null;
	private static Properties defaults = null;

	/**
	 *
	 */
	protected DB2400QueryHandler() {
		defaults = Environment.loadProperties(DefaultQueryHandler.SQL_PROPERTY_FILE_NAME);
		props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME, defaults);
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
			Object[] objects = {new Integer(pagination.getEnd()), new Integer(pagination.getNumResults())};
			return MessageFormat.format(sql, objects);
		} catch (Exception e) {
			logger.warning("Unable to format " + sql + " with values " + pagination.getEnd() + " / " + pagination.getNumResults(), e);
			return null;
		}
	}

	/**
	 *
	 */
	public WikiResultSet getCategories(int virtualWikiId, Pagination pagination) throws Exception {
		String sql = formatStatement(STATEMENT_SELECT_CATEGORIES, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, virtualWikiId);
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception {
		String sql = formatStatement(STATEMENT_SELECT_RECENT_CHANGES, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setString(1, virtualWiki);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(int topicId, Pagination pagination, boolean descending) throws Exception {
		String sql = formatStatement(STATEMENT_SELECT_RECENT_CHANGES_TOPIC, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, topicId);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception {
		String sql = null;
		if (Utilities.isIpAddress(userString)) {
			sql = formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS, pagination);
		} else {
			sql = formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN, pagination);
		}
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, userString);
		// FIXME - sort order ignored
		return stmt.executeQuery();
	}

	/**
	 *
	 */
	public WikiResultSet lookupTopicByType(int virtualWikiId, int topicType, Pagination pagination) throws Exception {
		String sql = formatStatement(STATEMENT_SELECT_TOPIC_BY_TYPE, pagination);
		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, topicType);
		return stmt.executeQuery();
	}
}
