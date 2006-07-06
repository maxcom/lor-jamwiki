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
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.jamwiki.servlets.JAMWikiServlet;
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
		// FIXME - this is broken now.  get only users who want a notification.
		List members = WikiBase.getHandler().getAllWikiUserLogins();
		WikiMail mailer = WikiMail.getInstance();
		Iterator anIterator = getMembers().iterator();
		while (anIterator.hasNext()) {
			String login = (String)anIterator.next();
			WikiUser user = WikiBase.getHandler().lookupWikiUser(login);
			String replyAddress = Environment.getValue(Environment.PROP_EMAIL_REPLY_ADDRESS);
			StringBuffer buffer = new StringBuffer();
			buffer.append(Utilities.getMessage("mail.notification.body.line1", locale));
			buffer.append(topicName);
			buffer.append("\n");
			if (!this.virtualWiki.equals(WikiBase.DEFAULT_VWIKI)) {
				buffer.append(Utilities.getMessage("mail.notification.body.line2", locale));
				buffer.append(this.virtualWiki);
				buffer.append("\n");
			}
			buffer.append(rootPath + "Wiki?" + Utilities.encodeURL(topicName));
			buffer.append("\n");
			Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
			if (topic == null) {
				throw new Exception("No topic found " + topicName + " / " + virtualWiki);
			}
			String author = null;
			java.util.Date lastRevisionDate = null;
			if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
				lastRevisionDate = WikiBase.getHandler().lastRevisionDate(virtualWiki, topicName);
				if (lastRevisionDate != null) {
					buffer.append(Utilities.getMessage("mail.notification.body.revision", locale) + Utilities.formatDateTime(lastRevisionDate) + "\n");
//					Collection c = WikiBase.getChangeLogInstance().getChanges(virtualWiki, lastRevisionDate);
//					if (c != null) {
//						Iterator it = c.iterator();
//						while (it.hasNext()) {
//							Change thischange = (Change) it.next();
//							if (thischange.getTopic().equals(topicName)) {
//								author = thischange.getUser();
//							}
//						}
//					}
				}
			}
			if (author != null) {
				buffer.append(Utilities.getMessage("mail.notification.body.author", locale) + author + "\n");
			}
			String diff = WikiBase.getHandler().diff(this.virtualWiki, topic.getName(), 0, 1, false);
			buffer.append("\n" + Utilities.getMessage("mail.notification.body.diff", locale) + "\n" + diff);
			buffer.append("\n\n\n----\n\n");
			buffer.append(Utilities.getMessage("mail.unsubscribe", locale) + " <");
			buffer.append(rootPath);
			buffer.append("Wiki?action=" + JAMWikiServlet.ACTION_NOTIFY);
			buffer.append("&notify_action=notify_off&topic=" + Utilities.encodeURL(topicName));
			buffer.append("&username=" + Utilities.encodeURL(login) + ">");
			logger.debug("Sending notification email to " + user.getEmail() + " for " + virtualWiki + "/" + topicName);
			String mailTopicName = topicName;
			if (mailTopicName.length() > 25) {
				mailTopicName = topicName.substring(0,25);
			}
			mailer.sendMail(replyAddress, user.getEmail(), Utilities.getMessage("mail.notification.subject", locale) + mailTopicName, buffer.toString());
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