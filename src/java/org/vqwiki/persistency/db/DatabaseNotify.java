/**
 * @author garethc
 *  22/10/2002 11:40:10
 */
package org.vqwiki.persistency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.vqwiki.AbstractNotify;
import org.vqwiki.Environment;

public class DatabaseNotify extends AbstractNotify {

	private static final Logger logger = Logger.getLogger(DatabaseNotify.class);
	private static final String PROPERTY_FILE_NAME = "/notify_" + DatabaseHandler.getDatabaseType() + ".sql.properties";
	private static Properties statements;

	static {
		statements = Environment.loadProperties(PROPERTY_FILE_NAME);
	}

	/**
	 *
	 */
	public DatabaseNotify(String virtualWiki, String topicName) {
		super();
		this.virtualWiki = virtualWiki;
		this.topicName = topicName;
	}

	/**
	 *
	 */
	public void addMember(String userName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_ADD_MEMBER"));
			stmt.setString(1, this.topicName);
			stmt.setString(2, userName);
			stmt.setString(3, this.virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public void removeMember(String userName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_REMOVE_MEMBER"));
			stmt.setString(1, this.topicName);
			stmt.setString(2, userName);
			stmt.setString(3, this.virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public boolean isMember(String userName) throws Exception {
		Connection conn = null;
		int count = 0;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_IS_MEMBER"));
			stmt.setString(1, this.topicName);
			stmt.setString(2, userName);
			stmt.setString(3, this.virtualWiki);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			count = rs.getInt(1);
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return count > 0;
	}

	/**
	 *
	 */
	public Collection getMembers() throws Exception {
		Collection all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_GET_MEMBERS"));
			stmt.setString(1, this.topicName);
			stmt.setString(2, this.virtualWiki);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String username;
				if (DatabaseHandler.isMySQL()) {
					username = rs.getString("user");
				} else {
					username = rs.getString("wikiuser");
				}
				all.add(username);
			}
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}

	/**
	 *
	 */
	public static Collection getAllNotifications(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_ALL_NOTIFICATIONS"));
			stmt.setString(1, virtualWiki);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				DatabaseNotify notify = new DatabaseNotify(rs.getString("virtualwiki"), rs.getString("topic"));
				all.add(notify);
			}
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}

	/**
	 *
	 */
	private static String getStatement(String key) {
		String statement = statements.getProperty(key);
		logger.debug("statement for " + key + " = " + statement);
		return statement.trim();
	}
}
