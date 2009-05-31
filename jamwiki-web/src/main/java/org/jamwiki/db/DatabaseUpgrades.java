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
package org.jamwiki.db;

import java.sql.Connection;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataHandler;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.Role;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.WikiLogger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class simply contains utility methods for upgrading database schemas
 * (if needed) between JAMWiki versions.  In general upgrade methods will only
 * be maintained for a few versions and then deleted - for example, JAMWiki version 10.0.0
 * does not need to keep the upgrade methods from JAMWiki 0.0.1 around.
 */
public class DatabaseUpgrades {

	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseUpgrades.class.getName());

	/**
	 *
	 */
	private DatabaseUpgrades() {
	}

	private static TransactionDefinition getTransactionDefinition() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return def;
	}

	/**
	 * Special login method - it cannot be assumed that the database schema
	 * is unchanged, so do not use standard methods.
	 */
	public static boolean login(String username, String password) throws Exception {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (!oldVersion.before(0, 7, 0)) {
			return (WikiBase.getDataHandler().authenticate(username, password));
		}
		Connection conn = DatabaseConnection.getConnection();
		WikiPreparedStatement stmt = new WikiPreparedStatement("select 1 from jam_wiki_user_info where login = ? and encoded_password = ?");
		if (!StringUtils.isBlank(password)) {
			password = Encryption.encrypt(password);
		}
		stmt.setString(1, username);
		stmt.setString(2, password);
		WikiResultSet rs = stmt.executeQuery(conn);
		return (rs.size() > 0);
	}

	/**
	 *
	 */
	public static List<String> upgrade061(List<String> messages) throws Exception {
		TransactionStatus status = DatabaseConnection.startTransaction(getTransactionDefinition());
		try {
			String sql = null;
			Connection conn = DatabaseConnection.getConnection();
			// delete ROLE_DELETE
			sql = "delete from jam_role_map where role_name = 'ROLE_DELETE'";
			DatabaseConnection.executeUpdate(sql, conn);
			sql = "delete from jam_role where role_name = 'ROLE_DELETE'";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Removed ROLE_DELETE");
		} catch (Exception e) {
			DatabaseConnection.rollbackOnException(status, e);
			throw e;
		} catch (Error err) {
			DatabaseConnection.rollbackOnException(status, err);
			throw err;
		}
		DatabaseConnection.commit(status);
		return messages;
	}

	/**
	 *
	 */
	public static List<String> upgrade063(List<String> messages) throws Exception {
		TransactionStatus status = DatabaseConnection.startTransaction(getTransactionDefinition());
		try {
			String sql = null;
			Connection conn = DatabaseConnection.getConnection();
			// increase the size of ip address columns
			String dbType = Environment.getValue(Environment.PROP_DB_TYPE);
			if (dbType.equals(DataHandler.DATA_HANDLER_DB2) || dbType.equals(DataHandler.DATA_HANDLER_DB2400)) {
				sql = "alter table jam_topic_version alter column wiki_user_ip_address set data type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_file_version alter column wiki_user_ip_address set data type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user alter column create_ip_address set data type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user alter column last_login_ip_address set data type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MYSQL) || dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_topic_version modify wiki_user_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_file_version modify wiki_user_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user modify create_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user modify last_login_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
			} else if (dbType.equals(DataHandler.DATA_HANDLER_POSTGRES)) {
				sql = "alter table jam_topic_version alter column wiki_user_ip_address type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_file_version alter column wiki_user_ip_address type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user alter column create_ip_address type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user alter column last_login_ip_address type varchar(39) ";
				DatabaseConnection.executeUpdate(sql, conn);
			} else {
				sql = "alter table jam_topic_version alter column wiki_user_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_file_version alter column wiki_user_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user alter column create_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
				sql = "alter table jam_wiki_user alter column last_login_ip_address varchar(39) not null ";
				DatabaseConnection.executeUpdate(sql, conn);
			}
			messages.add("Increased IP address field sizes to support IPv6");
		} catch (Exception e) {
			messages.add("Unable to modify database schema to support IPv6.  Please see UPGRADE.txt for further details on this optional modification." + e.getMessage());
			// do not throw this error and halt the upgrade process - changing the column size
			// is not required for systems that have already been successfully installed, it
			// is simply being done to keep new installs consistent with existing installs.
			logger.info("Failure while updating database for IPv6 support.  See UPGRADE.txt for instructions on how to manually complete this optional step.", e);
			DatabaseConnection.rollbackOnException(status, e);
			status = null;	// so we do not try to commit
		} catch (Error err) {
			DatabaseConnection.rollbackOnException(status, err);
			throw err;
		}
		if (status != null) {
			DatabaseConnection.commit(status);
		}
		return messages;
	}

	/**
	 *
	 */
	public static List<String> upgrade070(List<String> messages) throws Exception {
		TransactionStatus status = DatabaseConnection.startTransaction(getTransactionDefinition());
		String dbType = Environment.getValue(Environment.PROP_DB_TYPE);
		try {
			String sql = null;
			Connection conn = DatabaseConnection.getConnection();
			// add characters_changed column to jam_topic_version
			if (dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_topic_version add (characters_changed INTEGER) ";
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MSSQL)) {
				sql = "alter table jam_recent_change add [characters_changed] int ";
			} else {
				sql = "alter table jam_topic_version add column characters_changed INTEGER ";
			}
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added characters_changed column to jam_topic_version");
			// add characters_changed column to jam_recent_change
			if (dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_recent_change add (characters_changed INTEGER) ";
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MSSQL)) {
				sql = "alter table jam_topic_version add [characters_changed] int ";
			} else {
				sql = "alter table jam_recent_change add column characters_changed INTEGER ";
			}
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added characters_changed column to jam_recent_change");
			// copy columns from jam_wiki_user_info into jam_wiki_user
			if (dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_wiki_user add (email VARCHAR(100)) ";
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MSSQL)) {
				sql = "alter table jam_wiki_user add email VARCHAR(100) ";
			} else {
				sql = "alter table jam_wiki_user add column email VARCHAR(100) ";
			}
			DatabaseConnection.executeUpdate(sql, conn);
			sql = "update jam_wiki_user set email = ( "
			    +   "select email "
			    +   "from jam_wiki_user_info "
			    +   "where jam_wiki_user.wiki_user_id = jam_wiki_user_info.wiki_user_id "
			    + ") ";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added email column to jam_wiki_user");
			// add new columns to jam_wiki_user
			if (dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_wiki_user add (editor VARCHAR(50)) ";
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MSSQL)) {
				sql = "alter table jam_wiki_user add editor VARCHAR(50) ";
			} else {
				sql = "alter table jam_wiki_user add column editor VARCHAR(50) ";
			}
			DatabaseConnection.executeUpdate(sql, conn);
			if (dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_wiki_user add (signature VARCHAR(255)) ";
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MSSQL)) {
				sql = "alter table jam_wiki_user add signature VARCHAR(255) ";
			} else {
				sql = "alter table jam_wiki_user add column signature VARCHAR(255) ";
			}
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added editor and signature columns to jam_wiki_user");
			if (dbType.equals(DataHandler.DATA_HANDLER_HSQL)) {
				DatabaseConnection.executeUpdate(HSqlQueryHandler.STATEMENT_CREATE_USERS_TABLE, conn);
			} else {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_USERS_TABLE, conn);
			}
			sql = "insert into jam_users ( "
			    +    "username, password "
				+ ") "
				+ "select login, encoded_password "
				+ "from jam_wiki_user_info ";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added jam_users table");
			sql = "alter table jam_wiki_user drop column remember_key";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Dropped the remember_key column from jam_wiki_user");
			if (dbType.equals(DataHandler.DATA_HANDLER_HSQL)) {
				DatabaseConnection.executeUpdate(HSqlQueryHandler.STATEMENT_CREATE_AUTHORITIES_TABLE, conn);
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MYSQL)) {
				DatabaseConnection.executeUpdate(MySqlQueryHandler.STATEMENT_CREATE_AUTHORITIES_TABLE, conn);
			} else {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_AUTHORITIES_TABLE, conn);
			}
			sql = "insert into jam_authorities ( "
			    +    "username, authority "
				+ ") "
				+ "select jam_wiki_user.login, jam_role_map.role_name "
				+ "from jam_wiki_user, jam_role_map "
				+ "where jam_wiki_user.wiki_user_id = jam_role_map.wiki_user_id ";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added jam_authorities table");
			if (dbType.equals(DataHandler.DATA_HANDLER_HSQL)) {
				DatabaseConnection.executeUpdate(HSqlQueryHandler.STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE, conn);
			} else if (dbType.equals(DataHandler.DATA_HANDLER_MYSQL)) {
				DatabaseConnection.executeUpdate(MySqlQueryHandler.STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE, conn);
			} else {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE, conn);
			}
			sql = "insert into jam_group_authorities ( "
			    +    "group_id, authority "
				+ ") "
				+ "select jam_group.group_id, jam_role_map.role_name "
				+ "from jam_group, jam_role_map "
				+ "where jam_group.group_id = jam_role_map.group_id ";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Added jam_group_authorities table");
			sql = "drop table jam_role_map ";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Dropped the jam_role_map table");
			if (dbType.equals(DataHandler.DATA_HANDLER_HSQL)) {
				DatabaseConnection.executeUpdate(MySqlQueryHandler.STATEMENT_CREATE_GROUP_MEMBERS_TABLE, conn);
			} else {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_GROUP_MEMBERS_TABLE, conn);
			}
			sql = "select group_id from jam_group where group_name = '" + WikiGroup.GROUP_REGISTERED_USER + "'";
			WikiResultSet rs = DatabaseConnection.executeQuery(sql, conn);
			int groupId = rs.getInt("group_id");
			sql = "select username from jam_users ";
			rs = DatabaseConnection.executeQuery(sql, conn);
			int id = 1;
			while (rs.next()) {
				sql = "insert into jam_group_members ( "
				    +   "id, username, group_id "
				    + ") values ( "
				    +   id + ", '" + StringEscapeUtils.escapeSql(rs.getString("username")) + "', " + groupId
				    + ") ";
				DatabaseConnection.executeUpdate(sql, conn);
				id++;
			}
			messages.add("Added jam_group_members table");
			sql = "drop table jam_wiki_user_info";
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Dropped jam_wiki_user_info table");
		} catch (Exception e) {
			DatabaseConnection.rollbackOnException(status, e);
			try {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DROP_GROUP_MEMBERS_TABLE);
			} catch (Exception ex) {}
			try {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DROP_GROUP_AUTHORITIES_TABLE);
			} catch (Exception ex) {}
			try {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DROP_AUTHORITIES_TABLE);
			} catch (Exception ex) {}
			try {
				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DROP_USERS_TABLE);
			} catch (Exception ex) {}
			throw e;
		} catch (Error err) {
			DatabaseConnection.rollbackOnException(status, err);
			throw err;
		}
		DatabaseConnection.commit(status);
		// perform a second transaction to populate the new columns.  this code is in its own
		// transaction since if it fails the upgrade can still be considered successful.
		status = DatabaseConnection.startTransaction(getTransactionDefinition());
		try {
			String sql = null;
			if (dbType.equals(DataHandler.DATA_HANDLER_ORACLE)) {
				sql = "update jam_topic_version set characters_changed = ( "
			           +   "select (dbms_lob.getlength(current_version.version_content) - dbms_lob.getlength(previous_version.version_content)) "
			           +   "from jam_topic_version current_version "
			           +   "left outer join jam_topic_version previous_version "
			           +   "on current_version.previous_topic_version_id = previous_version.topic_version_id "
			           +   "where jam_topic_version.topic_version_id = current_version.topic_version_id "
			           + ") ";
			} else {
				sql = "update jam_topic_version set characters_changed = ( "
			           +   "select (char_length(current_version.version_content) - char_length(coalesce(previous_version.version_content, ''))) "
			           +   "from jam_topic_version current_version "
			           +   "left outer join jam_topic_version previous_version "
			           +   "on current_version.previous_topic_version_id = previous_version.topic_version_id "
			           +   "where jam_topic_version.topic_version_id = current_version.topic_version_id "
			           + ") ";
			}
			Connection conn = DatabaseConnection.getConnection();
			DatabaseConnection.executeUpdate(sql, conn);
			messages.add("Populated characters_changed column in jam_topic_version");
		} catch (Throwable t) {
			messages.add("Unable to populate characters_changed colum in jam_topic_version.  Please see UPGRADE.txt for further details on this optional modification." + t.getMessage());
			// do not throw this error and halt the upgrade process - populating the field
			// is not required for existing systems.
			logger.info("Failure while populating characters_changed colum in jam_topic_version.  See UPGRADE.txt for instructions on how to manually complete this optional step.", t);
			try {
				DatabaseConnection.rollbackOnException(status, t);
			} catch (Exception e) {
				// ignore
			}
			status = null; // so we do not try to commit
		}
		if (status != null) {
			DatabaseConnection.commit(status);
		}
		WikiBase.getDataHandler().reloadRecentChanges();
		messages.add("Populated characters_changed column in jam_recent_change");
		return messages;
	}
}
