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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki;

import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Class for controlling "pseudotopics". A pseudotopic is a topic name that maps to a redirect URL rather
 * than a real Wiki topic. Examples are RecentChanges and SetUsername. The mappings of topic names
 * to redirect URLs are persisted in WEB-INF/classes/pseudotopics.properties
 * <p/>
 * Pseudotopics can also have permanent parameters associated with them by making appropriate entries in
 * pseudotopics.properties. E.g. the entries for ToDoWikiTopics looks like this:
 * <pre>
 * ToDoWikiTopics=/WEB-INF/jsp/allTopics.jsp
 * ToDoWikiTopics.param.0=todo=true
 * </pre>
 * this means that when ToDoWikiTopics is redirected to the allTopics.jsp a parameter named "todo" with
 * the value "true" is also passed. In this way more than one pseudotopic can be mapped to a single
 * redirect URL.
 * <p/>
 * Subject to LGPL
 */
public class PseudoTopicHandler {

	/** Logger */
	private static final Logger logger = Logger.getLogger(PseudoTopicHandler.class);
	/** Singleton instance */
	private static PseudoTopicHandler instance;
	/** Properties bundle to store mappings */
	private Properties mapping;
	/** Name of resource to access the persisted bundle */
	private static final String RESOURCE_NAME = "/pseudotopics.properties";

	// initialize the singleton instance
	static {
		instance = new PseudoTopicHandler();
	}

	/**
	 * Get instance
	 *
	 * @return singleton instance
	 */
	public synchronized static PseudoTopicHandler getInstance() {
		return instance;
	}

	/**
	 * Hide constructor
	 */
	private PseudoTopicHandler() {
		this.mapping = Environment.loadProperties(RESOURCE_NAME);
	}

	/**
	 * Return a redirect URL for the given topic
	 *
	 * @param pseudotopicName topic
	 * @return redirect URL or null if no mapping exists
	 */
	public String getRedirectURL(String pseudotopicName) {
		String redirectURL = this.mapping.getProperty(pseudotopicName);
		String msg = ((redirectURL == null) ?
			"no pseudotopic redirect for " + pseudotopicName :
			"pseudo topic found for " + pseudotopicName + ": " + redirectURL
		);
		logger.debug(msg);
		return redirectURL;
	}

	/**
	 * Return true if there is a mapping for the given topic
	 *
	 * @param pseudotopicName topic
	 * @return true if mapping exists
	 */
	public boolean isPseudoTopic(String pseudotopicName) {
		return getRedirectURL(pseudotopicName) != null;
	}
}
