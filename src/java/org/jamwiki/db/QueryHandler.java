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
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.utils.Pagination;

/**
 * This interface provides all methods needed for retrieving, inserting, or updating
 * data from the database.
 */
public interface QueryHandler {

	/**
	 * Returns the simplest possible query that can be used to validate
	 * whether or not a database connection is valid.  Note that the query
	 * returned MUST NOT query any JAMWiki tables since it will be used prior
	 * to setting up the JAMWiki tables.
	 *
	 * @return Returns a simple query that can be used to validate a database
	 *  connection.
	 */
	public String connectionValidationQuery();

	/**
	 * Method called to set up all JAMWiki system tables, indexes, and other
	 * required database objects.  If a failure occurs during object creation
	 * then this method will not attempt to clean up any objects that were
	 * created prior to the failure.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void createTables(Connection conn) throws Exception;

	/**
	 * Delete all records from the recent changes table for a specific topic.
	 *
	 * @param topicId The topic id for which recent changes are being deleted.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void deleteRecentChanges(int topicId, Connection conn) throws Exception;

	/**
	 * Delete all categories associated with a topic.
	 *
	 * @param topicId The topic for which category association records are being
	 *  deleted.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void deleteTopicCategories(int topicId, Connection conn) throws Exception;

	/**
	 * Delete a user's watchlist entry using the topic name to determine which
	 * entry to remove.
	 *
	 * @param virtualWikiId The id of the virtual wiki for which the watchlist
	 *  entry is being deleted.
	 * @param topicName The topic name for which the watchlist entry is being
	 *  deleted.
	 * @param userId The user for which the watchlist entry is being deleted.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void deleteWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws Exception;

	/**
	 * Drop all JAMWiki database objects.  This method drops tables, indexes, and
	 * any database objects, as well as all data in those objects.  Note that if
	 * a failure occurs while deleting any one object the method will continue
	 * trying to delete any remaining objects.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 */
	public void dropTables(Connection conn);

	/**
	 * Return a simple query, that if successfully run indicates that JAMWiki
	 * tables have been initialized in the database.
	 *
	 * @return Returns a simple query that, if successfully run, indicates
	 *  that JAMWiki tables have been set up in the database.
	 */
	public String existenceValidationQuery();

