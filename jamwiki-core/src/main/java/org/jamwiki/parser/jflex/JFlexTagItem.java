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

import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility class used during parsing.  This class holds the elements of an
 * HTML tag (open tag, close tag, content) as it is generated during parsing.
 */
class JFlexTagItem {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexTagItem.class.getName());

	protected static final String ROOT_TAG = "jflex-root";
	private String closeTagOverride = null;
	private String tagAttributes = null;
	private final StringBuilder tagContent = new StringBuilder();
	private String tagType = null;

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
	 * This method exists solely for those cases where a mis-matched HTML tag
	 * is being parsed (<u><strong>text</u></strong>) and the parser closes the
	 * inner tag and needs to provide an indication on the stack that the next
	 * tag should ignore the close tag it finds and use an overridden closing tag.
	 *
	 * @return A close tag to use that differs from the close tag that will be
	 *  found by the parser.
	 */
	protected String getCloseTagOverride() {
		return this.closeTagOverride;
	}

	/**
	 * This method exists solely for those cases where a mis-matched HTML tag
	 * is being parsed (<u><strong>text</u></strong>) and the parser closes the
	 * inner tag and needs to provide an indication on the stack that the next
	 * tag should ignore the close tag it finds and use an overridden closing tag.
	 *
	 * @param closeTagOverride A close tag to use that differs from the close tag
	 *  that will be found by the parser.
	 */
	protected void setCloseTagOverride(String closeTagOverride) {
		this.closeTagOverride = closeTagOverride;
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
	protected StringBuilder getTagContent() {
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
		if (StringUtils.isBlank(content) && !JFlexParserUtil.isEmptyBodyTag(this.tagType)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		if (!JFlexParserUtil.isRootTag(this.tagType)) {
			result.append('<').append(this.tagType);
			if (!StringUtils.isBlank(this.tagAttributes)) {
				result.append(' ').append(this.tagAttributes);
			}
			result.append('>');
		}
		if (JFlexParserUtil.isRootTag(this.tagType)) {
			result.append(content);
		} else if (this.tagType.equals("pre")) {
			// pre-formatted, no trimming but make sure the open and close tags appear on their own lines
			if (!content.startsWith("\n")) {
				result.append('\n');
			}
			result.append(content);
			if (!content.endsWith("\n")) {
				result.append('\n');
			}
		} else if (JFlexParserUtil.isTextBodyTag(this.tagType)) {
			// ugly hack to handle cases such as "<li><ul>" where the "<ul>" should be on its own line
			if (JFlexParserUtil.isNonInlineTagStart(content.trim())) {
				result.append('\n');
			}
			result.append(content.trim());
			// ugly hack to handle cases such as "</ul></li>" where the "</li>" should be on its own line
			if (JFlexParserUtil.isNonInlineTagEnd(content.trim())) {
				result.append('\n');
			}
		} else {
			result.append('\n');
			result.append(content.trim());
			result.append('\n');
		}
		if (!JFlexParserUtil.isRootTag(this.tagType)) {
			result.append("</").append(this.tagType).append('>');
		}
		if (JFlexParserUtil.isTextBodyTag(this.tagType) && !JFlexParserUtil.isRootTag(this.tagType) && JFlexParserUtil.isInlineTag(this.tagType) && !this.tagType.equals("pre")) {
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
		return result.toString();
	}
}
