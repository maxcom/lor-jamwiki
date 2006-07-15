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
package org.jamwiki.persistency.db;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.jamwiki.AbstractNotify;

/**
 *
 */
public class DatabaseNotify extends AbstractNotify {

	private static final Logger logger = Logger.getLogger(DatabaseNotify.class);

	private static final String STATEMENT_ALL_NOTIFICATIONS =
		"SELECT * FROM Notification WHERE virtualwiki = ? ";

	/**
	 *
	 */
	public DatabaseNotify(String virtualWiki, String topicName) {
		super();
		this.virtualWiki = virtualWiki;
		this.topicName = topicName;
	}

	/**
	 *
	 */
	public static Collection getAllNotifications(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_ALL_NOTIFICATIONS);
		stmt.setString(1, virtualWiki);
		WikiResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			DatabaseNotify notify = new DatabaseNotify(rs.getString("virtualwiki"), rs.getString("topic"));
			all.add(notify);
		}
		return all;
	}
}
