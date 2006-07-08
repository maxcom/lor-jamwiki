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
package org.jamwiki.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;

/**
 * Parser used to implement MediaWiki syntax.
 */
public class JAMWikiParser extends AbstractParser {

	private static final Logger logger = Logger.getLogger(JAMWikiParser.class);

	/**
	 * Sets the basics for this parser.
	 *
	 * @param parserInfo General information about this parser.
	 */
	public JAMWikiParser(ParserInfo parserInfo) {
		super(parserInfo);
	}

	/**
	 * Utility method for executing a lexer parse.
	 * FIXME - this is copy & pasted here and in VQWikiParser
	 */
	 protected StringBuffer lex(Lexer lexer) throws IOException {
		StringBuffer contents = new StringBuffer();
		while (true) {
			String line = lexer.yylex();
			if (line == null) break;
			contents.append(line);
		}
		return contents;
	}

	/**
	 * Parse text for online display.
	 */
	public String parseHTML(String rawtext) throws IOException {
		StringBuffer contents = new StringBuffer();
		Reader raw = new StringReader(rawtext.toString());
		contents = this.parseSyntax(raw);
		raw = new StringReader(contents.toString());
		contents = this.parseParagraphs(raw);
		return contents.toString();
	}

	/**
	 *
	 */
	private StringBuffer parseSyntax(Reader raw) throws IOException {
		JAMWikiPreprocessor lexer = new JAMWikiPreprocessor(raw);
		lexer.setParserInfo(parserInfo);
		return this.lex(lexer);
	}

	/**
	 *
	 */
	private StringBuffer parseParagraphs(Reader raw) throws IOException {
		JAMWikiPostprocessor lexer = new JAMWikiPostprocessor(raw);
		lexer.setParserInfo(parserInfo);
		return this.lex(lexer);
	}
}
