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

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiConfiguration;

/**
 * Class for controlling "namespace". Namespaces allow the organization of
 * wiki topics by dividing topics into groups.  A namespace will precede the
 * topic, such as "Namespace:Topic".  Namespaces can be customized by
 * modifying the configuration values in
 * <code>/WEB-INF/classes/jamwiki-configuration.xml</code>.
 *
 * @see org.jamwiki.WikiConfiguration
 */
public class NamespaceHandler {

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(NamespaceHandler.class.getName());

	/**
	 *
	 */
	private NamespaceHandler() {
	}

	/**
	 *
	 */
	public static String getCommentsNamespace(String namespace) {
		if (StringUtils.isBlank(namespace)) {
			// main namespace
			return Namespace.COMMENTS.getLabel();
		}
		Map<String, String[]> namespaces = WikiConfiguration.getInstance().getNamespaces();
		for (String key : namespaces.keySet()) {
			String[] values = namespaces.get(key);
			String main = values[0];
			String comments = values[1];
			if (namespace.equals(Namespace.SPECIAL.getLabel())) {
				return Namespace.SPECIAL.getLabel();
			}
			if (namespace.equals(main) || (comments != null && namespace.equals(comments))) {
				return comments;
			}
		}
		// unrecognized namespace
		return Namespace.COMMENTS.getLabel() + Namespace.SEPARATOR + namespace;
	}

	/**
	 *
	 */
	public static String getMainNamespace(String namespace) {
		if (StringUtils.isBlank(namespace)) {
			// main namespace
			return "";
		}
		Map<String, String[]> namespaces = WikiConfiguration.getInstance().getNamespaces();
		for (String key : namespaces.keySet()) {
			String[] values = namespaces.get(key);
			String main = values[0];
			String comments = values[1];
			if (namespace.equals(main) || (comments != null && namespace.equals(comments))) {
				return main;
			}
		}
		// unrecognized namespace
		return namespace;
	}

	/**
	 *
	 */
	private static final String initializeNamespace(String name, boolean isComments) {
		Map<String, String[]> namespaces = WikiConfiguration.getInstance().getNamespaces();
		for (String key : namespaces.keySet()) {
			String[] values = namespaces.get(key);
			String main = values[0];
			String comments = values[1];
			if (name.equals(key)) {
				return (isComments) ? comments : main;
			}
		}
		logger.warning("Namespace not found in configuration: " + name);
		return null;
	}
}
