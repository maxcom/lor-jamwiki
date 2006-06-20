/**
 *
 */
package org.jmwiki.persistency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Collection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.jmwiki.AbstractWikiMembers;
import org.jmwiki.Environment;
import org.jmwiki.WikiMember;

/**
 *
 */
public class DatabaseWikiMembers extends AbstractWikiMembers {

	private static final Logger logger = Logger.getLogger(DatabaseWikiMembers.class);
	private static final String PROPERTY_FILE_NAME = "/wikimembers_" + DatabaseHandler.getDatabaseType() + ".sql.properties";
	private static Properties statements;

	static {
		statements = Environment.loadProperties(PROPERTY_FILE_NAME);
	}

	/**
	 *
	 */
	public DatabaseWikiMembers(String virtualWiki) {
		if ("".equals(virtualWiki)) throw new RuntimeException("Virtual wiki == blank!");
		this.virtualWiki = virtualWiki;
	}

	/**
	 *
	 */
	public void addMember(String username, String email, String key) throws Exception {
		WikiMember aMember = createMember(username, email);
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_ADD_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, email);
			stmt.setString(3, aMember.getKey());
			stmt.setString(4, this.virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public boolean requestMembership(String username, String email, HttpServletRequest virtualWiki) throws Exception {
		WikiMember aMember = createMember(username, email);
		mailMember(username, virtualWiki, aMember, email);
		// remove existing member (might be a re-confirmation)
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_REMOVE_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, this.virtualWiki);
			stmt.execute();
			stmt.close();
			stmt = conn.prepareStatement(getStatement("STATEMENT_ADD_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, email);
			stmt.setString(3, aMember.getKey());
			stmt.setString(4, this.virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return true;
	}

	/**
	 *
	 */
	public boolean createMembershipWithoutRequest(String username, String email) throws Exception {
		WikiMember aMember = createMember(username, email);
		aMember.confirm();
		// remove existing member (might be a re-confirmation)
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_REMOVE_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, this.virtualWiki);
			stmt.execute();
			stmt.close();
			stmt = conn.prepareStatement(getStatement("STATEMENT_ADD_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, email);
			stmt.setString(3, aMember.getKey());
			stmt.setString(4, this.virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return true;
	}

	/**
	 *
	 */
	private void saveMember(WikiMember member) throws Exception {
		logger.info("Saving member: " + member);
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_UPDATE_MEMBER"));
			stmt.setString(1, member.getEmail().trim());
			stmt.setString(2, member.getKey().trim());
			stmt.setString(3, member.getUserName().trim());
			stmt.setString(4, this.virtualWiki);
			stmt.execute();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 *
	 */
	public boolean confirmMembership(String username, String key) throws Exception {
		boolean result = super.confirmMembership(username, key);
		if (result == true) return true;
		WikiMember member = findMemberByName(username);
		// Look up the username and check the keys
		if (!member.checkKey(key)) return false;
		member.confirm();
		saveMember(member);
		return true;
	}

	/**
	 *
	 */
	public boolean removeMember(String username) throws Exception {
		Connection conn = null;
		int n = 0;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_REMOVE_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, this.virtualWiki);
			n = stmt.executeUpdate();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return (n > 0);
	}

	/**
	 *
	 */
	public WikiMember findMemberByName(String username) throws Exception {
		Connection conn = null;
		WikiMember member = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_GET_MEMBER"));
			stmt.setString(1, username);
			stmt.setString(2, this.virtualWiki);
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				rs.close();
				stmt.close();
				return new WikiMember(username);
			}
			if (DatabaseHandler.isMySQL()) {
				member = new WikiMember(rs.getString("user"), rs.getString("email"));
			} else {
				member = new WikiMember(rs.getString("wikiuser"), rs.getString("email"));
			}
			if (DatabaseHandler.isOracle()) {
				// Oracle handles "" and NULL as the same value. Too bad that a confirmed
				// member is validated against "" and Oracle saves that as NULL and this
				// is simply not the same...
				String key = rs.getString("userkey");
				if (rs.wasNull()) {
					key = "";
				}
				member.setKey(key);
			} else {
				member.setKey(rs.getString("userkey"));
			}
			rs.close();
			stmt.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return member;
	}

	/**
	 *
	 */
	public Collection getAllMembers() throws Exception {
		Collection all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(getStatement("STATEMENT_ALL_MEMBERS"));
			stmt.setString(1, this.virtualWiki);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				WikiMember member;
				if (DatabaseHandler.isMySQL()) {
					member = new WikiMember(rs.getString("user"), rs.getString("email"));
				} else {
					member = new WikiMember(rs.getString("wikiuser"), rs.getString("email"));
				}
				member.setKey(rs.getString("userkey"));
				all.add(member);
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
	private String getStatement(String key) {
		String statement = statements.getProperty(key);
		logger.debug("statement for " + key + " = " + statement);
		return statement.trim();
	}
}
