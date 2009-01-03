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
import java.util.ArrayList;
import java.util.Collection;
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

	private List dataHandlers = null;
	private Map editors = null;
	private Map namespaces = null;
	private List parsers = null;
	private List pseudotopics = null;
	private List searchEngines = null;
	private Map translations = null;

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
	public Collection getDataHandlers() {
		return this.dataHandlers;
	}

	/**
	 *
	 */
	public Map getEditors() {
		return this.editors;
	}

	/**
	 *
	 */
	public Map getNamespaces() {
		return this.namespaces;
	}

	/**
	 *
	 */
	public Collection getParsers() {
		return this.parsers;
	}

	/**
	 *
	 */
	public Collection getPseudotopics() {
		return this.pseudotopics;
	}

	/**
	 *
	 */
	public Collection getSearchEngines() {
		return this.searchEngines;
	}

	/**
	 *
	 */
	public Map getTranslations() {
		return this.translations;
	}

	/**
	 *
	 */
	private void initialize() {
		try {
			this.dataHandlers = new ArrayList();
			this.editors = new LinkedHashMap();
			this.namespaces = new LinkedHashMap();
			this.parsers = new ArrayList();
			this.pseudotopics = new ArrayList();
			this.searchEngines = new ArrayList();
			this.translations = new LinkedHashMap();
			File file = Utilities.getClassLoaderFile(JAMWIKI_CONFIGURATION_FILE);
			Document document = XMLUtil.parseXML(file, false);
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
		} catch (Exception e) {
			logger.severe("Failure while parsing configuration file " + JAMWIKI_CONFIGURATION_FILE, e);
		}
	}

	/**
	 *
	 */
	private WikiConfigurationObject parseConfigurationObject(Node node) throws Exception {
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
	private List parseConfigurationObjects(Node node, String name) throws Exception {
		List results = new ArrayList();
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
	private void parseMapNode(Node node, Map resultMap) throws Exception {
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
	private void parseMapNodes(Node node, Map resultsMap, String childNodeName) throws Exception {
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
	private void parseNamespace(Node node) throws Exception {
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
	private void parseNamespaces(Node node) throws Exception {
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
	private void parsePseudotopic(Node node) throws Exception {
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
	private void parsePseudotopics(Node node) throws Exception {
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