	/**
	 * Retrieve a WikiResultSet containing all topic names that exist for a
	 * virtual wiki. This method will not return the names of previously
	 * deleted topics.
	 *
	 * @param virtualWikiId The id of the virtual wiki for which topic names
	 *  are being retrieved.
	 * @return A WikiResultSet containing the names of all topics for the virtual
	 *  wiki, not including any previously deleted topics.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getAllTopicNames(int virtualWikiId) throws Exception;

	/**
	 * Retrieve a WikiResultSet consisting of all topic version information for
	 * a given topic.  Version information is sorted by topic version id, which
	 * in effect sorts the versions from newest to oldest.
	 *
	 * @param topic A Topic object for which version information is to be retrieved.
	 * @param descending If <code>true</code> then results are sorted newest to
	 *  oldest.
	 * @return A WikiResultSet containing topic version information for all
	 *  versions of the specified topic.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getAllTopicVersions(Topic topic, boolean descending) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all topic names for the WikiFile
	 * objects existing for a virtual wiki.
	 *
	 * @param virtualWikiId The id of the virtual wiki for which wiki file topic
	 *  names are being retrieved.
	 * @return A WikiResultSet containing the names of all wiki file topics for
	 *  the virtual wiki, not including any previously deleted topics.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getAllWikiFileTopicNames(int virtualWikiId) throws Exception;

	/**
	 * Retrieve a WikiResultSet consisting of all wiki file version information for
	 * a given wiki file.  Version information is sorted by wiki file version id, which
	 * in effect sorts the wiki file versions from newest to oldest.
	 *
	 * @param wikiFile A WikiFile object for which version information is to be retrieved.
	 * @param descending If <code>true</code> then results are sorted newest to
	 *  oldest.
	 * @return A WikiResultSet containing wiki file version information for all
	 *  versions of the specified wiki file.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getAllWikiFileVersions(WikiFile wikiFile, boolean descending) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing the login of every registered wiki user.
	 *
	 * @return A WikiResultSet containing the login of every registered wiki user.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getAllWikiUserLogins() throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all categories associated with a particular
	 * virtual wiki.  The result set may be limited by specifying the number of results
	 * to retrieve in a Pagination object.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki from which all
	 *  categories are to be retrieved.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A WikiResultSet containing all categories associated with a particular
	 *  virtual wiki.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getCategories(int virtualWikiId, Pagination pagination) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all recent changes made to the wiki for a
	 * specific virtual wiki.
	 *
	 * @param virtualWiki The name of the virtual wiki for which changes are being
	 *  retrieved.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @param descending If <code>true</code> then results are sorted newest to
	 *  oldest.
	 * @return A WikiResultSet containing all recent changes for a particular virtual
	 *  wiki.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all recent changes made to the wiki for a
	 * specific topic.
	 *
	 * @param topicId The id of the topic for which recent changes are being
	 *  retrieved.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @param descending If <code>true</code> then results are sorted newest to
	 *  oldest.
	 * @return A WikiResultSet containing all recent changes for a particular virtual
	 *  wiki.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getRecentChanges(int topicId, Pagination pagination, boolean descending) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing the topic names of all admin-only
	 * topics for the virtual wiki.
	 *
	 * @param virtualWikiId The id of the virtual wiki for which topic names
	 *  are being retrieved.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A WikiResultSet containing the topic names of all admin-only
	 *  topics for the virtual wiki.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getTopicsAdmin(int virtualWikiId, Pagination pagination) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all recent changes made to the wiki by a
	 * specific user.
	 *
	 * @param virtualWiki The name of the virtual wiki for which user contributions
	 *  are being retrieved.
	 * @param userString The login of the user for whom changes are being retrieved; or
	 *  for anonymous users, the IP address of the user.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @param descending If <code>true</code> then results are sorted newest to
	 *  oldest.
	 * @return A WikiResultSet containing all recent changes made by a particular user.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all virtual wiki information for all
	 * virtual wikis.
	 *
	 * @return Returns a WikiResult set containing all virtual wiki information
	 *  for every virtual wiki.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getVirtualWikis() throws Exception;

	/**
	 * Retrieve a WikiResultSet containing the topic ID and topic name for
	 * topics in the user's watchlist.
	 *
	 * @param virtualWikiId The virtual wiki ID for the virtual wiki for the
	 *  watchlist topics.
	 * @param userId The user ID for the user retrieving the watchlist.
	 * @return A WikiResultSet containing topic ID and topic name for all
	 *  watchlist items.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getWatchlist(int virtualWikiId, int userId) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all recent changes for topics in the
	 * user's watchlist.
	 *
	 * @param virtualWikiId The virtual wiki ID for the virtual wiki for the
	 *  watchlist topics.
	 * @param userId The user ID for the user retrieving the watchlist.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A WikiResultSet containing recent changes for the watchlist.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws Exception;

	/**
	 * Add a new category record to the database.  Note that this method will fail
	 * if an existing category of the same name is already associated with the
	 * topic.
	 *
	 * @param childTopicId The topic id for the topic which this category is
	 *  associated with.
	 * @param categoryName The name of the category being inserted.
	 * @param sortKey The sorting value to use with the category, or <code>null</code>
	 *  if no sorting value is specified.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertCategory(int childTopicId, String categoryName, String sortKey, Connection conn) throws Exception;

	/**
	 * Add a new recent change record to the database.
	 *
	 * @param change The RecentChange record that is to be added to the database.
	 * @param virtualWikiId The virtual wiki id for the record that is being added.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertRecentChange(RecentChange change, int virtualWikiId, Connection conn) throws Exception;

	/**
	 * Add a new topic record to the database.  The topic must not already exist
	 * in the database or else an error will be thrown.
	 *
	 * @param topic The Topic record that is to be added to the database.
	 * @param virtualWikiId The virtual wiki id for the record that is being added.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertTopic(Topic topic, int virtualWikiId, Connection conn) throws Exception;

	/**
	 * Add a new topic version record to the database.  The topic version must
	 * not already exist in the database or else an error will be thrown.
	 *
	 * @param topicVersion The TopicVersion record that is to be added to the database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertTopicVersion(TopicVersion topicVersion, Connection conn) throws Exception;

	/**
	 * Add a new virtual wiki record to the database.  The virtual wiki must
	 * not already exist in the database or else an error will be thrown.
	 *
	 * @param virtualWiki The VirtualWiki record that is to be added to the database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception;

	/**
	 * Add a new watchlist entry record to the database.  An identical entry
	 * must not already exist or else an exception will be thrown.
	 *
	 * @param virtualWikiId The virtual wiki id for the watchlist entry being
	 *  inserted.
	 * @param topicName The name of the topic for the watchlist entry.  This
	 *  value should be set only for topics that do not yet exist, and should
	 *  be set to <code>null</code> for existing topics.
	 * @param userId The ID of the user for the watchlist entry.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws Exception;

	/**
	 * Add a new wiki file record to the database.  The wiki file must not
	 * already exist in the database or else an error will be thrown.
	 *
	 * @param wikiFile The WikiFile record that is to be added to the database.
	 * @param virtualWikiId The virtual wiki id for the record that is being added.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertWikiFile(WikiFile wikiFile, int virtualWikiId, Connection conn) throws Exception;

	/**
	 * Add a new wiki file version record to the database.  The wiki file
	 * version must not already exist in the database or else an error will
	 * be thrown.
	 *
	 * @param wikiFileVersion The WikiFileVersion record that is to be added
	 *  to the database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws Exception;

	/**
	 * Add a new user record to the database.  The user must not already exist
	 * in the database or else an error will be thrown.
	 *
	 * @param user The WikiUser record that is to be added to the database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertWikiUser(WikiUser user, Connection conn) throws Exception;

	/**
	 * Add a new user information record to the database.  The user information
	 * must not already exist in the database or else an error will be thrown.
	 *
	 * @param userInfo The WikiUserInfo record that is to be added to the
	 *  database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void insertWikiUserInfo(WikiUserInfo userInfo, Connection conn) throws Exception;

	/**
	 * Retrieve a result set containing the topic name and sort key for all
	 * topics associated with a category.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the topics
	 *  being retrieved.
	 * @param categoryName The name of the category for which associated topics
	 *  are to be retrieved.
	 * @param topicType The topic type (image, normal, etc) for the topics to be
	 *  retrieved.
	 * @return A WikiResultSet containing topic name and sort key for all topics
	 *  associated with a specific category.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupCategoryTopics(int virtualWikiId, String categoryName, int topicType) throws Exception;

	/**
	 * Retrieve a WikiResultSet containing all topic information for a given topic.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the topic
	 *  being retrieved.
	 * @param topicName The name of the topic being retrieved.
	 * @param caseSensitive Set to <code>true</code> if the topic name should be
	 *  searched for in a case-sensitive manner.
	 * @param deleteOK Set to <code>true</code> if deleted topics should be searched,
	 *  otherwise deleted topics will not be included in the result set.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return A WikiResultSet containing all topic information for the given topic
	 *  name and virtual wiki.  If no matching topic is found an empty result set is
	 *  returned.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupTopic(int virtualWikiId, String topicName, boolean caseSensitive, boolean deleteOK, Connection conn) throws Exception;

	/**
	 * Retrieve a result set of all topics of a given type within a virtual wiki.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the topics
	 *  being retrieved.
	 * @param topicType The topic type (image, normal, etc) for the topics to be
	 *  retrieved.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A WikiResult set of all non-deleted topics for the given virtual wiki
	 *  of the specified topic type, and within the bounds specified by the pagination
	 *  object.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupTopicByType(int virtualWikiId, int topicType, Pagination pagination) throws Exception;

	/**
	 * Return a count of all topics, including redirects, comments pages and templates,
	 * currently available on the Wiki.  This method excludes deleted topics.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the topics
	 *  being retrieved.
	 */
	public WikiResultSet lookupTopicCount(int virtualWikiId) throws Exception;

