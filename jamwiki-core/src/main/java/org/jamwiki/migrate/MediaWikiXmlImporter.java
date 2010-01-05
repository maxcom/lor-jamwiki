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
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provide functionality for importing a Mediawiki XML file into JAMWiki.
 */
public class MediaWikiXmlImporter extends DefaultHandler implements TopicImporter {

	private static final WikiLogger logger = WikiLogger.getLogger(MediaWikiXmlImporter.class.getName());

	/** This map holds the current tag's attribute names and values.  It is cleared after an end-element is called and thus fails for nested elements. */
	private Map<String, String> currentAttributeMap = new HashMap<String, String>();
	/** This buffer holds the content of the current element during parsing.  It will be flushed after an end-element tag is reached. */
	private StringBuilder currentElementBuffer = new StringBuilder();
	private Topic currentTopic = new Topic();
	private TopicVersion currentTopicVersion = new TopicVersion();
	private Map<Date, TopicVersion> currentTopicVersions = new TreeMap<Date, TopicVersion>();
	private final Map<String, String> mediawikiNamespaceMap = new HashMap<String, String>();
	private Map<Topic, List<TopicVersion>> parsedTopics = new HashMap<Topic, List<TopicVersion>>();
	private int previousTopicContentLength = 0;

	/**
	 *
	 */
	public Map<Topic, List<TopicVersion>> importFromFile(File file) throws MigrationException {
		this.importWikiXml(file);
		return this.parsedTopics;
	}

