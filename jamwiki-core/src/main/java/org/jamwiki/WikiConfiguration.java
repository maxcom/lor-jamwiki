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

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jamwiki.utils.Utilities;
import org.jamwiki.model.WikiConfigurationObject;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The <code>WikiConfiguration</code> class provides the infrastructure for
 * retrieving configuration values.  Note that with JAMWiki configuration
 * values differ from site properties by being generally less site-specific
 * and falling into specific categories, such as pseudo-topics and parser
 * values.
 *
 * @see org.jamwiki.utils.PseudoTopicHandler
 * @see org.jamwiki.utils.NamespaceHandler
 */
public class WikiConfiguration {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(WikiConfiguration.class.getName());

	private static WikiConfiguration instance = null;

	private List<WikiConfigurationObject> dataHandlers = null;
	private Map<String, String> editors = null;
	private Map<String, String[]> namespaces = null;
	private List<WikiConfigurationObject> parsers = null;
	private List<String> pseudotopics = null;
	private List<WikiConfigurationObject> searchEngines = null;
	private Map<String, String> translations = null;

	/** Name of the configuration file. */
	public static final String JAMWIKI_CONFIGURATION_FILE = "jamwiki-configuration.xml";
	private static final String XML_CONFIGURATION_ROOT = "configuration";
	private static final String XML_DATA_HANDLER = "data-handler";
	private static final String XML_DATA_HANDLER_ROOT = "data-handlers";
	private static final String XML_EDITOR = "editor";
	private static final String XML_EDITOR_ROOT = "editors";
	private static final String XML_NAMESPACE = "namespace";
	private static final String XML_NAMESPACE_COMMENTS = "comments";
	private static final String XML_NAMESPACE_MAIN = "main";
	private static final String XML_NAMESPACE_ROOT = "namespaces";
	private static final String XML_PARAM_CLASS = "class";
	private static final String XML_PARAM_KEY = "key";
	private static final String XML_PARAM_NAME = "name";
	private static final String XML_PARAM_STATE = "state";
	private static final String XML_PARSER = "parser";
	private static final String XML_PARSER_ROOT = "parsers";
	private static final String XML_PSEUDOTOPIC = "pseudotopic";
	private static final String XML_PSEUDOTOPIC_ROOT = "pseudotopics";
	private static final String XML_SEARCH_ENGINE = "search-engine";
	private static final String XML_SEARCH_ENGINE_ROOT = "search-engines";
	private static final String XML_TRANSLATION = "translation";
	private static final String XML_TRANSLATION_ROOT = "translations";

	/**
	 *
	 */
	private WikiConfiguration() {
		this.initialize();
	}

