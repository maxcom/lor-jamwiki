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
package org.vqwiki;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;

/* TODO: to be implemented later on.
import org.vqwiki.Revision;
import org.vqwiki.AccessControlList;
import org.vqwiki.WikiBase;*/
import org.vqwiki.persistency.PersistencyHandler;
import org.vqwiki.parser.Lexer;

/**
 * This class represents a topic in the wiki with all information pertaining to it like revisions and acl's.
 */
public class Topic {

	protected WikiBase wb;				  /** Reference to the wiki base class */
	protected String name;				  /** Name of this topic */
	protected String contents;			  /** Contents of this topic */   //FIXME: remove and use actual revision object
	protected String author;				/** Author of this topic */
	protected Date lastRevisionDate;		/** Last modification date of this topic */
	protected int revision;				 /** revision number of this topic */
	protected Vector acls;

	private static Logger logger = Logger.getLogger(Topic.class);

	/**
	 * Creates a new Topic object.
	 *
	 * @param name The name of this topic
	 *
	 * @throws Exception If the topic cannot be retrieved
	 */
	public Topic(String name) throws Exception {
		this.name = name;
		wb = WikiBase.getInstance();
		this.contents = null;
		this.author = null;
		this.lastRevisionDate = null;
		this.revision = -1;
	}

	/**
	 * Find the most recent revision before the current
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 *
	 * @return the last revision date, or null if versioning is off
	 */
	public Date getMostRecentRevisionDate(String virtualWiki) throws Exception {
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			this.lastRevisionDate = wb.getVersionManagerInstance().lastRevisionDate(virtualWiki, this.name);
			return this.lastRevisionDate;
		} else {
			return null;
		}
	}

	/**
	 * Return a diff for the current vs the most recent revision before it.
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 * @param useHtml Set to true if the diff should be returned as HTML.
	 *  Returns the diff as text otherwise.
	 *
	 * @return a diff to the last revision
	 */
	public String mostRecentDiff(String virtualWiki, boolean useHtml) throws Exception {
		return wb.getVersionManagerInstance().diff(virtualWiki, name, 0, 1, useHtml);
	}

	/**
	 * Get a diff for two arbitrary versions of a topic
	 * @param virtualWiki the virtual wiki
	 * @param firstVersion the first version number
	 * @param secondVersion the second version number
	 * @param useHtml Set to true if the diff should be returned as HTML.
	 *  Returns the diff as text otherwise.
	 * @return
	 */
	public String getDiff(String virtualWiki, int firstVersion, int secondVersion, boolean useHtml) throws Exception {
		return wb.getVersionManagerInstance().diff(virtualWiki, name, firstVersion, secondVersion, useHtml);
	}

	/**
	 * Find the most recent author.
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 *
	 * @return the author, who editied the last revision, or null if versioning is off
	 */
	public String getMostRecentAuthor(String virtualWiki) throws Exception {
		this.author = null;
		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
			// get list of versions:
			List allVersions = wb.getVersionManagerInstance().getAllVersions(virtualWiki, this.name);
			// sort the list so that the most recent version is on top:
			Collections.sort(allVersions,new Comparator() {
				public int compare(Object o1, Object o2) {
					TopicVersion bean1 = (TopicVersion) o1;
					TopicVersion bean2 = (TopicVersion) o2;
					return bean2.getRevisionDate().compareTo((Date)bean1.getRevisionDate());
				}
			});
			logger.debug("Having " + allVersions.size() + " versions: " + allVersions);
			String foundAuthor = null;
			// go through all the versions
			for (Iterator iter = allVersions.iterator(); iter.hasNext() && foundAuthor == null;) {
				TopicVersion version = (TopicVersion) iter.next();
				if (version.getRevisionDate() != null) {
					logger.debug("Checking version date " + version.getRevisionDate());
					// get list of changes for that date
					Collection c = WikiBase.getInstance().getChangeLogInstance().getChanges(virtualWiki, version.getRevisionDate());
					if (c != null) {
						// remove all changes, which do not apply for this topic
						logger.debug("Got " + c.size() + " changes for that date");
						Collection cGood = new ArrayList();
						for (Iterator iterChange = c.iterator(); iterChange.hasNext();) {
							Change thischange = (Change) iterChange.next();
							if (thischange.getTopic().equals(this.name)) {
								cGood.add(thischange);
							}
						}
						logger.debug("Got " + cGood.size() + " changes left after filtering");
						if (!cGood.isEmpty()) {
							// find author on that day; check, if there are multiple
							// modifications on the same day
							Iterator it = cGood.iterator();
							Date authorDate = null;
							while (it.hasNext()) {
								Change thischange = (Change) it.next();
								if (authorDate == null) {
									foundAuthor = thischange.getUser();
									authorDate = thischange.getTime();
								} else {
									if (authorDate.before(thischange.getTime())) {
										foundAuthor = thischange.getUser();
										authorDate = thischange.getTime();
									}
								}
							}
						}
					}
				}
			}
			this.author = foundAuthor;
		} else {
			this.author = null;
		}
		return this.author;
	}

	/**
	 * Find the revision number.
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 *
	 * @return the revision number
	 */
	public int getRevision(String virtualWiki) throws Exception {
		this.revision = wb.getVersionManagerInstance().
		getNumberOfVersions(virtualWiki, this.name);
		return this.revision;
	}

	/**
	 * Make a topic read-only
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 */
	public synchronized void makeTopicReadOnly(String virtualWiki) throws Exception {
		wb.addReadOnlyTopic(virtualWiki, name);
	}

	/**
	 * Return whether a topic is read-only
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 */
	public boolean isReadOnlyTopic(String virtualWiki) throws Exception {
		return wb.isTopicReadOnly(virtualWiki, name);
	}

	/**
	 * Make a previously read-only topic writable
	 *
	 * @param virtualWiki The virtualWiki, which contains the topic
	 */
	public synchronized void makeTopicWritable(String virtualWiki) throws Exception {
		wb.removeReadOnlyTopic(virtualWiki, name);
	}


