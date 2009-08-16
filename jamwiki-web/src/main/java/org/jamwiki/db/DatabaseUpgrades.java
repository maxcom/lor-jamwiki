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
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.DataHandler;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
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
	public static boolean login(String username, String password) throws WikiException {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (!oldVersion.before(0, 7, 0)) {
			try {
				return (WikiBase.getDataHandler().authenticate(username, password));
			} catch (DataAccessException e) {
				logger.severe("Unable to authenticate user during upgrade", e);
				throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
			}
		}
		try {
			Connection conn = DatabaseConnection.getConnection();
			WikiPreparedStatement stmt = new WikiPreparedStatement("select 1 from jam_wiki_user_info where login = ? and encoded_password = ?");
			if (!StringUtils.isBlank(password)) {
				password = Encryption.encrypt(password);
			}
			stmt.setString(1, username);
			stmt.setString(2, password);
			WikiResultSet rs = stmt.executeQuery(conn);
			return (rs.size() > 0);
		} catch (SQLException e) {
			logger.severe("Database failure while authenticating user", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
	}

	/**
	 *
	 */
	public static List<WikiMessage> upgrade061(List<WikiMessage> messages) throws WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// delete ROLE_DELETE
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_061_DELETE_ROLE_MAP_ROLE_DELETE", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_061_DELETE_ROLE_ROLE_DELETE", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.deleted", "jam_role_map"));
			messages.add(new WikiMessage("upgrade.message.db.data.deleted", "jam_role"));
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.severe("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
		DatabaseConnection.commit(status);
		return messages;
	}

	/**
	 *
	 */
	public static List<WikiMessage> upgrade063(List<WikiMessage> messages) {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// increase the size of ip address columns
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_063_ALTER_USER_CREATE_IP", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_063_ALTER_USER_LAST_LOGIN_IP", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.modified", "create_ip_address", "jam_wiki_user"));
			messages.add(new WikiMessage("upgrade.message.db.column.modified", "last_login_ip_address", "jam_wiki_user"));
		} catch (SQLException e) {
			// do not throw this error and halt the upgrade process - changing the column size
			// is not required for systems that have already been successfully installed, it
			// is simply being done to keep new installs consistent with existing installs.
			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			logger.warning("Failure while updating database for IPv6 support.  See UPGRADE.txt for instructions on how to manually complete this optional step.", e);
			DatabaseConnection.rollbackOnException(status, e);
			status = null;	// so we do not try to commit
		}
		if (status != null) {
			DatabaseConnection.commit(status);
		}
		return messages;
	}

	/**
	 *
	 */
	public static List<WikiMessage> upgrade070(List<WikiMessage> messages) throws WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// add characters_changed column to jam_topic_version
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_ADD_TOPIC_VERSION_CHARACTERS_CHANGED", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "characters_changed", "jam_topic_version"));
			// add characters_changed column to jam_recent_change
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_ADD_RECENT_CHANGE_CHARACTERS_CHANGED", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "characters_changed", "jam_recent_change"));
			// copy columns from jam_wiki_user_info into jam_wiki_user
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_ADD_USER_EMAIL", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_UPDATE_USER_EMAIL", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "email", "jam_wiki_user"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_ADD_USER_EDITOR", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "editor", "jam_wiki_user"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_ADD_USER_SIGNATURE", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "signature", "jam_wiki_user"));
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_USERS_TABLE", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_INSERT_USERS", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_users"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_DROP_USER_REMEMBER_KEY", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.dropped", "remember_key", "jam_wiki_user"));
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_AUTHORITIES_TABLE", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_INSERT_AUTHORITIES", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_authorities"));
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_INSERT_GROUP_AUTHORITIES", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_group_authorities"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_DROP_ROLE_MAP", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.dropped", "jam_role_map"));
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_GROUP_MEMBERS_TABLE", conn);
			// FIXME - avoid hard coding
			String sql = "select group_id from jam_group where group_name = '" + WikiGroup.GROUP_REGISTERED_USER + "'";
			WikiResultSet rs = DatabaseConnection.executeQuery(sql, conn);
			int groupId = rs.getInt("group_id");
			// FIXME - avoid hard coding
			sql = "select username from jam_users ";
			rs = DatabaseConnection.executeQuery(sql, conn);
			int id = 1;
			while (rs.next()) {
				// FIXME - avoid hard coding
				sql = "insert into jam_group_members ( "
				    +   "id, username, group_id "
				    + ") values ( "
				    +   id + ", '" + StringEscapeUtils.escapeSql(rs.getString("username")) + "', " + groupId
				    + ") ";
				DatabaseConnection.executeUpdate(sql, conn);
				id++;
			}
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_group_members"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_DROP_USER_INFO", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.dropped", "jam_wiki_user_info"));
		} catch (SQLException e) {
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
			logger.severe("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
		DatabaseConnection.commit(status);
		try {
			// perform a second transaction to populate the new columns.  this code is in its own
			// transaction since if it fails the upgrade can still be considered successful.
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_070_UPDATE_TOPIC_VERSION_CHARACTERS_CHANGED", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.populated", "characters_changed", "jam_topic_version"));
		} catch (SQLException e) {
			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			// do not throw this error and halt the upgrade process - populating the field
			// is not required for existing systems.
			logger.warning("Failure while populating characters_changed colum in jam_topic_version.  See UPGRADE.txt for instructions on how to manually complete this optional step.", e);
			try {
				DatabaseConnection.rollbackOnException(status, e);
			} catch (Exception ex) {
				// ignore
			}
			status = null; // so we do not try to commit
		}
		if (status != null) {
			DatabaseConnection.commit(status);
		}
		messages.add(new WikiMessage("upgrade.message.db.column.populated", "characters_changed", "jam_recent_change"));
		return messages;
	}

	/**
	 *
	 */
	public static List<WikiMessage> upgrade080(List<WikiMessage> messages) throws WikiException {
		String dbType = Environment.getValue(Environment.PROP_DB_TYPE);
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			if (StringUtils.equals(dbType, DataHandler.DATA_HANDLER_POSTGRES)) {
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_GROUP_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_GROUP_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "group_id", "jam_group"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_GROUP_MEMBERS_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_MEMBERS_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_GROUP_MEMBERS_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "id", "jam_group_members"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_TOPIC_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_TOPIC_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_id", "jam_topic"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_TOPIC_VERSION_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_VERSION_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_TOPIC_VERSION_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_version_id", "jam_topic_version"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_VIRTUAL_WIKI_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_VIRTUAL_WIKI_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_VIRTUAL_WIKI_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "virtual_wiki_id", "jam_virtual_wiki"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_WIKI_FILE_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_WIKI_FILE_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_id", "jam_file"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_WIKI_FILE_VERSION_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_VERSION_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_WIKI_FILE_VERSION_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_version_id", "jam_file_version"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_WIKI_USER_ID", conn);
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_USER_ID", conn);
				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_WIKI_USER_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "wiki_user_id", "jam_wiki_user"));
			} else if (StringUtils.equals(dbType, DataHandler.DATA_HANDLER_MYSQL)) {
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "group_id", "jam_group"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_MEMBERS_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "id", "jam_group_members"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_id", "jam_topic"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_VERSION_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_version_id", "jam_topic_version"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_VIRTUAL_WIKI_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "virtual_wiki_id", "jam_virtual_wiki"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_id", "jam_file"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_VERSION_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_version_id", "jam_file_version"));
				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_USER_ID", conn);
				messages.add(new WikiMessage("upgrade.message.db.column.modified", "wiki_user_id", "jam_wiki_user"));
			}
			// add jam_log table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_LOG_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_log"));
			// add wiki_user_display column to jam_topic_version
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_TOPIC_VERSION_USER_DISPLAY", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_UPDATE_TOPIC_VERSION_USER_DISPLAY", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "wiki_user_display", "jam_topic_version"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_DROP_TOPIC_VERSION_IP_ADDRESS", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.dropped", "wiki_user_ip_address", "jam_topic_version"));
			// add wiki_user_display column to jam_file_version
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_FILE_VERSION_USER_DISPLAY", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_UPDATE_FILE_VERSION_USER_DISPLAY", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "wiki_user_display", "jam_file_version"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_DROP_FILE_VERSION_IP_ADDRESS", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.dropped", "wiki_user_ip_address", "jam_file_version"));
			// drop and restore the jam_recent_change table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_DROP_RECENT_CHANGE_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.dropped", "jam_recent_change"));
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_RECENT_CHANGE_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_recent_change"));
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.severe("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
		DatabaseConnection.commit(status);
		try {
			// perform a second transaction to assign ROLE_IMPORT.  this code is in its own
			// transaction since if it fails the upgrade can still be considered successful.
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_INSERT_ROLE_ROLE_IMPORT", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_role"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_INSERT_AUTHORITIES_ROLE_IMPORT", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_authorities"));
		} catch (SQLException e) {
			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			// do not throw this error and halt the upgrade process - populating the field
			// is not required for existing systems.
			logger.warning("Failure while populating characters_changed colum in jam_topic_version.  See UPGRADE.txt for instructions on how to manually complete this optional step.", e);
			try {
				DatabaseConnection.rollbackOnException(status, e);
			} catch (Exception ex) {
				// ignore
			}
			status = null; // so we do not try to commit
		}
		if (status != null) {
			DatabaseConnection.commit(status);
		}
		return messages;
	}
}
