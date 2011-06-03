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
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the main JFlex lexer.
 */
public abstract class AbstractJAMWikiLexer extends JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(AbstractJAMWikiLexer.class.getName());

	/**
	 *
	 */
	private String calculateListItemType(char wikiSyntax) {
		if (wikiSyntax == '*' || wikiSyntax == '#') {
			return "li";
		}
		if (wikiSyntax == ';') {
			return "dt";
		}
		if (wikiSyntax == ':') {
			return "dd";
		}
		throw new IllegalArgumentException("Unrecognized wiki syntax: " + wikiSyntax);
	}

	/**
	 *
	 */
	private String calculateListType(char wikiSyntax) {
		if (wikiSyntax == ';' || wikiSyntax == ':') {
			return "dl";
		}
		if (wikiSyntax == '#') {
			return "ol";
		}
		if (wikiSyntax == '*') {
			return "ul";
		}
		throw new IllegalArgumentException("Unrecognized wiki syntax: " + wikiSyntax);
	}

	/**
	 *
	 */
	protected void parseParagraphEmpty(String raw) throws ParserException {
		// push back everything up to the last of the opening newlines that were matched
		yypushback(StringUtils.stripStart(raw, " \n\r\t").length() + 1);
		if (this.mode < JFlexParser.MODE_LAYOUT) {
			return;
		}
		int newlineCount = 0;
		for (int i = 0; i < raw.length(); i++) {
			if (raw.charAt(i) != '\n') {
				// only count newlines for paragraph creation
				continue;
			}
			newlineCount++;
			if (newlineCount % 2 != 0) {
				// two newlines are required to create a paragraph
				continue;
			}
			this.pushTag("p", null);
			this.append("<br />\n");
			this.popTag("p");
		}
	}

	/**
	 *
	 */
	protected void parseParagraphEnd(String raw) {
		if (this.mode >= JFlexParser.MODE_LAYOUT && this.peekTag().getTagType().equals("p")) {
			// only perform processing if a paragraph is open - tag may have been already been
			// closed explicitly with a "</p>".
			this.popTag("p");
		}
		// push back everything except for any opening newline that was matched
		int pushback = raw.length();
		int pos = raw.indexOf('\n');
		if (pos != -1 && pos < raw.length()) {
			pushback = raw.substring(pos + 1).length();
		}
		yypushback(pushback);
	}

	/**
	 *
	 */
	protected void parseParagraphStart(String raw) throws ParserException {
		int pushback = raw.length();
		if (this.mode >= JFlexParser.MODE_LAYOUT) {
			this.pushTag("p", null);
			int newlineCount = StringUtils.countMatches(raw, "\n");
			if (newlineCount > 0) {
				pushback = StringUtils.stripStart(raw, " \n\r\t").length();
			}
			if (newlineCount == 2) {
				// if the pattern matched two opening newlines then start the paragraph with a <br /> tag
				this.append("<br />\n");
			}
		}
		yypushback(pushback);
	}

	/**
	 * Take Wiki text of the form "|" or "| style='foo' |" and convert to
	 * and HTML <td> or <th> tag.
	 *
	 * @param text The text to be parsed.
	 * @param tagType The HTML tag type, either "td" or "th".
	 * @param markup The Wiki markup for the tag, either "|", "|+" or "!"
	 */
	protected void parseTableCell(String text, String tagType, String markup) throws ParserException {
		if (text == null) {
			throw new IllegalArgumentException("No text specified while parsing table cell");
		}
		text = text.trim();
		String openTagRaw = null;
		int pos = StringUtils.indexOfAnyBut(text, markup);
		if (pos != -1) {
			text = text.substring(pos);
			pos = text.indexOf('|');
			if (pos != -1) {
				text = text.substring(0, pos);
			}
			openTagRaw = "<" + tagType + " " + text.trim() + ">";
		}
		this.pushTag(tagType, openTagRaw);
	}

	/**
	 *
	 */
	protected void processListStack(String wikiSyntax) throws ParserException {
		// before adding to a list, first make sure that any open inline tags or paragraph tags
		// have been closed (example: "<i><ul>" is invalid.  close the <i> first).
		while (!this.peekTag().isRootTag() && (this.peekTag().getTagType().equals("p") || this.peekTag().isInlineTag())) {
			this.popTag(this.peekTag().getTagType());
		}
		int previousDepth = this.currentListDepth();
		int currentDepth = wikiSyntax.length();
		String tagType;
		// if list was previously open to a greater depth, close the old list down to the
		// current depth.
		int tagsToPop = (previousDepth - currentDepth);
		if (tagsToPop > 0) {
			this.popListTags(tagsToPop);
			previousDepth -= tagsToPop;
		}
		// now look for differences in the current list stacks.  for example, if
		// the previous list was "::;" and the current list is "###" then there are
		// some lists that must be closed.
		for (int i=0; i < previousDepth; i++) {
			// get the tagType for the root list ("ul", "dl", etc, NOT "li")
			int tagPos = this.tagStack.size() - ((previousDepth - i) * 2);
			tagType = (this.tagStack.get(tagPos)).getTagType();
			if (tagType.equals(this.calculateListType(wikiSyntax.charAt(i)))) {
				continue;
			}
			// if the above test did not match, then the stack needs to be popped
			// to this point.
			tagsToPop = (previousDepth - i);
			this.popListTags(tagsToPop);
			previousDepth -= tagsToPop;
			break;
		}
		if (previousDepth == 0) {
			// if no list is open, open one
			this.pushTag(this.calculateListType(wikiSyntax.charAt(0)), null);
			// add the new list item to the stack
			this.pushTag(this.calculateListItemType(wikiSyntax.charAt(0)), null);
		} else if (previousDepth == currentDepth) {
			// pop the previous list item
			tagType = (this.tagStack.peek()).getTagType();
			popTag(tagType);
			// add the new list item to the stack
			this.pushTag(this.calculateListItemType(wikiSyntax.charAt(previousDepth - 1)), null);
		}
		// if the new list has additional elements, push them onto the stack
		int counterStart = (previousDepth > 1) ? previousDepth : 1;
		for (int i=counterStart; i < wikiSyntax.length(); i++) {
			String previousTagType = (this.tagStack.peek()).getTagType();
			// handle a weird corner case.  if a "dt" is open and there are
			// sub-lists, close the dt and open a "dd" for the sub-list
			if (previousTagType.equals("dt")) {
				this.popTag("dt");
				if (!this.calculateListType(wikiSyntax.charAt(i)).equals("dl")) {
					this.popTag("dl");
					this.pushTag("dl", null);
				}
				this.pushTag("dd", null);
			}
			this.pushTag(this.calculateListType(wikiSyntax.charAt(i)), null);
			this.pushTag(this.calculateListItemType(wikiSyntax.charAt(i)), null);
		}
	}

	/**
	 * Make sure any open table tags that need to be closed are closed.
	 */
	protected void processTableStack() {
		// before updating the table make sure that any open inline tags or paragraph tags
		// have been closed (example: "<td><b></td>" won't work.
		while (!this.peekTag().isRootTag() && (this.peekTag().getTagType().equals("p") || this.peekTag().isInlineTag())) {
			this.popTag(this.peekTag().getTagType());
		}
		String previousTagType = this.peekTag().getTagType();
		if (!previousTagType.equals("caption") && !previousTagType.equals("th") && !previousTagType.equals("td")) {
			// no table cell was open, so nothing to close
			return;
		}
		// pop the previous tag
		this.popTag(previousTagType);
	}
}
