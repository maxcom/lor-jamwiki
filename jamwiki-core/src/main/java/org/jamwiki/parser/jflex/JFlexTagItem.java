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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	private static Pattern NON_TEXT_BODY_TAG_PATTERN = null;
	private static Pattern NON_INLINE_TAG_PATTERN = null;
	private static Pattern NON_INLINE_TAG_START_PATTERN = null;
	private static Pattern NON_INLINE_TAG_END_PATTERN = null;
	private static final String nonTextBodyTagPattern = "(dl|ol|table|tr|ul)";
	private static final String nonInlineTagPattern = "(caption|dd|dl|dt|li|ol|table|td|th|tr|ul)";
	private static final String nonInlineTagStartPattern = "<" + nonInlineTagPattern + ">.*";
	private static final String nonInlineTagEndPattern = ".*</" + nonInlineTagPattern + ">";

	static {
		try {
			NON_TEXT_BODY_TAG_PATTERN = Pattern.compile(nonTextBodyTagPattern, Pattern.CASE_INSENSITIVE);
			NON_INLINE_TAG_PATTERN = Pattern.compile(nonInlineTagPattern, Pattern.CASE_INSENSITIVE);
			NON_INLINE_TAG_START_PATTERN = Pattern.compile(nonInlineTagStartPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			NON_INLINE_TAG_END_PATTERN = Pattern.compile(nonInlineTagEndPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

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
		if (isRootTag()) {
			result.append(content);
		} else if (isTextBodyTag()) {
			Matcher matcher = null;
			// ugly hack to handle cases such as "<li><ul>" where the "<ul>" should be on its own line
			matcher = NON_INLINE_TAG_START_PATTERN.matcher(content.trim());
			if (matcher.matches()) {
				result.append("\n");
			}
			result.append(content.trim());
			// ugly hack to handle cases such as "</ul></li>" where the "</li>" should be on its own line
			matcher = NON_INLINE_TAG_END_PATTERN.matcher(content.trim());
			if (matcher.matches()) {
				result.append("\n");
			}
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
	private boolean isRootTag() {
		// FIXME - temporary hack
		return (this.tagType == null);
	}

	/**
	 *
	 */
	private boolean isTextBodyTag() {
		if (isRootTag()) {
			return true;
		}
		Matcher matcher = NON_TEXT_BODY_TAG_PATTERN.matcher(this.tagType);
		return !matcher.matches();
	}

	/**
	 *
	 */
	private boolean isInlineTag() {
		if (isRootTag()) {
			return true;
		}
		Matcher matcher = NON_INLINE_TAG_PATTERN.matcher(this.tagType);
		return !matcher.matches();
	}
}
