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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.AbstractLexer;
import org.jamwiki.parser.ParserInfo;
import org.jamwiki.parser.TableOfContents;

/**
 *
 * Default lexer implementation for VQWiki.
 *
 */
public class VQWikiParser extends AbstractParser {

	private static final Logger logger = Logger.getLogger(VQWikiParser.class);
	private static List wikinameIgnore;

	/**
	 * Sets the basics for this parser.
	 *
	 * @param parserInfo General information about this parser.
	 */
	public VQWikiParser(ParserInfo parserInfo) {
		super(parserInfo);
	}

	/**
	 * This method looks for a properties file named wikinames.ignore and
	 * then checks a name passed to this method against the list of names (if
	 * any) in that file.  If the name is found return true, otherwise return
	 * false;
	 *
	 * @param name The name of the topic to check for in the ignore file.
	 * @return Returns <code>true</code> if the name is found in the ignore file,
	 *  otherwise returns <code>false</code>.
	 */
	public static boolean doIgnoreWikiname(String name) {
		if (wikinameIgnore == null) {
			wikinameIgnore = new ArrayList();
			// FIXME - "VQWikiParser.class" may not work in a static method
			InputStream in = VQWikiParser.class.getResourceAsStream("/wikiname.ignore");
			if (in == null) {
				logger.debug("No wikinames to ignore, wikiname.ignore does not exist");
				return false;
			}
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					logger.debug("Adding " + line.toLowerCase() + " to ignore list");
					wikinameIgnore.add(line.toLowerCase());
				}
				reader.close();
				in.close();
			} catch (Exception e) {
				logger.warn("Error reading wikiname.ignore", e);
			}
		}
		if (wikinameIgnore.isEmpty()) {
			return false;
		}
		boolean ignore = wikinameIgnore.contains(name.toLowerCase());
		if (ignore) {
			logger.debug("Do ignore " + name);
		}
		return ignore;
	}

	/**
	 * Parse text for online display.
	 */
	public String parseHTML(String rawtext, String topicName) throws Exception {
		StringBuffer contents = new StringBuffer();
		Reader raw = new StringReader(rawtext.toString());
		contents = this.parseFormat(raw);
		raw = new StringReader(contents.toString());
		contents = this.parseLayout(raw);
		raw = new StringReader(contents.toString());
		contents = this.parseLinks(raw);
		// remove trailing returns at the end of the site.
		return this.removeTrailingNewlines(contents.toString());
	}

	/**
	 *
	 */
	private StringBuffer parseFormat(Reader raw) throws Exception {
		StringBuffer contents = new StringBuffer();
		VQWikiFormatLex lexer = new VQWikiFormatLex(raw);
		lexer.setParserInfo(this.parserInfo);
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
					String converted = LexExtender.getInstance().lexify(
					tag,
					externalContents.toString()
					);
					if (converted != null) {
						contents.append(converted);
					}
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
	private StringBuffer parseLayout(Reader raw) throws Exception {
		VQWikiLayoutLex lexer = new VQWikiLayoutLex(raw);
		lexer.setParserInfo(this.parserInfo);
		return this.lex(lexer);
	}

	/**
	 *
	 */
	private StringBuffer parseLinks(Reader raw) throws Exception {
		VQWikiLinkLex lexer = new VQWikiLinkLex(raw);
		lexer.setParserInfo(this.parserInfo);
		return this.lex(lexer);
	}

	/**
	 * Do nothing
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return HTML representation of the text for online.
	 */
	public String parsePreSave(String raw) throws Exception {
		return raw;
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
	 * Utility method for executing a lexer parse.
	 * FIXME - this is copy & pasted here and in JAMWikiParser
	 */
	protected StringBuffer lex(AbstractLexer lexer) throws Exception {
		StringBuffer contents = new StringBuffer();
		while (true) {
			String line = lexer.yylex();
			if (line == null) {
				break;
			}
			contents.append(line);
		}
		return contents;
	}

	/**
	 *
	 */
	public Collection parseForSearch(String rawtext, String topicName) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public String parseSlice(String rawtext, String topicName, int targetSection) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public String parseSplice(String rawtext, String topicName, int targetSection, String replacementText) throws Exception {
		throw new UnsupportedOperationException();
	}
}
