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
import java.util.Hashtable;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.utils.NamespaceHandler;
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
	private final WikiUser user;
	private final String authorIpAddress;
	private String virtualWiki = "en";
	private final Hashtable<String, Object> namespaces = new Hashtable<String, Object>();
	private String mediawikiCategoryNamespace = "Category";
	private String mediawikiImageNamespace = "Image";
	private Integer nsKey = null;
	private StringBuffer lastStr = null;
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
		// Use an instance of ourselves as the SAX event handler
		// DefaultHandler handler = new XMLPageFactory();
		// Use the default (non-validating) parser
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

	//===========================================================
	// SAX DocumentHandler methods
	//===========================================================

	/**
	 * start of xml-tag
	 *
	 * @param lName Local name.
	 * @param qName Qualified name.
	 */
	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
		String eName = lName;
		if ("".equals(eName)) {
			eName = qName;
		}
		lastStr = new StringBuffer();
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) {
					aName = attrs.getQName(i);
				}
			}
			if ("namespace".equals(eName)) {
				// mapping of namespaces from imported file
				nsKey = Integer.valueOf(attrs.getValue("key"));
			}
		}
		if ("page".equals(eName)) {
			pageName = "";
			pageText = "";
		}
	}

	/**
	 * end of xml-tag
	 *
	 * @param sName Simple name.
	 * @param qName Qualified name.
	 */
	public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
		if ("namespace".equals(qName)) {
			// mapping of namespaces from imported file
			namespaces.put(lastStr.toString().trim(), nsKey);
			//Prepare locale namespaces
			if (nsKey.intValue() == 14) {
				mediawikiCategoryNamespace = lastStr.toString().trim();
			}
			if (nsKey.intValue() == 6) {
				mediawikiImageNamespace = lastStr.toString().trim();
			}
		}
		if ("title".equals(qName)) {
			pageName = lastStr.toString().trim();
		}
		if ("text".equals(qName)) {
			pageText = lastStr.toString().trim();
		}
		if ("page".equals(qName)) {
			//Create Topic
			String sNamespace = "";
			int namespace = 0;
			// get wiki namespace
			int pos = pageName.indexOf(':');
			if (pos > -1) {
				sNamespace = pageName.substring(0, pos);
				if (namespaces.containsKey(sNamespace)) {
					namespace = ((Integer)namespaces.get(sNamespace));
				} else { // unknown namespace
					namespace = -1;
				}
			} else { // main namespace
				namespace = 0;
			}
			// preprocess text of topic to fit JAMWiki
			pageText = preprocessText(pageText);
			Topic topic = new Topic();
			topic.setName(convertArticleNameFromWikipediaToJAMWiki(pageName));
			topic.setVirtualWiki(virtualWiki);
			topic.setTopicContent(pageText);
			int charactersChanged = StringUtils.length(pageText);
			TopicVersion topicVersion = new TopicVersion(user, authorIpAddress, "imported", pageText, charactersChanged);
			// manage mapping bitween MediaWiki and JAMWiki namespaces
			topic.setTopicType(convertNamespaceFromMediaWikiToJAMWiki(namespace));
			// Store topic in database
			try {
				ParserOutput parserOutput = ParserUtil.parserOutput(pageText, virtualWiki, pageName);
				WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks(), true);
				this.processedTopicName = topic.getName();
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}
	}

	/**
	 *
	 */
	public void characters(char buf[], int offset, int len) throws SAXException {
		lastStr.append(buf, offset, len);
	}

	/**
	 * convert MediaWiki namespace-id to JAMWiki namespace-id
	 * @param mediaWikiNamespaceId
	 * @return
	 */
	private int convertNamespaceFromMediaWikiToJAMWiki(int mediaWikiNamespaceId) {
		int ret = -1;
		switch(mediaWikiNamespaceId) {
			case 0:
				ret = Topic.TYPE_ARTICLE;
				break;
			case 6:
				ret = Topic.TYPE_IMAGE;
				break;
			case 14:
				ret = Topic.TYPE_CATEGORY;
				break;
			case 10:
				ret = Topic.TYPE_TEMPLATE;
				break;
		}
		return ret;
	}

	/**
	 *
	 */
	private String convertArticleNameFromWikipediaToJAMWiki(String fullName) {
		String ret = fullName;
		String sNamespace = "";
		String sJAMNamespace = "";
		String sTitle = pageName;
		int pos = pageName.indexOf(':');
		if (pos > -1) {
			sNamespace = pageName.substring(0, pos);
			if (namespaces.containsKey(sNamespace)) {
				int namespace = ((Integer)namespaces.get(sNamespace));
				sTitle = pageName.substring(pos+1);
				sJAMNamespace = WikiUtil.findNamespaceForTopicType(convertNamespaceFromMediaWikiToJAMWiki(namespace));
				if (sJAMNamespace.length() > 0) {
					ret = sJAMNamespace + ":" + sTitle;
				} else {
					//equivalent namespace in JAMWiki not found. Use original name
					ret = sNamespace + ":" + sTitle;
				}
			} else { //namespace not found
				ret = pageName;
			}
		} else { //main namespace
			ret = pageName;
		}
		return ret;
	}

	/**
	 * Preprocess the text of topic, converting all namespaces names from MediaWiki to JAMWiki
	 * local representation.
	 */
	private String preprocessText(String text) {
		String ret = text;
		// convert all namespaces names from MediaWiki to JAMWiki local representation
		ret = StringUtils.replace(ret, "[[category:", "[[" + NamespaceHandler.NAMESPACE_CATEGORY + ":");
		if (!"Category".equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
			ret = StringUtils.replace(ret, "[[Category:", "[["+NamespaceHandler.NAMESPACE_CATEGORY+":");
		}
		ret = StringUtils.replace(ret, "[[" + mediawikiCategoryNamespace + ":", "[[" + NamespaceHandler.NAMESPACE_CATEGORY + ":");
		ret = StringUtils.replace(ret, "[[image:", "[[" + NamespaceHandler.NAMESPACE_IMAGE + ":");
		if (!"Image".equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
			ret = StringUtils.replace(ret, "[[Image:", "[[" + NamespaceHandler.NAMESPACE_IMAGE + ":");
		}
		ret = StringUtils.replace(ret, "[[" + mediawikiImageNamespace + ":", "[["+NamespaceHandler.NAMESPACE_IMAGE+":");

		return ret;
	}
}
