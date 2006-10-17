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

import java.util.Iterator;
import java.util.Properties;
import org.jamwiki.Environment;
import org.springframework.util.StringUtils;

/**
 *
 */
public class NamespaceHandler {

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(NamespaceHandler.class.getName());
	/** Properties bundle to store mappings */
	private static Properties mapping;
	/** Name of resource to access the persisted bundle */
	private static final String RESOURCE_NAME = "/namespaces.properties";
	public static final String NAMESPACE_SEPARATOR = ":";
	public static final String NAMESPACE_SPECIAL;
	public static final String NAMESPACE_COMMENTS;
	public static final String NAMESPACE_IMAGE;
	public static final String NAMESPACE_IMAGE_COMMENTS;
	public static final String NAMESPACE_CATEGORY;
	public static final String NAMESPACE_CATEGORY_COMMENTS;
	public static final String NAMESPACE_JAMWIKI;
	public static final String NAMESPACE_JAMWIKI_COMMENTS;
	public static final String NAMESPACE_TEMPLATE;
	public static final String NAMESPACE_TEMPLATE_COMMENTS;
	public static final String NAMESPACE_USER;
	public static final String NAMESPACE_USER_COMMENTS;
	public static final String NAMESPACE_HELP;
	public static final String NAMESPACE_HELP_COMMENTS;

	static {
		NamespaceHandler.mapping = Environment.loadProperties(RESOURCE_NAME);
		// load all namespaces required for system operation.  see the
		// namespaces.properties to see the actual namespace values.
		NAMESPACE_SPECIAL = NamespaceHandler.mapping.getProperty("1");
		NAMESPACE_COMMENTS = NamespaceHandler.mapping.getProperty("4");
		NAMESPACE_IMAGE = NamespaceHandler.mapping.getProperty("5");
		NAMESPACE_IMAGE_COMMENTS = NamespaceHandler.mapping.getProperty("6");
		NAMESPACE_CATEGORY = NamespaceHandler.mapping.getProperty("7");
		NAMESPACE_CATEGORY_COMMENTS = NamespaceHandler.mapping.getProperty("8");
		NAMESPACE_JAMWIKI = NamespaceHandler.mapping.getProperty("9");
		NAMESPACE_JAMWIKI_COMMENTS = NamespaceHandler.mapping.getProperty("10");
		NAMESPACE_TEMPLATE = NamespaceHandler.mapping.getProperty("11");
		NAMESPACE_TEMPLATE_COMMENTS = NamespaceHandler.mapping.getProperty("12");
		NAMESPACE_USER = NamespaceHandler.mapping.getProperty("13");
		NAMESPACE_USER_COMMENTS = NamespaceHandler.mapping.getProperty("14");
		NAMESPACE_HELP = NamespaceHandler.mapping.getProperty("15");
		NAMESPACE_HELP_COMMENTS = NamespaceHandler.mapping.getProperty("16");
	}

	/**
	 *
	 */
	public static String getCommentsNamespace(String namespace) {
		if (!StringUtils.hasText(namespace)) {
			// main namespace
			return NAMESPACE_COMMENTS;
		}
		for (Iterator iterator = NamespaceHandler.mapping.keySet().iterator(); iterator.hasNext();) {
			String key = (String)iterator.next();
			String value = NamespaceHandler.mapping.getProperty(key);
			if (namespace.equals(value)) {
				int even = new Integer(key).intValue();
				if (even <= 2) {
					// special namespace
					return NAMESPACE_SPECIAL;
				}
				// get the closest even numbered property to this key that is not
				// less than this key
				if ((even % 2) != 0) {
					key = new Integer(even + 1).toString();
				}
				return NamespaceHandler.mapping.getProperty(key);
			}
		}
		// unrecognized namespace
		return NAMESPACE_COMMENTS + NAMESPACE_SEPARATOR + namespace;
	}

	/**
	 *
	 */
	public static String getMainNamespace(String namespace) {
		if (!StringUtils.hasText(namespace)) {
			// main namespace
			return "";
		}
		for (Iterator iterator = NamespaceHandler.mapping.keySet().iterator(); iterator.hasNext();) {
			String key = (String)iterator.next();
			String value = NamespaceHandler.mapping.getProperty(key);
			if (namespace.equals(value)) {
				int odd = new Integer(key).intValue();
				if (odd <= 2) {
					// special namespace
					return NAMESPACE_SPECIAL;
				}
				// get the closest odd numbered property to this key that is not
				// greater than this key
				if ((odd % 2) != 1) {
					key = new Integer(odd - 1).toString();
				}
				return NamespaceHandler.mapping.getProperty(key);
			}
		}
		// unrecognized namespace
		return namespace;
	}
}
