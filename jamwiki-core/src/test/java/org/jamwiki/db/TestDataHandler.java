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
package org.jamwiki.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataHandler;
import org.jamwiki.model.Category;
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
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 *
 */
public class TestDataHandler implements DataHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(TestDataHandler.class.getName());

	/**
	 *
	 */
	public boolean canMoveTopic(Topic fromTopic, String destination) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void deleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getAllCategories(String virtualWiki, Pagination pagination) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getAllRoles() throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getAllTopicNames(String virtualWiki) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, String topicName, Pagination pagination, boolean descending) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getRoleMapByLogin(String loginFragment) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getRoleMapByRole(String roleName) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Role[] getRoleMapGroup(String groupName) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getRoleMapGroups() throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Role[] getRoleMapUser(String login) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getTopicsAdmin(String virtualWiki, Pagination pagination) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList(Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Watchlist getWatchlist(String virtualWiki, int userId) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection getWatchlist(String virtualWiki, int userId, Pagination pagination) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection lookupCategoryTopics(String virtualWiki, String categoryName, int topicType) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public int lookupTopicCount(String virtualWiki) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection lookupTopicByType(String virtualWiki, int topicType, Pagination pagination) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(int topicVersionId, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public int lookupWikiFileCount(String virtualWiki) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(int userId, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String username, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public int lookupWikiUserCount() throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public Collection lookupWikiUsers(Pagination pagination) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void moveTopic(Topic fromTopic, TopicVersion fromVersion, String destination, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void reloadRecentChanges(Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void setup(Locale locale, WikiUser user) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void undeleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, String ipAddress, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeFile(WikiFile wikiFile, WikiFileVersion wikiFileVersion, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeRole(Role role, Object transactionObject, boolean update) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeRoleMapGroup(int groupId, Collection roles, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeRoleMapUser(int userId, Collection roles, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeTopic(Topic topic, TopicVersion topicVersion, LinkedHashMap categories, Vector links, boolean userVisible, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeVirtualWiki(VirtualWiki virtualWiki, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeWatchlistEntry(Watchlist watchlist, String virtualWiki, String topicName, int userId, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeWikiGroup(WikiGroup group, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void writeWikiUser(WikiUser user, WikiUserInfo userInfo, Object transactionObject) throws Exception {
		throw new UnsupportedOperationException();
	}
}
