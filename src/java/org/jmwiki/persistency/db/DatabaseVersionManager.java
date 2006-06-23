/**
 *
 */
package org.jmwiki.persistency.db;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;
import org.jmwiki.persistency.VersionManager;
import org.jmwiki.model.TopicVersion;
import org.jmwiki.utils.DiffUtil;

/**
 *
 */
public class DatabaseVersionManager implements VersionManager {

	protected final static String STATEMENT_VERSION_FIND =
		"SELECT * FROM TopicVersion WHERE name = ? AND virtualwiki = ? ORDER BY versionat DESC";
	protected final static String STATEMENT_VERSION_FIND_ONE =
		"SELECT * FROM TopicVersion WHERE name = ?  AND virtualwiki = ? AND versionAt = ?";
	protected final static String STATEMENT_COUNT_VERSIONS =
		"SELECT COUNT(*) FROM TopicVersion WHERE name = ?  AND virtualwiki = ?";

	protected static DatabaseVersionManager instance;
	private static final Logger logger = Logger.getLogger(DatabaseVersionManager.class);

	/**
	 *
	 */
	private DatabaseVersionManager() throws Exception {
	}

	/**
	 *
	 */
	public static DatabaseVersionManager getInstance() throws Exception {
		if (instance == null) instance = new DatabaseVersionManager();
		return instance;
	}

	/**
	 *
	 */
	public Object lookupLastRevision(String virtualWiki, String topicName) throws Exception {
		// improvement would be: find max "versionat" for revisions
		// return the date as a string
		return lookupRevision(virtualWiki, topicName, 0);
	}

	/**
	 *
	 */
	public Object lookupRevision(String virtualWiki, String topicName, int version) throws Exception {
		if (version < 0) throw new Exception("version # must be >= 0");
		Connection conn = null;
		Timestamp stamp = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement versionFindStatement = conn.prepareStatement(STATEMENT_VERSION_FIND);
			versionFindStatement.setString(1, topicName);
			versionFindStatement.setString(2, virtualWiki);
			ResultSet rs = versionFindStatement.executeQuery();
			for (int i = 0; i <= version; i++) {
				if (!rs.next()) {
					rs.close();
					versionFindStatement.close();
					return null;
				}
			}
			stamp = rs.getTimestamp("versionat");
			logger.debug("Revision #" + version + " @" + stamp);
			rs.close();
			versionFindStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return stamp;
	}

	/**
	 *
	 */
	public String revisionContents(String virtualWiki, String topicName, Timestamp date) throws Exception {
		Connection conn = null;
		String contents;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement versionFindStatementOne = conn.prepareStatement(STATEMENT_VERSION_FIND_ONE);
			versionFindStatementOne.setString(1, topicName);
			versionFindStatementOne.setString(2, virtualWiki);
			versionFindStatementOne.setTimestamp(3, date);
			ResultSet rs = versionFindStatementOne.executeQuery();
			if (!rs.next()) {
				rs.close();
				versionFindStatementOne.close();
				return null;
			}
			if (DatabaseHandler.isOracle()) {
				contents = OracleClobHelper.getClobValue(rs.getClob("contents"));
			} else {
				contents = rs.getString("contents");
			}
			logger.debug("Contents @" + date + ": " + contents);
			rs.close();
			versionFindStatementOne.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return contents;
	}

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int revision1, int revision2, boolean useHtml) throws Exception {
		logger.debug("Diff for version " + revision1 + " against version " + revision2 + ", " + virtualWiki + "/" + topicName);
		String contents1 = revisionContents(virtualWiki, topicName, (Timestamp) lookupRevision(virtualWiki, topicName, revision1));
		String contents2 = revisionContents(virtualWiki, topicName, (Timestamp) lookupRevision(virtualWiki, topicName, revision2));
		if (contents1 == null && contents2 == null) {
			logger.error("No versions found for " + revision1 + " against " + revision2);
			return "";
		}
		return DiffUtil.diff(contents1, contents2, useHtml);
	}

	/**
	 *
	 */
	public Date lastRevisionDate(String virtualWiki, String topicName) throws Exception {
		Timestamp stamp = (Timestamp) lookupLastRevision(virtualWiki, topicName);
		if (stamp == null) return null;
		return new Date(stamp.getTime());
	}

	/**
	 *
	 */
	public TopicVersion getTopicVersion(String context, String virtualWiki, String topicName, int versionNumber) throws Exception {
		List allVersions = WikiBase.getInstance().getHandler().getAllVersions(virtualWiki, topicName);
		TopicVersion version = (TopicVersion) allVersions.get(versionNumber);
		WikiBase instance = WikiBase.getInstance();
		String cookedContents = instance.cook(
			context,
			virtualWiki,
			new BufferedReader(new StringReader(
				instance.getVersionManagerInstance().getVersionContents(
					virtualWiki,
					topicName,
					versionNumber
				)
			))
		);
		version.setCookedContents(cookedContents);
		return version;
	}

	/**
	 *
	 */
	public String getVersionContents(String virtualWiki, String topicName, int versionNumber) throws Exception {
		Timestamp stamp = (Timestamp) lookupRevision(virtualWiki, topicName, versionNumber);
		return revisionContents(virtualWiki, topicName, stamp);
	}

	/**
	 *
	 */
	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement getAllStatement = conn.prepareStatement(STATEMENT_COUNT_VERSIONS);
			getAllStatement.setString(1, topicName);
			getAllStatement.setString(2, virtualWiki);
			ResultSet rs = getAllStatement.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				rs.close();
				getAllStatement.close();
				return count;
			}
			rs.close();
			getAllStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return -1;
	}
}
