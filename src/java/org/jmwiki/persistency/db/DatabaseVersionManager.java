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
	public String diff(String virtualWiki, String topicName, int topicVersionId1, int topicVersionId2, boolean useHtml) throws Exception {
		TopicVersion version1 = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId1);
		TopicVersion version2 = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId2);
		String contents1 = version1.getVersionContent();
		String contents2 = version2.getVersionContent();
		if (contents1 == null && contents2 == null) {
			logger.error("No versions found for " + topicVersionId1 + " against " + topicVersionId2);
			return "";
		}
		return DiffUtil.diff(contents1, contents2, useHtml);
	}

	/**
	 *
	 */
	public Date lastRevisionDate(String virtualWiki, String topicName) throws Exception {
		TopicVersion version = WikiBase.getInstance().getHandler().lookupLastTopicVersion(virtualWiki, topicName);
		return version.getEditDate();
	}

	/**
	 *
	 */
	public TopicVersion getTopicVersion(String context, String virtualWiki, String topicName, int topicVersionId) throws Exception {
		TopicVersion version = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId);
		String cookedContents = WikiBase.getInstance().cook(
			context,
			virtualWiki,
			new BufferedReader(new StringReader(
				WikiBase.getInstance().getVersionManagerInstance().getVersionContents(
					virtualWiki,
					topicName,
					topicVersionId
				)
			))
		);
		version.setCookedContents(cookedContents);
		return version;
	}

	/**
	 *
	 */
	public String getVersionContents(String virtualWiki, String topicName, int topicVersionId) throws Exception {
		TopicVersion version = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId);
		return version.getVersionContent();
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
