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
	protected static final String ROOT_TAG = "jflex-root";
	private static Pattern EMPTY_BODY_TAG_PATTERN = null;
	private static Pattern NON_TEXT_BODY_TAG_PATTERN = null;
	private static Pattern NON_INLINE_TAG_PATTERN = null;
	private static Pattern NON_INLINE_TAG_START_PATTERN = null;
	private static Pattern NON_INLINE_TAG_END_PATTERN = null;
	private static final String emptyBodyTagPattern = "(br|div|hr|td|th)";
	private static final String nonTextBodyTagPattern = "(dl|ol|table|tr|ul)";
	private static final String nonInlineTagPattern = "(caption|dd|dl|dt|li|ol|p|table|td|th|tr|ul)";
	private static final String nonInlineTagStartPattern = "<" + nonInlineTagPattern + ">.*";
	private static final String nonInlineTagEndPattern = ".*</" + nonInlineTagPattern + ">";

	static {
		try {
			EMPTY_BODY_TAG_PATTERN = Pattern.compile(emptyBodyTagPattern, Pattern.CASE_INSENSITIVE);
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
	JFlexTagItem(String tagType) {
		if (tagType == null) {
			throw new IllegalArgumentException("tagType must not be null");
		}
		this.tagType = tagType;
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
	 * This method should generally not be called.  It exists primarily to support
	 * wikibold tags, which generate cases where the bold and italic tags could be
	 * switched in the stack, such as '''''bold''' then italic''.
	 */
	protected void changeTagType(String tagType) {
		this.tagType = tagType;
	}

	/**
	 *
	 */
	public String toHtml() {
		String content = this.tagContent.toString();
		// if no content do not generate a tag
		if (StringUtils.isBlank(content) && !this.isEmptyBodyTag()) {
			return "";
		}
		StringBuffer result = new StringBuffer();
		if (!this.isRootTag()) {
			result.append("<").append(this.tagType);
			if (!StringUtils.isBlank(this.tagAttributes)) {
				result.append(" ").append(this.tagAttributes);
			}
			result.append(">");
		}
		if (isRootTag()) {
			result.append(content);
		} else if (this.tagType.equals("pre")) {
			// pre-formatted, no trimming or other modification
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
		if (!this.isRootTag()) {
			result.append("</").append(this.tagType).append(">");
		}
		if (isTextBodyTag() && !this.isRootTag() && this.isInlineTag() && !this.tagType.equals("pre")) {
			// work around issues such as "text''' text'''", where the output should
			// be "text <b>text</b>", by moving the whitespace to the parent tag
			int firstWhitespaceIndex = content.indexOf(content.trim());
			if (firstWhitespaceIndex > 0) {
				result.insert(0, content.substring(0, firstWhitespaceIndex));
			}
			int lastWhitespaceIndex = firstWhitespaceIndex + content.trim().length();
			if (lastWhitespaceIndex > content.length()) {
				result.append(content.substring(lastWhitespaceIndex));
			}
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
		return this.tagType.equals(JFlexTagItem.ROOT_TAG);
	}

	/**
	 *
	 */
	private boolean isEmptyBodyTag() {
		if (isRootTag()) {
			return true;
		}
		Matcher matcher = EMPTY_BODY_TAG_PATTERN.matcher(this.tagType);
		return matcher.matches();
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
