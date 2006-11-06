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
package org.jamwiki.model;

import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * Utility class for handling reference objects.
 */
public class WikiReference {

	private static WikiLogger logger = WikiLogger.getLogger(WikiReference.class.getName());

	private int citation = 1;
	private String content = null;
	private int count = 0;
	private String name = null;

	/**
	 *
	 */
	public WikiReference(String name, String content, int citation, int count) {
		this.name = name;
		this.content = content;
		this.citation = citation;
		this.count = count;
	}

	/**
	 *
	 */
	public int getCitation() {
		return this.citation;
	}

	/**
	 *
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 *
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public String getNotationName() {
		if (!StringUtils.hasText(this.name)) {
			return "_note-" + this.citation;
		}
		return "_note-" + Utilities.encodeForURL(this.name);
	}

	/**
	 *
	 */
	public String getReferenceName() {
		if (!StringUtils.hasText(this.name)) {
			return "_ref-" + this.citation;
		}
		return "_ref-" + Utilities.encodeForURL(this.name) + "_" + this.count;
	}
}