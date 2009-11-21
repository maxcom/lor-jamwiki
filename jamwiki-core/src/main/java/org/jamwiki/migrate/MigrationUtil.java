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
package org.jamwiki.migrate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * This class provides utility methods helpful when importing and exporting
 * file data.
 */
public class MigrationUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(MigrationUtil.class.getName());

	/**
	 * Given a file and a list of topic names, export the topics to the file.
	 *
	 * @param file The file that contains topic data to be parsed.
	 * @param virtualWiki The virtual wiki to which the topic name list belongs.
	 * @param topicNames A list of topic names to be exported.
	 * @param excludeHistory Set to <code>true</code> if only the most recent topic
	 *  version, not the full topic history, should be exported.
	 * @param errors A list of errors that will be updating if errors are encountered
	 *  during parsing.
	 * @throws MigrationException Thrown if a parsing error or data update error is
	 *  thrown while trying to parse and commit topic data.
	 * @throws WikiException Thrown if there is no topic data available.
	 */
	public static void exportToFile(File file, String virtualWiki, List<String> topicNames, boolean excludeHistory, List<WikiMessage> errors) throws MigrationException, WikiException {
		Migrator migrator = new MediaWikiXmlMigrator();
		Map<Topic, List<TopicVersion>> data = new java.util.HashMap<Topic, List<TopicVersion>>();
		logger.info("topicNames.size(): " + topicNames.size());
		for (String topicName : topicNames) {
			logger.info("Parsing topic name: " + topicName);
			List<TopicVersion> topicVersions = new ArrayList<TopicVersion>();
			try {
				Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
				if (topic == null) {
					throw new WikiException(new WikiMessage("export.error.topic.does.not.exist", topicName));
				}
				int count = 0;
				Integer topicVersionId = topic.getCurrentVersionId();
				int maxCount = Environment.getIntValue(Environment.PROP_MAX_TOPIC_VERSION_EXPORT);
				while (topicVersionId != null) {
					logger.info("Parsing topic version: " + topicVersionId);
					TopicVersion topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId);
					if (topicVersion == null) {
						logger.info("No topic with the specific version ID exists, this may indicate a data problem in the wiki database: " + topicVersionId);
						break;
					}
					// versions should be sorted earliest to latest, so add to the beginning of the list
					topicVersions.add(0, topicVersion);
					if (excludeHistory) {
						// only include the most recent version
						break;
					}
					topicVersionId = topicVersion.getPreviousTopicVersionId();
					count++;
					if (count > maxCount) {
						logger.info("Maximum number of topic revisions that can be exported (" + maxCount + ") reached for topic: " + topicName);
						break;
					}
				}
				data.put(topic, topicVersions);
			} catch (DataAccessException e) {
				throw new MigrationException("Data access exception while processing topic " + topicName, e);
			}
		}
		migrator.exportToFile(file, data);
	}

	/**
	 * Given a file containing import information, parse the file and commit all
	 * topic information within it.
	 *
	 * @param file The file that contains topic data to be parsed.
	 * @param virtualWiki The virtual wiki to write the topic data to.
	 * @param user The user (if any) that is performing the import.
	 * @param authorDisplay The display value for the user that is performing the
	 *  import.  This value is typically the user's IP address.
	 * @param locale The locale for the user that is performing the import.
	 * @param errors A list of errors that will be updating if errors are encountered
	 *  during parsing.
	 * @return A list of topic names that are successfully parsed and committed to
	 *  the database.
	 * @throws MigrationException Thrown if a parsing error or data update error is
	 *  thrown while trying to parse and commit topic data.
	 * @throws WikiException Thrown if there is no topic data available.
	 */
	public static List<String> importFromFile(File file, String virtualWiki, WikiUser user, String authorDisplay, Locale locale, List<WikiMessage> errors) throws MigrationException, WikiException {
		Migrator migrator = new MediaWikiXmlMigrator();
		Map<Topic, List<TopicVersion>> parsedTopics = migrator.importFromFile(file);
		if (parsedTopics.isEmpty()) {
			throw new WikiException(new WikiMessage("import.error.notopic"));
		}
		List<TopicVersion> topicVersions;
		List<String> successfulImports = new ArrayList<String>();
		for (Topic topic : parsedTopics.keySet()) {
			try {
				Topic existingTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topic.getName(), false, null);
				if (existingTopic != null) {
					// FIXME - update so that this merges any new versions instead of throwing an error
					errors.add(new WikiMessage("import.error.topicexists", topic.getName()));
					continue;
				}
				topic.setVirtualWiki(virtualWiki);
				topicVersions = parsedTopics.get(topic);
				if (topicVersions.isEmpty()) {
					throw new WikiException(new WikiMessage("import.error.notopic"));
				}
				for (TopicVersion topicVersion : topicVersions) {
					// only the final import version is logged
					topicVersion.setLoggable(false);
					// metadata is needed only for the final import version, so for performance reasons
					// do not include category or link data for older versions
					WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null);
				}
				// create a dummy version to indicate that the topic was imported
				String importedBy = (user != null && user.getUserId() > 0) ? user.getUsername() : authorDisplay;
				String editComment = Utilities.formatMessage("import.message.importedby", locale, new Object[]{importedBy});
				TopicVersion topicVersion = new TopicVersion(user, authorDisplay, editComment, topic.getTopicContent(), 0);
				topicVersion.setEditType(TopicVersion.EDIT_IMPORT);
				ParserOutput parserOutput = null;
				try {
					parserOutput = ParserUtil.parserOutput(topicVersion.getVersionContent(), virtualWiki, topic.getName());
				} catch (ParserException e) {
					throw new MigrationException("Failure while parsing topic version of topic: " + topic.getName(), e);
				}
				if (!StringUtils.isBlank(parserOutput.getRedirect())) {
					// set up a redirect
					topic.setRedirectTo(parserOutput.getRedirect());
					topic.setTopicType(Topic.TYPE_REDIRECT);
				}
				WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
			} catch (DataAccessException e) {
				throw new MigrationException("Data access exception while processing topic " + topic.getName(), e);
			}
			successfulImports.add(topic.getName());
		}
		return successfulImports;
	}
}
