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
package org.jamwiki.model;

import java.util.Collection;
import java.util.Vector;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a watchlist object consisting of a virtual
 * wiki and a collection of topics being watched.
 */
public class Watchlist {

	private String virtualWiki = null;
	private Collection topics = new Vector();
	private static WikiLogger logger = WikiLogger.getLogger(Watchlist.class.getName());

	/**
	 *
	 */
	public Watchlist() {
	}

	/**
	 *
	 */
	public Watchlist(String virtualWiki, Collection topics) {
		this.virtualWiki = virtualWiki;
		this.topics = topics;
	}

	/**
	 *
	 */
	public void add(String topicName) {
		if (topicName != null) {
			this.topics.add(topicName);
		}
	}

	/**
	 *
	 */
	public boolean containsTopic(String topicName) {
		return (topicName == null) ? false : this.topics.contains(topicName);
	}

	/**
	 *
	 */
	public Collection getTopics() {
		return this.topics;
	}

	/**
	 *
	 */
	public void setTopics(Collection topics) {
		this.topics = topics;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void remove(String topicName) {
		if (topicName != null) {
			this.topics.remove(topicName);
		}
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}
}
