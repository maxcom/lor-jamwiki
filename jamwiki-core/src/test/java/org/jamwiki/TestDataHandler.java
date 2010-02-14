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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.model.Category;
import org.jamwiki.model.LogItem;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Role;
import org.jamwiki.model.RoleMap;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class TestDataHandler implements DataHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(TestDataHandler.class.getName());
	/** Keep a map of topic name and topic object in memory to support the writeTopic method. */
	private Map<String, Topic> topics = new LinkedHashMap<String, Topic>();

	/**
	 *
	 */
	public boolean authenticate(String username, String password) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public boolean canMoveTopic(Topic fromTopic, String destination) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void deleteTopic(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void executeUpgradeQuery(String prop, Connection conn) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void executeUpgradeUpdate(String prop, Connection conn) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<Category> getAllCategories(String virtualWiki, Pagination pagination) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<Role> getAllRoles() throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<String> getAllTopicNames(String virtualWiki) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<WikiFileVersion> getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<LogItem> getLogItems(String virtualWiki, int logType, Pagination pagination, boolean descending) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RoleMap> getRoleMapByLogin(String loginFragment) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RoleMap> getRoleMapByRole(String roleName) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<Role> getRoleMapGroup(String groupName) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RoleMap> getRoleMapGroups() throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<Role> getRoleMapUser(String login) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RecentChange> getTopicHistory(String virtualWiki, String topicName, Pagination pagination, boolean descending) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<String> getTopicsAdmin(String virtualWiki, Pagination pagination) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RecentChange> getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<VirtualWiki> getVirtualWikiList() throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Watchlist getWatchlist(String virtualWiki, int userId) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<RecentChange> getWatchlist(String virtualWiki, int userId, Pagination pagination) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<Category> lookupCategoryTopics(String virtualWiki, String categoryName) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Connection conn) throws DataAccessException {
		String content = null;
		if (topics.get(topicName) != null) {
			// first check the memory store created by writeTopic
			return topics.get(topicName);
		}
		try {
			content = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, topicName);
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
		if (content == null) {
			return null;
		}
		Topic topic = new Topic();
		topic.setName(topicName);
		topic.setVirtualWiki(virtualWiki);
		topic.setTopicContent(content);
		return topic;
	}

	/**
	 *
	 */
	public Topic lookupTopicById(String virtualWiki, int topicId) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public int lookupTopicCount(String virtualWiki) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<String> lookupTopicByType(String virtualWiki, int topicType1, int topicType2, Pagination pagination) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(int topicVersionId) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws DataAccessException {
		if (!StringUtils.equals(virtualWikiName, "en") && !StringUtils.equals(virtualWikiName, "test")) {
			// test handler, so hard-code "en" and "test" as the only valid virtual wikis
			return null;
		}
		VirtualWiki virtualWiki = new VirtualWiki();
		virtualWiki.setName(virtualWikiName);
		return virtualWiki;
	}

	/**
	 *
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String fileName) throws DataAccessException {
		String content = null;
		try {
			content = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, fileName);
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
		if (content == null) {
			return null;
		}
		WikiFile wikiFile = new WikiFile();
		wikiFile.setFileName(fileName);
		wikiFile.setVirtualWiki(virtualWiki);
		wikiFile.setUrl(StringUtils.trim(content));
		return wikiFile;
	}

	/**
	 *
	 */
	public int lookupWikiFileCount(String virtualWiki) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiGroup lookupWikiGroup(String groupName) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(int userId) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String username) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public int lookupWikiUserCount() throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public String lookupWikiUserEncryptedPassword(String username) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public List<String> lookupWikiUsers(Pagination pagination) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void moveTopic(Topic fromTopic, TopicVersion fromVersion, String destination) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void orderTopicVersions(Topic topic, List<Integer> topicVersionIdList) throws DataAccessException {
		topic.setCurrentVersionId(topicVersionIdList.get(topicVersionIdList.size() - 1));
		this.topics.put(topic.getName(), topic);
	}

	/**
	 *
	 */
	public void reloadLogItems() throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void reloadRecentChanges() throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void setup(Locale locale, WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void undeleteTopic(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, String userDisplay) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeFile(WikiFile wikiFile, WikiFileVersion wikiFileVersion) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeNamespace(Integer namespaceId, String namespace) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeNamespaceTranslation(int namespaceId, String virtualWiki, String namespaceTranslation) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeRole(Role role, boolean update) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeRoleMapGroup(int groupId, List<String> roles) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeRoleMapUser(String username, List<String> roles) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeTopic(Topic topic, TopicVersion topicVersion, LinkedHashMap<String, String> categories, List<String> links) throws DataAccessException, WikiException {
		// store topics in a local map.  not very sophisticated, but enough to support testing.
		this.topics.put(topic.getName(), topic);
	}

	/**
	 *
	 */
	public void writeTopicVersion(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
		// store topics in a local map.  not very sophisticated, but enough to support testing.
		this.topics.put(topic.getName(), topic);
	}

	/**
	 *
	 */
	public void writeVirtualWiki(VirtualWiki virtualWiki) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeWatchlistEntry(Watchlist watchlist, String virtualWiki, String topicName, int userId) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeWikiGroup(WikiGroup group) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeWikiUser(WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
		throw new UnsupportedOperationException();
	}
}
