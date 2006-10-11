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

import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class HtmlTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(HtmlTag.class.getName());

	/**
	 *
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, ParserMode mode, String raw) throws Exception {
		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML)) {
			return Utilities.escapeHTML(raw);
		} else {
			return ParserUtil.validateHtmlTag(raw);
		}
	}
}