	/**
	 * Retrieve a result set containing a specific topic version.
	 *
	 * @param topicVersionId The id for the topic version record being retrieved.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return A WikiResultSet containing the topic version record, or an empty
	 *  result set if no matching record is found.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupTopicVersion(int topicVersionId, Connection conn) throws Exception;

	/**
	 * Retrieve a result set containing all wiki file information for a given WikiFile.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the wiki file
	 *  being retrieved.
	 * @param topicId The id of the parent topic for the wiki file being retrieved.
	 * @return A WikiResultSet containing all wiki file information for the given topic
	 *  id and virtual wiki.  If no matching wiki file is found an empty result set is
	 *  returned.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupWikiFile(int virtualWikiId, int topicId) throws Exception;

	/**
	 * Return a count of all wiki files currently available on the Wiki.  This
	 * method excludes deleted files.
	 *
	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the files
	 *  being retrieved.
	 */
	public WikiResultSet lookupWikiFileCount(int virtualWikiId) throws Exception;

	/**
	 * Retrieve a result set containing all user information for a given WikiUser.
	 *
	 * @param userId The id of the user record being retrieved.
	 * @return A WikiResultSet containing all information for the given user, or
	 *  an empty result set if no matching user exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupWikiUser(int userId) throws Exception;

	/**
	 * Retrieve a result set containing all user information for a given WikiUser.
	 *
	 * @param userId The id of the user record being retrieved.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return A WikiResultSet containing all information for the given user, or
	 *  an empty result set if no matching user exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupWikiUser(int userId, Connection conn) throws Exception;

	/**
	 * Retrieve a result set containing all user information for a given WikiUser.
	 *
	 * @param login The login of the user record being retrieved.
	 * @return A WikiResultSet containing all information for the given user, or
	 *  an empty result set if no matching user exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupWikiUser(String login) throws Exception;

	/**
	 * Retrieve a result set containing all user information for a given WikiUser.
	 *
	 * @param login The login of the user record being retrieved.
	 * @param encryptedPassword The encrypted password for the user record being
	 *  retrieved.
	 * @return A WikiResultSet containing all information for the given user, or
	 *  an empty result set if no matching user exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupWikiUser(String login, String encryptedPassword) throws Exception;

	/**
	 * Return a count of all wiki users.
	 */
	public WikiResultSet lookupWikiUserCount() throws Exception;

