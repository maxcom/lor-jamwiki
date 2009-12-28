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

import org.jamwiki.utils.WikiLogger;

/**
 * Process wiki bold and italic syntax (''italic'', '''bold''', '''''bold italic''''').
 */
public class WikiBoldItalicTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiBoldItalicTag.class.getName());

	/**
	 * Parse a call to a Mediawiki noinclude tag of the form
	 * "<noinclude>text</noinclude>" and return the resulting output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) {
		if (logger.isFinerEnabled()) {
			logger.finer("bold / italic: " + raw + " (" + lexer.yystate() + ")");
		}
		if (args.length == 0) {
			throw new IllegalArgumentException("Must pass heading depth to WikiHeadingTag.parse");
		}
		this.processBoldItalic(lexer, (String)args[0]);
		return "";
	}

	/**
	 * Handle parsing of bold, italic, and bolditalic tags.
	 *
	 * @param tagType The tag type being parsed - either "i", "b", or <code>null</code>
	 *  if a bolditalic tag is being parsed.
	 */
	private void processBoldItalic(JFlexLexer lexer, String tagType) {
		if (tagType == null) {
			// bold-italic
			if (lexer.peekTag().getTagType().equals("i")) {
				// italic tag already opened
				this.processBoldItalic(lexer, "i");
				this.processBoldItalic(lexer, "b");
			} else {
				// standard bold-italic processing
				this.processBoldItalic(lexer, "b");
				this.processBoldItalic(lexer, "i");
			}
			return;
		}
		// bold or italic
		if (lexer.peekTag().getTagType().equals(tagType)) {
			// tag was open, close it
			lexer.popTag(tagType);
			return;
		}
		// TODO - make this more generic and implement it globally
		if (tagType.equals("b") && lexer.peekTag().getTagType().equals("i")) {
			// since Mediawiki syntax unfortunately chose to use the same character
			// for bold and italic ('' and '''), see if the syntax is of the form
			// '''''bold''' then italic'', in which case the current stack contains
			// "b" followed by "i" when it should be the reverse.
			int stackLength = lexer.getTagStack().size();
			if (stackLength > 2) {
				JFlexTagItem grandparent = lexer.getTagStack().get(stackLength - 2);
				if (grandparent.getTagType().equals("b")) {
					// swap the tag types and close the current tag
					grandparent.changeTagType("i");
					lexer.peekTag().changeTagType("b");
					lexer.popTag(tagType);
					return;
				}
			}
		}
		// push the new tag onto the stack
		lexer.pushTag(tagType, null);
	}
}
