/**
 * Copyright 2006 - Martijn van der Kleijn.
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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jmwiki;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.jmwiki.persistency.PersistencyHandler;
import org.jmwiki.persistency.db.DBDate;
import org.jmwiki.persistency.db.DatabaseChangeLog;
import org.jmwiki.persistency.db.DatabaseHandler;
import org.jmwiki.persistency.db.DatabaseNotify;
import org.jmwiki.persistency.db.DatabaseSearchEngine;
import org.jmwiki.persistency.db.DatabaseVersionManager;
import org.jmwiki.persistency.db.DatabaseWikiMembers;
import org.jmwiki.persistency.file.FileChangeLog;
import org.jmwiki.persistency.file.FileExtensionFilter;
import org.jmwiki.persistency.file.FileHandler;
import org.jmwiki.persistency.file.FileNotify;
import org.jmwiki.persistency.file.FileSearchEngine;
import org.jmwiki.persistency.file.FileVersionManager;
import org.jmwiki.persistency.file.FileWikiMembers;
import org.jmwiki.parser.AbstractParser;
import org.jmwiki.parser.alt.BackLinkLex;
import org.jmwiki.users.LdapUsergroup;
import org.jmwiki.users.NoUsergroup;
import org.jmwiki.users.Usergroup;

/**
 * This class represents the core of JMWiki. It has some central methods, like parsing the URI, and keeps an
 * instance of the <code>Environment</code> class.
 */
public class WikiBase {

	// FIXME - remove this
	public final static String WIKI_VERSION = "0.0.1";
	private static WikiBase instance;					   /** An instance to myself. Singleton pattern. */
	public static final int FILE = 0;					   /** The topics are stored in a flat file */
	public static final int DATABASE = 1;				   /** The topics are stored in a database */
	public static final int LDAP = 2;					   /** Members are retrieved from LDAP */
	public static final String DEFAULT_VWIKI = "jsp";	   /** Name of the default wiki */
	public static final String PLUGINS_DIR = "plugins";	 /** Name of the Plugins-Directory */
	private static final Logger logger = Logger.getLogger(WikiBase.class);  /** Log output */
	protected PersistencyHandler handler;				   /** The handler that looks after read/write operations for a persitence type */
	private List topicListeners;							/** Listeners for topic changes */
	private int virtualWikiCount;						   /** Number of virtual wikis */


	/**
	 * Creates an instance of <code>WikiBase</code> with a specified persistency sub-system.
	 *
	 * @param persistencyType
	 * @throws Exception If the handler cannot be instanciated.
	 */
	private WikiBase(int persistencyType) throws Exception {
		switch (persistencyType) {
			case FILE:
				this.handler = new FileHandler();
				break;
			case DATABASE:
				this.handler = new DatabaseHandler();
				break;
		}

		new SearchRefreshThread(
			Environment.getIntValue(Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL)
		);

		PluginManager.getInstance().installAll();
		this.topicListeners = new ArrayList();
		this.topicListeners.addAll(PluginManager.getInstance().getTopicListeners());
	}


