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

import java.io.Reader;
import java.util.Collection;
import java.util.List;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * Abstract class to be used when implementing new lexers.  New lexers
 * should extend this class and override any methods that need to be
 * implemented differently.
 */
public abstract class AbstractParser {

	private static final WikiLogger logger = WikiLogger.getLogger(AbstractParser.class.getName());
	protected ParserInput parserInput = null;

	/**
	 * Sets the basics for this parser.
	 *
	 * @param parserInput General information about this parser.
	 */
	public AbstractParser(ParserInput parserInput) {
		this.parserInput = parserInput;
	}

	/**
	 *
	 */
	public abstract String buildRedirectContent(String topicName);

	/**
	 * This method parses content, performing all transformations except for
	 * layout changes such as adding paragraph tags.  It is suitable to be used
	 * when parsing the contents of a link or performing similar internal
	 * manipulation.
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 */
	// FIXME - should this have a mode flag???
	public abstract ParserDocument parseFragment(String raw, int mode) throws Exception;

	/**
	 * Returns a HTML representation of the given wiki raw text for online representation.
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 */
	public abstract ParserDocument parseHTML(String raw) throws Exception;

	/**
	 * This method provides a way to parse content and set all output metadata,
	 * such as link values used by the search engine.
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return Results from parser execution.
	 */
	public abstract ParserDocument parseMetadata(String raw) throws Exception;

	/**
	 * Parse MediaWiki signatures and other tags that should not be
	 * saved as part of the topic source.  This method is usually only called
	 * during edits.
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return A ParserDocument object containing results of the parsing process.
	 */
	public abstract ParserDocument parseSave(String raw) throws Exception;

	/**
	 * When making a section edit this function provides the capability to retrieve
	 * all text within a specific heading level.  For example, if targetSection is
	 * specified as five, and the sixth heading is an &lt;h2&gt;, then this method
	 * will return the heading tag and all text up to either the next &lt;h2&gt;,
	 * &lt;h1&gt;, or the end of the document, whichever comes first.
	 *
	 * @param raw The raw Wiki text that is to be parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 */
	public abstract ParserDocument parseSlice(String raw, int targetSection) throws Exception;

	/**
	 * This method provides the capability for re-integrating a section edit back
	 * into the main topic.  The text to be re-integrated is provided along with the
	 * full Wiki text and a targetSection.  All of the content of targetSection
	 * is then replaced with the new text.
	 *
	 * @param raw The raw Wiki text that is to be parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @param replacementText The text to replace the target section text with.
	 */
	public abstract ParserDocument parseSplice(String raw, int targetSection, String replacementText) throws Exception;
}