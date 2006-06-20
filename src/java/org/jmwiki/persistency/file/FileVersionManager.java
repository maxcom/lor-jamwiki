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
import org.jmwiki.VersionManager;
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
	public synchronized String lookupLastRevision(String virtualWiki, String topicName) throws Exception {
		return (String) lookupRevision(virtualWiki, topicName, 0);
	}

	/**
	 * Revision 0 is the most recent revision
	 */
	public synchronized Object lookupRevision(String virtualWiki, String topicName, int version) throws Exception {
		logger.debug("Looking up revision " + version + " for " + virtualWiki + "/" + topicName);
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR);
		String fileName = Utilities.encodeSafeFileName(topicName + FileHandler.EXT);
		String[] files = file.list(new FileStartFilter(fileName));
		if (files == null) return null;
		if (files.length >= (1 + version)) {
			Arrays.sort(files);
			if (logger.isDebugEnabled()) {
				for (int i = 0; i < files.length; i++) {
					logger.debug("File " + i + " is " + files[i]);
				}
			}
			logger.debug("Returning looked-up file: " + files[version]);
			return files[files.length - 1 - version];
		}
		logger.debug("No version for revision " + version);
		return null;
	}

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int revision1, int revision2, boolean useHtml) throws Exception {
		logger.debug("Diff for version " + revision1 + " against version " + revision2 + " of topic " + topicName);
		String revision1Name = (String) lookupRevision(virtualWiki, topicName, revision1);
		String revision2Name = (String) lookupRevision(virtualWiki, topicName, revision2);
		StringBuffer fileName = new StringBuffer();
		fileName.append(FileHandler.VERSION_DIR);
		fileName.append(Utilities.sep());
		fileName.append(revision1Name);
		logger.debug("Finding path for " + fileName);
		String fileName1 = FileHandler.getPathFor(virtualWiki, Utilities.decodeSafeFileName(fileName.toString())).getPath();
		fileName = new StringBuffer();
		fileName.append(FileHandler.VERSION_DIR);
		fileName.append(Utilities.sep());
		fileName.append(revision2Name);
		logger.debug("Finding path for " + fileName);
		String fileName2 = FileHandler.getPathFor(virtualWiki, Utilities.decodeSafeFileName(fileName.toString())).getPath();
		logger.debug("Diffing: " + fileName1 + " against " + fileName2);
		FileHandler handler = new FileHandler();
		String contents1 = (handler.read(new File(fileName1))).toString();
		String contents2 = (handler.read(new File(fileName2))).toString();
		return DiffUtil.diff(contents1, contents2, useHtml);
	}

	/**
	 *
	 */
	public Date lastRevisionDate(String virtualWiki, String topicName) throws Exception {
		String revision = this.lookupLastRevision(virtualWiki, topicName);
		if (revision == null) return null;
		return Utilities.convertFileFriendlyDate(revision);
	}

	/**
	 * Returns all versions of the given topic in reverse chronological order
	 * @param virtualWiki
	 * @param topicName
	 * @return
	 * @throws Exception
	 */
	public List getAllVersions(String virtualWiki, String topicName) throws Exception {
		List all = new LinkedList();
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR);
		String fileName = Utilities.encodeSafeFileName(topicName + FileHandler.EXT);
		String[] files = file.list(new FileStartFilter(fileName));
		if (files == null) return all;
		Arrays.sort(
			files,
			new Comparator() {
				public int compare(Object o1, Object o2) {
					String one = (String) o1;
					String two = (String) o2;
					return two.compareTo(one);
				}
			}
		);
		for (int i = 0; i < files.length; i++) {
			String currentFile = files[i];
			TopicVersion version = new TopicVersion(
				virtualWiki,
				topicName,
				new DBDate(Utilities.convertFileFriendlyDate(currentFile)),
				i
			);
			all.add(version);
		}
		return all;
	}

	/**
	 *
	 */
	public TopicVersion getTopicVersion(String context, String virtualWiki, String topicName, int versionNumber) throws Exception {
		List allVersions = getAllVersions(virtualWiki, topicName);
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
		String fileName = (String) lookupRevision(virtualWiki, topicName, versionNumber);
		logger.debug("Getting file " + fileName);
		FileHandler fileHandler = (FileHandler) WikiBase.getInstance().getHandler();
		File file = new File(
			FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR),
			fileName
		);
		return fileHandler.read(file).toString();
	}

	/**
	 *
	 */
	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR);
		String fileName = Utilities.encodeSafeFileName(topicName + FileHandler.EXT);
		String[] files = file.list(new FileStartFilter(fileName));
		if (files != null) return files.length;
		return -1;
	}

	/**
	 *
	 */
	public void addVersion(String virtualWiki, String topicName, String contents, Date at) throws Exception {
		File versionDir = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR);
		StringBuffer buffer = new StringBuffer();
		buffer.append(topicName);
		buffer.append(FileHandler.EXT);
		buffer.append(Utilities.fileFriendlyDate(at));
		File versionFile = new File(versionDir, buffer.toString());
		Writer writer = new OutputStreamWriter(new FileOutputStream(versionFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
		writer.write(contents);
		writer.close();
	}
}
