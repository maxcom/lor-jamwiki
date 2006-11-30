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
import java.util.Hashtable;
import java.util.Vector;
import org.jamwiki.model.WikiConfigurationObject;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class WikiConfiguration {

	/** Standard logger. */
	private static WikiLogger logger = WikiLogger.getLogger(WikiConfiguration.class.getName());

	private static Vector dataHandlers = null;
	private static Hashtable namespaces = null;
	private static Vector parsers = null;
	private static Vector pseudotopics = null;
	private static Vector userHandlers = null;

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
	private static final String XML_USER_HANDLER = "user-handler";
	private static final String XML_USER_HANDLER_ROOT = "user-handlers";

	static {
		WikiConfiguration.initialize();
	}

	/**
	 *
	 */
	public static Collection getDataHandlers() {
		return WikiConfiguration.dataHandlers;
	}

	/**
	 *
	 */
	public static Hashtable getNamespaces() {
		return WikiConfiguration.namespaces;
	}

	/**
	 *
	 */
	public static Collection getParsers() {
		return WikiConfiguration.parsers;
	}

	/**
	 *
	 */
	public static Collection getPseudotopics() {
		return WikiConfiguration.pseudotopics;
	}

	/**
	 *
	 */
	public static Collection getUserHandlers() {
		return WikiConfiguration.userHandlers;
	}

	/**
	 *
	 */
	private static void initialize() {
		try {
			WikiConfiguration.dataHandlers = new Vector();
			WikiConfiguration.namespaces = new Hashtable();
			WikiConfiguration.parsers = new Vector();
			WikiConfiguration.pseudotopics = new Vector();
			WikiConfiguration.userHandlers = new Vector();
			File file = Utilities.getClassLoaderFile(JAMWIKI_CONFIGURATION_FILE);
			Document document = XMLUtil.parseXML(file, false);
			Node node = document.getElementsByTagName(XML_CONFIGURATION_ROOT).item(0);
			NodeList children = node.getChildNodes();
			Node child = null;
			for (int i=0; i < children.getLength(); i++) {
				child = children.item(i);
				if (child.getNodeName().equals(XML_PARSER_ROOT)) {
					WikiConfiguration.parsers = WikiConfiguration.parseConfigurationObjects(child, XML_PARSER);
				} else if (child.getNodeName().equals(XML_DATA_HANDLER_ROOT)) {
					WikiConfiguration.dataHandlers = WikiConfiguration.parseConfigurationObjects(child, XML_DATA_HANDLER);
				} else if (child.getNodeName().equals(XML_USER_HANDLER_ROOT)) {
					WikiConfiguration.userHandlers = WikiConfiguration.parseConfigurationObjects(child, XML_USER_HANDLER);
				} else if (child.getNodeName().equals(XML_NAMESPACE_ROOT)) {
					WikiConfiguration.parseNamespaces(child);
				} else if (child.getNodeName().equals(XML_PSEUDOTOPIC_ROOT)) {
					WikiConfiguration.parsePseudotopics(child);
				} else {
					logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
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
	private static WikiConfigurationObject parseConfigurationObject(Node node) throws Exception {
		WikiConfigurationObject configurationObject = new WikiConfigurationObject();
		NodeList children = node.getChildNodes();
		for (int j=0; j < children.getLength(); j++) {
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
				logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
			}
		}
		return configurationObject;
	}

	/**
	 *
	 */
	private static Vector parseConfigurationObjects(Node node, String name) throws Exception {
		Vector results = new Vector();
		NodeList children = node.getChildNodes();
		for (int j=0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(name)) {
				results.add(WikiConfiguration.parseConfigurationObject(child));
			} else {
				logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
			}
		}
		return results;
	}

	/**
	 *
	 */
	private static void parseNamespace(Node node) throws Exception {
		NodeList children = node.getChildNodes();
		String name = "";
		String main = "";
		String comments = "";
		for (int j=0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_NAME)) {
				name = XMLUtil.getTextContent(child);
			} else if (child.getNodeName().equals(XML_NAMESPACE_MAIN)) {
				main = XMLUtil.getTextContent(child);
			} else if (child.getNodeName().equals(XML_NAMESPACE_COMMENTS)) {
				comments = XMLUtil.getTextContent(child);
			} else {
				logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
			}
		}
		WikiConfiguration.namespaces.put(name, new String[]{main, comments});
	}

	/**
	 *
	 */
	private static void parseNamespaces(Node node) throws Exception {
		NodeList children = node.getChildNodes();
		for (int j=0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_NAMESPACE)) {
				WikiConfiguration.parseNamespace(child);
			} else {
				logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
			}
		}
	}

	/**
	 *
	 */
	private static void parsePseudotopic(Node node) throws Exception {
		NodeList children = node.getChildNodes();
		for (int j=0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_NAME)) {
				WikiConfiguration.pseudotopics.add(XMLUtil.getTextContent(child));
			} else {
				logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
			}
		}
	}

	/**
	 *
	 */
	private static void parsePseudotopics(Node node) throws Exception {
		NodeList children = node.getChildNodes();
		for (int j=0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PSEUDOTOPIC)) {
				WikiConfiguration.parsePseudotopic(child);
			} else {
				logger.finest("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
			}
		}
	}
}
