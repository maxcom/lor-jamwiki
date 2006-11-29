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
package org.jamwiki.utils;

import java.io.File;
import java.util.Hashtable;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The purpose of this class is to load MediaWiki XML-file to the JAMWiki.
 */
public class XMLTopicFactory extends DefaultHandler {

	private WikiUser user;
	private String authorIpAddress;
	private String indentString = "    "; // Amount to indent
	private int indentLevel = 0;
	String virtualWiki = "en";
	Hashtable htCat = new Hashtable();
	Hashtable htNamespace = new Hashtable();
	String ns14 = "Category";
	String ns6 = "Image";
	Integer nsKey = null;
	String nsVal = null;
	String lastStr = null;
	String sPageName = null;
	String sPageText = null;
	private boolean bInText = false;
	private boolean bFirstPartOfText = false;
	private static String lineEnd =  System.getProperty("line.separator");
	private static final WikiLogger logger = WikiLogger.getLogger(XMLTopicFactory.class.getName());

	/**
	 *
	 */
	public XMLTopicFactory() {
	}

	/**
	 *
	 */
	public void importWikiXml(File file, String virtualWiki, WikiUser user, String authorIpAddress) throws Exception{

		this.virtualWiki = virtualWiki;
		this.authorIpAddress = authorIpAddress;
		this.user = user;
		//read ini params from file
		// TODO read all params from JAMWiki properties
		//importProps = Environment.loadProperties(PROPERTY_FILE_NAME);

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
			logger.severe("Error by impoting "+((XMLTopicFactory)this).sPageName, t);
			throw new Exception("Error by impot: "+t.getMessage(), t);
		}
	}

	//===========================================================
	// SAX DocumentHandler methods
	//===========================================================

	/**
	 *
	 */
	public void startDocument() throws SAXException {
		nl();
		nl();
		emit("START DOCUMENT");
		nl();
		emit("<?xml version='1.0' encoding='UTF-8'?>");
	}

	/**
	 *
	 */
	public void endDocument() throws SAXException {
		nl(); emit("END DOCUMENT");
		nl();
	}

	/**
	 * start of xml-tag
	 *
	 * @param lName Local name.
	 * @param qName Qualified name.
	 */
	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
		indentLevel++;
		nl(); emit("ELEMENT: ");
		String eName = lName; // element name
		if ("".equals(eName)) eName = qName; // namespaceAware = false
		emit("<"+eName);
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) aName = attrs.getQName(i);
				nl();
				emit("   ATTR: ");
				emit(aName);
				emit("\t\"");
				emit(attrs.getValue(i));
				emit("\"");
			}
		}
		if (attrs.getLength() > 0) nl();
		emit(">");
		if ("namespace".equals(eName)) { // mapping of namespaces from imported file
			nsKey = new Integer(attrs.getValue("key"));
		}
		if ("page".equals(eName)) {
			sPageName = "";
			sPageText = "";
		}
		if ("title".equals(qName)) {
			bInText = true;
			bFirstPartOfText = true;
		}
		if ("text".equals(qName)) {
			bInText = true;
			bFirstPartOfText = true;
		}
	}

	/**
	 * end of xml-tag
	 *
	 * @param sName Simple name.
	 * @param qName Qualified name.
	 */
	public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
		nl();
		emit("END_ELM: ");
		emit("</"+sName+">");
		if ("namespace".equals(qName)) { // mapping of namespaces from imported file
			htNamespace.put(lastStr.trim(), nsKey);
			//Prepare locale namespaces
			//WikiArticle.addNamespace(nsKey.intValue(), lastStr.trim());
			if (nsKey.intValue() == 14) {
				ns14 = lastStr.trim();
			}
			if (nsKey.intValue() == 6) {
				ns6 = lastStr.trim();
			}
		}
		if ("title".equals(qName)) {
			bInText = false;
			bFirstPartOfText = false;
			sPageName = lastStr.trim();
		}
		if ("text".equals(qName)) {
			bInText = false;
			bFirstPartOfText = false;
			sPageText = lastStr.trim();
		}
		if ("page".equals(qName)) {
			//Create Topic
			String sNamespace = "";
			int namespace = 0;
			// get wiki namespace
			int pos = sPageName.indexOf(":");
			if (pos > -1) {
				sNamespace = sPageName.substring(0, pos);
				if (htNamespace.containsKey(sNamespace)) {
					namespace = ((Integer)htNamespace.get(sNamespace)).intValue();
				} else { // unknown namespace
					namespace = -1;
				}
			} else { // main namespace
				namespace = 0;
			}
			// preprocess text of article to fit JAMWiki
			sPageText = preprocessText(sPageText);
			Topic article = new Topic();
			article.setName(convertArticleNameFromWikipediaToJAMWiki(sPageName));
			article.setVirtualWiki(virtualWiki);
			article.setTopicContent(sPageText);
			TopicVersion topicVersion = new TopicVersion(user, authorIpAddress, "imported", sPageText);
			// manage mapping bitween MediaWiki and JAMWiki namespaces
			article.setTopicType(convertNamespaceFromMediaWikiToJAMWiki(namespace));
			// Store article in database
			try {
				//TopicVersion topicVersion = new TopicVersion(user, request.getRemoteAddr(), request.getParameter("editComment"), contents);
				WikiBase.getDataHandler().writeTopic(article, topicVersion, Utilities.parserDocument(sPageText, virtualWiki, sPageName), true, null);
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}
		indentLevel--;
	}

	/**
	 *
	 */
	public void characters(char buf[], int offset, int len) throws SAXException {
		nl(); emit("CHARS:   ");
		String s = new String(buf, offset, len);
		if (!s.trim().equals("")) emit(s);
		if (bInText) {
			if (bFirstPartOfText) {
				bFirstPartOfText = false;
				lastStr = s;
			} else {
				lastStr += s;
			}
		} else {
			lastStr = s;
		}
	}

	/**
	 * Wrap I/O exceptions in SAX exceptions, to suit handler signature requirements.
	 */
	private void emit(String s) throws SAXException {
		logger.fine(s);
	}

	/**
	 * Start a new line and indent the next line appropriately.
	 */
	private void nl() throws SAXException {
		logger.fine(lineEnd);
		for (int i=0; i < indentLevel; i++) logger.fine(indentString);
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
			ret = Topic.TYPE_ARTICLE; break;
		//case 0: ret = Topic.TYPE_REDIRECT; break; //special hendling for redirects
		case 6:
			ret = Topic.TYPE_IMAGE; break;
		case 14:
			ret = Topic.TYPE_CATEGORY; break;
		//case 0: ret = Topic.TYPE_FILE; break;
		//case 0: ret = Topic.TYPE_SYSTEM_FILE; break;
		case 10:
			ret = Topic.TYPE_TEMPLATE; break;
		}
		return ret;
	}

	/**
	 *
	 */
	private String getJAMWikiNamespaceById(int jamWikiNamespaceId) {
		String ret = "";
		switch(jamWikiNamespaceId) {
		case Topic.TYPE_IMAGE:
			ret = NamespaceHandler.NAMESPACE_IMAGE; break;
		case Topic.TYPE_CATEGORY:
			ret = NamespaceHandler.NAMESPACE_CATEGORY; break;
		case Topic.TYPE_TEMPLATE:
			ret = NamespaceHandler.NAMESPACE_TEMPLATE; break;
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
		String sTitle = sPageName;
		int pos = sPageName.indexOf(":");
		if (pos > -1) {
			sNamespace = sPageName.substring(0, pos);
			if (htNamespace.containsKey(sNamespace)) {
				int namespace = ((Integer)htNamespace.get(sNamespace)).intValue();
				sTitle = sPageName.substring(pos+1);
				sJAMNamespace = getJAMWikiNamespaceById(convertNamespaceFromMediaWikiToJAMWiki(namespace));
				if (sJAMNamespace.length() > 0) {
					ret = sJAMNamespace + ":" + sTitle;
				} else {//equivalent namespace in JAMWiki not found. Use original name
					ret = sNamespace + ":" + sTitle;
				}
			} else { //namespace not found
				ret = sPageName;
			}
		} else { //main namespace
			ret = sPageName;
		}
		return ret;
	}

	/**
	 * preprocess the text of article
	 * convert all namespaces names from MediaWiki to JAMWiki local representation
	 * and so on...
	 */
	public String preprocessText(String text) {
		String ret = text;
		// convert all namespaces names from MediaWiki to JAMWiki local representation
		ret = StringUtils.replace(ret, "[[category:","[["+NamespaceHandler.NAMESPACE_CATEGORY+":");
		if (!"Category".equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
			ret = StringUtils.replace(ret, "[[Category:","[["+NamespaceHandler.NAMESPACE_CATEGORY+":");
		}
		ret = StringUtils.replace(ret, "[["+ns14+":","[["+NamespaceHandler.NAMESPACE_CATEGORY+":");
		ret = StringUtils.replace(ret, "[[image:","[["+NamespaceHandler.NAMESPACE_IMAGE+":");
		if (!"Image".equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
			ret = StringUtils.replace(ret, "[[Image:","[["+NamespaceHandler.NAMESPACE_IMAGE+":");
		}
		ret = StringUtils.replace(ret, "[["+ns6+":","[["+NamespaceHandler.NAMESPACE_IMAGE+":");

		return ret;
	}
}
