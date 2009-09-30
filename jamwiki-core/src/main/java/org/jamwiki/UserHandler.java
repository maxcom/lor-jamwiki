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
package org.jamwiki;

import org.jamwiki.model.WikiUserInfo;

/**
 * This interface provides methods needed when authenticating and retrieving
 * basic user information.
 *
 * @see org.jamwiki.WikiBase#getUserHandler
 */
public interface UserHandler {

	/**
	 * Add new user information to the user information data store.  If the
	 * data store is write-only then this method should throw an
	 * OperationNotSupportedException.
	 *
	 * @param userInfo The WikiUserInfo object that is to be added to the
	 *  data store.
	 * @param transactionObject If the user information is being added to the
	 *  data store as part of a transaction then this parameter should contain
	 *  the transaction object, such as a database connection.  If no
	 *  transaction is being used then this value should be <code>null</code>.
	 * @throws Exception Thrown if an error occurs while adding the user
	 *  information, or if the data store is read-only.
	 */
	void addWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception;

	/**
	 * Determine if a value matching the given username and password exists in
	 * the data store.
	 *
	 * @param username The username that is being validated against.
	 * @param password The password that is being validated against.
	 * @return <code>true</code> if the username / password combination matches
	 *  an existing record in the data store, <code>false</code> otherwise.
	 * @throws Exception Thrown if an error occurs while accessing the data
	 *  store.
	 */
	boolean authenticate(String username, String password) throws Exception;

	/**
	 * This method returns <code>true</code> if the user information data
	 * store is writeable, and <code>false</code> if it is read-only.
	 *
	 * @return <code>true</code> if the user information data store is
	 *  writeable, and <code>false</code> if it is read-only.
	 */
	boolean isWriteable();

	/**
	 * Given a username, retrieving a WikiUserInfo containing values for
	 * the specified user.
	 *
	 * @param username The username for the user information being retrieved.
	 * @return A WikiUserInfo object containing user information matching the
	 *  the username, or <code>null</code> if no record matching the username
	 *  can be found.
	 * @throws Exception Thrown if an error occurs while retrieving user
	 *  information.
	 */
	WikiUserInfo lookupWikiUserInfo(String username) throws Exception;

	/**
	 *
	 * @param userInfo The WikiUserInfo object that is to be updated in the
	 *  data store.
	 * @param transactionObject If the user information is being updated in
	 *  the data store as part of a transaction then this parameter should
	 *  contain the transaction object, such as a database connection.  If no
	 *  transaction is being used then this value should be <code>null</code>.
	 * @throws Exception Thrown if an error occurs while updating the user
	 *  information, or if the data store is read-only.
	 */
	void updateWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception;
}
