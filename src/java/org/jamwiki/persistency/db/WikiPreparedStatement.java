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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import org.apache.log4j.Logger;

/**
 * This class is a wrapper around the java.sql.PreparedStatement class, allowing a
 * statement to be prepared without requiring that a database connection be
 * held.  The main advantage of this approach is that all connection handling can
 * be done by low-level database functions, and the user can build and execute a
 * query without the need to ensure that the connection is properly closed after
 * the data is processed.
 */
public class WikiPreparedStatement {

	private static Logger logger = Logger.getLogger(WikiPreparedStatement.class.getName());
	private Object[] params = null;
	private int[] paramTypes = null;
	private String sql = null;
	private PreparedStatement statement = null;
	private int numElements = -1;

	/**
	 *
	 */
	public WikiPreparedStatement(String sql) {
		this.sql = sql;
		this.numElements = countElements(sql);
		this.params = new Object[numElements];
		this.paramTypes = new int[numElements];
	}

	/**
	 *
	 */
	private static int countElements(String sql) {
		int total = 0;
		int pos = 0;
		while ((pos = sql.indexOf("?", pos)) != -1) {
			total++;
			pos++;
		}
		return total;
	}

	/**
	 *
	 */
	public WikiResultSet executeQuery() throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			this.statement = conn.prepareStatement(this.sql);
			this.loadStatement();
			rs = this.statement.executeQuery();
			return new WikiResultSet(rs);
		} finally {
			DatabaseConnection.closeConnection(conn, this.statement, rs);
		}
	}

	/**
	 *
	 */
	public int executeUpdate() throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			this.statement = conn.prepareStatement(this.sql);
			this.loadStatement();
			return this.statement.executeUpdate();
		} finally {
			DatabaseConnection.closeConnection(conn, this.statement);
		}
	}

	/**
	 *
	 */
	private void loadStatement() throws SQLException {
		for (int i=0; i < this.paramTypes.length; i++) {
			if (params[i] == null) {
				this.statement.setNull(i+1, paramTypes[i]);
			} else if (paramTypes[i] == Types.BOOLEAN) {
				boolean value = ((Boolean)params[i]).booleanValue();
				this.statement.setBoolean(i+1, value);
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
	 * Sets the designated parameter to the given Java boolean value. The
	 * driver converts this to an SQL BIT value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws SQLException If a parameter is invalid.
	 */
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		this.verifyParams(parameterIndex, new Boolean(x));
		this.paramTypes[parameterIndex - 1] = Types.BOOLEAN;
		this.params[parameterIndex - 1] = new Boolean(x);
	}

	/**
	 * Sets the designated parameter to the given Java int value. The driver
	 * converts this to an SQL INTEGER value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws SQLException If a parameter is invalid.
	 */
	public void setInt(int parameterIndex, int x) throws SQLException {
		this.verifyParams(parameterIndex, new Integer(x));
		this.paramTypes[parameterIndex - 1] = Types.INTEGER;
		this.params[parameterIndex - 1] = new Integer(x);
	}

	/**
	 * Sets the designated parameter to SQL NULL.
	 *
	 * <b>Note</b>: You must specify the parameter's SQL type.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param sqlType The SQL type code defined in java.sql.Types
	 * @throws SQLException If a parameter is invalid.
	 */
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		this.verifyParams(parameterIndex, null);
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
	 * @throws SQLException If a parameter is invalid.
	 */
	public void setString(int parameterIndex, String x) throws SQLException {
		this.verifyParams(parameterIndex, x);
		this.paramTypes[parameterIndex - 1] = Types.VARCHAR;
		this.params[parameterIndex - 1] = x;
	}

	/**
	 * Sets the designated parameter to the given java.sql.Timestamp value. The
	 * driver converts this to an SQL TIMESTAMP value when it sends it to the database.
	 *
	 * @param parameterIndex The first parameter is 1, the second is 2, ...
	 * @param x The parameter value.
	 * @throws SQLException If a parameter is invalid.
	 */
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		this.verifyParams(parameterIndex, x);
		this.paramTypes[parameterIndex - 1] = Types.TIMESTAMP;
		this.params[parameterIndex - 1] = x;
	}

	/**
	 *
	 */
	private void verifyParams(int pos, Object value) throws SQLException {
		if (pos <= 0) {
			throw new SQLException("Invalid PreparedStatement index " + pos);
		}
	}
}
