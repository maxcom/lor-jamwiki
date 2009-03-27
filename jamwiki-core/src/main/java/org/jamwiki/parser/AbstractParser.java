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

import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class to be used when implementing new parsers.  New parsers
 * should extend this class and override any methods that need to be
 * implemented differently.
 */
public abstract class AbstractParser {

	private static final WikiLogger logger = WikiLogger.getLogger(AbstractParser.class.getName());
	/** Parser configuration information. */
	protected ParserInput parserInput = null;

	/**
	 * The constructor creates a parser instance, initialized with the
	 * specified parser input settings.
	 *
	 * @param parserInput Input configuration settings for this parser
	 *  instance.
	 */
	public AbstractParser(ParserInput parserInput) {
		this.parserInput = parserInput;
	}

	/**
	 * Return a parser-specific value that can be used as the content of a
	 * topic representing a redirect.  For the Mediawiki syntax parser the
	 * value returned would be of the form "#REDIRECT [[Topic]]".
	 *
	 * @param topicName The name of the topic to redirect to.
	 * @return A parser-specific value that can be used as the content of a
	 *  topic representing a redirect.
	 */
	public abstract String buildRedirectContent(String topicName);

	/**
	 * This method parses content, performing all transformations except for
	 * layout changes such as adding paragraph tags.  It is suitable to be used
	 * when parsing the contents of a link or performing similar internal
	 * manipulation.
	 *
	 * @param parserOutput A ParserOutput object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @param mode The parser mode to use when parsing.  Mode affects what
	 *  type of parsing actions are taken when processing raw text.
	 * @return The parsed content.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	// FIXME - should this have a mode flag???
	public abstract String parseFragment(ParserOutput parserOutput, String raw, int mode) throws ParserException;

	/**
	 * Returns a HTML representation of the given wiki raw text for online
	 * representation.
	 *
	 * @param parserOutput A ParserOutput object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public abstract String parseHTML(ParserOutput parserOutput, String raw) throws ParserException;

	/**
	 * This method provides a way to parse content and set all output
	 * metadata, such as link values used by the search engine.
	 *
	 * @param parserOutput A ParserOutput object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public abstract void parseMetadata(ParserOutput parserOutput, String raw) throws ParserException;

	/**
	 * Perform a bare minimum of parsing as required prior to saving a topic
	 * to the database.  In general this method will simply parse signature
	 * tags are return.
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public abstract String parseMinimal(String raw) throws ParserException;

	/**
	 * When making a section edit this function provides the capability to retrieve
	 * all text within a specific heading level.  For example, if targetSection is
	 * specified as five, and the sixth heading is an &lt;h2&gt;, then this method
	 * will return the heading tag and all text up to either the next &lt;h2&gt;,
	 * &lt;h1&gt;, or the end of the document, whichever comes first.
	 *
	 * @param parserOutput A ParserOutput object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki text that is to be parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @return Returns the raw topic content for the target section.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public abstract String parseSlice(ParserOutput parserOutput, String raw, int targetSection) throws ParserException;

	/**
	 * This method provides the capability for re-integrating a section edit back
	 * into the main topic.  The text to be re-integrated is provided along with the
	 * full Wiki text and a targetSection.  All of the content of targetSection
	 * is then replaced with the new text.
	 *
	 * @param parserOutput A ParserOutput object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki text that is to be parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @param replacementText The text to replace the target section text with.
	 * @return The raw topic content including the new replacement text.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public abstract String parseSplice(ParserOutput parserOutput, String raw, int targetSection, String replacementText) throws ParserException;
}