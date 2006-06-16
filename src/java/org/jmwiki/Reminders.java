package org.jmwiki;

import java.util.Date;

/**
 * Stores a list of usernames and dates for each topic page in the JMWiki
 * system, so that an email can be sent to their registered
 * addresses on the specified date.
 *
 * @author Robert E Brewer
 * @version 0.1
 */
public interface Reminders {

	/**
	 *
	 */
	public void addReminder(String userName, Date dateToRemind) throws Exception;

	/**
	 *
	 */
	public void removeReminder(String userName) throws Exception;

	/**
	 *
	 */
	public boolean hasReminder(String userName) throws Exception;

	/**
	 *
	 */
	public Date dateToRemind(String userName) throws Exception;

	/**
	 *
	 */
	public boolean sendReminders(Date remindDate) throws Exception;
}
