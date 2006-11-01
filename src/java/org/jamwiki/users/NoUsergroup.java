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
package org.jamwiki.users;

import java.util.List;

/**
 * Usergroup, if there is no usergroup. This class implements the
 * default wiki behavior.
 */
public class NoUsergroup extends Usergroup {

	/**
	 * Get an instance of the user group class.
	 *
	 * @return Instance to the user group class
	 */
	public static Usergroup getInstance() {
		return new NoUsergroup();
	}

	/**
	 * Get the full name of an user by its user-ID.
	 *
	 * @param uid The user-ID of this user
	 * @return The full name of this user
	 */
	public String getFullnameById(String uid) {
		return uid;
	}

	/**
	 * Get the email address of an user by its user-ID.
	 *
	 * @param user The user-ID of this user
	 * @return The email address of this user
	 */
	public String getKnownEmailById(String user) {
		return null;
	}

	/**
	 * Get a list of all users.
	 *
	 * @return List of all users. The list contains SelectorBeans with the
	 *  user-ID as key and the full username as label.
	 */
	public List getListOfAllUsers() {
		return null;
	}

	/**
	 * Get the user details of this user by its user-ID. The user details is
	 * a string, which is set in the admin section. It contains some
	 * placeholders, which are replaced by values from the user repository.
	 *
	 * @param uid The user-ID of this user
	 * @return The user details section
	 */
	public String getUserDetails(String uid) {
		return null;
	}

	/**
	 * If the repository contains confirmed validated email addresses then
	 * return <code>true</code>.  This method allows the registration process
	 * to skip email validation when addresses are known to be valid.
	 *
	 * @return <code>true</code> if the repository contains validated email
	 *  addresses, otherwise returns <code>false</code>.
	 */
	public boolean isEmailValidated() {
		return false;
	}
}
