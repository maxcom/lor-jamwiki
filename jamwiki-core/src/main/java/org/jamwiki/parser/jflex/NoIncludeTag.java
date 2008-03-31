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

import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.WikiLogger;

/**
 * This class parses nowiki tags of the form <code>&lt;noinclude&gt;content&lt;/noinclude&gt;</code>.
 */
public class NoIncludeTag {

	private static final WikiLogger logger = WikiLogger.getLogger(NoIncludeTag.class.getName());

	/**
	 * Parse a call to a Mediawiki noinclude tag of the form
	 * "<noinclude>text</noinclude>" and return the resulting output.
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) {
		if (mode <= JFlexParser.MODE_MINIMAL) {
			return raw;
		}
		if (parserInput.getTemplateDepth() > 0) {
			// no content is returned when called from a template
			return "";
		}
		try {
			String content = JFlexParserUtil.tagContent(raw);
			// run the pre-processor against the noinclude content
			JFlexParser parser = new JFlexParser(parserInput);
			return parser.parseFragment(parserOutput, content, JFlexParser.MODE_PREPROCESS);
		} catch (Throwable t) {
			logger.info("Unable to parse " + raw, t);
			return raw;
		}
	}
}
