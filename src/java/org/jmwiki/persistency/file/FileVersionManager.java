package org.jmwiki.persistency.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.persistency.VersionManager;
import org.jmwiki.WikiBase;
import org.jmwiki.model.TopicVersion;
import org.jmwiki.persistency.db.DBDate;
import org.jmwiki.utils.DiffUtil;
import org.jmwiki.utils.Utilities;

/**
 *
 */
public class FileVersionManager implements VersionManager {

	private static final Logger logger = Logger.getLogger(FileVersionManager.class);
	private static VersionManager instance;

	/**
	 *
	 */
	private FileVersionManager() throws Exception {
	}

	/**
	 *
	 */
	public static VersionManager getInstance() throws Exception {
		if (instance == null) instance = new FileVersionManager();
		return instance;
	}

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int topicVersionId1, int topicVersionId2, boolean useHtml) throws Exception {
		TopicVersion version1 = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId1);
		TopicVersion version2 = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId2);
		String contents1 = version1.getVersionContent();
		String contents2 = version2.getVersionContent();
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
}