	/**
	 *
	 */
	private void importWikiXml(File file) throws MigrationException {
		// For big file parsing
		System.setProperty("entityExpansionLimit", "1000000");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		FileInputStream fis = null;
		try {
			// at least in 1.5, the SaxParser has a bug where files with names like "%25s"
			// will be read as "%s", generating FileNotFound exceptions.  To work around this
			// issue use a FileInputStream rather than just SAXParser.parse(file, handler)
			fis = new FileInputStream(file);
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(fis, this);
		} catch (ParserConfigurationException e) {
			throw new MigrationException(e);
		} catch (IOException e) {
			throw new MigrationException(e);
		} catch (SAXException e) {
			throw new MigrationException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ignore) {}
			}
		}
	}

	/**
	 * Convert the Wikipedia article namespace (if any) to a JAMWiki article namespace.
	 */
	private String convertArticleNameFromWikipediaToJAMWiki(String fullName) {
		String ret = fullName;
		int pos = fullName.indexOf(':');
		if (pos > 0) {
			String namespace = fullName.substring(0, pos);
			String title = fullName.substring(pos+1);
			String jamwikiNamespace = mediawikiNamespaceMap.get(namespace);
			if (!StringUtils.isBlank(jamwikiNamespace)) {
				// matching JAMWiki namespace found
				ret = jamwikiNamespace + ":" + title;
			}
		}
		// remove any characters that are valid for Mediawiki but not JAMWiki
		ret = StringUtils.remove(ret, '?');
		return ret;
	}

	/**
	 * Convert all namespaces names from MediaWiki to JAMWiki local representation.
	 */
	private void convertToJAMWikiNamespaces(StringBuilder builder) {
		// convert all namespaces names from MediaWiki to JAMWiki local representation
		String jamwikiNamespace, mediawikiPattern, jamwikiPattern;
		int start = 0;
		for (String mediawikiNamespace : mediawikiNamespaceMap.keySet()) {
			jamwikiNamespace = mediawikiNamespaceMap.get(mediawikiNamespace);
			mediawikiPattern = "[[" + mediawikiNamespace + ":";
			jamwikiPattern = "[[" + jamwikiNamespace + ":";
			while ((start = builder.indexOf(mediawikiPattern, start + 1)) != -1) {
				builder.replace(start, start + mediawikiPattern.length(), jamwikiPattern);
			}
		}
	}

	/**
	 *
	 */
	private Timestamp parseMediaWikiTimestamp(String timestamp) {
		try {
			Date date = DateUtils.parseDate(timestamp, new String[]{MediaWikiConstants.ISO_8601_DATE_FORMAT});
			return new Timestamp(date.getTime());
		} catch (ParseException e) {
			// FIXME - this should be handled somehow
			return new Timestamp(System.currentTimeMillis());
		}
	}

	//===========================================================
	// SAX DocumentHandler methods
	//===========================================================

	/**
	 * start of xml-tag
	 *
	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
	 *  if Namespace processing is not being performed.
	 * @param localName The local name (without prefix), or the empty string if Namespace processing
	 *  is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The attributes attached to the element. If there are no attributes, it shall be an
	 *  empty Attributes object.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		this.currentElementBuffer = new StringBuilder();
		this.currentAttributeMap = new HashMap<String, String>();
		String key;
		if (attrs != null) {
			// populate the attribute map
			for (int i = 0; i < attrs.getLength(); i++) {
				key = attrs.getQName(i);
				if (!StringUtils.isBlank(key)) {
					this.currentAttributeMap.put(key, attrs.getValue(i));
				}
			}
		}
		if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION.equals(qName)) {
			this.currentTopicVersion = new TopicVersion();
			this.currentTopicVersion.setEditType(TopicVersion.EDIT_IMPORT);
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC.equals(qName)) {
			this.currentTopic = new Topic();
			this.currentTopicVersions = new TreeMap<Date, TopicVersion>();
		}
	}

	/**
	 * end of xml-tag
	 *
	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
	 *  if Namespace processing is not being performed.
	 * @param localName The local name (without prefix), or the empty string if Namespace processing
	 *  is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (StringUtils.equals(MediaWikiConstants.MEDIAWIKI_ELEMENT_NAMESPACE, qName)) {
			int key = NumberUtils.toInt(this.currentAttributeMap.get("key"));
			String jamwikiNamespace = MediaWikiConstants.NAMESPACE_CONVERSION_MAP.get(key);
			if (!StringUtils.isBlank(jamwikiNamespace)) {
				String mediawikiNamespace = currentElementBuffer.toString().trim();
				mediawikiNamespaceMap.put(mediawikiNamespace, jamwikiNamespace);
			}
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_NAME.equals(qName)) {
			String topicName = currentElementBuffer.toString().trim();
			topicName = convertArticleNameFromWikipediaToJAMWiki(topicName);
			WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
			currentTopic.setTopicType(WikiUtil.findTopicTypeForNamespace(wikiLink.getNamespace()));
			currentTopic.setName(topicName);
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_CONTENT.equals(qName)) {
			this.convertToJAMWikiNamespaces(currentElementBuffer);
			String topicContent = currentElementBuffer.toString().trim();
			currentTopicVersion.setVersionContent(topicContent);
			currentTopicVersion.setCharactersChanged(StringUtils.length(topicContent) - previousTopicContentLength);
			previousTopicContentLength = StringUtils.length(topicContent);
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_COMMENT.equals(qName)) {
			this.currentTopicVersion.setEditComment(currentElementBuffer.toString().trim());
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_EDIT_DATE.equals(qName)) {
			this.currentTopicVersion.setEditDate(this.parseMediaWikiTimestamp(currentElementBuffer.toString().trim()));
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_IP.equals(qName) || MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_USERNAME.equals(qName)) {
			this.currentTopicVersion.setAuthorDisplay(currentElementBuffer.toString().trim());
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION.equals(qName)) {
			this.currentTopicVersions.put(currentTopicVersion.getEditDate(), currentTopicVersion);
		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC.equals(qName)) {
			if (this.currentTopicVersions.isEmpty()) {
				throw new SAXException("No topic versions found for " + currentTopic.getName());
			}
			List<TopicVersion> currentTopicVersionList = new ArrayList<TopicVersion>();
			// topic versions are stored in a tree map to allow sorting... convert to a list
			TopicVersion lastTopicVersion = null;
			for (TopicVersion topicVersion : this.currentTopicVersions.values()) {
				currentTopicVersionList.add(topicVersion);
				lastTopicVersion = topicVersion;
			}
			currentTopic.setTopicContent(lastTopicVersion.getVersionContent());
			this.parsedTopics.put(currentTopic, currentTopicVersionList);
		}
	}

	/**
	 * When the parser encounters plain text (not XML elements), it calls this method
	 * which accumulates them in a string buffer
	 */
	public void characters(char buf[], int offset, int len) throws SAXException {
		currentElementBuffer.append(buf, offset, len);
	}
}
