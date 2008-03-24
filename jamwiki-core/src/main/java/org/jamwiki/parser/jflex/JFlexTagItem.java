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

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility class used during parsing.  This class holds the elements of an
 * HTML tag (open tag, close tag, content) as it is generated during parsing.
 */
class JFlexTagItem {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexTagItem.class.getName());

	private String tagType = null;
	private final StringBuffer tagContent = new StringBuffer();
	private String tagAttributes = null;

	/**
	 *
	 */
	JFlexTagItem() {
	}

	/**
	 *
	 */
	protected String getTagAttributes() {
		return this.tagAttributes;
	}

	/**
	 *
	 */
	protected void setTagAttributes(String tagAttributes) {
		this.tagAttributes = tagAttributes;
	}

	/**
	 *
	 */
	protected StringBuffer getTagContent() {
		return this.tagContent;
	}

	/**
	 *
	 */
	protected String getTagType() {
		return this.tagType;
	}

	/**
	 *
	 */
	protected void setTagType(String tagType) {
		this.tagType = tagType;
	}

	/**
	 *
	 */
	public String toHtml() {
		String content = this.tagContent.toString();
		StringBuffer result = new StringBuffer();
		if (this.tagType != null) {
			result.append("<").append(this.tagType);
			if (!StringUtils.isEmpty(this.tagAttributes)) {
				result.append(" ").append(this.tagAttributes);
			}
			result.append(">");
		}
		if (isTextBodyTag()) {
			result.append(content.trim());
		} else {
			result.append("\n");
			result.append(content.trim());
			result.append("\n");
		}
		if (this.tagType != null) {
			result.append("</").append(this.tagType).append(">");
		}
		if (!isInlineTag()) {
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 *
	 */
	private boolean isTextBodyTag() {
		if (this.tagType == null) {
			return false;
		}
		ArrayList nonTextBodyTagList = new ArrayList();
		nonTextBodyTagList.add("table");
		nonTextBodyTagList.add("tr");
		/*
		nonTextBodyTagList.add("dl");
		nonTextBodyTagList.add("ol");
		nonTextBodyTagList.add("ul");
		*/
		return (nonTextBodyTagList.indexOf(this.tagType) == -1);
	}

	/**
	 *
	 */
	private boolean isInlineTag() {
		if (this.tagType == null) {
			return false;
		}
		ArrayList nonInlineTagList = new ArrayList();
		nonInlineTagList.add("table");
		nonInlineTagList.add("tr");
		nonInlineTagList.add("th");
		nonInlineTagList.add("td");
		nonInlineTagList.add("caption");
		/*
		nonInlineTagList.add("dl");
		nonInlineTagList.add("ol");
		nonInlineTagList.add("ul");
		nonInlineTagList.add("li");
		nonInlineTagList.add("dt");
		nonInlineTagList.add("dd");
		*/
		return (nonInlineTagList.indexOf(this.tagType) == -1);
	}
}
