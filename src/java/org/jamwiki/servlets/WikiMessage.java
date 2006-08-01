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

/**
 * This class is a utility class useful for storing messages key and object
 * values that can later be displayed using the jstl fmt:message tag.
 */
public class WikiMessage {

	private static final Logger logger = Logger.getLogger(WikiMessage.class);
	private String key = null;
	private String[] params = null;

	/**
	 *
	 */
	public WikiMessage(String key) {
		this.key = key;
	}

	/**
	 *
	 */
	public WikiMessage(String key, String param1) {
		this.key = key;
		this.params = new String[1];
		params[0] = param1;
	}

	/**
	 *
	 */
	public WikiMessage(String key, String param1, String param2) {
		this.key = key;
		this.params = new String[2];
		params[0] = param1;
		params[1] = param2;
	}

	/**
	 *
	 */
	public WikiMessage(String key, String[] params) {
		this.key = key;
		this.params = params;
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
