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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import JSX.ObjIn;
import JSX.ObjOut;
import org.apache.log4j.Logger;
import org.jamwiki.AbstractWikiMembers;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMember;
import org.jamwiki.utils.Utilities;

/**
 * Stores a list of usernames and their registered email addresses so
 * that users may set notifications and reminders per topic page. Users
 * must set a canonical username and provide a valid email address.
 * An email will then be sent to the supplied address with a hyperlink
 * containing a validation key. The key is then checked against the
 * list of registered names and confirmed, at which point the user
 * is allowed to set notifications and reminders. This is a file-based
 * implementation of the WikiMembers interface.
 */
public class FileWikiMembers extends AbstractWikiMembers {

	private static final Logger logger = Logger.getLogger(FileWikiMembers.class);
	private File memberFile;
	private Hashtable memberTable;

	/**
	 * Constructor for the members list.
	 *
	 * @exception java.lang.ClassNotFoundException if hashtable file is the wrong class
	 * @exception java.io.IOException if the members file could not be read
	 */
	public FileWikiMembers(String virtualWiki) throws ClassNotFoundException, IOException {
		this.virtualWiki = virtualWiki;
		this.memberFile = FileHandler.getPathFor(virtualWiki, null, "member.xml");
		if (!memberFile.exists()) {
			File dirFile = new File(Utilities.dir());
			if (!dirFile.exists()) dirFile.mkdir();
			memberTable = new Hashtable();
		} else {
			ObjIn in = new ObjIn(new FileReader(memberFile));
			memberTable = (Hashtable) in.readObject();
			in.close();
		}
	}

	/**
	 *
	 */
	public boolean confirmMembership(String username, String key) throws Exception {
		boolean result = super.confirmMembership(username, key);
		if (result == true) return true;
		WikiMember member = findMemberByName(username);
		// Look up the username and check the keys
		if (!member.checkKey(key)) return false;
		member.confirm();
		writeMemberTable();
		return writeMemberTable();
	}

	/**
	 * Finds a WikiMember object in the Member collection using the username.
	 *
	 * @param username  the name of the user to find
	 * @return WikiMember  the Member object for the specified user
	 */
	public WikiMember findMemberByName(String username) {
		WikiMember aMember;
		if (memberTable.containsKey(username)) {
			aMember = (WikiMember) memberTable.get(username);
		} else {
			aMember = new WikiMember(username);
		}
		return aMember;
	}

	/**
	 *
	 */
	public Collection getAllMembers() throws Exception {
		return memberTable.values();
	}

	/**
	 *
	 */
	public void addMember(String username, String email, String key) throws Exception {
		WikiMember aMember = new WikiMember(username, email);
		aMember.setKey(key);
		memberTable.put(username, aMember);
		writeMemberTable();
	}

	/**
	 * Add a user account to the Members collection. A key will be generated and sent via
	 * email to the specified address in a hyperlink, which the user can then visit
	 * to confirm the membership request.
	 *
	 * @param username  the name of the user for whom membership is requested
	 * @param email  the email address of the user for whom membership is requested
	 * in the confirmation email. For example, http://www.mybogusdomain.com/jamwiki/jsp/confirm.jsp
	 * @return boolean  true if the user account has been added, false if an account already exists for this username or the member file could not be written
	 * @exception jamwiki.WikiException if the mailer could not be instantiated
	 */
	public synchronized boolean requestMembership(String username, String email, HttpServletRequest request) throws WikiException {
		WikiMember aMember = createMember(username, email);
		mailMember(username, request, aMember, email);
		// Add the request to the members table
		memberTable.put(username, aMember);
		return writeMemberTable();
	}

	/**
	 * Add a user account to the Members collection. It is assumed that the
	 * confirmation was done somewhere else e.g. in the LDAP directory.
	 *
	 * @param username  the name of the user for whom membership is requested
	 * @param email  the email address of the user for whom membership is requested
	 * in the confirmation email. For example, http://www.mybogusdomain.com/jamwiki/jsp/confirm.jsp
	 * @exception jamwiki.WikiException if the mailer could not be instantiated
	 */
	public synchronized boolean createMembershipWithoutRequest(String username, String email) throws WikiException {
		WikiMember aMember = createMember(username, email);
		aMember.confirm();
		// Add the request to the members table
		memberTable.put(username, aMember);
		return writeMemberTable();
	}

	/**
	 * Removes a WikiMember from the members list.
	 *
	 * @param username  the name of the user to remove
	 * @return boolean  true if the operation completed successfully, false if the member file could not be written
	 */
	public synchronized boolean removeMember(String username) {
		if (!memberTable.containsKey(username)) return false;
		memberTable.remove(username);
		return writeMemberTable();
	}

	/**
	 *
	 */
	private boolean writeMemberTable() {
		try {
			ObjOut out = new ObjOut(new FileWriter(memberFile));
			out.writeObject(memberTable);
			return true;
		} catch (IOException e) {
			logger.error(e);
			return false;
		}
	}
}
