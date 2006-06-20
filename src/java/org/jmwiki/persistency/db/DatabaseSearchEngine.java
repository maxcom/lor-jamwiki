/**
 *
 */
package org.jmwiki.persistency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import org.jmwiki.AbstractSearchEngine;
import org.jmwiki.WikiBase;

/**
 *
 */
public class DatabaseSearchEngine extends AbstractSearchEngine {

	/** SQL-Statement to get a name */
	protected static final String STATEMENT_ALL_NAMES = "SELECT name FROM Topic WHERE virtualwiki = ? ORDER BY name";

	/** An instance to myself (Singleton pattern) */
	protected static DatabaseSearchEngine instance;

	/**
	 * Creates a new DatabaseSearchEngine object.
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	private DatabaseSearchEngine() throws Exception {
	}

	/**
	 * Get the one and only existing instance of this class
	 *
	 * @return An Instance to this class
	 *
	 * @throws Exception If something really goes wrong
	 */
	public static synchronized DatabaseSearchEngine getInstance() throws Exception {
		if (instance == null) {
			instance = new DatabaseSearchEngine();
			instance.initSearchEngine();
			instance.refreshIndex();
		}
		return instance;
	}

	/**
	 * Get a collection of all topic names.
	 *
	 * @param virtualWiki The virtual wiki, for all topic names
	 *
	 * @return A Collection of String, containing all topic names for this virtual wiki
	 *
	 * @throws Exception If something goes wrong
	 */
	public Collection getAllTopicNames(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		if (virtualWiki == null || virtualWiki.length() == 0) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement statement = conn.prepareStatement(STATEMENT_ALL_NAMES);
			statement.setString(1, virtualWiki);
			ResultSet rs = statement.executeQuery();
			for (; rs.next();) {
				all.add(rs.getString("name"));
			}
			rs.close();
			statement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}

	/**
	 * Get the filename of a topic file.
	 * @see jmwiki.AbstractSearchEngine#getFilename(java.lang.String, java.lang.String)
	 */
	protected String getFilename(String currentWiki, String topic) {
		return null;
	}

}
