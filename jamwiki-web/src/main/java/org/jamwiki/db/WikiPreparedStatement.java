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
import java.sql.Timestamp;
import java.sql.Types;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;

/**
 * This class is a wrapper around the java.sql.PreparedStatement class, allowing a
 * statement to be prepared without requiring that a database connection be
 * held.  The main advantage of this approach is that all connection handling can
 * be done by low-level database functions, and the user can build and execute a
 * query without the need to ensure that the connection is properly closed after
 * the data is processed.
 */
public class WikiPreparedStatement {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiPreparedStatement.class.getName());
	private Object[] params = null;
	private int[] paramTypes = null;
	private final String sql;
	private PreparedStatement statement = null;
	private final int numElements;

	/**
	 *
	 */
	public WikiPreparedStatement(String sql) {
		this.sql = sql;
		this.numElements = StringUtils.countMatches(sql, "?");
		this.params = new Object[numElements];
		this.paramTypes = new int[numElements];
	}

	/**
	 *
	 */
	public WikiResultSet executeQuery() throws SQLException {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			return this.executeQuery(conn);
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public WikiResultSet executeQuery(Connection conn) throws SQLException {
		ResultSet rs = null;
		try {
			long start = System.currentTimeMillis();
			this.statement = conn.prepareStatement(this.sql);
			this.loadStatement();
			rs = this.statement.executeQuery();
			long execution = System.currentTimeMillis() - start;
			if (execution > DatabaseConnection.SLOW_QUERY_LIMIT) {
				logger.warning("Slow query: " + sql + " (" + (execution / 1000.000) + " s.)");
			}
			logger.fine("Executed " + this.sql + " (" + (execution / 1000.000) + " s.)");
			return new WikiResultSet(rs);
		} catch (SQLException e) {
			logger.severe("Failure while executing " + this.sql, e);
			throw e;
		} finally {
			DatabaseConnection.closeConnection(null, this.statement, rs);
		}
	}

	/**
	 *
	 */
	public int executeUpdate() throws SQLException {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			return this.executeUpdate(conn);
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public int executeUpdate(Connection conn) throws SQLException {
		try {
			long start = System.currentTimeMillis();
			this.statement = conn.prepareStatement(this.sql);
			this.loadStatement();
			int result = this.statement.executeUpdate();
			long execution = System.currentTimeMillis() - start;
			if (execution > DatabaseConnection.SLOW_QUERY_LIMIT) {
				logger.warning("Slow query: " + sql + " (" + (execution / 1000.000) + " s.)");
			}
			logger.fine("Executed " + this.sql + " (" + (execution / 1000.000) + " s.)");
			return result;
		} catch (SQLException e) {
			logger.severe("Failure while executing " + this.sql, e);
			throw e;
		} finally {
			DatabaseConnection.closeConnection(null, this.statement);
		}
	}

	/**
	 *
	 */
	private void loadStatement() throws SQLException {
		for (int i = 0; i < this.paramTypes.length; i++) {
			if (params[i] == null) {
				this.statement.setNull(i+1, paramTypes[i]);
			} else if (paramTypes[i] == Types.CHAR) {
				char value = ((Character)params[i]).charValue();
				this.statement.setString(i+1, Character.toString(value));
			} else if (paramTypes[i] == Types.INTEGER) {
				int value = ((Integer)params[i]).intValue();
				this.statement.setInt(i+1, value);
			} else if (paramTypes[i] == Types.TIMESTAMP) {
				Timestamp value = (Timestamp)params[i];
				this.statement.setTimestamp(i+1, value);
			} else if (paramTypes[i] == Types.VARCHAR) {
				String value = (String)params[i];
				this.statement.setString(i+1, value);
			}
		}
	}

	/**
	 * Sets the designated parameter to the given Java character value. The
	 * driver converts this to an SQL CHAR value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws IndexOutOfBoundsException If a parameter is invalid.
	 */
	public void setChar(int parameterIndex, char x) {
		this.verifyParams(parameterIndex);
		this.paramTypes[parameterIndex - 1] = Types.CHAR;
		this.params[parameterIndex - 1] = new Character(x);
	}

	/**
	 * Sets the designated parameter to the given Java int value. The driver
	 * converts this to an SQL INTEGER value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws IndexOutOfBoundsException If a parameter is invalid.
	 */
	public void setInt(int parameterIndex, int x) {
		this.verifyParams(parameterIndex);
		this.paramTypes[parameterIndex - 1] = Types.INTEGER;
		this.params[parameterIndex - 1] = new Integer(x);
	}

	/**
	 * Sets the designated parameter to the given Java int value. The driver
	 * converts this to an SQL INTEGER value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws IndexOutOfBoundsException If a parameter is invalid.
	 */
	public void setInt(int parameterIndex, long x) {
		// this is a bit kludgy - cast the long to an int.  problem for very big values.
		this.verifyParams(parameterIndex);
		this.paramTypes[parameterIndex - 1] = Types.INTEGER;
		this.params[parameterIndex - 1] = new Integer((int)x);
	}

	/**
	 * Sets the designated parameter to SQL NULL.
	 *
	 * <b>Note</b>: You must specify the parameter's SQL type.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param sqlType The SQL type code defined in java.sql.Types
	 * @throws IndexOutOfBoundsException If a parameter is invalid.
	 */
	public void setNull(int parameterIndex, int sqlType) {
		this.verifyParams(parameterIndex);
		this.paramTypes[parameterIndex - 1] = sqlType;
		this.params[parameterIndex - 1] = null;
	}

	/**
	 * Sets the designated parameter to the given Java String value. The driver
	 * converts this to an SQL VARCHAR or LONGVARCHAR value (depending on the
	 * argument's size relative to the driver's limits on VARCHAR values) when
	 * it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws IndexOutOfBoundsException If a parameter is invalid.
	 */
	public void setString(int parameterIndex, String x) {
		this.verifyParams(parameterIndex);
		this.paramTypes[parameterIndex - 1] = Types.VARCHAR;
		this.params[parameterIndex - 1] = x;
	}

	/**
	 * Sets the designated parameter to the given java.sql.Timestamp value. The
	 * driver converts this to an SQL TIMESTAMP value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws IndexOutOfBoundsException If a parameter is invalid.
	 */
	public void setTimestamp(int parameterIndex, Timestamp x) {
		this.verifyParams(parameterIndex);
		this.paramTypes[parameterIndex - 1] = Types.TIMESTAMP;
		this.params[parameterIndex - 1] = x;
	}

	/**
	 *
	 */
	private void verifyParams(int pos) {
		if (pos <= 0) {
			throw new IndexOutOfBoundsException("Invalid PreparedStatement index " + pos);
		}
	}
}
