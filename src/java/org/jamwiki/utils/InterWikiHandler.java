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
import org.jamwiki.WikiBase;

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
	public static String formatInterWiki(String namespace, String value) {
		String pattern = InterWikiHandler.mapping.getProperty(namespace.toLowerCase());
		if (pattern == null) {
			return namespace + WikiBase.NAMESPACE_SEPARATOR + value;
		}
		try {
			Object[] objects = {Utilities.encodeForURL(value)};
			return MessageFormat.format(pattern, objects);
		} catch (Exception e) {
			logger.warning("Unable to format " + pattern + " with value " + value, e);
			return namespace + WikiBase.NAMESPACE_SEPARATOR + value;
		}
	}

	/**
	 * Return true if there is an inter-wiki mapping for the given namespace.
	 *
	 * @param namespace The inter-wiki link namespace.
	 * @return <code>true</code> if an inter-wiki mapping exists, <code>false</code>
	 *  otherwise.
	 */
	public static boolean isInterWiki(String namespace) {
		return InterWikiHandler.mapping.containsKey(namespace.toLowerCase());
	}
}
