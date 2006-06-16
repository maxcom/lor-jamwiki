package org.jmwiki.persistency.file;

import org.jmwiki.*;

import java.io.*;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Implementation of Reminders which stores reminder records in text files.
 * Reminder files use the same filename as the associated topic and use
 * the extension ".rmd". Only one reminder is allowed per user per topic;
 * the user must either create a new page to set an additional reminder,
 * or plan to alter the existing reminder on a recurring basis to simulate
 * recurring reminders. Each Reminders object represents a single topic,
 * which must be specified at instantiation, either by name or by File.
 *
 * @author Robert E Brewer
 * @version 0.1
 */
public class FileReminders implements Reminders {

	private Hashtable curReminders = new Hashtable();
	private String topicName;
	private File remindFile;

	/**
	 * No-arg constructor for compatibility only; always use FileReminders(newTopicName)
	 * or FileReminders(newRemindFile) instead.
	 */
	public FileReminders() {
	}

	/**
	 * Opens and reads a Reminders object using the topic name.
	 *
	 * @param newTopicName the name of the topic
	 * @exception jmwiki.WikiException if the reminder file could not be opened or read
	 */
	public FileReminders(String newTopicName) throws WikiException {
		topicName = newTopicName;
		remindFile = makeRemindFile();
		if (remindFile.exists()) readRemindFile();
	}

	/**
	 * Reads a Reminders object from a File which has already been opened.
	 *
	 * @param newRemindFile the open File from which to read Reminders data.
	 * @exception jmwiki.WikiException if the reminder file could not be read
	 */
	public FileReminders(File newRemindFile) throws WikiException {
		remindFile = newRemindFile;
		topicName = remindFile.getName();
		topicName = topicName.substring(0, topicName.length() - 4);
		if (remindFile.exists()) readRemindFile();
	}

	/**
	 *
	 */
	private File makeRemindFile() {
		return new File(fileBase() + System.getProperty("file.separator") + topicName + ".rmd");
	}

	/**
	 *
	 */
	private synchronized boolean createRemindFile() throws WikiException {
		try {
			remindFile.createNewFile();
			if (remindFile.exists()) return true;
		} catch (IOException e) {
		}
		throw new WikiException("Remind File could not be created.");
	}

	/**
	 *
	 */
	private synchronized boolean readRemindFile() throws WikiException {
		try {
			ObjectInputStream in = null;
			in = new ObjectInputStream(new FileInputStream(remindFile));
			curReminders = (Hashtable) in.readObject();
			in.close();
		} catch (Exception e) {
			throw new WikiException("Remind File could not be read.");
		}
		return true;
	}

	/**
	 *
	 */
	private synchronized boolean writeRemindFile() throws WikiException {
		try {
			if (!remindFile.exists()) createRemindFile();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(remindFile));
			out.writeObject(curReminders);
			out.close();
		} catch (IOException e) {
			throw new WikiException("Remind File could not be written");
		}
		return true;
	}

	/**
	 * Adds a reminder to this topic. If a reminder already exists for this user, it will be
	 * replaced with the new reminder date.
	 *
	 * @param userName  the name of the user to add
	 * @param dateToRemind  the date on which to send a reminder to the specified user
	 * @exception jmwiki.WikiException if the file could not be written
	 */
	public void addReminder(String userName, Date dateToRemind) throws WikiException {
		WikiReminder tReminder = new WikiReminder(userName, dateToRemind);
		curReminders.put(userName, tReminder);
		writeRemindFile();
	}

	/**
	 * Removes reminder for the specified user for this topic. If the removal
	 * empties the reminder list for this topic, the reminder file is deleted.
	 *
	 * @param userName  the name of the user for whom to remove reminders
	 * @exception jmwiki.WikiException if the file could not be written
	 */
	public synchronized void removeReminder(String userName) throws WikiException {
		curReminders.remove(userName);
		if (curReminders.isEmpty()) {
			remindFile.delete();
		} else {
			writeRemindFile();
		}
	}

	/**
	 * Checks whether the specified user has a reminder set for this topic.
	 *
	 * @param userName  the name of the user to check
	 * @return boolean  True if the specified user has a reminder set for this topic.
	 */
	public boolean hasReminder(String userName) {
		return curReminders.containsKey(userName);
	}

	/**
	 * Returns the date specified for a reminder for this user for this topic.
	 *
	 * @param userName  the name of the user to check
	 * @return Date  the date of the reminder, or null if no reminder is set
	 */
	public Date dateToRemind(String userName) {
		if (!hasReminder(userName)) return null;
		WikiReminder aReminder = (WikiReminder) curReminders.get(userName);
		return aReminder.getRemindDate();
	}

	/**
	 * Retrieves the home directory of the JMWiki installation.
	 *
	 * @return String the home directory
	 */
	protected String fileBase() {
		return Environment.getValue(Environment.PROP_FILE_HOME_DIR);
	}

	/**
	 * Sends reminders via email to all users who have requested a reminder
	 * on the specified date for this topic.
	 *
	 * @param remindDate  the date of reminders to send
	 * @return boolean  true if the operation completes successfully
	 * @exception java.lang.ClassNotFoundException, IOException if the mailer could not be instantiated
	 */
	public boolean sendReminders(Date remindDate) throws Exception {
		WikiMembers members = WikiBase.getInstance().getWikiMembersInstance(null);
		WikiMail mailer = WikiMail.getInstance();
		Iterator anIterator = curReminders.values().iterator();
		while (anIterator.hasNext()) {
			WikiReminder aReminder = (WikiReminder) anIterator.next();
			if (aReminder.remindDateEquals(remindDate)) {
				WikiMember aMember = members.findMemberByName(aReminder.getUserName());
				String replyAddress = Environment.getValue(Environment.PROP_EMAIL_REPLY_ADDRESS);
				mailer.sendMail(replyAddress, aMember.getEmail(), "Wiki Reminder", "Please visit the topic, '" + topicName + "'.");
			}
		}
		return true;
	}
}
