package org.vqwiki.persistency.file;

import org.apache.log4j.Logger;
import org.vqwiki.*;
import org.vqwiki.persistency.PersistencyHandler;
import org.vqwiki.persistency.db.DBDate;
import org.vqwiki.utils.TextFileFilter;
import org.vqwiki.utils.Utilities;

import java.io.*;
import java.util.*;

/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

public class FileHandler implements PersistencyHandler {

	public static final String VERSION_DIR = "versions";
	public final static String EXT = ".txt";
	private static final Logger logger = Logger.getLogger(FileHandler.class);
	// the read-only topics
	protected Map readOnlyTopics;
	// file used for storing read-only topics
	private final static String READ_ONLY_FILE = "ReadOnlyTopics";
	public static final String VIRTUAL_WIKI_LIST = "virtualwikis.lst";
	public static final String TEMPLATES_DIR = "templates";
	private File file;
	private final String LOCK_EXTENSION = ".lock";

	/**
	 *
	 */
	public FileHandler() {
		this.readOnlyTopics = new HashMap();
		createDefaults(Locale.ENGLISH);
	}

	/**
	 * Set up the file system and default topics if necessary
	 */
	public void createDefaults(Locale locale) {
		// create wiki home if necessary
		File dirCheck = new File(fileBase(""));
		//logger.info( "Using filebase: " + dirCheck );
		dirCheck.mkdir();
		// create default virtual wiki versions directory if necessary
		File versionDirCheck = new File(fileBase("") + VERSION_DIR);
		versionDirCheck.mkdir();
		// create the virtual wiki list file if necessary
		File virtualList = new File(fileBase("") + VIRTUAL_WIKI_LIST);
		// resources for i18n
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
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
				dummy = getPathFor(vWiki, "");
				dummy.mkdir();
				dummy = getPathFor(vWiki, VERSION_DIR);
				dummy.mkdir();
				// write out default topics
				setupSpecialPage(vWiki, messages.getString("specialpages.startingpoints"));
				setupSpecialPage(vWiki, messages.getString("specialpages.textformattingrules"));
				setupSpecialPage(vWiki, messages.getString("specialpages.leftMenu"));
				setupSpecialPage(vWiki, messages.getString("specialpages.topArea"));
				setupSpecialPage(vWiki, messages.getString("specialpages.bottomArea"));
				setupSpecialPage(vWiki, messages.getString("specialpages.stylesheet"));
				setupSpecialPage(vWiki, messages.getString("specialpages.adminonlytopics"));
				loadReadOnlyTopics(vWiki);
			}
			in.close();
		} catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
	}

	/**
	 *
	 */
	private void setupSpecialPage(String vWiki, String specialPage) throws Exception {
		File dummy = getPathFor(vWiki, specialPage + ".txt");
		if (!dummy.exists()) {
			Writer writer = new OutputStreamWriter(new FileOutputStream(dummy), Environment.getValue(Environment.PROP_FILE_ENCODING));
			writer.write(WikiBase.readDefaultTopic(specialPage));
			writer.close();
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
	public static File getPathFor(String virtualWiki, String dir, String fileName) {
		StringBuffer buffer = new StringBuffer();
		if (virtualWiki == null || virtualWiki.equals(WikiBase.DEFAULT_VWIKI)) {
			virtualWiki = "";
		}
		buffer.append(fileBase(virtualWiki));
		buffer.append(File.separator);
		if (dir != null) {
			buffer.append(dir);
			buffer.append(File.separator);
		}
		if (fileName != null) {
			buffer.append(Utilities.encodeSafeFileName(fileName));
		}
		return new File(buffer.toString());
	}

	/**
	 *
	 */
	public static File getPathFor(String virtualWiki, String fileName) {
		return getPathFor(virtualWiki, null, fileName);
	}

	/**
	 * Reads a file from disk
	 */
	public String read(String virtualWiki, String topicName) throws Exception {
		if (topicName.indexOf(System.getProperty("file.separator")) >= 0) {
			throw new WikiException("WikiNames may not contain special characters:" + topicName);
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(topicName);
		buffer.append(EXT);
		logger.debug("Virtual wiki was determined to be: " + virtualWiki + " Topic name was: " + topicName);
		File file = getPathFor(virtualWiki, buffer.toString());
		StringBuffer contents = read(file);
		return contents.toString();
	}

	/**
	 *
	 */
	public StringBuffer read(File file) throws IOException {
		StringBuffer contents = new StringBuffer();
		if (file.exists()) {
			Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
			//CR=13=\r LF=10=\n
			boolean cr = false;
			while (true) {
				int c = in.read();
				if (c == -1) break;
				if (c == 13) cr = true;
				if (cr && c == 10) {
					cr = false;
					contents.append((char) 10);
				} else {
					if (c == 13) {
						// do nothing
					} else if (cr) {
						contents.append((char) 13);
						contents.append((char) c);
						cr = false;
					} else {
						contents.append((char) c);
					}
				}
			}
			in.close();
		} else {
			logger.debug("File does not exist, returning default contents: " + file);
			contents.append("This is a new topic");
		}
		return contents;
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
	 * Locks a file for editing
	 */
	public synchronized boolean lockTopic(String virtualWiki, String topicName, String key) throws IOException {
		File lockFile = makeLockFile(virtualWiki, topicName);
		logger.debug("Locking " + topicName);
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
	public boolean exists(String virtualWiki, String topicName) throws Exception {
		File checkFile = getPathFor(virtualWiki, topicName + ".txt");
		return checkFile.exists();
	}

	/**
	 * Create a lock file of the format topicName.lock
	 */
	private File makeLockFile(String virtualWiki, String topicName) {
		StringBuffer buffer = new StringBuffer();
		if (virtualWiki.equals(WikiBase.DEFAULT_VWIKI)) virtualWiki = "";
		buffer.append(fileBase(virtualWiki));
		buffer.append(File.separator);
		buffer.append(Utilities.encodeSafeFileName(topicName));
		buffer.append(LOCK_EXTENSION);
		return new File(buffer.toString());
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
	 * Unlocks a locked file
	 */
	public synchronized void unlockTopic(String virtualWiki, String topicName) throws IOException {
		File lockFile = getPathFor(virtualWiki, topicName + LOCK_EXTENSION);
		if (!lockFile.exists()) {
			logger.warn("attempt to unlock topic by deleting lock file failed (file does not exist): " + lockFile);
		}
		lockFile.delete();
	}

	/**
	 * Write contents to file
	 * Write to version file if versioning is on
	 */
	public synchronized void write(String virtualWiki, String contents, boolean convertTabs, String topicName) throws Exception {
		if (topicName.indexOf(System.getProperty("file.separator")) >= 0) {
			throw new WikiException("WikiNames may not contain special characters:" + topicName);
		}
		File versionFile = getPathFor(virtualWiki, VERSION_DIR, topicName + EXT + "." + Utilities.fileFriendlyDate(new Date()));
		File file = getPathFor(virtualWiki, topicName + EXT);
		PrintWriter writer = getNewPrintWriter(file, true);
		PrintWriter versionWriter = null;
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			versionWriter = getNewPrintWriter(versionFile, true);
		}
		if (convertTabs) {
			contents = Utilities.convertTabs(contents);
		}
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			logger.debug("Writing version: " + versionFile);
			versionWriter.print(contents);
			versionWriter.close();
		}
		logger.debug("Writing topic: " + file);
		writer.print(contents);
		writer.close();
	}

	/**
	 *  returns a printwriter using utf-8 encoding
	 *
	 */
	private PrintWriter getNewPrintWriter(File file, boolean autoflush) throws IOException {
		return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)), autoflush);
	}

	/**
	 * Write the read-only list out to disk
	 */
	protected synchronized void saveReadOnlyTopics(String virtualWiki) throws IOException {
		File roFile = getPathFor(virtualWiki, READ_ONLY_FILE);
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
	 * Makes check to see if the specified topic is read-only. The check is case-insensitive.
	 * @param virtualWiki the virtual wiki it appears in
	 * @param topicName the name of the topic
	 * @return
	 * @throws Exception
	 */
	public boolean isTopicReadOnly(String virtualWiki, String topicName) throws Exception {
		logger.debug("isTopicReadonly: " + virtualWiki + "/" + topicName);
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
	 * Return a list of all read-only topics
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		logger.debug("Returning read only topics for " + virtualWiki);
		return (Collection) this.readOnlyTopics.get(virtualWiki);
	}

	/**
	 * Read the read-only topics from disk
	 */
	protected synchronized void loadReadOnlyTopics(String virtualWiki) {
		logger.debug("Loading read only topics for " + virtualWiki);
		Collection roTopics = new ArrayList();
		File roFile = getPathFor(virtualWiki, READ_ONLY_FILE);
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
	 *
	 */
	public static String fileBase(String virtualWiki) {
		return Environment.getValue(Environment.PROP_FILE_HOME_DIR) + Utilities.sep() + virtualWiki;
	}

	/**
	 *
	 */
	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		logger.debug("Adding read-only topic: " + topicName);
		Collection roTopics = (Collection) this.readOnlyTopics.get(virtualWiki);
		roTopics.add(topicName);
		this.saveReadOnlyTopics(virtualWiki);
	}

	/**
	 *
	 */
	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		logger.debug("Removing read-only topic: " + topicName);
		((Collection) this.readOnlyTopics.get(virtualWiki)).remove(topicName);
		this.saveReadOnlyTopics(virtualWiki);
	}

	/**
	 *
	 */
	public void initialise(Locale locale) throws Exception {
		this.createDefaults(locale);
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		Collection all = new ArrayList();
		File file = getPathFor("", VIRTUAL_WIKI_LIST);
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
	 *
	 */
	public Collection getTemplateNames(String virtualWiki) throws Exception {
		File file = getPathFor(virtualWiki, TEMPLATES_DIR);
		file.mkdir();
		Collection all = new ArrayList();
		String[] filenames = file.list(new TextFileFilter());
		if (filenames != null) {
			for (int i = 0; i < filenames.length; i++) {
				String filename = filenames[i];
				all.add(filename.substring(0, filename.length() - 4));
			}
		}
		return all;
	}

	/**
	 *
	 */
	public String getTemplate(String virtualWiki, String templateName) throws Exception {
		File dir = getPathFor(virtualWiki, TEMPLATES_DIR);
		File file = new File(dir, templateName + EXT);
		StringBuffer buffer = new StringBuffer();
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		int nextByte;
		while (-1 != (nextByte = in.read())) {
			char nextChar = (char) nextByte;
			buffer.append(nextChar);
		}
		in.close();
		return buffer.toString();
	}

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		File file = getPathFor("", VIRTUAL_WIKI_LIST);
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
	 *
	 */
	public Collection purgeDeletes(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		file = getPathFor(virtualWiki, "");
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
	 *
	 */
	public void saveAsTemplate(String virtualWiki, String templateName, String contents) throws Exception {
		File dir = getPathFor(virtualWiki, TEMPLATES_DIR);
		logger.debug("saving template: " + templateName + " to " + dir);
		dir.mkdir();
		File file = new File(dir, templateName + EXT);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
		writer.print(contents);
		writer.close();
	}

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception {
		if (virtualWiki == null) virtualWiki = "";
		List all = new ArrayList();
		File path = getPathFor(virtualWiki, "");
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
}
