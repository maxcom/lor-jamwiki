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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.LinkedHashMap;
import org.jamwiki.utils.WikiLogger;

/**
 * This class is a wrapper around the java.sql.ResultSet class, allowing the data
 * from a SQL query to be accessed without requiring that a database connection be
 * held.  The main advantage of this approach is that all connection handling can
 * be done by low-level database functions, and the user can process a result set
 * without the need to ensure that the connection is properly closed after the
 * data is processed.
 */
public class WikiResultSet {

	private static WikiLogger logger = WikiLogger.getLogger(WikiResultSet.class.getName());
	private Vector rows = new Vector();
	private int rowPointer = -1;
	private int totalRows = -1;
	private LinkedHashMap currentRow = null;

	/**
	 * Constructor used primarily for building new result sets.  Use this
	 * constructor to create an empty result set, then fill it using the
	 * {@link WikiResultSet#addRow addRow()} method.
	 */
	public WikiResultSet() {
	}

	/**
	 * Create a WikiResultSet from a standard ResultSet.
	 *
	 * @see ResultSet
	 * @param rs The ResultSet used to populate this WikiResultSet.
	 */
	protected WikiResultSet(ResultSet rs) throws Exception {
		ResultSetMetaData rsmd = rs.getMetaData();
		int size = rsmd.getColumnCount();
		int type;
		while (rs.next()) {
			LinkedHashMap column = new LinkedHashMap();
			for (int i=1; i <= size; i++) {
				String columnName = rsmd.getColumnName(i);
				type = rsmd.getColumnType(i);
				switch (type) {
				case java.sql.Types.VARCHAR:
				case java.sql.Types.CLOB:
					String varchar = rs.getString(columnName);
					column.put(columnName.toLowerCase(), varchar);
					break;
				case java.sql.Types.INTEGER:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.TINYINT:
					int integer = rs.getInt(columnName);
					column.put(columnName.toLowerCase(), new Integer(integer));
					break;
				case java.sql.Types.BIGINT:
					long longint = rs.getLong(columnName);
					column.put(columnName.toLowerCase(), new Long(longint));
					break;
				case java.sql.Types.DATE:
					Date date = rs.getDate(columnName);
					column.put(columnName.toLowerCase(), date);
					break;
				case java.sql.Types.TIMESTAMP:
					Timestamp timestamp = rs.getTimestamp(columnName);
					column.put(columnName.toLowerCase(), timestamp);
					break;
				case java.sql.Types.CHAR:
					String value = rs.getString(columnName);
					char character = '0';
					if (value != null && value.length() > 0) character = value.charAt(0);
					column.put(columnName.toLowerCase(), new Character(character));
					break;
				default:
					Object object = rs.getObject(columnName);
					column.put(columnName.toLowerCase(), object);
				}
			}
			this.rows.add(column);
		}
		this.totalRows = this.rows.size();
	}

	/**
	 * <p>Moves the cursor to the given row number in this ResultSet object.</p>
	 *
	 * <p>If the row number is positive, the cursor moves to the given row number
	 * with respect to the beginning of the result set. The first row is row 1, the
	 * second is row 2, and so on.</p>
	 *
	 * <p>If the given row number is negative, the cursor moves to an absolute row
	 * position with respect to the end of the result set. For example, calling the
	 * method absolute(-1) positions the cursor on the last row; calling the method
	 * absolute(-2) moves the cursor to the next-to-last row, and so on.</p>
	 *
	 * <p>An attempt to position the cursor beyond the first/last row in the result
	 * set leaves the cursor before the first row or after the last row.</p>
	 *
	 * <p>This method duplicates the functionality of the
	 * {@link java.sql.ResultSet#absolute ResultSet.absolute(int row)}
	 * method.</p>
	 *
	 * @param row The number of the row to which the cursor should move. A positive
	 *  number indicates the row number counting from the beginning of the result
	 *  set; a negative number indicates the row number counting from the end of
	 *  the result set.
	 * @return <code>true</code> if the cursor is on the result set; <code>false</code> otherwise.
	 */
	public boolean absolute(int row) {
		// row starts at 1, rowPointer starts at 0
		if (row > 0) {
			this.rowPointer = (row - 1);
		} else {
			this.rowPointer = (this.totalRows + row);
		}
		if (this.rowPointer < 0) {
			this.rowPointer = -1;
			return false;
		} else if (this.rowPointer >= this.totalRows) {
			this.rowPointer = this.totalRows;
			return false;
		}
		return true;
	}

	/**
	 * Method used to create a new WikiResultSet by copying rows of other
	 * WikiResultSet objects.
	 *
	 * @param rs The SQLResult that is being copied.  Only the current row will
	 *  be copied into the new WikiResultSet object.
	 * @throws Exception Thrown if the row pointer of the WikiResultSet being
	 *  copied has passed the end of the WikiResultSet.
	 */
	public void addRow(WikiResultSet rs) throws Exception {
		if (rs.rowPointer == -1) rs.rowPointer++;
		if (rs.rowPointer >= rs.totalRows) {
			throw new Exception("Attempt to access beyond final row of WikiResultSet");
		}
		this.rows.add(rs.rows.elementAt(rs.rowPointer));
		this.totalRows = this.rows.size();
	}

