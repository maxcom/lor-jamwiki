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

import org.jamwiki.DataAccessException;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Abstract parent class used for parsing wiki & HTML heading tags.
 */
public abstract class AbstractHeadingTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(AbstractHeadingTag.class.getName());

	/**
	 *
	 */
	private String buildSectionEditLink(ParserInput parserInput, int section) {
		if (!parserInput.getAllowSectionEdit()) {
			return "";
		}
		if (parserInput.getLocale() == null) {
			logger.info("Unable to build section edit links for " + parserInput.getTopicName() + " - locale is empty");
			return "";
		}
		// FIXME - template inclusion causes section edits to break, so disable for now
		String inclusion = (String)parserInput.getTempParams().get(TemplateTag.TEMPLATE_INCLUSION);
		boolean disallowInclusion = (inclusion != null && inclusion.equals("true"));
		if (disallowInclusion) {
			return "";
		}
		String url = "";
		try {
			url = LinkUtil.buildEditLinkUrl(parserInput.getContext(), parserInput.getVirtualWiki(), parserInput.getTopicName(), null, section);
		} catch (DataAccessException e) {
			logger.severe("Failure while building link for topic " + parserInput.getVirtualWiki() + " / " + parserInput.getTopicName(), e);
		}
		StringBuilder output = new StringBuilder("<span class=\"editsection\">[<a href=\"").append(url).append("\">");
		output.append(Utilities.formatMessage("common.sectionedit", parserInput.getLocale()));
		output.append("</a>]</span>");
		return output.toString();
	}

	/**
	 *
	 */
	private String buildTagName(JFlexLexer lexer, String tocText) {
		String tagName = lexer.getParserInput().getTableOfContents().checkForUniqueName(tocText);
		// re-convert any &uuml; or other (converted by the parser) entities back
		return StringEscapeUtils.unescapeHtml(tagName);
	}

	/**
	 *
	 */
	private String buildTocText(JFlexLexer lexer, String tagText) throws ParserException {
		ParserInput tmpParserInput = new ParserInput(lexer.getParserInput());
		String tocText = JFlexParserUtil.parseFragment(tmpParserInput, tagText, JFlexParser.MODE_PROCESS);
		return Utilities.stripMarkup(tocText);
	}

	/**
	 *
	 */
	private String generateOutput(JFlexLexer lexer, String tagName, String tocText, String tagText, int level, String raw, Object... args) throws ParserException {
		StringBuilder output = new StringBuilder(this.updateToc(lexer.getParserInput(), tagName, tocText, level));
		int nextSection = lexer.getParserInput().getTableOfContents().size();
		output.append("<a name=\"").append(Utilities.encodeAndEscapeTopicName(tagName)).append("\"></a>");
		output.append(generateTagOpen(raw, args));
		output.append(this.buildSectionEditLink(lexer.getParserInput(), nextSection));
		output.append("<span>").append(JFlexParserUtil.parseFragment(lexer.getParserInput(), tagText, lexer.getMode())).append("</span>");
		output.append("</h").append(level).append('>');
		return output.toString();
	}

	/**
	 *
	 */
	protected abstract int generateTagLevel(String raw, Object... args) throws ParserException;

	/**
	 *
	 */
	protected abstract String generateTagOpen(String raw, Object... args) throws ParserException;

	/**
	 *
	 */
	protected abstract String generateTagText(String raw, Object... args) throws ParserException;

	/**
	 * Parse a Mediawiki heading of the form "==heading==" and return the
	 * resulting HTML output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		if (logger.isFinerEnabled()) {
			logger.finer("heading: " + raw + " (" + lexer.yystate() + ")");
		}
		int level = this.generateTagLevel(raw, args);
		String tagText = this.generateTagText(raw, args);
		if (lexer.peekTag().getTagType().equals("p")) {
			lexer.popTag("p");
		}
		if (lexer instanceof JAMWikiProcessor && lexer.yystate() == JAMWikiProcessor.PARAGRAPH) {
			lexer.endState();
		}
		String tocText = this.buildTocText(lexer, tagText);
		String tagName = this.buildTagName(lexer, tocText);
		if (lexer.getMode() <= JFlexParser.MODE_SLICE) {
			lexer.getParserOutput().setSectionName(tagName);
			return raw;
		}
		return this.generateOutput(lexer, tagName, tocText, tagText, level, raw, args);
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
