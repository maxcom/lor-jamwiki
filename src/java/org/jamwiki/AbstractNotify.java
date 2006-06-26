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
package org.jamwiki;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.servlets.JAMController;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public abstract class AbstractNotify implements Notify {

	private static final Logger logger = Logger.getLogger(AbstractNotify.class);
	protected String topicName;
	protected String virtualWiki;

	/**
	 *
	 */
	public abstract void addMember(String userName) throws Exception;

	/**
	 *
	 */
	public abstract void removeMember(String userName) throws Exception;

	/**
	 *
	 */
	public abstract boolean isMember(String userName) throws Exception;

	/**
	 *
	 */
	public abstract Collection getMembers() throws Exception;

	/**
	 *
	 */
	public boolean sendNotifications(String rootPath, Locale locale) throws Exception {
		logger.debug("sending notifications for path " + rootPath);
		ResourceBundle messages =
			ResourceBundle.getBundle("ApplicationResources", locale);
		WikiMembers members = WikiBase.getInstance().getWikiMembersInstance(virtualWiki);
		logger.debug(members.getAllMembers().size() + " members found");
		WikiMail mailer = WikiMail.getInstance();
		Iterator anIterator = getMembers().iterator();
		while (anIterator.hasNext()) {
			String aUsername = (String) anIterator.next();
			logger.debug("notification for " + aUsername);
			WikiMember aMember = members.findMemberByName(aUsername);
			String replyAddress = Environment.getValue(Environment.PROP_EMAIL_REPLY_ADDRESS);
			StringBuffer buffer = new StringBuffer();
			buffer.append(messages.getString("mail.notification.body.line1"));
			buffer.append(topicName);
			buffer.append("\n");
			if (!this.virtualWiki.equals(WikiBase.DEFAULT_VWIKI)) {
				buffer.append(messages.getString("mail.notification.body.line2"));
				buffer.append(this.virtualWiki);
				buffer.append("\n");
			}
			buffer.append(rootPath + "Wiki?" + Utilities.encodeURL(topicName));
			buffer.append("\n");
			Topic topicObject = new Topic(topicName);
			String author = null;
			java.util.Date lastRevisionDate = null;
			if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
				lastRevisionDate = topicObject.getMostRecentRevisionDate(virtualWiki);
				if (lastRevisionDate != null) {
					buffer.append(messages.getString("mail.notification.body.revision") + Utilities.formatDateTime(lastRevisionDate) + "\n");
					Collection c = WikiBase.getInstance().getChangeLogInstance().getChanges(virtualWiki, lastRevisionDate);
					if (c != null) {
						Iterator it = c.iterator();
						while (it.hasNext()) {
							Change thischange = (Change) it.next();
							if (thischange.getTopic().equals(topicName)) {
								author = thischange.getUser();
							}
						}
					}
				}
			}
			if (author != null) {
				buffer.append(messages.getString("mail.notification.body.author") + author + "\n");
			}
			buffer.append("\n" + messages.getString("mail.notification.body.diff") + "\n" + topicObject.mostRecentDiff(this.virtualWiki, false));
			buffer.append("\n\n\n----\n\n");
			buffer.append(messages.getString("mail.unsubscribe") + " <");
			buffer.append(rootPath);
			buffer.append("Wiki?action=" + JAMController.ACTION_NOTIFY);
			buffer.append("&notify_action=notify_off&topic=" + Utilities.encodeURL(topicName));
			buffer.append("&username=" + Utilities.encodeURL(aUsername) + ">");
			logger.debug("Sending notification email to " + aMember.getEmail() + " for " + virtualWiki + "/" + topicName);
			String mailTopicName = topicName;
			if (mailTopicName.length() > 25) {
				mailTopicName = topicName.substring(0,25);
			}
			mailer.sendMail(replyAddress, aMember.getEmail(), messages.getString("mail.notification.subject") + mailTopicName, buffer.toString());
		}
		return true;
	}

	/**
	 *
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return virtualWiki;
	}
}