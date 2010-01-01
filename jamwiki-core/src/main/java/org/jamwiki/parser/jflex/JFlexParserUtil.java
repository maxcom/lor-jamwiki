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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.model.WikiReference;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility methods used with the Mediawiki lexers.
 */
public class JFlexParserUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexParserUtil.class.getName());
	private static final String emptyBodyTagPattern = "(br|div|hr|td|th)";
	private static final String nonNestingTagPattern = "(dd|dl|dt|hr|li|ol|table|tbody|td|tfoot|th|thead|tr|ul)";
	private static final String nonTextBodyTagPattern = "(dl|ol|table|tr|ul)";
	private static final String nonInlineTagPattern = "(caption|dd|div|dl|dt|hr|li|ol|p|table|td|th|tr|ul)";
	private static final String nonInlineTagStartPattern = "<" + nonInlineTagPattern + ">.*";
	private static final String nonInlineTagEndPattern = ".*</" + nonInlineTagPattern + ">";
	private static final Pattern EMPTY_BODY_TAG_PATTERN = Pattern.compile(emptyBodyTagPattern, Pattern.CASE_INSENSITIVE);
	private static final Pattern NON_NESTING_TAG_PATTERN = Pattern.compile(nonNestingTagPattern, Pattern.CASE_INSENSITIVE);
	private static final Pattern NON_TEXT_BODY_TAG_PATTERN = Pattern.compile(nonTextBodyTagPattern, Pattern.CASE_INSENSITIVE);
	private static final Pattern NON_INLINE_TAG_PATTERN = Pattern.compile(nonInlineTagPattern, Pattern.CASE_INSENSITIVE);
	private static final Pattern NON_INLINE_TAG_START_PATTERN = Pattern.compile(nonInlineTagStartPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern NON_INLINE_TAG_END_PATTERN = Pattern.compile(nonInlineTagEndPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static JAMWikiHtmlProcessor JFLEX_HTML_PROCESSOR = null;

	/**
	 *
	 */
	private JFlexParserUtil() {
	}

	/**
	 * An empty body tag is one that contains no content, such as "br".
	 */
	protected static boolean isEmptyBodyTag(String tagType) {
		if (isRootTag(tagType)) {
			return true;
		}
		Matcher matcher = EMPTY_BODY_TAG_PATTERN.matcher(tagType);
		return matcher.matches();
	}

	/**
	 * An inline tag is a tag that does not affect page flow such as
	 * "b" or "i".  A non-inline tag such as "div" is one that creates
	 * its own display box.
	 */
	protected static boolean isInlineTag(String tagType) {
		if (isRootTag(tagType)) {
			return true;
		}
		Matcher matcher = NON_INLINE_TAG_PATTERN.matcher(tagType);
		return !matcher.matches();
	}

	/**
	 * A non-nesting tag is a tag such as "li" which cannot be nested within
	 * another "li" tag.
	 */
	protected static boolean isNonNestingTag(String tagType) {
		Matcher matcher = NON_NESTING_TAG_PATTERN.matcher(tagType);
		return matcher.matches();
	}

	/**
	 *
	 */
	protected static boolean isNonInlineTagEnd(String tagText) {
		Matcher matcher = NON_INLINE_TAG_END_PATTERN.matcher(tagText);
		return matcher.matches();
	}

	/**
	 *
	 */
	protected static boolean isNonInlineTagStart(String tagText) {
		Matcher matcher = NON_INLINE_TAG_START_PATTERN.matcher(tagText);
		return matcher.matches();
	}

	/**
	 * Evaluate the tag to determine whether it is the parser root tag
	 * that indicates the bottom of the parser tag stack.
	 */
	protected static boolean isRootTag(String tagType) {
		return tagType.equals(JFlexTagItem.ROOT_TAG);
	}

	/**
	 * Determine whether the tag allows text body content.  Some tags, such
	 * as "table", allow only tag content and no text content.
	 */
	protected static boolean isTextBodyTag(String tagType) {
		if (isRootTag(tagType)) {
			return true;
		}
		Matcher matcher = NON_TEXT_BODY_TAG_PATTERN.matcher(tagType);
		return !matcher.matches();
	}

	/**
	 * Provide a way to run the pre-processor against a fragment of text, such
	 * as an image caption.  This method should be used sparingly since it is
	 * not very efficient.
	 */
	public static String parseFragment(ParserInput parserInput, String raw, int mode) throws ParserException {
		if (StringUtils.isBlank(raw)) {
			return raw;
		}
		JFlexParser parser = new JFlexParser(parserInput);
		ParserOutput parserOutput = new ParserOutput();
		return parser.parseFragment(parserOutput, raw, mode);
	}

	/**
	 * Parse a raw Wiki link of the form "[[link|text]]", and return a WikiLink
	 * object representing the link.
	 *
	 * @param raw The raw Wiki link text.
	 * @return A WikiLink object that represents the link.
	 */
	protected static WikiLink parseWikiLink(String raw) {
		if (StringUtils.isBlank(raw)) {
			return new WikiLink();
		}
		raw = raw.trim();
		String suffix = ((!raw.endsWith("]]")) ? raw.substring(raw.lastIndexOf("]]") + 2) : null);
		// for performance reasons use String methods rather than regex
		// private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[\\s*(\\:\\s*)?\\s*(.+?)(\\s*\\|\\s*(.+))?\\s*\\]\\]([a-z]*)");
		raw = raw.substring(raw.indexOf("[[") + 2, raw.lastIndexOf("]]")).trim();
		boolean colon = false;
		if (raw.startsWith(":")) {
			colon = true;
			raw = raw.substring(1).trim();
		}
		String text = null;
		int pos = raw.indexOf('|');
		if (pos != -1 && pos != (raw.length() - 1)) {
			text = raw.substring(pos + 1).trim();
			raw = raw.substring(0, pos).trim();
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(raw);
		wikiLink.setColon(colon);
		wikiLink.setText(text);
		if (!StringUtils.isBlank(suffix)) {
			wikiLink.setText((StringUtils.isBlank(text) ? wikiLink.getDestination() : text) + suffix);
		}
		return wikiLink;
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
	 * During parsing the reference objects will be stored as a temporary array.  This method
	 * parses that array and returns the reference objects.
	 *
	 * @param parserInput The current ParserInput object for the topic that is being parsed.
	 * @return A list of reference objects (never <code>null</code>) for the current topic that
	 *  is being parsed.
	 */
	protected static List<WikiReference> retrieveReferences(ParserInput parserInput) {
		List<WikiReference> references = (List<WikiReference>)parserInput.getTempParams().get(WikiReferenceTag.REFERENCES_PARAM);
		if (references == null) {
			references = new ArrayList<WikiReference>();
			parserInput.getTempParams().put(WikiReferenceTag.REFERENCES_PARAM, references);
		}
		return references;
	}

	/**
	 * Parse an opening or closing HTML tag to validate attributes and make sure it is XHTML compliant.
	 *
	 * @param tag The HTML tag to be parsed.
	 * @return An HtmlTagItem containing the parsed content, or <code>null</code> if a
	 *  null or empty string is passed as the argument.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public static HtmlTagItem sanitizeHtmlTag(String tag) throws ParserException {
		if (StringUtils.isBlank(tag)) {
			return null;
		}
		if (JFLEX_HTML_PROCESSOR == null) {
			JFLEX_HTML_PROCESSOR = new JAMWikiHtmlProcessor(new StringReader(tag));
		} else {
			JFLEX_HTML_PROCESSOR.yyreset(new StringReader(tag));
		}
		StringBuilder result = new StringBuilder();
		String line;
		try {
			while ((line = JFLEX_HTML_PROCESSOR.yylex()) != null) {
				result.append(line);
			}
		} catch (Exception e) {
			throw new ParserException(e);
		}
		return new HtmlTagItem(JFLEX_HTML_PROCESSOR.getTagType(), result.toString());
	}

	/**
	 * Parse a template string of the form "param1|param2|param3" into tokens
	 * (param1, param2, and param3 in the example), handling such cases as
	 * "param1|[[foo|bar]]|param3" correctly.
	 */
	protected static List<String> tokenizeParamString(String content) {
		List<String> tokens = new ArrayList<String>();
		int pos = 0;
		int endPos = -1;
		String substring = "";
		String value = "";
		while (pos < content.length()) {
			substring = content.substring(pos);
			endPos = -1;
			if (substring.startsWith("{{{")) {
				// template parameter
				endPos = Utilities.findMatchingEndTag(content, pos, "{{{", "}}}");
			} else if (substring.startsWith("{{")) {
				// template
				endPos = Utilities.findMatchingEndTag(content, pos, "{{", "}}");
			} else if (substring.startsWith("[[")) {
				// link
				endPos = Utilities.findMatchingEndTag(content, pos, "[[", "]]");
			} else if (substring.startsWith("{|")) {
				// table
				endPos = Utilities.findMatchingEndTag(content, pos, "{|", "|}");
			} else if (content.charAt(pos) == '|') {
				// new token
				tokens.add(value);
				value = "";
				pos++;
				continue;
			}
			if (endPos != -1) {
				value += content.substring(pos, endPos);
				pos = endPos;
			} else {
				value += content.charAt(pos);
				pos++;
			}
		}
		// add the last one
		tokens.add(value);
		return tokens;
	}
}
