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
package org.jamwiki.parser;

import java.util.Vector;
import java.util.HashMap;

/**
 *
 */
public class ParserOutput {

	private HashMap categories = new HashMap();
	private Vector links = new Vector();
	private String content = null;

	/**
	 *
	 */
	public ParserOutput() {
	}

	/**
	 *
	 */
	public void addCategory(String categoryName, String sortKey) {
		this.categories.put(categoryName, sortKey);
	}

	/**
	 *
	 */
	public void addLink(String topicName) {
		this.links.add(topicName);
	}

	/**
	 *
	 */
	public HashMap getCategories() {
		return this.categories;
	}

	/**
	 * Return the parsed content
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * Return the parsed content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 *
	 */
	public Vector getLinks() {
		return this.links;
	}
}
