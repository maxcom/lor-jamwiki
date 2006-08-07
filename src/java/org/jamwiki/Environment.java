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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
// FIXME - remove this import
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.jamwiki.utils.Utilities;

/**
 * Provides environmental information to the JAMWiki system.
 */
public class Environment {

	static Logger logger = Logger.getLogger(Environment.class.getName());

	public static final String PROP_BASE_COOKIE_EXPIRE = "cookie-expire";
	public static final String PROP_BASE_DEFAULT_TOPIC = "default-topic";
	public static final String PROP_BASE_FILE_DIR = "homeDir";
	public static final String PROP_BASE_INITIALIZED = "props-initialized";
	public static final String PROP_BASE_LOGO_IMAGE = "logo-image";
	public static final String PROP_BASE_PERSISTENCE_TYPE = "persistenceType";
	public static final String PROP_BASE_WIKI_VERSION = "wiki-version";
	public static final String PROP_DB_DRIVER= "driver";
	public static final String PROP_DB_PASSWORD = "db-password";
	public static final String PROP_DB_TYPE = "database-type";
	public static final String PROP_DB_URL = "url";
	public static final String PROP_DB_USERNAME = "db-user";
	public static final String PROP_DBCP_LOG_ABANDONED = "dbcp-log-abandoned";
	public static final String PROP_DBCP_MAX_ACTIVE = "dbcp-max-active";
	public static final String PROP_DBCP_MAX_IDLE = "dbcp-max-idle";
	public static final String PROP_DBCP_MIN_EVICTABLE_IDLE_TIME = "dbcp-min-evictable-idle-time";
	public static final String PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN = "dbcp-num-tests-per-eviction-run";
	public static final String PROP_DBCP_REMOVE_ABANDONED = "dbcp-remove-abandoned";
	public static final String PROP_DBCP_REMOVE_ABANDONED_TIMEOUT = "dbcp-remove-abandoned-timeout";
	public static final String PROP_DBCP_TEST_ON_BORROW = "dbcp-test-on-borrow";
	public static final String PROP_DBCP_TEST_ON_RETURN = "dbcp-test-on-return";
	public static final String PROP_DBCP_TEST_WHILE_IDLE = "dbcp-test-while-idle";
	public static final String PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS = "dbcp-time-between-eviction-runs";
	public static final String PROP_DBCP_VALIDATION_QUERY = "dbcp-validation-query";
	public static final String PROP_DBCP_WHEN_EXHAUSTED_ACTION = "dbcp-when-exhausted-action";
	public static final String PROP_EMAIL_REPLY_ADDRESS = "reply-address";
	public static final String PROP_EMAIL_SMTP_HOST = "smtp-host";
	public static final String PROP_EMAIL_SMTP_PASSWORD = "smtp-password";
	public static final String PROP_EMAIL_SMTP_USERNAME = "smtp-username";
	public static final String PROP_FILE_DIR_FULL_PATH = "file-dir-full-path";
	public static final String PROP_FILE_DIR_RELATIVE_PATH = "file-dir-relative-path";
	public static final String PROP_FILE_MAX_FILE_SIZE = "max-file-size";
	public static final String PROP_PARSER_ALLOW_HTML = "allowHTML";
	public static final String PROP_PARSER_ALLOW_JAVASCRIPT = "allow-javascript";
	public static final String PROP_PARSER_CLASS = "parser";
	public static final String PROP_PARSER_TOC = "allow-toc";
	public static final String PROP_RECENT_CHANGES_DAYS = "recent-changes-days";
	public static final String PROP_SEARCH_INDEX_REFRESH_INTERVAL = "indexRefreshInterval";
	public static final String PROP_TOPIC_BASE_CONTEXT = "base-context";
	public static final String PROP_TOPIC_FORCE_USERNAME = "force-username";
	public static final String PROP_TOPIC_USE_PREVIEW = "use-preview";
	public static final String PROP_TOPIC_VERSIONING_ON = "versioningOn";
	public static final String PROP_USERGROUP_BASIC_SEARCH = "usergroupBasicSearch";
	public static final String PROP_USERGROUP_DETAILVIEW = "usergroupDetailView";
	public static final String PROP_USERGROUP_FACTORY = "usergroupFactory";
	public static final String PROP_USERGROUP_FULLNAME_FIELD = "usergroupFullnameField";
	public static final String PROP_USERGROUP_MAIL_FIELD = "usergroupMailField";
	public static final String PROP_USERGROUP_PASSWORD = "usergroupPassword";
	public static final String PROP_USERGROUP_SEARCH_RESTRICTIONS = "usergroupSearchRestrictions";
	public static final String PROP_USERGROUP_TYPE = "usergroup-type";
	public static final String PROP_USERGROUP_URL = "usergroupUrl";
	public static final String PROP_USERGROUP_USERID_FIELD = "usergroupUseridField";
	public static final String PROP_USERGROUP_USERNAME = "usergroupUsername";
	private static final String PROPERTY_FILE_NAME = "jamwiki.properties";

