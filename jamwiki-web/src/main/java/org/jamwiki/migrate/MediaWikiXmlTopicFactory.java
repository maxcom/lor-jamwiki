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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The purpose of this class is to load MediaWiki XML-file to the JAMWiki.
 */
public class MediaWikiXmlTopicFactory extends DefaultHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(MediaWikiXmlTopicFactory.class.getName());
	private static final int MEDIAWIKI_MAIN_NAMESPACE_ID = 0;
	private static final int MEDIAWIKI_FILE_NAMESPACE_ID = 6;
	private static final int MEDIAWIKI_TEMPLATE_NAMESPACE_ID = 10;
	private static final int MEDIAWIKI_CATEGORY_NAMESPACE_ID = 14;

	/** This map holds the current tag's attribute names and values.  It is cleared after an end-element is called and thus fails for nested elements. */
	private Map<String, String> currentAttributeMap = new HashMap<String, String>();
	/** This buffer holds the content of the current element during parsing.  It will be flushed after an end-element tag is reached. */
	private StringBuffer currentElementBuffer = new StringBuffer();
	private final WikiUser user;
	private final String authorIpAddress;
	private String virtualWiki = "en";
	private final Map<Integer, String> namespaces = new HashMap<Integer, String>();
	private String pageName = null;
	private String pageText = null;
	private String processedTopicName = null;

	/**
	 *
	 */
	public MediaWikiXmlTopicFactory(String virtualWiki, WikiUser user, String authorIpAddress) {
		super();
		this.virtualWiki = virtualWiki;
		this.authorIpAddress = authorIpAddress;
		this.user = user;
	}

	/**
	 *
	 */
	public String importWikiXml(File file) throws Exception{
		//For big file parsing
		System.setProperty("entityExpansionLimit", "1000000");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input file
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(file, this);
		} catch (Throwable t) {
			logger.severe("Error by importing "+((MediaWikiXmlTopicFactory)this).pageName, t);
			throw new Exception("Error by import: "+t.getMessage(), t);
		}
		return this.processedTopicName;
	}

	/**
	 * Once a <page> element has finished parsing create the topic specified in the XML data.
	 */
	private void createTopic() throws SAXException {
		Topic topic = new Topic();
		String topicName = convertArticleNameFromWikipediaToJAMWiki(pageName);
		topic.setName(topicName);
		topic.setVirtualWiki(virtualWiki);
		// preprocess text of topic to fit JAMWiki
		pageText = convertNamespaces(pageText);
		topic.setTopicContent(pageText);
		int charactersChanged = StringUtils.length(pageText);
		TopicVersion topicVersion = new TopicVersion(user, authorIpAddress, "imported", pageText, charactersChanged);
		WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
		topic.setTopicType(WikiUtil.findTopicTypeForNamespace(wikiLink.getNamespace()));
		// Store topic in database
		try {
			ParserOutput parserOutput = ParserUtil.parserOutput(pageText, virtualWiki, pageName);
			WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks(), true);
			this.processedTopicName = topic.getName();
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	/**
	 * Convert MediaWiki namespace to JAMWiki namespace
	 *
	 * @param mediaWikiNamespaceId
	 * @return
	 */
	private String findJamwikiNamespace(String mediaWikiNamespace) {
		String ret = "";
		if (StringUtils.equals(namespaces.get(MEDIAWIKI_FILE_NAMESPACE_ID), mediaWikiNamespace)) {
			ret = NamespaceHandler.NAMESPACE_IMAGE;
		} else if (StringUtils.equals(namespaces.get(MEDIAWIKI_CATEGORY_NAMESPACE_ID), mediaWikiNamespace)) {
			ret = NamespaceHandler.NAMESPACE_CATEGORY;
		} else if (StringUtils.equals(namespaces.get(MEDIAWIKI_TEMPLATE_NAMESPACE_ID), mediaWikiNamespace)) {
			ret = NamespaceHandler.NAMESPACE_TEMPLATE;
		}
		return ret;
	}

	/**
	 * Convert the Wikipedia article namespace (if any) to a JAMWiki article namespace.
	 */
	private String convertArticleNameFromWikipediaToJAMWiki(String fullName) {
		String ret = pageName;
		int pos = pageName.indexOf(':');
		if (pos > 0) {
			String namespace = pageName.substring(0, pos);
			String title = pageName.substring(pos+1);
			String jamwikiNamespace = findJamwikiNamespace(namespace);
			if (jamwikiNamespace.length() > 0) {
				// matching JAMWiki namespace found
				ret = jamwikiNamespace + ":" + title;
			}
		}
		return ret;
	}

	/**
	 * Convert all namespaces names from MediaWiki to JAMWiki local representation.
	 */
	private String convertNamespaces(String text) {
		String ret = text;
		// convert all namespaces names from MediaWiki to JAMWiki local representation
		ret = Pattern.compile("\\[\\[" + namespaces.get(MEDIAWIKI_CATEGORY_NAMESPACE_ID) + "\\:", Pattern.CASE_INSENSITIVE).matcher(ret).replaceAll("[[" + NamespaceHandler.NAMESPACE_CATEGORY + ":");
		ret = Pattern.compile("\\[\\[" + namespaces.get(MEDIAWIKI_FILE_NAMESPACE_ID) + "\\:", Pattern.CASE_INSENSITIVE).matcher(ret).replaceAll("[[" + NamespaceHandler.NAMESPACE_IMAGE + ":");
		return ret;
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
		this.currentElementBuffer = new StringBuffer();
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
		if (StringUtils.equals("page", qName)) {
			// new page, reset values
			pageName = "";
			pageText = "";
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
		if (StringUtils.equals("namespace", qName)) {
			namespaces.put(NumberUtils.toInt(this.currentAttributeMap.get("key")), currentElementBuffer.toString().trim());
		}
		if ("title".equals(qName)) {
			pageName = currentElementBuffer.toString().trim();
		}
		if ("text".equals(qName)) {
			pageText = currentElementBuffer.toString().trim();
		}
		if ("page".equals(qName)) {
			this.createTopic();
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
