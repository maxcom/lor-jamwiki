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
package org.jamwiki.persistency.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.TopicLock;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.persistency.db.DBDate;
import org.jamwiki.servlets.JAMController;
import org.jamwiki.utils.DiffUtil;
import org.jamwiki.utils.TextFileFilter;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class FileHandler implements PersistencyHandler {

	private static final Logger logger = Logger.getLogger(FileHandler.class);

	public static final String VERSION_DIR = "versions";
	public final static String EXT = ".txt";
	// the read-only topics
	protected Map readOnlyTopics;
	// file used for storing read-only topics
	private final static String READ_ONLY_FILE = "ReadOnlyTopics";
	public static final String VIRTUAL_WIKI_LIST = "virtualwikis.lst";
	private File file;
	private static final String LOCK_EXTENSION = ".lock";
	private static final String TOPIC_VERSION_ID_FILE = "topic_version.id";
	private static int NEXT_TOPIC_VERSION_ID = -1;

	/**
	 *
	 */
	public FileHandler() {
		this.readOnlyTopics = new HashMap();
		createDefaults(Locale.ENGLISH);
	}

	/**
	 *
	 */
	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		Collection roTopics = (Collection) this.readOnlyTopics.get(virtualWiki);
		roTopics.add(topicName);
		this.saveReadOnlyTopics(virtualWiki);
	}

	/**
	 *
	 */
	public void addTopicVersion(String virtualWiki, String topicName, String contents, Date at, String ipAddress) throws Exception {
		int topicVersionId = nextTopicVersionId();
		addTopicVersion(virtualWiki, topicName, contents, at, ipAddress, topicVersionId);
	}

	/**
	 *
	 */
	public void addTopicVersion(String virtualWiki, String topicName, String contents, Date at, String ipAddress, int topicVersionId) throws Exception {
		if (topicVersionId > NEXT_TOPIC_VERSION_ID) {
			NEXT_TOPIC_VERSION_ID = topicVersionId;
		}
		String filename = topicVersionFilename(topicVersionId);
		File versionFile = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR, topicName, filename);
		Writer writer = new OutputStreamWriter(new FileOutputStream(versionFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
		writer.write(contents);
		writer.close();
	}

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		File file = getPathFor("", null, VIRTUAL_WIKI_LIST);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
		while (true) {
			String line = in.readLine();
			if (line == null) break;
			all.add(line);
		}
		in.close();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			String s = (String) iterator.next();
			writer.println(s);
		}
		writer.println(virtualWiki);
		writer.close();
	}

	/**
	 * Set up the file system and default topics if necessary
	 */
	public void createDefaults(Locale locale) {
		// create wiki home if necessary
		File dirCheck = new File(fileBase(""));
		dirCheck.mkdir();
		// create default virtual wiki versions directory if necessary
		File versionDirCheck = getPathFor(null, null, VERSION_DIR);
		// create the virtual wiki list file if necessary
		File virtualList = getPathFor(null, null, VIRTUAL_WIKI_LIST);
		// get the virtual wiki list and set up the file system
		try {
			if (!virtualList.exists()) {
				createVirtualWikiList(virtualList);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(virtualList), Environment.getValue(Environment.PROP_FILE_ENCODING)));
			boolean lastOne = false;
			while (true) {
				String vWiki = in.readLine();
				if (vWiki == null) {
					if (lastOne) {
						break;
					} else {
						// default Wiki (no sub-directory)
						vWiki = "";
						lastOne = true;
					}
				}
				logger.debug("Creating defaults for " + vWiki);
				File dummy;
				// create the directories for the virtual wiki
				dummy = getPathFor(vWiki, null, "");
				dummy = getPathFor(vWiki, null, VERSION_DIR);
				// write out default topics
				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.startingpoints", locale));
				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.leftMenu", locale));
				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.topArea", locale));
				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.bottomArea", locale));
				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.stylesheet", locale));
				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.adminonlytopics", locale));
				loadReadOnlyTopics(vWiki);
			}
			in.close();
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	/**
	 *
	 */
	private void createVirtualWikiList(File virtualList) throws IOException {
		PrintWriter writer = getNewPrintWriter(virtualList, true);
		writer.println(WikiBase.DEFAULT_VWIKI);
		writer.close();
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
	public boolean exists(String virtualWiki, String topicName) throws Exception {
		String filename = topicFilename(topicName);
		File checkFile = getPathFor(virtualWiki, null, filename);
		return checkFile.exists();
	}

	/**
	 *
	 */
	public static String fileBase(String virtualWiki) {
		return Environment.getValue(Environment.PROP_FILE_HOME_DIR) + Utilities.sep() + virtualWiki;
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
		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
		for (int i = 0; i < files.length; i++) {
			TopicVersion version = initTopicVersion(files[i]);
			all.add(version);
		}
		return all;
	}

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception {
		if (virtualWiki == null) virtualWiki = "";
		List all = new ArrayList();
		File path = getPathFor(virtualWiki, null, "");
		File[] files = path.listFiles(new FileExtensionFilter(LOCK_EXTENSION));
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String fileName = file.getName();
			logger.debug("filename: " + fileName);
			String topicName = fileName.substring(0, fileName.indexOf("."));
			DBDate lockedAt = new DBDate(new Date(file.lastModified()));
			all.add(new TopicLock(
				virtualWiki, topicName, lockedAt, readLockFileKey(file)
			));
		}
		return all;
	}

	/**
	 *  returns a printwriter using utf-8 encoding
	 *
	 */
	private PrintWriter getNewPrintWriter(File file, boolean autoflush) throws IOException {
		return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)), autoflush);
	}

	/**
	 *
	 */
	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception {
		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
		return (files != null) ? files.length : -1;
	}

	/**
	 *
	 */
	public static File getPathFor(String virtualWiki, String dir, String fileName) {
		return getPathFor(virtualWiki, dir, null, fileName);
	}

	/**
	 *
	 */
	public static File getPathFor(String virtualWiki, String dir1, String dir2, String fileName) {
		StringBuffer buffer = new StringBuffer();
		if (virtualWiki == null || virtualWiki.equals(WikiBase.DEFAULT_VWIKI)) {
			virtualWiki = "";
		}
		buffer.append(fileBase(virtualWiki));
		buffer.append(File.separator);
		if (dir1 != null) {
			buffer.append(Utilities.encodeSafeFileName(dir1));
			buffer.append(File.separator);
		}
		if (dir2 != null) {
			buffer.append(Utilities.encodeSafeFileName(dir2));
			buffer.append(File.separator);
		}
		File directory = new File(buffer.toString());
		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (fileName != null) {
			buffer.append(Utilities.encodeSafeFileName(fileName));
		}
		return new File(buffer.toString());
	}

	/**
	 * Return a list of all read-only topics
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		logger.debug("Returning read only topics for " + virtualWiki);
		return (Collection) this.readOnlyTopics.get(virtualWiki);
	}

	/**
	 *
	 */
	public TopicVersion getTopicVersion(String context, String virtualWiki, String topicName, int topicVersionId) throws Exception {
		TopicVersion version = WikiBase.getInstance().getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId);
		String cookedContents = WikiBase.getInstance().cook(
			context,
			virtualWiki,
			new BufferedReader(new StringReader(version.getVersionContent()))
		);
		version.setCookedContents(cookedContents);
		return version;
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		Collection all = new ArrayList();
		File file = getPathFor("", null, VIRTUAL_WIKI_LIST);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
		while (true) {
			String line = in.readLine();
			if (line == null) break;
			all.add(line);
		}
		in.close();
		if (!all.contains(WikiBase.DEFAULT_VWIKI)) {
			all.add(WikiBase.DEFAULT_VWIKI);
		}
		return all;
	}

	/**
	 * Checks if lock exists
	 */
	public synchronized boolean holdsLock(String virtualWiki, String topicName, String key) throws IOException {
		File lockFile = makeLockFile(virtualWiki, topicName);
		if (lockFile.exists()) {
			String lockKey = readLockFileKey(lockFile);
			// key is guaranteed non-null, but lockKey might not be (backwards compatibility), so compare this way
			if (key.equals(lockKey)) return true;
		} else {
			// File is not locked, so user can save content without problems.. lock it now and return outcome.
			return lockTopic(virtualWiki, topicName, key);
		}
		return false;
	}

	/**
	 *
	 */
	protected static Topic initTopic(String virtualWiki, File file) {
		try {
			// FIXME - clean this up.
			// get topic name
			if (!file.exists() || file.getName() == null) {
				return null;
			}
			int pos = file.getName().lastIndexOf(EXT);
			if (pos < 0) {
				return null;
			}
			String topicName = file.getName().substring(0, pos);
			Topic topic = new Topic();
			topic.setName(topicName);
			topic.setVirtualWiki(virtualWiki);
			topic.setTopicContent(read(file).toString());
			// FIXME - set these
			/*
			topic.setAdminOnly(boolean);
			topic.setTopicId(int);
			topic.setLockedBy(int);
			topic.setLockedDate(Timestamp);
			topic.setLockSessionKey(String);
			topic.setReadOnly(boolean);
			topic.setTopicType(int);
			*/
			return topic;
		} catch (Exception e) {
			logger.error("Failure while initializing topic", e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static TopicVersion initTopicVersion(File file) {
		try {
			// FIXME - clean this up.
			// get topic version id
			if (!file.exists() || file.getName() == null) {
				return null;
			}
			int pos = file.getName().lastIndexOf(EXT);
			if (pos < 0) {
				return null;
			}
			int topicVersionId = new Integer(file.getName().substring(0, pos)).intValue();
			// get version content
			String contents = read(file).toString();
			TopicVersion topicVersion = new TopicVersion();
			topicVersion.setTopicVersionId(topicVersionId);
			topicVersion.setVersionContent(contents);
			// FIXME - set these
			/*
			topicVersion.setTopicId(rs.getInt("topic_id"));
			topicVersion.setEditComment(rs.getString("edit_comment"));
			topicVersion.setAuthorId(rs.getInt("author_id"));
			topicVersion.setEditDate(rs.getTimestamp("edit_date"));
			topicVersion.setEditType(rs.getInt("edit_type"));
			topicVersion.setAuthorIpAddress(rs.getString("author_ip_address"));
			*/
			return topicVersion;
		} catch (Exception e) {
			logger.error("Failure while initializing topic version", e);
			return null;
		}
	}

	/**
	 * Makes check to see if the specified topic is read-only. The check is case-insensitive.
	 * @param virtualWiki the virtual wiki it appears in
	 * @param topicName the name of the topic
	 * @return
	 * @throws Exception
	 */
	public boolean isTopicReadOnly(String virtualWiki, String topicName) throws Exception {
		if (readOnlyTopics == null) {
			return false;
		} else {
			if (readOnlyTopics.get(virtualWiki) == null) {
				return false;
			}
			Collection readOnlyTopicsForVWiki = ((Collection) readOnlyTopics.get(virtualWiki));
			for (Iterator iterator = readOnlyTopicsForVWiki.iterator(); iterator.hasNext();) {
				String readOnlyTopicName = (String) iterator.next();
				if (topicName.equalsIgnoreCase(readOnlyTopicName)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 *
	 */
	public Date lastRevisionDate(String virtualWiki, String topicName) throws Exception {
		TopicVersion version = WikiBase.getInstance().getHandler().lookupLastTopicVersion(virtualWiki, topicName);
		return version.getEditDate();
	}

	/**
	 * Read the read-only topics from disk
	 */
	protected synchronized void loadReadOnlyTopics(String virtualWiki) {
		logger.debug("Loading read only topics for " + virtualWiki);
		Collection roTopics = new ArrayList();
		File roFile = getPathFor(virtualWiki, null, READ_ONLY_FILE);
		if (!roFile.exists()) {
			logger.debug("Empty read only topics for " + virtualWiki);
			if (virtualWiki == null || virtualWiki.equals("")) {
				virtualWiki = WikiBase.DEFAULT_VWIKI;
			}
			this.readOnlyTopics.put(virtualWiki, roTopics);
			return;
		}
		logger.debug("Loading read-only topics from " + roFile);
		BufferedReader in = null;
		try {
			roFile.createNewFile();
			in = new BufferedReader(new InputStreamReader(new FileInputStream(roFile), Environment.getValue(Environment.PROP_FILE_ENCODING)));
		} catch (IOException e) {
			logger.error(e);
		}
		while (true) {
			String line = null;
			try {
				line = in.readLine();
			} catch (IOException e) {
				logger.error(e);
			}
			if (line == null) break;
			roTopics.add(line);
		}
		try {
			in.close();
		} catch (IOException e) {
			logger.error(e);
		}
		if (virtualWiki.equals("")) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		this.readOnlyTopics.put(virtualWiki, roTopics);
	}

	/**
	 * Locks a file for editing
	 */
	public synchronized boolean lockTopic(String virtualWiki, String topicName, String key) throws IOException {
		File lockFile = makeLockFile(virtualWiki, topicName);
		Date currentDate = new Date();
		logger.debug("Edit timeout in minutes is " + Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT));
		long fiveMinutesAgo = currentDate.getTime() - 60000 * Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT);
		if (lockFile.exists()) {
			long mDate = lockFile.lastModified();
			logger.debug("Lock exists for " + topicName + " modified " + mDate);
			if (mDate < fiveMinutesAgo) {
				logger.debug("Lock has expired (timeout " + fiveMinutesAgo + ")");
				lockFile.delete();
			} else {
				String lockKey = readLockFileKey(lockFile);
				// key is guaranteed non-null, but lockKey might not be (backwards compatibility), so compare this way
				if (key.equals(lockKey)) lockFile.delete();
			}
		}
		if (!lockFile.createNewFile()) return false;
		Writer writer = new OutputStreamWriter(new FileOutputStream(lockFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
		writer.write(key);
		writer.close();
		return true;
	}

	/**
	 *
	 */
	public synchronized TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception {
		// get all files, sorted.  last one is last version.
		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
		if (files == null) return null;
		File file = files[files.length - 1];
		return initTopicVersion(file);
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
		String filename = topicFilename(topicName);
		File file = getPathFor(virtualWiki, null, filename);
		return initTopic(virtualWiki, file);
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
		String filename = topicVersionFilename(topicVersionId);
		File file = getPathFor(virtualWiki, VERSION_DIR, topicName, filename);
		return initTopicVersion(file);
	}

	/**
	 * Create a lock file of the format topicName.lock
	 */
	private File makeLockFile(String virtualWiki, String topicName) {
		return getPathFor(virtualWiki, null, topicName + LOCK_EXTENSION);
	}

	/**
	 *
	 */
	private static int nextTopicVersionId() throws Exception {
		if (NEXT_TOPIC_VERSION_ID < 0) {
			// read value from file
			File topicVersionIdFile = getPathFor(null, null, TOPIC_VERSION_ID_FILE);
			if (!topicVersionIdFile.exists()) {
				NEXT_TOPIC_VERSION_ID = 0;
			} else {
				NEXT_TOPIC_VERSION_ID = new Integer(read(topicVersionIdFile).toString()).intValue();
			}
		}
		return NEXT_TOPIC_VERSION_ID++;
	}

	/**
	 *
	 */
	public Collection purgeDeletes(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		file = getPathFor(virtualWiki, null, "");
		File[] files = file.listFiles(new TextFileFilter());
		for (int i = 0; i < files.length; i++) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[i]), Environment.getValue(Environment.PROP_FILE_ENCODING)));
			String line = reader.readLine();
			reader.close();
			if (line != null) {
				if (line.trim().equals("delete")) {
					files[i].delete();
					String name = files[i].getName();
					all.add(Utilities.decodeSafeFileName(name.substring(0, name.length() - 4)));
				}
			}
		}
		return all;
	}

	/**
	 *
	 */
	public void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception {
		throw new UnsupportedOperationException("New version purging available for file handler yet");
	}

	/**
	 * Reads a file from disk
	 */
	public String read(String virtualWiki, String topicName) throws Exception {
		if (topicName.indexOf(System.getProperty("file.separator")) >= 0) {
			throw new WikiException("WikiNames may not contain special characters:" + topicName);
		}
		String filename = topicFilename(topicName);
		File file = getPathFor(virtualWiki, null, filename);
		StringBuffer contents = read(file);
		return contents.toString();
	}

	/**
	 *
	 */
	public static StringBuffer read(File file) throws IOException {
		StringBuffer contents = new StringBuffer();
		if (file.exists()) {
			FileReader reader = new FileReader(file);
			char[] buf = new char[4096];
			int c;
			while ((c = reader.read(buf, 0, buf.length)) != -1) {
				contents.append(buf, 0, c);
			}
			reader.close();
		} else {
			logger.debug("File does not exist, returning default contents: " + file);
			contents.append("This is a new topic");
		}
		return contents;
	}

	/**
	 * Reads the key from a lockFile
	 */
	private synchronized String readLockFileKey(File lockFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lockFile), Environment.getValue(Environment.PROP_FILE_ENCODING)));
		String lockKey = reader.readLine();
		reader.close();
		return lockKey;
	}

	/**
	 *
	 */
	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		((Collection) this.readOnlyTopics.get(virtualWiki)).remove(topicName);
		this.saveReadOnlyTopics(virtualWiki);
	}

	/**
	 *
	 */
	private File[] retrieveTopicVersionFiles(String virtualWiki, String topicName) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR, topicName);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new TopicVersionComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 * Write the read-only list out to disk
	 */
	protected synchronized void saveReadOnlyTopics(String virtualWiki) throws IOException {
		File roFile = getPathFor(virtualWiki, null, READ_ONLY_FILE);
		logger.debug("Saving read-only topics to " + roFile);
		Writer out = new OutputStreamWriter(new FileOutputStream(roFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
		Iterator it = ((Collection) this.readOnlyTopics.get(virtualWiki)).iterator();
		while (it.hasNext()) {
			out.write((String) it.next() + System.getProperty("line.separator"));
		}
		out.close();
		logger.debug("Saved read-only topics: " + this.readOnlyTopics);
	}

	/**
	 *
	 */
	private void setupSpecialPage(String vWiki, String specialPage) throws Exception {
		File dummy = getPathFor(vWiki, null, specialPage + ".txt");
		if (!dummy.exists()) {
			Writer writer = new OutputStreamWriter(new FileOutputStream(dummy), Environment.getValue(Environment.PROP_FILE_ENCODING));
			writer.write(WikiBase.readDefaultTopic(specialPage));
			writer.close();
		}
	}

	/**
	 *
	 */
	protected static String topicFilename(String topicName) {
		return topicName + EXT;
	}

	/**
	 *
	 */
	protected static String topicVersionFilename(int topicVersionId) {
		return topicVersionId + EXT;
	}

	/**
	 * Unlocks a locked file
	 */
	public synchronized void unlockTopic(String virtualWiki, String topicName) throws IOException {
		File lockFile = getPathFor(virtualWiki, null, topicName + LOCK_EXTENSION);
		if (!lockFile.exists()) {
			logger.warn("attempt to unlock topic by deleting lock file failed (file does not exist): " + lockFile);
		}
		lockFile.delete();
	}

	/**
	 * Write contents to file
	 * Write to version file if versioning is on
	 */
	public synchronized void write(String virtualWiki, String contents, String topicName, String ipAddress) throws Exception {
		if (topicName.indexOf(System.getProperty("file.separator")) >= 0) {
			throw new WikiException("WikiNames may not contain special characters:" + topicName);
		}
		String filename = topicFilename(topicName);
		File file = getPathFor(virtualWiki, null, filename);
		PrintWriter writer = getNewPrintWriter(file, true);
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			addTopicVersion(virtualWiki, topicName, contents, new Date(), ipAddress);
		}
		logger.debug("Writing topic: " + file);
		writer.print(contents);
		writer.close();
	}

	/**
	 *
	 */
	class TopicVersionComparator implements Comparator {

		/**
		 *
		 */
		public int compare(Object first, Object second) {
			String one = ((File)first).getName();
			String two = ((File)second).getName();
			int pos = one.lastIndexOf(EXT);
			int arg1 = new Integer(one.substring(0, pos)).intValue();
			pos = two.lastIndexOf(EXT);
			int arg2 = new Integer(two.substring(0, pos)).intValue();
			return arg1 - arg2;
		}
	}
}
