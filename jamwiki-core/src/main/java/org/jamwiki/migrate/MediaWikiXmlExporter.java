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
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.jamwiki.utils.XMLUtil;

/**
 * Provide functionality for exporting a JAMWiki topic to Mediawiki XML format.
 */
public class MediaWikiXmlExporter implements TopicExporter {

	private static final WikiLogger logger = WikiLogger.getLogger(MediaWikiXmlExporter.class.getName());

	/**
	 *
	 */
	public void exportToFile(File file, String virtualWiki, List<String> topicNames, boolean excludeHistory) throws MigrationException {
		FileWriter writer = null;
		boolean success = false;
		try {
			writer = new FileWriter(file);
			writer.write("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
			this.writeSiteInfo(writer);
			this.writePages(writer, virtualWiki, topicNames, excludeHistory);
			writer.write("\n</mediawiki>");
			success = true;
		} catch (DataAccessException e) {
			throw new MigrationException(e);
		} catch (IOException e) {
			throw new MigrationException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignore) {}
			}
			if (!success) {
				// make sure partial files are deleted
				file.delete();
			}
		}
	}

	/**
	 *
	 */
	private void writeSiteInfo(FileWriter writer) throws DataAccessException, IOException {
		writer.write("\n<siteinfo>");
		String sitename = Environment.getValue(Environment.PROP_SITE_NAME);
		writer.write('\n' + XMLUtil.buildTag("sitename", sitename, true));
		String base = WikiUtil.getBaseUrl();
		writer.write('\n' + XMLUtil.buildTag("base", base, true));
		String generator = "JAMWiki " + WikiVersion.CURRENT_WIKI_VERSION;
		writer.write('\n' + XMLUtil.buildTag("generator", generator, true));
		/*
		Cannot have two titles differing only by case of first letter.  Default behavior through 1.5, $wgCapitalLinks = true
			<enumeration value="first-letter" />
		Complete title is case-sensitive. Behavior when $wgCapitalLinks = false
			<enumeration value="case-sensitive" />
		Cannot have two titles differing only by case. Not yet implemented as of MediaWiki 1.5
			<enumeration value="case-insensitive" />
		*/
		writer.write('\n' + XMLUtil.buildTag("case", "case-sensitive", true));
		writer.write("\n<namespaces>");
		Map<String, String> attributes = new HashMap<String, String>();
		String namespace = null;
		for (Integer key : MediaWikiConstants.MEDIAWIKI_NAMESPACE_MAP.keySet()) {
			namespace = MediaWikiConstants.MEDIAWIKI_NAMESPACE_MAP.get(key);
			attributes.put("key", key.toString());
			writer.write('\n' + XMLUtil.buildTag("namespace", namespace, attributes, true));
		}
		writer.write("\n</namespaces>");
		writer.write("\n</siteinfo>");
	}

	/**
	 *
	 */
	private void writePages(FileWriter writer, String virtualWiki, List<String> topicNames, boolean excludeHistory) throws DataAccessException, IOException, MigrationException {
		TopicVersion topicVersion;
		Topic topic;
		WikiUser user;
		// choose 100,000 as an arbitrary max
		Pagination pagination = new Pagination(100000, 0);
		List<Integer> topicVersionIds;
		String versionContent;
		Map<String, String> textAttributes = new HashMap<String, String>();
		textAttributes.put("xml:space", "preserve");
		for (String topicName : topicNames) {
			topicVersionIds = new ArrayList<Integer>();
			topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
			if (topic == null) {
				throw new MigrationException("Failure while exporting: topic " + topicName + " does not exist");
			}
			writer.write("\n<page>");
			writer.write('\n' + XMLUtil.buildTag("title", topic.getName(), true));
			writer.write('\n' + XMLUtil.buildTag("id", topic.getTopicId()));
			if (excludeHistory) {
				// only include the most recent version
				topicVersionIds.add(topic.getCurrentVersionId());
			} else {
				// FIXME - changes sorted newest-to-oldest, should be reverse
				List<RecentChange> changes = WikiBase.getDataHandler().getTopicHistory(virtualWiki, topicName, pagination, true);
				for (int i = (changes.size() - 1); i >= 0; i--) {
					topicVersionIds.add(changes.get(i).getTopicVersionId());
				}
			}
			for (int topicVersionId : topicVersionIds) {
				topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId);
				writer.write("\n<revision>");
				writer.write('\n' + XMLUtil.buildTag("id", topicVersion.getTopicVersionId()));
				writer.write('\n' + XMLUtil.buildTag("timestamp", this.parseJAMWikiTimestamp(topicVersion.getEditDate()), true));
				writer.write("\n<contributor>");
				user = (topicVersion.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(topicVersion.getAuthorId()) : null;
				if (user != null) {
					writer.write('\n' + XMLUtil.buildTag("username", user.getUsername(), true));
					writer.write('\n' + XMLUtil.buildTag("id", user.getUserId()));
				} else if (Utilities.isIpAddress(topicVersion.getAuthorDisplay())) {
					writer.write('\n' + XMLUtil.buildTag("ip", topicVersion.getAuthorDisplay(), true));
				} else {
					writer.write('\n' + XMLUtil.buildTag("username", topicVersion.getAuthorDisplay(), true));
				}
				writer.write("\n</contributor>");
				writer.write('\n' + XMLUtil.buildTag("comment", topicVersion.getEditComment(), true));
				versionContent = this.convertToMediawikiNamespaces(topicVersion.getVersionContent());
				writer.write('\n' + XMLUtil.buildTag("text", versionContent, textAttributes, true));
				writer.write("\n</revision>");
			}
			writer.write("\n</page>");
		}
	}

	/**
	 *
	 */
	private String parseJAMWikiTimestamp(Timestamp timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat(MediaWikiConstants.ISO_8601_DATE_FORMAT);
		return sdf.format(timestamp);
	}

	/**
	 * Convert all namespaces names from JAMWiki to MediaWiki local representation.
	 */
	private String convertToMediawikiNamespaces(String text) {
		StringBuilder builder = new StringBuilder(text);
		String jamwikiNamespace, mediawikiNamespace, mediawikiPattern, jamwikiPattern;
		int start = 0;
		for (Integer key : MediaWikiConstants.MEDIAWIKI_NAMESPACE_MAP.keySet()) {
			// use the JAMWiki namespace if one exists
			jamwikiNamespace = MediaWikiConstants.NAMESPACE_CONVERSION_MAP.get(key);
			mediawikiNamespace = MediaWikiConstants.MEDIAWIKI_NAMESPACE_MAP.get(key);
			if (jamwikiNamespace == null) {
				continue;
			}
			mediawikiPattern = "[[" + mediawikiNamespace + ":";
			jamwikiPattern = "[[" + jamwikiNamespace + ":";
			while ((start = builder.indexOf(jamwikiPattern, start + 1)) != -1) {
				builder.replace(start, start + jamwikiPattern.length(), mediawikiPattern);
			}
		}
		return builder.toString();
	}
}
