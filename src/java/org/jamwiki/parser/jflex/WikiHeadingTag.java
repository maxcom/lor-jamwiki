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
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiHeadingTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(WikiHeadingTag.class.getName());

	/**
	 * Parse a Mediawiki heading of the form "==heading==" and return the
	 * resulting HTML output.
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, ParserMode mode, String raw) throws Exception {
		int level = 0;
		if (raw.startsWith("=====") && raw.endsWith("=====")) {
			level = 5;
		} else if (raw.startsWith("====") && raw.endsWith("====")) {
			level = 4;
		} else if (raw.startsWith("===") && raw.endsWith("===")) {
			level = 3;
		} else if (raw.startsWith("==") && raw.endsWith("==")) {
			level = 2;
		} else if (raw.startsWith("=") && raw.endsWith("=")) {
			level = 1;
		} else {
			return raw;
		}
		String tagText = raw.substring(level, raw.length() - level).trim();
		String tocText = this.stripMarkup(tagText);
		String tagName = tocText;
		String output = this.updateToc(parserInput, tagName, tocText, level);
		int nextSection = parserInput.getTableOfContents().size();
		output += ParserUtil.buildSectionEditLink(parserInput, nextSection);
		output += "<a name=\"" + Utilities.encodeForURL(tagName) + "\"></a>";
		output += "<h" + level + ">";
		output += ParserUtil.parseFragment(parserInput, tagText, mode.getMode());
		output += "</h" + level + ">";
		return output.toString();
	}

	/**
	 * Strip Wiki markup from text
	 */
	private String stripMarkup(String text) {
		// FIXME - this could be a bit more thorough and also strip HTML
		text = StringUtils.delete(text, "'''");
		text = StringUtils.delete(text, "''");
		text = StringUtils.delete(text, "[[");
		text = StringUtils.delete(text, "]]");
		return text;
	}

	/**
	 *
	 */
	private String updateToc(ParserInput parserInput, String name, String text, int level) {
		String output = "";
		if (parserInput.getTableOfContents().getStatus() == TableOfContents.STATUS_TOC_UNINITIALIZED) {
			output += "__TOC__";
		}
		parserInput.getTableOfContents().addEntry(name, text, level);
		return output;
	}
}
