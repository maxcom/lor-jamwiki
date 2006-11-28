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

import java.util.Locale;
import org.jamwiki.model.WikiUser;
import org.jamwiki.search.LuceneSearchEngine;
import org.jamwiki.search.SearchEngine;
import org.jamwiki.utils.InterWikiHandler;
import org.jamwiki.utils.PseudoTopicHandler;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * This class should be used for retrieving core JAMWiki elements, such as the
 * current data handler (database, file, etc).
 */
public class WikiBase {

	/** Standard logger. */
	private static WikiLogger logger = WikiLogger.getLogger(WikiBase.class.getName());
	/** The singleton instance of this class. */
	private static WikiBase instance = null;
	/** The data handler that looks after read/write operations. */
	private static DataHandler dataHandler = null;
	/** The handler for user login/authentication. */
	private static UserHandler userHandler = null;
	/** The search engine instance. */
	private static SearchEngine searchEngine = null;

	/** Cache name for the cache of parsed topic content. */
	public static final String CACHE_PARSED_TOPIC_CONTENT = "org.jamwiki.WikiBase.CACHE_PARSED_TOPIC_CONTENT";
	/** The topics are stored in a flat file */
	public static final int PERSISTENCE_INTERNAL_DB = 0;
	/** The topics are stored in a database */
	public static final int PERSISTENCE_EXTERNAL_DB = 1;
	/** Name of the default wiki */
	public static final String DEFAULT_VWIKI = "en";
	/** Root directory within the WAR distribution that contains the default topic pages. */
	public static final String SPECIAL_PAGE_DIR = "pages";
	/** Name of the default starting points topic. */
	public static final String SPECIAL_PAGE_STARTING_POINTS = "StartingPoints";
	/** Name of the default left menu topic. */
	public static final String SPECIAL_PAGE_LEFT_MENU = "LeftMenu";
	/** Name of the default footer topic. */
	public static final String SPECIAL_PAGE_BOTTOM_AREA = "BottomArea";
	/** Name of the default jamwiki.css topic. */
	public static final String SPECIAL_PAGE_SPECIAL_PAGES = "SpecialPages";
	/** Name of the default jamwiki.css topic. */
	public static final String SPECIAL_PAGE_STYLESHEET = "StyleSheet";
	/** Database user handler class */
	public static final String USER_HANDLER_DATABASE = "org.jamwiki.db.DatabaseUserHandler";
	/** LDAP user handler class */
	public static final String USER_HANDLER_LDAP = "org.jamwiki.ldap.LdapUserHandler";

	static {
		try {
			WikiBase.instance = new WikiBase();
		} catch (Exception e) {
			logger.severe("Failure while initializing WikiBase", e);
		}
	}

	/**
	 * Creates an instance of <code>WikiBase</code>, initializing the default
	 * data handler instance and search engine instance.
	 *
	 * @throws Exception If the instance cannot be instantiated.
	 */
	private WikiBase() throws Exception {
		WikiBase.dataHandler = Utilities.dataHandlerInstance();
		WikiBase.userHandler = Utilities.userHandlerInstance();
		this.searchEngine = new LuceneSearchEngine();
	}

	/**
	 * Utility method for determining if a topic exists.  This method will
	 * return true if a method is a special topic (such as the recent changes
	 * page) or if it is an existing topic.
	 *
	 * @param virtualWiki The virtual wiki for the topic being checked.
	 * @param topicName The name of the topic that is being checked.
	 * @return <code>true</code> if the topic exists or is a special system topic.
	 * @throws Exception Thrown if any error occurs during lookup.
	 */
	public static boolean exists(String virtualWiki, String topicName) throws Exception {
		if (PseudoTopicHandler.isPseudoTopic(topicName)) {
			return true;
		}
		if (InterWikiHandler.isInterWiki(topicName)) {
			return true;
		}
		if (!StringUtils.hasText(Environment.getValue(Environment.PROP_BASE_FILE_DIR)) || !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
			// not initialized yet
			return false;
		}
		return WikiBase.dataHandler.exists(virtualWiki, topicName);
	}

	/**
	 * Get an instance of the current data handler.
	 *
	 * @return The current data handler instance, or <code>null</code>
	 *  if the handler has not yet been initialized.
	 */
	public static DataHandler getDataHandler() {
		return WikiBase.dataHandler;
	}

	/**
	 * Return an instance of the current persistency type, either internal
	 * or external.
	 *
	 * @return The current persistency type.
	 */
	public static int getPersistenceType() {
		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("DATABASE")) {
			return WikiBase.PERSISTENCE_EXTERNAL_DB;
		} else {
			return WikiBase.PERSISTENCE_INTERNAL_DB;
		}
	}

	/**
	 * Get an instance of the current search engine.
	 *
	 * @return The current search engine instance.
	 */
	public static SearchEngine getSearchEngine() {
		return WikiBase.searchEngine;
	}

	/**
	 *
	 */
	public static UserHandler getUserHandler() {
		return WikiBase.userHandler;
	}

	/**
	 * Reset the WikiBase object, re-initializing the data handler and
	 * other values.
	 *
	 * @param locale The locale to be used if any system pages need to be set up
	 *  as a part of the initialization process.
	 * @param user A sysadmin user to be used in case any system pages need to
	 *  be created as a part of the initialization process.
	 * @throws Exception Thrown if an error occurs during re-initialization.
	 */
	public static void reset(Locale locale, WikiUser user) throws Exception {
		WikiMail.init();
		WikiBase.instance = new WikiBase();
		WikiCache.initialize();
		WikiBase.dataHandler.setup(locale, user);
		WikiBase.searchEngine.refreshIndex();
	}
}
