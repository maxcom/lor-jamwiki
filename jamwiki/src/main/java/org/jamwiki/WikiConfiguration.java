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
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
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

	private Vector dataHandlers = null;
	private HashMap namespaces = null;
	private Vector parsers = null;
	private Vector pseudotopics = null;
	private Vector searchEngines = null;
	private HashMap translations = null;
	private Vector userHandlers = null;

	/** Name of the configuration file. */
	public static final String JAMWIKI_CONFIGURATION_FILE = "jamwiki-configuration.xml";
	private static final String XML_CONFIGURATION_ROOT = "configuration";
	private static final String XML_DATA_HANDLER = "data-handler";
	private static final String XML_DATA_HANDLER_ROOT = "data-handlers";
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
	private static final String XML_USER_HANDLER = "user-handler";
	private static final String XML_USER_HANDLER_ROOT = "user-handlers";

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
	public HashMap getNamespaces() {
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
	public HashMap getTranslations() {
		return this.translations;
	}

	/**
	 *
	 */
	public Collection getUserHandlers() {
		return this.userHandlers;
	}

	/**
	 *
	 */
	private void initialize() {
		try {
			this.dataHandlers = new Vector();
			this.namespaces = new HashMap();
			this.parsers = new Vector();
			this.pseudotopics = new Vector();
			this.searchEngines = new Vector();
			this.translations = new HashMap();
			this.userHandlers = new Vector();
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
				} else if (child.getNodeName().equals(XML_SEARCH_ENGINE_ROOT)) {
					this.searchEngines = this.parseConfigurationObjects(child, XML_SEARCH_ENGINE);
				} else if (child.getNodeName().equals(XML_USER_HANDLER_ROOT)) {
					this.userHandlers = this.parseConfigurationObjects(child, XML_USER_HANDLER);
				} else if (child.getNodeName().equals(XML_NAMESPACE_ROOT)) {
					this.parseNamespaces(child);
				} else if (child.getNodeName().equals(XML_PSEUDOTOPIC_ROOT)) {
					this.parsePseudotopics(child);
				} else if (child.getNodeName().equals(XML_TRANSLATION_ROOT)) {
					this.parseTranslations(child);
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
	private Vector parseConfigurationObjects(Node node, String name) throws Exception {
		Vector results = new Vector();
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
	 *
	 */
	private void parseTranslation(Node node) throws Exception {
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
		this.translations.put(key, name);
	}

	/**
	 *
	 */
	private void parseTranslations(Node node) throws Exception {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_TRANSLATION)) {
				this.parseTranslation(child);
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
