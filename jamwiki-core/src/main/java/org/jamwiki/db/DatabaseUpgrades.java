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
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.WikiLogger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class simply contains utility methods for upgrading database schemas
 * (if needed) between JAMWiki versions.  These methods are typically called automatically
 * by the UpgradeServlet when an upgrade is detected and will automatically upgrade the
 * database schema without the need for manual intervention from the user.
 *
 * In general upgrade methods will only be maintained for two major releases and then
 * deleted - for example, JAMWiki version 0.9.0 will not support upgrading from versions
 * prior to 0.7.0.
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
		try {
			return WikiBase.getDataHandler().authenticate(username, password);
		} catch (DataAccessException e) {
			logger.error("Unable to authenticate user during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
	}

	/**
	 *
	 */
	public static List<WikiMessage> upgrade090(List<WikiMessage> messages) throws WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// add the namespace tables
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_NAMESPACE_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_namespace"));
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_NAMESPACE_TRANSLATION_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_namespace_translation"));
			// populate the namespace table
			WikiDatabase.setupDefaultNamespaces();
			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_namespace"));
			// update jam_topic to add a namespace column, defaulted to the main namespace
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_NAMESPACE_ID", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_NAMESPACE_ID_CONSTRAINT", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "namespace_id", "jam_topic"));
			// update jam_topic to add page_name and page_name_lower
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "page_name", "jam_topic"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME_LOWER", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "page_name_lower", "jam_topic"));
			// add an index for topic_id on the jam_topic_version table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_VERSION_TOPIC_INDEX", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_topic_version"));
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		} catch (DataAccessException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
		DatabaseConnection.commit(status);
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// populate jam_topic.namespace_id, jam_topic.page_name and jam_topic.page_name_lower
			int numUpdated = WikiDatabase.fixIncorrectTopicNamespaces();
			messages.add(new WikiMessage("admin.maintenance.message.topicsUpdated", Integer.toString(numUpdated)));
			// add not null constraints for jam_topic.page_name and jam_topic.page_name_lower
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME_NOT_NULL_CONSTRAINT", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.modified", "page_name", "jam_topic"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME_LOWER_NOT_NULL_CONSTRAINT", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.modified", "page_name_lower", "jam_topic"));
			// add the indexes after adding the not null constraint, otherwise MS SQL Server gets angry
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_PAGE_NAME_INDEX", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_PAGE_NAME_LOWER_INDEX", conn);
		} catch (SQLException e) {
			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			// do not throw this error and halt the upgrade process - populating the field
			// is not required for existing systems.
			logger.warn("Failure while populating correct namespace_id values in the jam_topic table.  Try running the 'Fix Incorrect Topic Namespaces' from Special:Maintenance to complete this step.", e);
			try {
				DatabaseConnection.rollbackOnException(status, e);
			} catch (Exception ex) {
				// ignore
			}
			status = null; // so we do not try to commit
		} catch (DataAccessException e) {
			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			// do not throw this error and halt the upgrade process - populating the field
			// is not required for existing systems.
			logger.warn("Failure while populating correct namespace_id values in the jam_topic table.  Try running the 'Fix Incorrect Topic Namespaces' from Special:Maintenance to complete this step.", e);
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

	/**
	 * The following updates the jam_virtual_wiki table, but these changes are required for
	 * a successful 0.9.0 upgrade so move them into a separate method so that they can be
	 * executed before anything else.
	 */
	public static List<WikiMessage> preUpgrade100(List<WikiMessage> messages) throws WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_100_ADD_VIRTUAL_WIKI_LOGO_URL", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "logo_image_url", "jam_virtual_wiki"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_100_ADD_VIRTUAL_WIKI_SITE_NAME", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "site_name", "jam_virtual_wiki"));
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_100_ADD_VIRTUAL_WIKI_META_DESCRIPTION", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.added", "meta_description", "jam_virtual_wiki"));
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
		DatabaseConnection.commit(status);
		return messages;
	}

	/**
	 *
	 */
	public static List<WikiMessage> upgrade100(List<WikiMessage> messages) throws WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// add the jam_topic_links table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_LINKS_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_topic_links"));
			// add the interwiki table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_INTERWIKI_TABLE", conn);
			// populate the jam_interwiki table
			WikiDatabase.setupDefaultInterwikis();
			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_interwiki"));
			// add the jam_configuration table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_CONFIGURATION_TABLE", conn);
			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_configuration"));
			// drop the not null constraints for jam_virtual_wiki.default_topic_name
			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_100_DROP_VIRTUAL_WIKI_DEFAULT_TOPIC_NOT_NULL", conn);
			messages.add(new WikiMessage("upgrade.message.db.column.modified", "default_topic_name", "jam_virtual_wiki"));
		} catch (DataAccessException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
		DatabaseConnection.commit(status);
		try {
			// perform a separate transaction to update existing data.  this code is in its own
			// transaction since if it fails the upgrade can still be considered successful.
			status = DatabaseConnection.startTransaction(getTransactionDefinition());
			Connection conn = DatabaseConnection.getConnection();
			// add an index to the jam_topic_links table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_LINKS_INDEX", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_topic_links"));
			// add an index to the jam_category table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_CATEGORY_INDEX", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_category"));
			// add indexes to the jam_topic table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_CURRENT_VERSION_INDEX", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_NAMESPACE_INDEX", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_VIRTUAL_WIKI_INDEX", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_topic"));
			// add several indexes to the jam_topic_version table
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_VERSION_PREVIOUS_INDEX", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_VERSION_USER_DISPLAY_INDEX", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_VERSION_USER_ID_INDEX", conn);
			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_topic_version"));
		} catch (SQLException e) {
			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			// do not throw this error and halt the upgrade process - populating the field
			// is not required for existing systems.
			logger.warn("Non-fatal error while upgrading.", e);
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
