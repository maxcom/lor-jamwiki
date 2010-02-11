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
 * This class parses nowiki tags of the form <code>&lt;includeonly&gt;content&lt;/includeonly&gt;</code>.
 */
public class IncludeOnlyTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(IncludeOnlyTag.class.getName());

	/**
	 * Parse a call to a Mediawiki includeonly tag of the form
	 * "<includeonly>text</includeonly>" and return the resulting output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) {
		if (lexer.getMode() <= JFlexParser.MODE_MINIMAL) {
			return raw;
		}
		try {
			if (lexer.getParserInput().getTemplateDepth() > 0) {
				String content = JFlexParserUtil.tagContent(raw);
				// run the pre-processor against the includeonly content
				JFlexParser parser = new JFlexParser(lexer.getParserInput());
				return parser.parseFragment(lexer.getParserOutput(), content, JFlexParser.MODE_PREPROCESS);
			}
			// anything else then the tag content is not included
			return "";
		} catch (Throwable t) {
			logger.info("Unable to parse " + raw, t);
			return raw;
		}
	}
}
