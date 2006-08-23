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

import java.sql.Connection;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;

/**
 * This class simply contains utility methods for upgrading database schemas
 * (if needed) between JAMWiki versions.  In general upgrade methods will only
 * be maintained for a few versions and then deleted - for example, JAMWiki version 10.0.0
 * does not need to keep the upgrade methods from JAMWiki 0.0.1 around.
 */
public class DatabaseUpgrades {

	private static Logger logger = Logger.getLogger(DatabaseUpgrades.class.getName());

	/**
	 *
	 */
	public static Vector upgrade010(Vector messages) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			String sql = "alter table jam_virtual_wiki add column default_topic_name VARCHAR(200)";
			DatabaseConnection.executeUpdate(sql, conn);
			sql = "update jam_virtual_wiki set default_topic_name = ?";
			WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
			stmt.setString(1, Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC));
			stmt.executeUpdate(conn);
			sql = "alter table jam_virtual_wiki alter column default_topic_name set NOT NULL";
			DatabaseConnection.executeUpdate(sql, conn);
			conn.commit();
			// FIXME - hard coding
			messages.add("Updated jam_virtual_wiki table");
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return messages;
	}

	/**
	 *
	 */
	public static Vector upgrade030(Vector messages) throws Exception {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			String sql = "drop table jam_image";
			DatabaseConnection.executeUpdate(sql, conn);
			// FIXME - hard coding
			messages.add("Dropped jam_image table");
			DatabaseConnection.executeUpdate(DefaultQueryHandler.STATEMENT_CREATE_CATEGORY_TABLE, conn);
			// FIXME - hard coding
			messages.add("Added jam_category table");
			conn.commit();
		} catch (Exception e) {
			try {
				DatabaseConnection.executeUpdate(DefaultQueryHandler.STATEMENT_DROP_CATEGORY_TABLE, conn);
			} catch (Exception ex) {}
			conn.rollback();
			throw e;
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return messages;
	}
}
