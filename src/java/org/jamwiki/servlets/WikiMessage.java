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
package org.jamwiki.servlets;

import org.apache.log4j.Logger;
import org.jamwiki.utils.Utilities;

/**
 * This class is a utility class useful for storing messages key and object
 * values that can later be displayed using the jstl fmt:message tag.
 */
public class WikiMessage {

	private static final Logger logger = Logger.getLogger(WikiMessage.class);
	private String key = null;
	private String[] params = null;

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using parameter param1.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 */
	public WikiMessage(String key) {
		this.key = key;
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using parameter param1.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param param1 The parameter that corresponds to the {0} param in the
	 *  specified message key value.  Note that this parameter is automatically
	 *  HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, String param1) {
		this.key = key;
		this.params = new String[1];
		params[0] = Utilities.escapeHTML(param1);
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using parameter param1.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param param1 The parameter that corresponds to the {0} param in the
	 *  specified message key value.  Note that this parameter is automatically
	 *  HTML escaped to prevent erorrs in display.
	 * @param param2 The parameter that corresponds to the {1} param in the
	 *  specified message key value.  Note that this parameter is automatically
	 *  HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, String param1, String param2) {
		this.key = key;
		this.params = new String[2];
		params[0] = Utilities.escapeHTML(param1);
		params[1] = Utilities.escapeHTML(param2);
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using parameter param1.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param params An array of parameters that correspond to the {0}, {1}, etc
	 *  params in the specified message key value.  Note that these parameters are
	 *  automatically HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, String[] params) {
		this.key = key;
		if (params != null) {
			this.params = new String[params.length];
			for (int i=0; i < params.length; i++) {
				this.params[i] = Utilities.escapeHTML(params[i]);
			}
		}
	}

	/**
	 *
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 *
	 */
	public String[] getParams() {
		return this.params;
	}
}
