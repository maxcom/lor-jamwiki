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

import java.util.Random;
import java.util.ResourceBundle;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.jamwiki.servlets.JAMController;
import org.jamwiki.utils.Utilities;

/*
 *
 */
public abstract class AbstractWikiMembers implements WikiMembers {

	protected String virtualWiki;
	private static final Logger logger = Logger.getLogger(AbstractWikiMembers.class);
	private static final int KEY_LENGTH = 20;
	private static Random rn = new Random();

	/**
	 * Confirms a member (that is, gives them the ability to set notifications and reminders)
	 * if the supplied key matches the key generated when the account was created. It is expected
	 * that another class or JSP page will call this function at the request of a user.
	 *
	 * @param username  the name of the user to confirm
	 * @param key the key to compare to the key generated when the account was created
	 * @return boolean  True if the user is in the list.
	 */
	public boolean confirmMembership(String username, String key) throws Exception {
		WikiMember aMember = findMemberByName(username);
		logger.debug("Confirming membership by key " + key + " for " + aMember);
		if (aMember.isConfirmed()) return true;
		return false;
	}

	/**
	 * Send the confirmation email to the member
	 */
	protected void mailMember(String username, HttpServletRequest request, WikiMember aMember, String email) {
		ResourceBundle messages =
			ResourceBundle.getBundle("ApplicationResources", request.getLocale());
		// Send an email to the requester with the generated key
		logger.debug("Sending email to " + email);
		StringBuffer buffer = new StringBuffer();
		buffer.append(username);
		buffer.append("\n");
		buffer.append(messages.getString("mail.confirmation.body"));
		buffer.append("\n\n");
		String wikiServerHostname = Environment.getValue(Environment.PROP_BASE_SERVER_HOSTNAME);
		buffer.append(Utilities.createRootPath(request, virtualWiki, wikiServerHostname));
		buffer.append("Wiki");
		buffer.append("?userName=");
		buffer.append(Utilities.encodeURL(username));
		buffer.append("&key=");
		buffer.append(aMember.getKey());
		buffer.append("&action=").append(JAMController.ACTION_MEMBER);
		WikiMail mailer = WikiMail.getInstance();
		String replyAddress = Environment.getValue(Environment.PROP_EMAIL_REPLY_ADDRESS);
		mailer.sendMail(
			replyAddress, email,
			messages.getString("mail.confirmation.subject"),
			buffer.toString()
		);
	}

	/**
	 * Create a new member with a random key
	 */
	protected WikiMember createMember(String username, String email) {
		// Generate a key to confirm the request
		WikiMember aMember = new WikiMember(username, email);
		byte b[] = new byte[KEY_LENGTH];
		for (int i = 0; i < KEY_LENGTH; i++) {
			b[i] = (byte) (rn.nextInt(26) + 65);
		}
		String newKey = new String(b);
		aMember.setKey(newKey);
		return aMember;
	}
}
