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

import java.util.Collection;
import org.jamwiki.model.Topic;

/**
 * This interface provides all methods needed for interacting with a search
 * engine.
 *
 * @see org.jamwiki.WikiBase#getSearchEngine
 */
public interface SearchEngine {

	/**
	 * Add a topic to the search index.
	 *
	 * @param topic The Topic object that is to be added to the index.
	 * @param links A collection containing the topic names for all topics that link
	 *  to the current topic.
	 */
	void addToIndex(Topic topic, Collection links);

	/**
	 * Remove a topic from the search index.
	 *
	 * @param topic The topic object that is to be removed from the index.
	 */
	void deleteFromIndex(Topic topic);

	/**
	 * Find all documents that link to a specified topic.
	 *
	 * @param virtualWiki The virtual wiki for the topic.
	 * @param topicName The name of the topic.
	 * @return A collection of SearchResultEntry objects for all documents that
	 *  link to the topic.
	 */
	Collection findLinkedTo(String virtualWiki, String topicName);

	/**
	 * Find all documents that contain a specific search term, ordered by relevance.
	 *
	 * @param virtualWiki The virtual wiki for the topic.
	 * @param text The search term being searched for.
	 * @return A collection of SearchResultEntry objects for all documents that
	 *  contain the search term.
	 */
	Collection findResults(String virtualWiki, String text);

	/**
	 * Refresh the current search index by re-visiting all topic pages.
	 *
	 * @throws Exception Thrown if any error occurs while re-indexing the Wiki.
	 */
	void refreshIndex() throws Exception;
}
