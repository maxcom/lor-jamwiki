/**
 * @author garethc
 *  22/10/2002 13:20:38
 */
package org.jmwiki;

import java.util.Random;
import java.util.ResourceBundle;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.jmwiki.servlets.WikiServlet;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;

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
		buffer.append(JSPUtils.createRootPath(request, virtualWiki, wikiServerHostname));
		buffer.append("Wiki");
		buffer.append("?userName=");
		buffer.append(JSPUtils.encodeURL(username));
		buffer.append("&key=");
		buffer.append(aMember.getKey());
		buffer.append("&action=").append(WikiServlet.ACTION_MEMBER);
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

// $Log$
// Revision 1.20  2006/04/24 18:07:18  studer
// Just a small change for making bugfix http://www.jmwiki.org/jira/browse/VQW-72 perfect.
//
// Revision 1.19  2006/04/23 07:52:28  wrh2
// Coding style updates (VQW-73).
//
// Revision 1.18  2006/04/22 07:39:36  studer
// Fixing http://www.jmwiki.org/jira/browse/VQW-72
//
// Revision 1.17  2006/04/19 08:31:58  wrh2
// Implement VQW-77 to remove hard-coding of action values.
//
// Revision 1.16  2006/04/12 21:01:56  studer
// fix for http://www.jmwiki.org/jira/browse/VQW-72
//
// Revision 1.15  2006/04/11 21:43:18  mvdkleijn
// fixes generation of confirmation link in email (VQW-72)
//
// Revision 1.14  2006/04/05 21:53:39  studer
// Small fix for Tomcat 5 <> 5.5 incompatibility.
//
// Revision 1.13  2003/10/05 05:07:30  garethc
// fixes and admin file encoding option + merge with contributions
//
// Revision 1.12  2003/09/21 20:52:43  garethc
// merge and typos
//
// Revision 1.11  2003/05/14 19:02:58  mrgadget4711
// MOD: Internationalization extended
//
// Revision 1.10  2003/04/28 03:54:41  garethc
// beginning of work on import tool
//
// Revision 1.9  2003/04/15 23:10:57  garethc
// lucene fixes
//
// Revision 1.8  2003/04/09 20:44:09  garethc
// package org
//
// Revision 1.7  2003/02/18 20:48:07  garethc
// 2.4.0 RC3
//
// Revision 1.6  2003/01/07 23:54:39  garethc
// almost finished taglibs
//
// Revision 1.5  2003/01/07 03:11:51  garethc
// beginning of big cleanup, taglibs etc
//
// Revision 1.4  2002/12/08 20:58:58  garethc
// 2.3.6 almost ready
//
// Revision 1.3  2002/12/02 19:26:51  garethc
// fixes
//
// Revision 1.2  2002/11/11 22:19:23  garethc
// printable page
//
// Revision 1.1  2002/10/22 20:14:32  garethc
// 2.2.0
//