	/**
	 * Retrieve a result set containing all user information for a given
	 * WikiUserInfo.
	 *
	 * @param login The login of the user record being retrieved.
	 * @return A WikiResultSet containing all information for the given user, or
	 *  an empty result set if no matching user exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiResultSet lookupWikiUserInfo(String login) throws Exception;

	/**
	 * Retrieve the next available topic id from the topic table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return The next available topic id from the topic table.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public int nextTopicId(Connection conn) throws Exception;

	/**
	 * Retrieve the next available topic version id from the topic version table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return The next available topic version id from the topic version table.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public int nextTopicVersionId(Connection conn) throws Exception;

	/**
	 * Retrieve the next available virtual wiki id from the virtual wiki table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return The next available virtual wiki id from the virtual wiki table.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public int nextVirtualWikiId(Connection conn) throws Exception;

	/**
	 * Retrieve the next available wiki file id from the wiki file table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return The next available wiki file id from the wiki file table.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public int nextWikiFileId(Connection conn) throws Exception;

	/**
	 * Retrieve the next available wiki file version id from the wiki file
	 * version table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return The next available wiki file version id from the wiki file
	 *  version table.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public int nextWikiFileVersionId(Connection conn) throws Exception;

	/**
	 * Retrieve the next available wiki user id from the wiki user table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @return The next available wiki user id from the wiki user table.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public int nextWikiUserId(Connection conn) throws Exception;

	/**
	 * Refresh the recent changes content by reloading the recent changes table.
	 *
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void reloadRecentChanges(Connection conn) throws Exception;

	/**
	 * Update a topic record in the database.
	 *
	 * @param topic The Topic record that is to be updated in the database.
	 * @param virtualWikiId The virtual wiki id for the record that is being updated.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void updateTopic(Topic topic, int virtualWikiId, Connection conn) throws Exception;

	/**
	 * Update a virtual wiki record in the database.
	 *
	 * @param virtualWiki The VirtualWiki record that is to be updated in the database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception;

	/**
	 * Update a wiki file record in the database.
	 *
	 * @param wikiFile The WikiFile record that is to be updated in the database.
	 * @param virtualWikiId The virtual wiki id for the record that is being updated.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void updateWikiFile(WikiFile wikiFile, int virtualWikiId, Connection conn) throws Exception;

	/**
	 * Update a wiki user record in the database.
	 *
	 * @param user The WikiUser record that is to be updated in the database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void updateWikiUser(WikiUser user, Connection conn) throws Exception;

	/**
	 * Update a wiki user information record in the database.
	 *
	 * @param userInfo The WikiUserInfo record that is to be updated in the
	 *  database.
	 * @param conn A database connection to use when connecting to the database
	 *  from this method.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public void updateWikiUserInfo(WikiUserInfo userInfo, Connection conn) throws Exception;
}
