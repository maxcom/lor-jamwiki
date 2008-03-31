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
import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility methods used with the Mediawiki lexers.
 */
public class JFlexParserUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexParserUtil.class.getName());
	private static Pattern TAG_PATTERN = null;
	private static Pattern JAVASCRIPT_PATTERN1 = null;
	private static Pattern JAVASCRIPT_PATTERN2 = null;

	static {
		try {
			TAG_PATTERN = Pattern.compile("(<[ ]*[/]?[ ]*)([^\\ />]+)([ ]*(.*?))([/]?[ ]*>)");
			// catch script insertions of the form "onsubmit="
			JAVASCRIPT_PATTERN1 = Pattern.compile("( on[^=]{3,}=)+", Pattern.CASE_INSENSITIVE);
			// catch script insertions that use a javascript url
			JAVASCRIPT_PATTERN2 = Pattern.compile("(javascript[ ]*\\:)+", Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 *
	 */
	private JFlexParserUtil() {
	}

	/**
	 * Provide a way to run the pre-processor against a fragment of text, such
	 * as an image caption.  This method should be used sparingly since it is
	 * not very efficient.
	 */
	protected static String parseFragment(ParserInput parserInput, String raw, int mode) throws Exception {
		if (StringUtils.isBlank(raw)) {
			return raw;
		}
		JFlexParser parser = new JFlexParser(parserInput);
		ParserOutput parserOutput = new ParserOutput();
		return parser.parseFragment(parserOutput, raw, mode);
	}

	/**
	 * Clean up HTML tags to make them XHTML compliant (lowercase, no
	 * unnecessary spaces).
	 */
	protected static String sanitizeHtmlTag(String tag) {
		String result = tag.trim();
		result = StringUtils.remove(result, " ").toLowerCase();
		if (result.endsWith("/>")) {
			// spaces were stripped, so make sure tag is of the form "<br />"
			result = result.substring(0, result.length() - 2) + " />";
		}
		return result;
	}

	/**
	 * Given a tag of the form "<tag>content</tag>", return all content between
	 * the tags.  Consider the following examples:
	 *
	 * "<tag>content</tag>" returns "content".
	 * "<tag />" returns and empty string.
	 * "<tag><sub>content</sub></tag>" returns "<sub>content</sub>".
	 *
	 * @param raw The raw tag content to be analyzed.
	 * @return The content for the tag being analyzed.
	 */
	protected static String tagContent(String raw) {
		int start = raw.indexOf('>') + 1;
		int end = raw.lastIndexOf('<');
		if (start == 0) {
			// no tags
			return raw;
		}
		if (end <= start) {
			return "";
		}
		return raw.substring(start, end);
	}

	/**
	 * Given an HTML tag, split it into its tag type and tag attributes,
	 * cleaning up the attribtues in the process - allowing Javascript
	 * action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String[] parseHtmlTag(String tag) {
		Matcher m = TAG_PATTERN.matcher(tag);
		String[] result = new String[4];
		if (!m.find()) {
			logger.severe("Failure while attempting to match html tag for pattern " + tag);
			return result;
		}
		String tagType = m.group(2).toLowerCase().trim();
		String tagAttributes = m.group(3).trim();
		String tagOpen = m.group(1).trim();
		String tagClose = m.group(5).trim();
		if (!StringUtils.isBlank(tagAttributes)) {
			tagAttributes = JFlexParserUtil.validateHtmlTagAttributes(tagAttributes).trim();
		}
		result[0] = tagType;
		result[1] = tagAttributes;
		result[2] = tagOpen;
		result[3] = tagClose;
		return result;
	}

	/**
	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String validateHtmlTag(String tag) {
		String[] tagInfo = JFlexParserUtil.parseHtmlTag(tag);
		String tagOpen = tagInfo[2];
		String tagKeyword = tagInfo[0];
		String attributes = tagInfo[1];
		String tagClose = tagInfo[3];
		String result = "<";
		if (tagOpen.indexOf('/') != -1) {
			result += "/";
		}
		result += tagKeyword;
		if (!StringUtils.isBlank(attributes)) {
			result += " " + attributes;
		}
		if (tagClose.indexOf('/') != -1) {
			tagClose = " />";
		}
		result += tagClose.trim();
		return result;
	}

	/**
	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String validateHtmlTagAttributes(String attributes) {
		if (StringUtils.isBlank(attributes)) {
			return attributes;
		}
		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT)) {
			// FIXME - can these two patterns be combined into one?
			// pattern requires a space prior to the "onFoo", so make sure one exists
			Matcher m = JAVASCRIPT_PATTERN1.matcher(" " + attributes);
			if (m.find()) {
				logger.warning("Attempt to include Javascript in Wiki syntax " + attributes);
				return "";
			}
			m = JAVASCRIPT_PATTERN2.matcher(attributes);
			if (m.find()) {
				logger.warning("Attempt to include Javascript in Wiki syntax " + attributes);
				return "";
			}
		}
		return attributes;
	}
}