	/**
	 *
	 */
	public static WikiConfiguration getInstance() {
		if (WikiConfiguration.instance == null) {
			WikiConfiguration.instance = new WikiConfiguration();
		}
		return WikiConfiguration.instance;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getDataHandlers() {
		return this.dataHandlers;
	}

	/**
	 *
	 */
	public Map<String, String> getEditors() {
		return this.editors;
	}

	/**
	 *
	 */
	public Map<String, String[]> getNamespaces() {
		return this.namespaces;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getParsers() {
		return this.parsers;
	}

	/**
	 *
	 */
	public List<String> getPseudotopics() {
		return this.pseudotopics;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getSearchEngines() {
		return this.searchEngines;
	}

	/**
	 *
	 */
	public Map<String, String> getTranslations() {
		return this.translations;
	}

	/**
	 *
	 */
	private void initialize() {
		this.dataHandlers = new ArrayList<WikiConfigurationObject>();
		this.editors = new LinkedHashMap<String, String>();
		this.namespaces = new LinkedHashMap<String, String[]>();
		this.parsers = new ArrayList<WikiConfigurationObject>();
		this.pseudotopics = new ArrayList<String>();
		this.searchEngines = new ArrayList<WikiConfigurationObject>();
		this.translations = new LinkedHashMap<String, String>();
		File file = null;
		Document document = null;
		try {
			file = Utilities.getClassLoaderFile(JAMWIKI_CONFIGURATION_FILE);
			document = XMLUtil.parseXML(file, false);
		} catch (ParseException e) {
			// this should never happen unless someone mangles the config file
			throw new IllegalStateException("Unable to parse configuration file " + JAMWIKI_CONFIGURATION_FILE, e);
		} catch (FileNotFoundException e) {
			// this should never happen unless someone deletes the file
			throw new IllegalStateException("Unable to find configuration file " + JAMWIKI_CONFIGURATION_FILE, e);
		}
		Node node = document.getElementsByTagName(XML_CONFIGURATION_ROOT).item(0);
		NodeList children = node.getChildNodes();
		Node child = null;
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			if (child.getNodeName().equals(XML_PARSER_ROOT)) {
				this.parsers = this.parseConfigurationObjects(child, XML_PARSER);
			} else if (child.getNodeName().equals(XML_DATA_HANDLER_ROOT)) {
				this.dataHandlers = this.parseConfigurationObjects(child, XML_DATA_HANDLER);
			} else if (child.getNodeName().equals(XML_EDITOR_ROOT)) {
				this.parseMapNodes(child, this.editors, XML_EDITOR);
			} else if (child.getNodeName().equals(XML_SEARCH_ENGINE_ROOT)) {
				this.searchEngines = this.parseConfigurationObjects(child, XML_SEARCH_ENGINE);
			} else if (child.getNodeName().equals(XML_NAMESPACE_ROOT)) {
				this.parseNamespaces(child);
			} else if (child.getNodeName().equals(XML_PSEUDOTOPIC_ROOT)) {
				this.parsePseudotopics(child);
			} else if (child.getNodeName().equals(XML_TRANSLATION_ROOT)) {
				this.parseMapNodes(child, this.translations, XML_TRANSLATION);
			} else {
				logUnknownChild(node, child);
			}
		}
		logger.config("Configuration values loaded from " + file.getPath());
	}

	/**
	 *
	 */
	private WikiConfigurationObject parseConfigurationObject(Node node) {
		WikiConfigurationObject configurationObject = new WikiConfigurationObject();
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_CLASS)) {
				configurationObject.setClazz(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_KEY)) {
				configurationObject.setKey(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_NAME)) {
				configurationObject.setName(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_STATE)) {
				configurationObject.setState(XMLUtil.getTextContent(child));
			} else {
				logUnknownChild(node, child);
			}
		}
		return configurationObject;
	}

	/**
	 *
	 */
	private List<WikiConfigurationObject> parseConfigurationObjects(Node node, String name) {
		List<WikiConfigurationObject> results = new ArrayList<WikiConfigurationObject>();
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(name)) {
				results.add(this.parseConfigurationObject(child));
			} else {
				logUnknownChild(node, child);
			}
		}
		return results;
	}

	/**
	 * Utility method for parsing a key-value node.
	 */
	private void parseMapNode(Node node, Map<String, String> resultMap) {
		NodeList children = node.getChildNodes();
		String name = "";
		String key = "";
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_NAME)) {
				name = XMLUtil.getTextContent(child);
			} else if (child.getNodeName().equals(XML_PARAM_KEY)) {
				key = XMLUtil.getTextContent(child);
			} else {
				logUnknownChild(node, child);
			}
		}
		resultMap.put(key, name);
	}

	/**
	 * Utility method for parsing nodes that are collections of key-value pairs.
	 */
	private void parseMapNodes(Node node, Map<String, String> resultsMap, String childNodeName) {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(childNodeName)) {
				this.parseMapNode(child, resultsMap);
			} else {
				logUnknownChild(node, child);
			}
		}
	}

	/**
	 *
	 */
	private void parseNamespace(Node node) {
		NodeList children = node.getChildNodes();
		String name = "";
		String main = "";
		String comments = "";
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_NAME)) {
				name = XMLUtil.getTextContent(child);
			} else if (child.getNodeName().equals(XML_NAMESPACE_MAIN)) {
				main = XMLUtil.getTextContent(child);
			} else if (child.getNodeName().equals(XML_NAMESPACE_COMMENTS)) {
				comments = XMLUtil.getTextContent(child);
			} else {
				logUnknownChild(node, child);
			}
		}
		this.namespaces.put(name, new String[]{main, comments});
	}

	/**
	 *
	 */
	private void parseNamespaces(Node node) {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_NAMESPACE)) {
				this.parseNamespace(child);
			} else {
				logUnknownChild(node, child);
			}
		}
	}

	/**
	 *
	 */
	private void parsePseudotopic(Node node) {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_NAME)) {
				this.pseudotopics.add(XMLUtil.getTextContent(child));
			} else {
				logUnknownChild(node, child);
			}
		}
	}

	/**
	 *
	 */
	private void parsePseudotopics(Node node) {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PSEUDOTOPIC)) {
				this.parsePseudotopic(child);
			} else {
				logUnknownChild(node, child);
			}
		}
	}

	/**
	 * Utility class to log two XML nodes.
	 * @param node
	 * @param child
	 */
	private void logUnknownChild(Node node, Node child) {
		logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
	}
}