	/**
	 * Singleton. Retrieves an intance of <code>WikiBase</code> and creates one if it doesn't exist yet.
	 *
	 * @return Instance of this class
	 * @throws Exception If the storage produces errors
	 */
	public static WikiBase getInstance() throws Exception {
		int persistenceType = -1;
		String type = Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE);
		if (type != null && type.equals("DATABASE")) {
			persistenceType = WikiBase.DATABASE;
		} else {
			persistenceType = WikiBase.FILE;
		}
		if (instance == null) {
			instance = new WikiBase(persistenceType);
		}
		return instance;
	}


	/**
	 * Get an instance to the search enginge.
	 *
	 * @return Reference to the SearchEngine
	 * @throws Exception the current search engine
	 */
	public SearchEngine getSearchEngineInstance() throws Exception {
		switch (WikiBase.getPersistenceType()) {
			case FILE:
				return FileSearchEngine.getInstance();
			case DATABASE:
				return DatabaseSearchEngine.getInstance();
			default:
				return FileSearchEngine.getInstance();
		}
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
	 *
	 * @return Reference to the SearchEngine
	 * @throws Exception the current search engine
	 */
	public Usergroup getUsergroupInstance() throws Exception {
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
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public VersionManager getVersionManagerInstance() throws Exception {
		switch (WikiBase.getPersistenceType()) {
			case FILE:
				return FileVersionManager.getInstance();
			case DATABASE:
				return DatabaseVersionManager.getInstance();
			default:
				return FileVersionManager.getInstance();
		}
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public ChangeLog getChangeLogInstance() throws Exception {
		switch (WikiBase.getPersistenceType()) {
			case FILE:
				return FileChangeLog.getInstance();
			case DATABASE:
				return DatabaseChangeLog.getInstance();
			default:
				return FileChangeLog.getInstance();
		}
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topic	   TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public Notify getNotifyInstance(String virtualWiki, String topic) throws Exception {
		switch (WikiBase.getPersistenceType()) {
			case FILE:
				return new FileNotify(virtualWiki, topic);
			case DATABASE:
				return new DatabaseNotify(virtualWiki, topic);
			default:
				return new FileNotify();
		}
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public WikiMembers getWikiMembersInstance(String virtualWiki) throws Exception {
		switch (WikiBase.getPersistenceType()) {
			case FILE:
				return new FileWikiMembers(virtualWiki);
			case DATABASE:
				return new DatabaseWikiMembers(virtualWiki);
			default:
				return new FileWikiMembers(virtualWiki);
		}
	}

	/**
	 * Finds a default topic file and returns the contents
	 *
	 * FIXME - this doesn't belong here
	 */
	public static String readDefaultTopic(String topicName) throws Exception {
		String resourceName = "/" + topicName + ".txt";
		java.net.URL resource = WikiBase.class.getResource(resourceName);
		if (resource == null) {
			throw new IllegalArgumentException("unknown default topic: " + topicName);
		}
		File f = new File(WikiBase.class.getResource(resourceName).getFile());
		logger.debug("Found the default topic: " + f);
		// Previous implementation using Readers (UTF-8) was adding a \n to the end
		// of the file resulting in an unwanted <br> in pages, and causing problems
		// when rendering them in a layout of composed wiki pages
		// (top-area, bottom-area, etc).
		InputStream in = WikiBase.class.getResourceAsStream(resourceName);
		BufferedInputStream is = new BufferedInputStream(in);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream os = new BufferedOutputStream(baos);
		String output = null;
		try {
			int c;
			while ((c = is.read()) != -1) {
				os.write(c); // also... it uses the buffers
			}
			os.flush();
			output = baos.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Closes the streams, if necessary
			if (is != null) {
				try {
					is.close();
				} catch (Exception ignore) {
					logger.warn(
						"exception closing file (you can ignore this warning)",
						ignore
					);
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception ignore) {
					logger.warn(
						"exception closing output stream (you can ignore this warning)",
						ignore
					);
				}
			}
			in.close();
		}
		return output;
	}

	/**
	 * Reads a file and returns the raw contents. Used for the editing version.
	 */
	public synchronized String readRaw(String virtualWiki, String topicName) throws Exception {
		return handler.read(virtualWiki, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public synchronized boolean exists(String virtualWiki, String topicName) throws Exception {
		if (PseudoTopicHandler.getInstance().isPseudoTopic(topicName)) {
			return true;
		}
		return handler.exists(virtualWiki, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki	TODO: DOCUMENT ME!
	 * @param topicName	  TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public synchronized String readCooked(String context, String virtualWiki, String topicName) throws Exception {
		String s = handler.read(virtualWiki, topicName);
		BufferedReader in = new BufferedReader(new StringReader(s));
		return cook(context, virtualWiki, in);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param in		  TODO: DOCUMENT ME!
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @return				TODO: DOCUMENT ME!
	 * @throws Exception	TODO: DOCUMENT ME!
	 */
	public synchronized String cook(String context, String virtualWiki, BufferedReader in) throws Exception {
		String parserClass = Environment.getValue(Environment.PROP_PARSER_CLASS);
		logger.debug("Using parser: " + parserClass);
		Class clazz = Class.forName(parserClass);
		Class[] parameterTypes = null;
		Constructor constructor = clazz.getConstructor(parameterTypes);
		Object[] initArgs = null;
		AbstractParser parser = (AbstractParser)constructor.newInstance(initArgs);
		String line;
		StringBuffer raw = new StringBuffer();
		while ((line = in.readLine()) != null) {
			raw.append(line).append("\n");
		}
		return parser.parseHTML(context, virtualWiki, raw.toString());
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @param contents	TODO: DOCUMENT ME!
	 */
	private void fireTopicSaved(String virtualWiki, String topicName,
		String contents, String user, Date time) {
		logger.debug("topic saved event: " + topicListeners);
		if (topicListeners == null) {
			return;
		}
		for (Iterator iterator = topicListeners.iterator(); iterator.hasNext();) {
			TopicListener listener = (TopicListener) iterator.next();
			listener.topicSaved(
				new TopicSavedEvent(
					virtualWiki, topicName,
					contents, user, time
				)
			);
		}
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param contents	TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public synchronized void write(String virtualWiki, String contents, String topicName, String user) throws Exception {
		// If the last line is not a return value, the parser can be tricked out.
		// (got this from wikipedia)
		if (!contents.endsWith("\n")) {
			contents += "\n";
		}
		fireTopicSaved(virtualWiki, topicName, contents, user, new Date());
		handler.write(virtualWiki, contents, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @param key		 TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public synchronized boolean holdsLock(String virtualWiki, String topicName,
		String key) throws Exception {
		return handler.holdsLock(virtualWiki, topicName, key);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @param key		 TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public synchronized boolean lockTopic(String virtualWiki, String topicName,
		String key) throws Exception {
		return handler.lockTopic(virtualWiki, topicName, key);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public synchronized void unlockTopic(String virtualWiki, String topicName) throws Exception {
		handler.unlockTopic(virtualWiki, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public boolean isTopicReadOnly(String virtualWiki, String topicName) throws Exception {
		return this.handler.isTopicReadOnly(virtualWiki, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @return TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		return this.handler.getReadOnlyTopics(virtualWiki);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		this.handler.addReadOnlyTopic(virtualWiki, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param virtualWiki TODO: DOCUMENT ME!
	 * @param topicName   TODO: DOCUMENT ME!
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		this.handler.removeReadOnlyTopic(virtualWiki, topicName);
	}

	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public static void initialise() throws Exception {
		int persistenceType = WikiBase.getPersistenceType();
		WikiMail.init();
		instance = new WikiBase(persistenceType);
	}

	/**
	 * Find all topics without links to them
	 */
	public Collection getOrphanedTopics(String virtualWiki) throws Exception {
		Collection results = new HashSet();
		Collection all = getSearchEngineInstance().getAllTopicNames(virtualWiki);
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			String topicName = (String) iterator.next();
			Collection matches = getSearchEngineInstance().findLinkedTo(
				virtualWiki,
				topicName
			);
			logger.debug(topicName + ": " + matches.size() + " matches");
			if (matches.size() == 0) {
				results.add(topicName);
			}
		}
		logger.debug(results.size() + " orphaned topics found");
		return results;
	}

	/**
	 * Find all topics that haven't been written but are linked to
	 */
	public Collection getToDoWikiTopics(String virtualWiki) throws Exception {
		Collection results = new TreeSet();
		Collection all = getSearchEngineInstance().getAllTopicNames(virtualWiki);
		Set topicNames = new HashSet();
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			String topicName = (String) iterator.next();
			String content = handler.read(virtualWiki, topicName);
			StringReader reader = new StringReader(content);
			BackLinkLex lexer = new BackLinkLex(reader);
			while (lexer.yylex() != null) {
				;
			}
			reader.close();
			topicNames.addAll(lexer.getLinks());
		}
		for (Iterator iterator = topicNames.iterator(); iterator.hasNext();) {
			String topicName = (String) iterator.next();
			if (!PseudoTopicHandler.getInstance().isPseudoTopic(topicName)
				&& !handler.exists(virtualWiki, topicName)
				&& !"\\\\\\\\link\\\\\\\\".equals(topicName)) {
				results.add(topicName);
			}
		}
		logger.debug(results.size() + " todo topics found");
		return results;
	}

	/**
	 * Return a list of all virtual wikis on the server
	 */
	public Collection getVirtualWikiList() throws Exception {
		return this.handler.getVirtualWikiList();
	}

	/**
	 * Purge deleted files
	 *
	 * @return a collection of strings that are the deleted topic names
	 */
	public Collection purgeDeletes(String virtualWiki) throws Exception {
		return this.handler.purgeDeletes(virtualWiki);
	}

	/**
	 * Add virtual wiki
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception {
		this.handler.addVirtualWiki(virtualWiki);
	}

	/**
	 * purge versions older than a certain date
	 */
	public void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception {
		this.handler.purgeVersionsOlderThan(virtualWiki, date);
	}

	/**
	 * get a count of the number of virtual wikis in the system
	 *
	 * @return the number of virtual wikis
	 */
	public int getVirtualWikiCount() {
		if (virtualWikiCount == 0) {
			try {
				virtualWikiCount = getVirtualWikiList().size();
			} catch (Exception e) {
				logger.warn(e);
			}
		}
		return virtualWikiCount;
	}

	/**
	 * return the current handler instance
	 *
	 * @return the current handler instance
	 */
	public PersistencyHandler getHandler() {
		return handler;
	}

	/**
	 * Do emergency repairs by clearing all locks and deleting recent changes files
	 */
	public void panic() {
		Collection wikis = null;
		try {
			wikis = getVirtualWikiList();
		} catch (Exception e) {
			logger.error("problem getting the virtual wiki list", e);
			return;
		}
		for (Iterator iterator = wikis.iterator(); iterator.hasNext();) {
			String virtualWikiName = (String) iterator.next();
			try {
				List lockList = handler.getLockList(virtualWikiName);
				for (Iterator lockIterator = lockList.iterator(); lockIterator.hasNext();) {
					TopicLock lock = (TopicLock) lockIterator.next();
					handler.unlockTopic(virtualWikiName, lock.getTopicName());
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			// destroy recent changes
			if (WikiBase.getPersistenceType() != DATABASE) {
				try {
					FileHandler.getPathFor(virtualWikiName, "recent.xml").delete();
				} catch (Exception e) {
					logger.error("error removing recent.xml", e);
				}
			}
			// failsafe
			if (WikiBase.getPersistenceType() != DATABASE) {
				try {
					File wikiDir = FileHandler.getPathFor(virtualWikiName, null);
					File[] locks = wikiDir.listFiles(new FileExtensionFilter(".lock"));
					for (int i = 0; i < locks.length; i++) {
						File lock = locks[i];
						lock.delete();
					}
				} catch (Exception e) {
					logger.error("error removing recent.xml", e);
				}
			}
		}
	}

	/**
	 * Return true if the given topic is marked as "admin only", i.e. it is present in the admin only topics topic
	 *
	 * @param virtualWiki
	 * @param topicName
	 * @return
	 */
	public boolean isAdminOnlyTopic(Locale locale, String virtualWiki, String topicName) throws Exception {
		String adminOnlyTopics = readRaw(virtualWiki, getMessages(locale).getString("specialpages.adminonlytopics"));
		StringTokenizer tokenizer = new StringTokenizer(adminOnlyTopics);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals(topicName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get messages for the given locale
	 * @param locale locale
	 * @return
	 */
	private static ResourceBundle getMessages(Locale locale) {
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
		return messages;
	}
}