	/**
	 * <p>Moves the cursor to the front of this <code>WikiResultSet</code> object, just
	 * before the first row. This method has no effect if the result set contains no
	 * rows.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#beforeFirst ResultSet.beforeFirst()} method.</p>
	 */
	public void beforeFirst() {
		this.rowPointer = -1;
	}

	/**
	 * <p>Moves the cursor to the first row in this <code>WikiResultSet</code> object.</p>
	 *
	 * <p>This method duplicates the {@link java.sql.ResultSet#first ResultSet.first()} method.</p>
	 *
	 * @return <code>true</code> if the cursor is on a valid row; <code>false</code>
	 *  if there are no rows in the result set.
	 */
	public boolean first() {
		this.rowPointer = 0;
		return (this.totalRows > 0);
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as a <code>java.sql.Date</code> object in
	 * the Java programming language.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#getDate ResultSet.getDate(String columnName)} method.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>null</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public Date getDate(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		return (Date)this.currentRow.get(columnName.toLowerCase());
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as a <code>char</code> value in
	 * the Java programming language.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>0</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public char getChar(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		Character value = null;
		try {
			value = (Character)this.currentRow.get(columnName.toLowerCase());
		} catch (Exception e) {
			// ignore, probably null
		}
		return (value == null) ? '0' : value.charValue();
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as an <code>int</code> in the Java
	 * programming language.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#getInt ResultSet.getInt(String columnName)} method.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>0</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public int getInt(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		Integer value = null;
		try {
			value = (Integer)this.currentRow.get(columnName.toLowerCase());
		} catch (Exception e) {
			// is it a long?
			try {
				value = new Integer(((Long)this.currentRow.get(columnName.toLowerCase())).intValue());
			} catch (Exception ex) {}
		}
		return (value == null) ? 0 : value.intValue();
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as a <code>long</code> in the Java
	 * programming language.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#getLong ResultSet.getLong(String columnName)} method.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>0</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public long getLong(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		Long value = (Long)this.currentRow.get(columnName.toLowerCase());
		return (value == null) ? 0 : value.longValue();
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as a <code>java.lang.Object</code> object in
	 * the Java programming language.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#getObject ResultSet.getObject(String columnName)} method.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>null</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public Object getObject(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		return this.currentRow.get(columnName.toLowerCase());
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as a <code>java.lang.String</code> object in
	 * the Java programming language.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#getString ResultSet.getString(String columnName)} method.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>null</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public String getString(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		return (String)this.currentRow.get(columnName.toLowerCase());
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row of this
	 * <code>WikiResultSet</code> object as a <code>java.lang.Timestamp</code> object in
	 * the Java programming language.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#getTimestamp ResultSet.getTimestamp(String columnName)} method.</p>
	 *
	 * @param columnName The SQL name of the column.
	 * @return The column value; if the value is SQL <code>NULL</code>, the value
	 *  returned is <code>null</code>.
	 * @throws SQLException If the cursor position is invalid or if the column name does
	 *  not exist in the result set.
	 */
	public Timestamp getTimestamp(String columnName) throws SQLException {
		this.verifyColumn(columnName);
		return (Timestamp)this.currentRow.get(columnName.toLowerCase());
	}

	/**
	 * <p>Moves the cursor to the last row in this ResultSet object.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#last ResultSet.last()} method.</p>
	 *
	 * @return <code>true</code> if the cursor is on a valid row; <code>false</code>
	 *  if there are no rows in the result set.
	 */
	public boolean last() {
		if (this.totalRows > 0) {
			this.rowPointer = (this.totalRows - 1);
			return true;
		}
		return false;
	}

	/**
	 * <p>Moves the cursor down one row from its current position. A
	 * <code>ResultSet</code> cursor is initially positioned before the
	 * first row; the first call to the method <code>next</code> makes the
	 * first row the current row; the second call makes the second row the
	 * current row, and so on.</p>
	 *
	 * <p>This method duplicates the
	 * {@link java.sql.ResultSet#next ResultSet.next()} method.</p>
	 *
	 * @return <code>true</code> if the new current row is valid; <code>false</code>
	 *  if there are no more rows.
	 */
	public boolean next() {
		this.rowPointer++;
		return (this.rowPointer < this.totalRows);
	}

	/**
	 * <p>Return the total number of rows that exist in this result set.</p>
	 *
	 * @return The total number of rows that exist in this result set.
	 */
	public int size() {
		return this.totalRows;
	}

	/**
	 * Utility method used when calling any of the <code>get</code> methods
	 * in this class.
	 */
	private void verifyColumn(String columnName) throws SQLException {
		if (this.rowPointer == -1) this.rowPointer++;
		if (this.rowPointer >= this.totalRows) {
			throw new SQLException("Attempt to access beyond last row of result set");
		}
		this.currentRow = (LinkedHashMap)this.rows.elementAt(this.rowPointer);
		if (columnName == null || !this.currentRow.containsKey(columnName)) {
			throw new SQLException("Invalid column name " + columnName);
		}
	}
}