	private static Properties defaults = null;
	private static Environment instance = null;
	private static Properties props = null;

	// initialize the singleton instance
	static {
		instance = new Environment();
	}

	/**
	 * Constructor loads property values from the property file.
	 */
	private Environment() {
		try {
			initDefaultProperties();
			logger.debug("Default properties initialized: " + defaults.toString());
			props = loadProperties(PROPERTY_FILE_NAME, defaults);
			logger.debug("Property file loaded: " + props.toString());
		} catch (Exception e) {
			logger.error("Failure while initializing property values", e);
		}
	}

	/**
	 * Returns the value of a property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static String getValue(String name) {
		return props.getProperty(name);
	}

	/**
	 * Get the value of an integer property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static int getIntValue(String name) {
		String value = getValue(name);
		try {
			 return Integer.parseInt(value);
		} catch (Exception e) {
			logger.info("Invalid integer property " + name + " with value " + value);
		}
		// FIXME - should this otherwise indicate an invalid property?
		return -1;
	}

	/**
	 * Get the value of a long property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static long getLongValue(String name) {
		String value = getValue(name);
		try {
			 return Long.parseLong(value);
		} catch (Exception e) {
			logger.info("Invalid long property " + name + " with value " + value);
		}
		// FIXME - should this otherwise indicate an invalid property?
		return -1;
	}

	/**
	 * Get the value of a boolean property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static boolean getBooleanValue(String name) {
		String value = getValue(name);
		try {
			return Boolean.valueOf(value).booleanValue();
		} catch (Exception e) {
			logger.error("Invalid boolean property " + name + " with value " + value);
		}
		// FIXME - should this otherwise indicate an invalid property?
		return false;
	}

	/**
	 * Sets a new value for the given property name.
	 *
	 * @param name
	 * @param value
	 */
	public static void setValue(String name, String value) {
		// it is invalid to set a property value null, so convert to empty string
		if (value == null) value = "";
		props.setProperty(name, value);
	}

	/**
	 * Sets a new boolean value for the given property name.
	 *
	 * @param name
	 * @param value
	 */
	public static void setBooleanValue(String name, boolean value) {
		props.setProperty(name, new Boolean(value).toString());
	}

	/**
	 * Sets a new integer value for the given property name.
	 *
	 * @param name
	 * @param value
	 */
	public static void setIntValue(String name, int value) {
		props.setProperty(name, new Integer(value).toString());
	}

	/**
	 * Used to persist the current properties to disk.
	 */
	public static void saveProperties() throws IOException {
		Environment.saveProperties(PROPERTY_FILE_NAME, props, null);
	}

