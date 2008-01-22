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
package org.jamwiki.utils;

import org.jamwiki.WikiConfiguration;

/**
 * Class for controlling "pseudotopics". A pseudotopic is a topic name that maps to
 * an internal Wikk page, such as Special:RecentChanges and Special:Edit. The
 * mappings of topic names to redirect URLs are persisted in
 * <code>/WEB-INF/classes/jamwiki-configuration.xml</code>.
 *
 * @see org.jamwiki.WikiConfiguration
 */
public class PseudoTopicHandler {

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(PseudoTopicHandler.class.getName());

	/**
	 *
	 */
	private PseudoTopicHandler() {
	}

	/**
	 * Return true if there is a mapping for the given topic
	 *
	 * @param pseudotopicName The name of the pseudo-topic that is being tested
	 *  for existence.
	 * @return <code>true</code> if a pseudo-topic with the specified name
	 *  exists, <code>false</code> otherwise.
	 */
	public static boolean isPseudoTopic(String pseudotopicName) {
		return WikiConfiguration.getInstance().getPseudotopics().contains(pseudotopicName);
	}
}