// NEW AND UPDATED METHODS FOR RELEASE 3

	/**
	 * Populate the current instance of the Topic class from the persistency sub-system using a specified topic name.
	 *
	 * @param name - The name of the topic to be loaded.
	 * @param vwiki - The name of the virtual wiki to which the topic belongs.
	 */
	public void loadTopic(String vwiki, String name) throws Exception {
		if (name == null || name.equals("")) {
			logger.debug("Attempted to load a topic from persistency mechanism with empty topic name.");
		}
		if (vwiki == null || vwiki.equals("")) {
			logger.debug("Attempted to load a topic from persistency mechanism with empty virtual wiki name.");
		}

		PersistencyHandler ph = WikiBase.getInstance().getHandler();	   //FIXME: implement

		try {
			this.contents = ph.read(vwiki, name);
		} catch(Exception e) {
			logger.debug("PROBLEMS reading content!");
		}
	}

	/**
	 * Populate the current instance of the Topic class from the persistency sub-system.
	 *
	 */
	public void loadTopic(String virtualwiki) throws Exception {
		loadTopic(virtualwiki, this.name);
	}


	/**
	 * This method should return a rendered version of the topic's content using whatever
	 * rendering system is in place.
	 *
	 * @return A <code>String</code> object containing the rendered topic.
	 */
	public String getRenderedContent() {		// FIXME!!


		BufferedReader in = new BufferedReader(new StringReader(this.contents));
//		return "Env prop: " + Environment.getValue("setup.dir.upload") + "\n Cooked: \n" + cook(in, "jsp", "org.vqwiki.parser.VQWikiFormatLex", "org.vqwiki.parser.VQWikiLayoutLex", "org.vqwiki.parser.LinkLex");
		return this.contents;
	}


	/**
	 * This method should return a rendered version of the topic's content using a
	 * specified rendering system that should be in place.
	 *
	 * @return A <code>String</code> object containing the rendered topic.
	 */
	public String getRenderedContent(String parsername) {			   // FIXME!!

		BufferedReader in = new BufferedReader(new StringReader(this.contents));
//		return "Env prop: " + Environment.getValue("setup.dir.upload") + "\n Cooked: \n" + cook(in, "jsp", "org.vqwiki.parser.VQWikiFormatLex", "org.vqwiki.parser.VQWikiLayoutLex", "org.vqwiki.parser.LinkLex");
		return this.contents;
	}


	/**
	 * Returns the <code>Topic</code>'s name.
	 *
	 * @return A <code>String</code> object.
	 */
	public String getName() {
		return this.name;
	}

	/* TODO: create remaining methods */
	/* Things you might want to do with a Topic
	boolean isLocked()
	saveTopic()
	loadTopic(String name)
	lock()
	unlock()
	setName(String name)
	createRevision()
	Revision getRevision()
	setAcl(AccessControlList acl)
	AccessControlList getAcl()
	Revision getLastRevision()
	Revision getCurrentRevision()
	*/
}