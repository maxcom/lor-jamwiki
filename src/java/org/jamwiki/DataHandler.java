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
import java.util.Locale;
import org.jamwiki.model.Category;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.Pagination;

/**
 *
 */
public interface DataHandler {

	/**
	 *
	 */
	public boolean canMoveTopic(Topic fromTopic, String destination) throws Exception;

	/**
	 *
	 */
	public void deleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public Collection diff(String topicName, int topicVersionId1, int topicVersionId2) throws Exception;

	/**
	 *
	 */
	public Collection getAllCategories(String virtualWiki, Pagination pagination) throws Exception;

	/**
	 *
	 */
	public Collection getAllTopicNames(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public Collection getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception;

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception;

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, String topicName, Pagination pagination, boolean descending) throws Exception;

	/**
	 *
	 */
	public Collection getTopicsAdmin(String virtualWiki, Pagination pagination) throws Exception;

	/**
	 *
	 */
	public Collection getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception;

	/**
	 * Return a collection of all VirtualWiki objects that exist for the Wiki.
	 */
	public Collection getVirtualWikiList(Object transactionObject) throws Exception;

	/**
	 * Retrieve a watchlist containing a Collection of topic ids and topic
	 * names that can be used to determine if a topic is in a user's current
	 * watchlist.
	 */
	public Watchlist getWatchlist(String virtualWiki, int userId) throws Exception;

	/**
	 * Retrieve a watchlist containing a Collection of RecentChanges objects
	 * that can be used for display on the Special:Watchlist page.
	 */
	public Collection getWatchlist(String virtualWiki, int userId, Pagination pagination) throws Exception;

	/**
	 *
	 */
	public Collection lookupCategoryTopics(String virtualWiki, String categoryName, int topicType) throws Exception;

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception;

	/**
	 * Return a count of all topics, including redirects, comments pages and templates,
	 * currently available on the Wiki.  This method excludes deleted topics.
	 *
	 * @param virtualWiki The virtual wiki for which the total topic count is being returned
	 *  for.
	 */
	public int lookupTopicCount(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public Collection lookupTopicByType(String virtualWiki, int topicType, Pagination pagination) throws Exception;

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String topicName, int topicVersionId, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws Exception;

	/**
	 *
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception;

	/**
	 * Return a count of all wiki files currently available on the Wiki.  This
	 * method excludes deleted files.
	 *
	 * @param virtualWiki The virtual wiki of the files being retrieved.
	 */
	public int lookupWikiFileCount(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public WikiUser lookupWikiUser(int userId, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login, Object transactionObject) throws Exception;

	/**
	 * Return a count of all wiki users.
	 */
	public int lookupWikiUserCount() throws Exception;

	/**
	 *
	 */
	public void moveTopic(Topic fromTopic, TopicVersion fromVersion, String destination, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void reloadRecentChanges(Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void setup(Locale locale, WikiUser user) throws Exception;

	/**
	 *
	 */
	// FIXME - move this to another location
	public void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void undeleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Object transactionObject) throws Exception;

	/**
	 *
	 */
	// FIXME - move this to another location
	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, String ipAddress, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void writeFile(String topicName, WikiFile wikiFile, WikiFileVersion wikiFileVersion, Object transactionObject) throws Exception;

	/**
	 *
	 */
	// FIXME - should not need ParserDocument here
	public void writeTopic(Topic topic, TopicVersion topicVersion, ParserDocument parserDocument, boolean userVisible, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void writeVirtualWiki(VirtualWiki virtualWiki, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void writeWatchlistEntry(Watchlist watchlist, String virtualWiki, String topicName, int userId, Object transactionObject) throws Exception;

	/**
	 *
	 */
	public void writeWikiUser(WikiUser user, WikiUserInfo userInfo, Object transactionObject) throws Exception;
}
