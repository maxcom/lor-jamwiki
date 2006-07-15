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
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.persistency.file;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.jamwiki.AbstractNotify;
import org.jamwiki.Environment;

/**
 * Implementation of Notify which stores notification records in text files.
 * Notification files use the same filename as the associated topic and use
 * the extension ".ntf".
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
	 * @exception Exception if the file could not be opened or read
	 */
	public FileNotify(String virtualWiki, String newTopicName) throws Exception {
		this.topicName = newTopicName;
		this.virtualWiki = virtualWiki;
		this.notifyFile = makeNotifyFile();
		if (this.notifyFile.exists()) readNotifyFile();
	}

	/**
	 *
	 */
	private File makeNotifyFile() {
		return FileHandler.getPathFor(virtualWiki, null, topicName + ".ntf");
	}

	/**
	 *
	 */
	private synchronized boolean createNotifyFile() throws Exception {
		try {
			notifyFile.createNewFile();
			if (notifyFile.exists()) return true;
		} catch (IOException e) {
		}
		throw new Exception("Notify File could not be created.");
	}

	/**
	 *
	 */
	private synchronized boolean readNotifyFile() throws Exception {
		try {
			String aMember;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(notifyFile)));
			do {
				aMember = reader.readLine();
				if (aMember != null) membersToNotify.add(aMember);
			} while (aMember != null);
			reader.close();
		} catch (IOException e) {
			throw new Exception("Notify File could not be read.");
		}
		return true;
	}

	/**
	 *
	 */
	private synchronized boolean writeNotifyFile() throws Exception {
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
			throw new Exception("Notify File could not be written");
		}
		return true;
	}

	/**
	 * Retrieves the home directory of the JAMWiki installation.
	 *
	 * @return String the home directory
	 */
	protected String fileBase() {
		return Environment.getValue(Environment.PROP_BASE_FILE_DIR);
	}

	/**
	 *
	 */
	public static Collection getAll(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		File path = FileHandler.getPathFor(virtualWiki, null, "");
//		File[] list = path.listFiles(new FileExtensionFilter("ntf"));
//		for (int i = 0; i < list.length; i++) {
//			File file = list[i];
//			String fileName = file.getName();
//			all.add(
//				new FileNotify(
//					virtualWiki,
//					fileName.substring(0, fileName.length() - 4)
//				)
//			);
//		}
		return all;
	}
}
