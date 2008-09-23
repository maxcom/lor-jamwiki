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
import org.jamwiki.utils.WikiLogger;

/**
 * Implementation of the {@link org.jamwiki.UserHandler} interface that uses a
 * database for storing user login, password and other basic user information.
 */
public class TestUserHandler implements UserHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(TestUserHandler.class.getName());

	/**
	 *
	 */
	public void addWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public boolean authenticate(String username, String password) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public boolean isWriteable() {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiUserInfo lookupWikiUserInfo(String username) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void updateWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}
}
