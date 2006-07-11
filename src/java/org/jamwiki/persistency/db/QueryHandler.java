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
package org.jamwiki.persistency.db;

import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;

/**
 *
 */
public interface QueryHandler {

	/**
	 *
	 */
	public void createTables() throws Exception;

	/**
	 *
	 */
	public WikiResultSet getAllTopicNames(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public WikiResultSet getAllTopicVersions(Topic topic) throws Exception;

	/**
	 *
	 */
	public WikiResultSet getAllWikiUserLogins() throws Exception;

	/**
	 *
	 */
	public WikiResultSet getLockList(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public WikiResultSet getReadOnlyTopics(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, int num) throws Exception;

	/**
	 *
	 */
	public WikiResultSet getUserContributions(String virtualWiki, String userString, int num) throws Exception;

	/**
	 *
	 */
	public WikiResultSet getVirtualWikis() throws Exception;

	/**
	 *
	 */
	public void insertRecentChange(RecentChange change) throws Exception;

	/**
	 *
	 */
	public void insertTopic(Topic topic) throws Exception;

	/**
	 *
	 */
	public void insertTopicVersion(TopicVersion topicVersion) throws Exception;

	/**
	 *
	 */
	public void insertVirtualWiki(int virtualWikiId, String virtualWikiName) throws Exception;

	/**
	 *
	 */
	public void insertWikiUser(WikiUser user) throws Exception;

	/**
	 *
	 */
	public void insertWikiUserInfo(WikiUser user) throws Exception;

	/**
	 *
	 */
	public WikiResultSet lookupLastTopicVersion(Topic topic) throws Exception;

	/**
	 *
	 */
	public WikiResultSet lookupTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public WikiResultSet lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception;

	/**
	 *
	 */
	public WikiResultSet lookupWikiUser(int userId) throws Exception;

	/**
	 *
	 */
	public WikiResultSet lookupWikiUser(String login) throws Exception;

	/**
	 *
	 */
	public WikiResultSet lookupWikiUser(String login, String password) throws Exception;

	/**
	 *
	 */
	public int nextTopicId() throws Exception;

	/**
	 *
	 */
	public int nextTopicVersionId() throws Exception;

	/**
	 *
	 */
	public int nextVirtualWikiId() throws Exception;

	/**
	 *
	 */
	public int nextWikiUserId() throws Exception;

	/**
	 *
	 */
	public void reloadRecentChanges() throws Exception;

	/**
	 *
	 */
	public void updateTopic(Topic topic) throws Exception;

	/**
	 *
	 */
	public void updateWikiUser(WikiUser user) throws Exception;

	/**
	 *
	 */
	public void updateWikiUserInfo(WikiUser user) throws Exception;
}
