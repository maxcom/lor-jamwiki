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
package org.jamwiki.search;

import java.io.IOException;
import java.util.Collection;

/**
 *
 */
public interface SearchEngine {

	/**
	 * Index the given text for the search engine database.
	 */
	public void indexText(String virtualWiki, String topic, String text) throws IOException;

	/**
	 * Should be called by a monitor thread at regular intervals, rebuilds the
	 * entire seach index to account for removed items. Due to the additive rather
	 * than subtractive nature of a Wiki, it probably only needs to be called once
	 * or twice a day
	 */
	public void refreshIndex() throws Exception;

	/**
	 * Find topics that contain the given term
	 */
	public Collection find(String virtualWiki, String text, boolean doTextBeforeAfterParse) throws Exception;

	/**
	 * Find topics that contain the given term
	 */
	public Collection findLinkedTo(String virtualWiki, String topicName) throws Exception;

	/**
	 * Find topics that contain any of the space delimited terms
	 */
	public Collection findMultiple(String virtualWiki, String text, boolean fuzzy) throws Exception;

	/**
	 * Get the path, which holds all index files
	 */
	public String getSearchIndexPath(String vrtualWiki);
}
