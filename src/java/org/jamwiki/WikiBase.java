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
import org.apache.log4j.Logger;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.persistency.file.FileHandler;
import org.jamwiki.search.LuceneSearchEngine;
import org.jamwiki.servlets.JAMWikiServlet;
import org.jamwiki.users.LdapUsergroup;
import org.jamwiki.users.NoUsergroup;
import org.jamwiki.users.Usergroup;
import org.springframework.util.StringUtils;

/**
 * This class represents the core of JAMWiki. It has some central methods, like parsing the URI, and keeps an
 * instance of the <code>Environment</code> class.
 */
public class WikiBase {

	// FIXME - remove this
	public final static String WIKI_VERSION = "0.3.0";
	/** An instance to myself. Singleton pattern. */
	private static WikiBase instance;
	/** The topics are stored in a flat file */
	public static final int FILE = 0;
	/** The topics are stored in a database */
	public static final int DATABASE = 1;
	/** Members are retrieved from LDAP */
	public static final int LDAP = 2;
	/** Name of the default wiki */
	public static final String DEFAULT_VWIKI = "en";
	/** Log output */
	private static final Logger logger = Logger.getLogger(WikiBase.class);
	/** The handler that looks after read/write operations for a persitence type */
	private static PersistencyHandler handler;
	public static final String NAMESPACE_COMMENTS = "Comments:";
	public static final String NAMESPACE_IMAGE = "Image:";
	public static final String NAMESPACE_IMAGE_COMMENTS = "Image comments:";
	public static final String NAMESPACE_SPECIAL = "Special:";
	public static final String NAMESPACE_USER = "User:";
	public static final String NAMESPACE_USER_COMMENTS = "User comments:";
	public static final String SPECIAL_PAGE_DIR = "pages";
	public static final String SPECIAL_PAGE_STARTING_POINTS = "StartingPoints";
	public static final String SPECIAL_PAGE_LEFT_MENU = "LeftMenu";
	public static final String SPECIAL_PAGE_BOTTOM_AREA = "BottomArea";
	public static final String SPECIAL_PAGE_STYLESHEET = "StyleSheet";
	public static final String SPECIAL_PAGE_ADMIN_ONLY_TOPICS = "AdminOnlyTopics";

	static {
		try {
			WikiBase.instance = new WikiBase();
		} catch (Exception e) {
			logger.error("Failure while initializing WikiBase", e);
		}
	}

	/**
	 * Creates an instance of <code>WikiBase</code> with a specified persistency sub-system.
	 *
	 * @param persistencyType
	 * @throws Exception If the handler cannot be instanciated.
	 */
	private WikiBase() throws Exception {
		String type = Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE);
		if (type != null && type.equals("DATABASE")) {
			WikiBase.handler = new DatabaseHandler();
		} else {
			WikiBase.handler = new FileHandler();
		}
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public static boolean exists(String virtualWiki, String topicName) throws Exception {
		if (PseudoTopicHandler.isPseudoTopic(topicName)) {
			return true;
		}
		if (!StringUtils.hasText(Environment.getValue(Environment.PROP_BASE_FILE_DIR)) || !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
			// not initialized yet
			return false;
		}
		return WikiBase.handler.exists(virtualWiki, topicName);
	}

	/**
	 * return the current handler instance
	 *
	 * @return the current handler instance
	 */
	public static PersistencyHandler getHandler() {
		if (!WikiBase.handler.isInitialized()) {
			// not initialized yet
			return null;
		}
		return WikiBase.handler;
	}

	/**
	 *
	 */
	public static int getPersistenceType() {
		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("DATABASE")) {
			return WikiBase.DATABASE;
		} else {
			return WikiBase.FILE;
		}
	}

	/**
	 * Get an instance of the user group
	 */
	public static Usergroup getUsergroupInstance() throws Exception {
		switch (Usergroup.getUsergroupType()) {
			case LDAP:
				return LdapUsergroup.getInstance();
			//TODO case DATABASE:
			//  return DatabaseUsergroup.getInstance();
			default:
				return NoUsergroup.getInstance();
		}
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public static void reset(Locale locale, WikiUser user) throws Exception {
		WikiMail.init();
		WikiBase.instance = new WikiBase();
		if (!WikiBase.handler.isInitialized()) {
			WikiBase.handler.initialize(locale, user);
		}
		JAMWikiServlet.removeCachedContents();
		LuceneSearchEngine.refreshIndex();
	}

	/**
	 *
	 */
	public static void setHandler(PersistencyHandler handler) {
		WikiBase.handler = handler;
	}
}
