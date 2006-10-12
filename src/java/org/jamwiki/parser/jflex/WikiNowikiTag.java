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
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class WikiNowikiTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(WikiNowikiTag.class.getName());

	/**
	 * Parse a Mediawiki heading of the form "==heading==" and return the
	 * resulting HTML output.
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) throws Exception {
		if (mode <= JFlexParser.MODE_PREPROCESS) {
			// return content unchanged
			return raw;
		}
		String content = tagContent(raw);
		if (mode == JFlexParser.MODE_PROCESS) {
			return "<nowiki>" + Utilities.escapeHTML(content) + "</nowiki>";
		}
		return content;
	}

	/**
	 *
	 */
	private String tagContent(String raw) {
		int start = raw.indexOf(">") + 1;
		int end = raw.lastIndexOf("<");
		if (start == 0) {
			// no tags
			return raw;
		}
		if (end <= start) {
			// no end tag?
			end = start;
			start = 0;
		}
		return raw.substring(start, end);
	}
}
