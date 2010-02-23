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

import java.text.MessageFormat;
import java.util.Properties;
import org.jamwiki.Environment;

/**
 * Class for controlling inter-wiki links. An interwiki link is a link that is
 * specified using a namespace that is then resolved into an external link, such
 * as "Wikipedia:Main Page", which would resolve to
 * "http://en.wikipedia.org/wiki/Main_Page".  The mappings of namespace values to
 * URL patterns are persisted in WEB-INF/classes/interwiki.properties.
 */
public class InterWikiHandler {

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(InterWikiHandler.class.getName());
	/** Properties bundle to store mappings */
	private static Properties mapping;
	/** Name of resource to access the persisted bundle */
	private static final String RESOURCE_NAME = "/interwiki.properties";

	static {
		InterWikiHandler.mapping = Environment.loadProperties(RESOURCE_NAME);
	}

	/**
	 *
	 */
	private InterWikiHandler() {
	}

	/**
	 * Retrieve an inter-wiki mapping for the given namespace and use the value
	 * parameter to create a URL to that wiki.  For example, a namespace of
	 * "wikipedia" and a value of "Main Page" will resolve to a the URL
	 * "http://en.wikipedia.org/wiki/Main_Page".
	 *
	 * @param namespace The inter-wiki link namespace corresponding to the key
	 *  value in the interwiki.properties file.  This link is compared in a
	 *  case-insensitive manner.
	 * @param value The page or topic name that is being linked to.
	 * @return Returns a formatted URL that links to the page specified by the
	 *  namespace and value.  If no namespace mapping is present then a string
	 *  that combines the namespace and value is returned, such as
	 *  "namespace:value".
	 */
	public static String formatInterWiki(String namespace, String value) {
		String pattern = InterWikiHandler.mapping.getProperty(namespace.toLowerCase());
		if (pattern == null) {
			return namespace + Namespace.SEPARATOR + value;
		}
		Object[] objects = {Utilities.encodeAndEscapeTopicName(value)};
		try {
			return MessageFormat.format(pattern, objects);
		} catch (IllegalArgumentException e) {
			logger.warning("Unable to format " + pattern + " with value " + value, e);
			return namespace + Namespace.SEPARATOR + value;
		}
	}

	/**
	 * Return true if there is an inter-wiki mapping for the given namespace.
	 *
	 * @param namespace The inter-wiki link namespace corresponding to the key
	 *  value in the interwiki.properties file.  This link is compared in a
	 *  case-insensitive manner.
	 * @return <code>true</code> if an inter-wiki mapping exists, <code>false</code>
	 *  otherwise.
	 */
	public static boolean isInterWiki(String namespace) {
		return InterWikiHandler.mapping.containsKey(namespace.toLowerCase());
	}
}
