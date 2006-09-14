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

import java.util.Properties;
import org.jamwiki.utils.WikiLogger;

/**
 * Class for controlling "pseudotopics". A pseudotopic is a topic name that maps to
 * a redirect URL rather than a real Wiki topic. Examples are Special:RecentChanges
 * and Special:Edit. The mappings of topic names to redirect URLs are persisted
 * in WEB-INF/classes/pseudotopics.properties
 * <p/>
 */
public class PseudoTopicHandler {

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(PseudoTopicHandler.class.getName());
	/** Singleton instance */
	private static PseudoTopicHandler instance;
	/** Properties bundle to store mappings */
	private static Properties mapping;
	/** Name of resource to access the persisted bundle */
	private static final String RESOURCE_NAME = "/pseudotopics.properties";

	// initialize the singleton instance
	static {
		instance = new PseudoTopicHandler();
	}

	/**
	 * Hide constructor
	 */
	private PseudoTopicHandler() {
		PseudoTopicHandler.mapping = Environment.loadProperties(RESOURCE_NAME);
	}

	/**
	 * Return true if there is a mapping for the given topic
	 *
	 * @param pseudotopicName topic
	 * @return true if mapping exists
	 */
	public static boolean isPseudoTopic(String pseudotopicName) {
		return PseudoTopicHandler.mapping.containsKey(pseudotopicName);
	}
}
