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
package org.jamwiki.parser.alt;

import java.io.StringReader;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.AbstractLexer;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.TableOfContents;

/**
 *
 * Default lexer implementation for VQWiki.
 *
 */
public class VQWikiParser extends AbstractParser {

	private static final Logger logger = Logger.getLogger(VQWikiParser.class);

	/**
	 * Sets the basics for this parser.
	 *
	 * @param parserInput General information about this parser.
	 */
	public VQWikiParser(ParserInput parserInput) {
		super(parserInput);
	}

	/**
	 *
	 */
	public String buildRedirectContent(String topicName) {
		// not implemented
		return "";
	}

	/**
	 * Parse text for online display.
	 */
	public ParserOutput parseHTML(String rawtext, String topicName) throws Exception {
		StringBuffer contents = new StringBuffer();
		StringReader raw = new StringReader(rawtext.toString());
		contents = this.parseFormat(raw);
		raw = new StringReader(contents.toString());
		ParserOutput parserOutput = this.parseLayout(raw);
		raw = new StringReader(parserOutput.getContent());
		parserOutput = this.parseLinks(raw);
		// remove trailing returns at the end of the site.
		String content = this.removeTrailingNewlines(parserOutput.getContent());
		parserOutput.setContent(content);
		return parserOutput;
	}

	/**
	 *
	 */
	private StringBuffer parseFormat(StringReader raw) throws Exception {
		StringBuffer contents = new StringBuffer();
		VQWikiFormatLex lexer = new VQWikiFormatLex(raw);
		lexer.setParserInput(this.parserInput);
		boolean external = false;
		String tag = null;
		StringBuffer externalContents = null;
		while (true) {
			String line = null;
			try {
				line = lexer.yylex();
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.debug(e);
			}
			logger.debug(line);
			if (line == null) {
				break;
			}
			if (line.startsWith("[<")) {
				if (!external) {
					external = true;
					tag = line.substring(2, line.length() - 2);
					logger.debug("External lex call (tag=" + tag + ")");
					externalContents = new StringBuffer();
					contents.append(line);
				} else {
					external = false;
					contents.append(line);
					logger.debug("External ends");
				}
			} else {
				if (!external) {
					contents.append(line);
				} else {
					externalContents.append(line);
				}
			}
		}
		if (Environment.getBooleanValue(Environment.PROP_PARSER_TOC)) {
			contents = new StringBuffer(TableOfContents.addTableOfContents(contents.toString()));
		}
		return contents;
	}

	/**
	 *
	 */
	private ParserOutput parseLayout(StringReader raw) throws Exception {
		VQWikiLayoutLex lexer = new VQWikiLayoutLex(raw);
		lexer.setParserInput(this.parserInput);
		return this.lex(lexer);
	}

	/**
	 *
	 */
	private ParserOutput parseLinks(StringReader raw) throws Exception {
		VQWikiLinkLex lexer = new VQWikiLinkLex(raw);
		lexer.setParserInput(this.parserInput);
		return this.lex(lexer);
	}

	/**
	 * Do nothing
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return HTML representation of the text for online.
	 */
	public ParserOutput parsePreSave(String raw) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	private String removeTrailingNewlines(String content) {
		// remove trailing returns at the end of the site.
		// TODO better do this with StringBuffer, but actually no more
		// time for a proper cleanup.
		if (content.endsWith("<br/>\n")) {
			content = content.substring(0, content.length() - 6);
			while (content.endsWith("<br/>")) {
				content = content.substring(0, content.length() - 5);
			}
		}
		return content;
	}

	/**
	 *
	 */
	public ParserOutput parseSlice(String rawtext, String topicName, int targetSection) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public ParserOutput parseSplice(String rawtext, String topicName, int targetSection, String replacementText) throws Exception {
		throw new UnsupportedOperationException();
	}
}
