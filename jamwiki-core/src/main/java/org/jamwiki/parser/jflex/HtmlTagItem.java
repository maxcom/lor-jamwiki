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
package org.jamwiki.parser.jflex;

import java.util.LinkedHashMap;

/**
 * Wrapper for an HTML or HTML-like tag of the form
 * <tag attribute1="value1" attribute2="value2">.  Used for utility purposes with
 * the JFlex parser.
 */
public class HtmlTagItem {

	/** HTML open tag of the form <tag> or <tag attribute="value">. */
	public static final int TAG_PATTERN_OPEN = 1;
	/** HTML close tag of the form </tag>. */
	public static final int TAG_PATTERN_CLOSE = 2;
	/** HTML tag without content of the form <tag /> or <tag attribute="value" />. */
	public static final int TAG_PATTERN_EMPTY_BODY = 3;
	/** The tag type, for example <tag attribute="value"> has a tag type of "tag". */
	private String tagType;
	/** The pattern type for the tag - open, close, or empty body. */
	private int tagPattern;
	/** The tag's attributes, mapped as an ordered list of key-value pairs. */
	private LinkedHashMap<String, String> attributes;

	/**
	 *
	 */
	protected HtmlTagItem(String tagType, int tagPattern, LinkedHashMap<String, String> attributes) {
		this.tagType = tagType;
		this.tagPattern = tagPattern;
		this.attributes = new LinkedHashMap<String, String>(attributes);
	}

	/**
	 * Return a mapping of key-value pairs for all attributes of this tag.
	 */
	protected LinkedHashMap<String, String> getAttributes() {
		return this.attributes;
	}

	/**
	 * Return <code>true</code> if this tag's type is TAG_PATTERN_EMPTY_BODY,
	 * otherwise return <code>false</code>.
	 */
	protected boolean isTagEmptyBody() {
		return (this.tagPattern == TAG_PATTERN_EMPTY_BODY);
	}

	/**
	 * Return the tag pattern (open tag, close tag, empty body tag).
	 */
	protected int getTagPattern() {
		return this.tagPattern;
	}

	/**
	 * Return the tag type (example: "ul").
	 */
	protected String getTagType() {
		return this.tagType;
	}

	/**
	 * Convert the tag to an HTML or HTML-like representation.
	 */
	public String toHtml() {
		String value;
		StringBuilder result = new StringBuilder("<");
		if (this.tagPattern == TAG_PATTERN_CLOSE) {
			result.append("/");
		}
		result.append(this.tagType);
		for (String key : this.attributes.keySet()) {
			result.append(' ').append(key);
			value = this.attributes.get(key);
			if (value != null) {
				result.append('=').append("\"").append(value.trim()).append("\"");
			}
		}
		if (this.isTagEmptyBody()) {
			result.append(" /");
		}
		result.append(">");
		return result.toString();
	}
}
