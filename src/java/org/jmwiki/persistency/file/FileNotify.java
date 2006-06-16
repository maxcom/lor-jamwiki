package org.jmwiki.persistency.file;

import org.apache.log4j.Logger;
import org.jmwiki.AbstractNotify;
import org.jmwiki.Environment;
import org.jmwiki.WikiException;

import java.io.*;
import java.util.*;

/**
 * Implementation of Notify which stores notification records in text files.
 * Notification files use the same filename as the associated topic and use
 * the extension ".ntf".
 *
 * @author Robert E Brewer
 * @version 0.1
 */
public class FileNotify extends AbstractNotify {

	private static final Logger logger = Logger.getLogger(FileNotify.class);
	protected Set membersToNotify = new HashSet();
	protected File notifyFile;

	/**
	 * No-arg constructor for compatibility only, always use FileNotify(newTopicName) instead.
	 */
	public FileNotify() {
	}

	/**
	 * Instantiates and reads in a Notify object.
	 *
	 * @param newTopicName  the topic name with which this notification is associated
	 * @exception jmwiki.WikiException if the file could not be opened or read
	 */
	public FileNotify(String virtualWiki, String newTopicName) throws WikiException {
		this.topicName = newTopicName;
		this.virtualWiki = virtualWiki;
		this.notifyFile = makeNotifyFile();
		if (this.notifyFile.exists()) readNotifyFile();
	}

	/**
	 *
	 */
	private File makeNotifyFile() {
		return FileHandler.getPathFor(virtualWiki, topicName + ".ntf");
	}

	/**
	 *
	 */
	private synchronized boolean createNotifyFile() throws WikiException {
		try {
			notifyFile.createNewFile();
			if (notifyFile.exists()) return true;
		} catch (IOException e) {
		}
		throw new WikiException("Notify File could not be created.");
	}

	/**
	 *
	 */
	private synchronized boolean readNotifyFile() throws WikiException {
		try {
			String aMember;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(notifyFile)));
			do {
				aMember = reader.readLine();
				if (aMember != null) membersToNotify.add(aMember);
			} while (aMember != null);
			reader.close();
		} catch (IOException e) {
			throw new WikiException("Notify File could not be read.");
		}
		return true;
	}

	/**
	 *
	 */
	public Collection getMembers() throws Exception {
		return this.membersToNotify;
	}

	/**
	 *
	 */
	private synchronized boolean writeNotifyFile() throws WikiException {
		try {
			if (!notifyFile.exists()) createNotifyFile();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(notifyFile), Environment.getValue(Environment.PROP_FILE_ENCODING)));
			Iterator anIterator = membersToNotify.iterator();
			while (anIterator.hasNext()) {
				String aMember = (String) anIterator.next();
				writer.write(aMember);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			throw new WikiException("Notify File could not be written");
		}
		return true;
	}

	/**
	 * Adds a user to the list of members to be notified when the associated topic changes.
	 *
	 * @param userName  the name of the user to add
	 * @exception jmwiki.WikiException if the file could not be written
	 */
	public void addMember(String userName) throws WikiException {
		membersToNotify.add(userName);
		writeNotifyFile();
	}

	/**
	 * Removes a user from the list of members to be notified when the associated topic changes.
	 *
	 * @param userName  the name of the user to remove
	 * @exception jmwiki.WikiException if the file could not be written
	 */
	public synchronized void removeMember(String userName) throws WikiException {
		membersToNotify.remove(userName);
		if (membersToNotify.isEmpty()) {
			notifyFile.delete();
		} else {
			writeNotifyFile();
		}
	}

	/**
	 * Checks whether the user is in the list of members to be notified when the associated topic changes.
	 *
	 * @param userName  the name of the user to check
	 * @return boolean  True if the user is in the list.
	 */
	public boolean isMember(String userName) {
		return membersToNotify.contains(userName);
	}

	/**
	 * Retrieves the home directory of the VQWiki installation.
	 *
	 * @return String the home directory
	 */
	protected String fileBase() {
		return Environment.getValue(Environment.PROP_FILE_HOME_DIR);
	}

	/**
	 *
	 */
	public static Collection getAll(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		File path = FileHandler.getPathFor(virtualWiki, "");
		File[] list = path.listFiles(new FileExtensionFilter("ntf"));
		for (int i = 0; i < list.length; i++) {
			File file = list[i];
			String fileName = file.getName();
			all.add(
				new FileNotify(
					virtualWiki,
					fileName.substring(0, fileName.length() - 4)
				)
			);
		}
		return all;
	}
}