	/**
	 * Used to persist a property file to disk.
	 *
	 * @param propertyFile The name of the property file to save.
	 * @param properties The properties object that is to be saved.
	 * @param comments A comment to save in the properties file.
	 */
	public static void saveProperties(String propertyFile, Properties properties, String comments) throws IOException {
		File file = findProperties(propertyFile);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			properties.store(out, comments);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// ignore, unimportant if a close fails
				}
			}
		}
	}

	/**
	 * Load a property file and return the property file object.
	 *
	 * @param propertyFile The name of the property file to load.
	 * @return The loaded property file.
	 */
	public static Properties loadProperties(String propertyFile) {
		return loadProperties(propertyFile, null);
	}

	/**
	 * Load a property file and return the property file object.
	 *
	 * @param propertyFile The name of the property file to load.
	 * @param defaults Default property values.
	 * @return The loaded property file.
	 */
	public static Properties loadProperties(String propertyFile, Properties def) {
		Properties properties = new Properties();
		if (def != null) {
			properties = new Properties(def);
		}
		File file = null;
		try {
			file = findProperties(propertyFile);
			if (file == null) {
				logger.warn("Property file " + propertyFile + " does not exist");
			} else if (!file.exists()) {
				logger.warn("Property file " + file.toString() + " does not exist");
			} else {
				logger.warn("Loading properties from " + file.toString());
				properties.load(new FileInputStream(file));
			}
		} catch (Exception e) {
			logger.error("Failure while trying to load properties file " + file.toString(), e);
		}
		return properties;
	}

	/**
	 * Initialize the default property values.
	 */
	private static void initDefaultProperties() {
		defaults = new Properties();
		defaults.setProperty(PROP_BASE_COOKIE_EXPIRE, "31104000");
		defaults.setProperty(PROP_BASE_DEFAULT_TOPIC, "StartingPoints");
		defaults.setProperty(PROP_BASE_FILE_DIR, "");
		defaults.setProperty(PROP_BASE_LOGO_IMAGE, "logo.gif");
		defaults.setProperty(PROP_BASE_INITIALIZED, "false");
		defaults.setProperty(PROP_BASE_PERSISTENCE_TYPE, "FILE");
		defaults.setProperty(PROP_BASE_WIKI_VERSION, "0.0.0");
		defaults.setProperty(PROP_DB_DRIVER, "org.postgresql.Driver");
		defaults.setProperty(PROP_DB_PASSWORD, "");
		defaults.setProperty(PROP_DB_TYPE, "postgres");
		defaults.setProperty(PROP_DB_URL, "jdbc:postgresql://localhost:5432/database");
		defaults.setProperty(PROP_DB_USERNAME, "");
		defaults.setProperty(PROP_DBCP_LOG_ABANDONED, "true");
		defaults.setProperty(PROP_DBCP_MAX_ACTIVE, "10");
		defaults.setProperty(PROP_DBCP_MAX_IDLE, "3");
		defaults.setProperty(PROP_DBCP_MIN_EVICTABLE_IDLE_TIME, "600");
		defaults.setProperty(PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN, "5");
		defaults.setProperty(PROP_DBCP_REMOVE_ABANDONED, "true");
		defaults.setProperty(PROP_DBCP_REMOVE_ABANDONED_TIMEOUT, "120");
		defaults.setProperty(PROP_DBCP_TEST_ON_BORROW, "true");
		defaults.setProperty(PROP_DBCP_TEST_ON_RETURN, "true");
		defaults.setProperty(PROP_DBCP_TEST_WHILE_IDLE, "true");
		defaults.setProperty(PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS, "120");
		defaults.setProperty(PROP_DBCP_VALIDATION_QUERY, "SELECT 1");
		defaults.setProperty(PROP_DBCP_WHEN_EXHAUSTED_ACTION, String.valueOf(GenericObjectPool.WHEN_EXHAUSTED_GROW));
		defaults.setProperty(PROP_EMAIL_REPLY_ADDRESS, "");
		defaults.setProperty(PROP_EMAIL_SMTP_HOST, "");
		defaults.setProperty(PROP_EMAIL_SMTP_PASSWORD, "");
		defaults.setProperty(PROP_EMAIL_SMTP_USERNAME, "");
		defaults.setProperty(PROP_FILE_DIR_FULL_PATH, "");
		defaults.setProperty(PROP_FILE_DIR_RELATIVE_PATH, "");
		// size is in bytes
		defaults.setProperty(PROP_FILE_MAX_FILE_SIZE, "2000000");
		defaults.setProperty(PROP_PARSER_ALLOW_HTML, "true");
		defaults.setProperty(PROP_PARSER_ALLOW_JAVASCRIPT, "false");
		defaults.setProperty(PROP_PARSER_CLASS, "org.jamwiki.parser.JAMWikiParser");
		defaults.setProperty(PROP_PARSER_TOC, "false");
		defaults.setProperty(PROP_RECENT_CHANGES_DAYS, "100");
		defaults.setProperty(PROP_SEARCH_INDEX_REFRESH_INTERVAL, "1440");
		defaults.setProperty(PROP_TOPIC_FORCE_USERNAME, "false");
		defaults.setProperty(PROP_TOPIC_USE_PREVIEW, "true");
		defaults.setProperty(PROP_TOPIC_VERSIONING_ON, "true");
		defaults.setProperty(PROP_USERGROUP_BASIC_SEARCH, "ou=users,dc=mycompany,dc=com");
		defaults.setProperty(PROP_USERGROUP_DETAILVIEW, "@@cn@@</a><br/>@@title@@<br/>Telefon: @@telephoneNumber@@<br/>Mobil: @@mobile@@<br/>@@ou@@ / @@businessCategory@@<br/><a href=\"mailto:@@mail@@\">@@mail@@</a> <br/>");
		defaults.setProperty(PROP_USERGROUP_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		defaults.setProperty(PROP_USERGROUP_FULLNAME_FIELD, "cn");
		defaults.setProperty(PROP_USERGROUP_MAIL_FIELD, "mail");
		defaults.setProperty(PROP_USERGROUP_PASSWORD, "");
		defaults.setProperty(PROP_USERGROUP_SEARCH_RESTRICTIONS, "objectClass=person");
		defaults.setProperty(PROP_USERGROUP_TYPE, "0");
		defaults.setProperty(PROP_USERGROUP_URL, "ldap://localhost:389");
		defaults.setProperty(PROP_USERGROUP_USERID_FIELD, "uid");
		defaults.setProperty(PROP_USERGROUP_USERNAME, "");
	}

	/**
	 * Load a property file.  First check for the file in the path from which
	 * the application was started, then check other classpath locations.
	 *
	 * @param filename The name of the property file to be loaded.  This name can be
	 *  either absolute or relative; if relative then the file will be loaded from
	 *  the class path or from the directory from which the JVM was loaded.
	 */
	private static File findProperties(String filename) throws FileNotFoundException {
		// read in properties file
		File file = new File(filename);
		if (file.exists()) {
			return file;
		}
		// search for file in class loader path
		return Environment.getPropertyFile(filename);
	}

	/**
	 * Utility methods for retrieving property files from the class path, based on
	 * code from the org.apache.log4j.helpers.Loader class.
	 *
	 * @param filename Given a filename return a File object for the file.  The filename
	 *  may be relative to the class path or the directory from which the JVM was
	 *  initialized.
	 */
	private static File getPropertyFile(String filename) {
		File file = null;
		try {
			file = Utilities.getClassLoaderFile(filename);
			return file;
		} catch (Exception e) {
			// file might not exist
		}
		try {
			file = new File(Utilities.getClassLoaderRoot(), filename);
			return file;
		} catch (Exception e) {
			logger.error("Error while searching for resource " + filename, e);
		}
		return null;
	}
}
