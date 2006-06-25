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
package org.jmwiki;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Stores a list of usernames and their registered email addresses so
 * that users may set notifications and reminders per topic page. Users
 * must set a canonical username and provide a valid email address.
 * An email will then be sent to the supplied address with a hyperlink
 * containing a validation key. The key is then checked against the
 * list of registered names and confirmed, at which point the user
 * is allowed to set notifications and reminders.
 */
public interface WikiMembers {

	/**
	 *
	 */
	public boolean requestMembership(String username, String email, HttpServletRequest request) throws Exception;

	/**
	 *
	 */
	public boolean createMembershipWithoutRequest(String username, String email) throws Exception;

	/**
	 *
	 */
	public boolean confirmMembership(String username, String key) throws Exception;

	/**
	 *
	 */
	public boolean removeMember(String username) throws Exception;

	/**
	 *
	 */
	public WikiMember findMemberByName(String username) throws Exception;

	/**
	 *
	 */
	public Collection getAllMembers() throws Exception;

	/**
	 *
	 */
	public void addMember(String username, String email, String key) throws Exception;
}
