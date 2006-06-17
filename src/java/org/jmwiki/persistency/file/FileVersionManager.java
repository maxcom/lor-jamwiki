package org.jmwiki.persistency.file;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.jmwiki.*;
import org.jmwiki.persistency.TopicVersion;
import org.jmwiki.persistency.db.DBDate;
import org.jmwiki.utils.DiffUtil;
import org.jmwiki.utils.Utilities;

/**
 * Java MediaWiki - WikiWikiWeb clone
 * Copyright (C) 2001-2002 Gareth Cronin
 *
 * FileVersionManager is the JMWiki native file system implementation of
 * the version manager for looking after the version trail that is used
 * in diffs etc.
 *
 *This program is free software; you can redistribute it and/or modify
 *it under the terms of the latest version of the GNU Lesser General
 *Public License as published by the Free Software Foundation;
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program (gpl.txt); if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
	public TopicVersion getTopicVersion(String virtualWiki, String topicName, int versionNumber) throws Exception {
		List allVersions = getAllVersions(virtualWiki, topicName);
		return (TopicVersion) allVersions.get(versionNumber);
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
