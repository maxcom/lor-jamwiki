/**
 *
 */
package org.jmwiki;

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
	 * Get all topics
	 */
	public Collection getAllTopicNames(String virtualWiki) throws Exception;

	/**
	 * Get the path, which holds all index files
	 */
	public String getSearchIndexPath(String vrtualWiki);
}
