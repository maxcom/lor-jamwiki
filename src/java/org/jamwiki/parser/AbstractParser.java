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
import org.apache.log4j.Logger;

import org.jamwiki.parser.ParserInfo;

/**
 * Abstract class to be used when implementing new lexers.  New lexers
 * should extend this class and override any methods that need to be
 * implemented differently.
 */
public abstract class AbstractParser {

	private static final Logger logger = Logger.getLogger(AbstractParser.class);
	protected ParserInfo parserInfo;

	/**
	 * Sets the basics for this parser.
	 *
	 * @param parserInfo General information about this parser.
	 */
	public AbstractParser(ParserInfo parserInfo) {
		this.parserInfo = parserInfo;
	}

	/**
	 * For getting general information about this parser.
	 *
	 * @return General information about this parser.
	 */
	public ParserInfo getParserInfo() {
		return parserInfo;
	}

	/**
	 *
	 */
	public abstract Collection parseForSearch(String rawtext, String topicName) throws Exception;

	/**
	 * Returns a HTML representation of the given wiki raw text for online representation.
	 *
	 * @param rawtext The raw Wiki syntax to be converted into HTML.
	 * @return HTML representation of the text for online.
	 */
	public abstract String parseHTML(String rawtext, String topicName) throws Exception;

	/**
	 * For syntax that is not saved with the topic source, this method provides
	 * a way of parsing prior to saving.
	 *
	 * @param rawtext The raw Wiki syntax to be converted into HTML.
	 * @return HTML representation of the text for online.
	 */
	public abstract String parsePreSave(String rawtext) throws Exception;

	/**
	 * When making a section edit this function provides the capability to retrieve
	 * all text within a specific heading level.  For example, if targetSection is
	 * specified as five, and the sixth heading is an &lt;h2&gt;, then this method
	 * will return the heading tag and all text up to either the next &lt;h2&gt;,
	 * &lt;h1&gt;, or the end of the document, whichever comes first.
	 *
	 * @param rawtext The raw Wiki text that is to be parsed.
	 * @param topicName The name of the topic that is being parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @return All content within the target section, including the heading, up to
	 *  either the next target section heading of the same or greater level, or the
	 *  end of the document, whichever comes first.
	 */
	public abstract String parseSlice(String rawtext, String topicName, int targetSection) throws Exception;

	/**
	 * This method provides the capability for re-integrating a section edit back
	 * into the main topic.  The text to be re-integrated is provided along with the
	 * full Wiki text and a targetSection.  All of the content of targetSection
	 * is then replaced with the new text.
	 *
	 * @param rawtext The raw Wiki text that is to be parsed.
	 * @param topicName The name of the topic that is being parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @param replacementText The text to replace the target section text with.
	 * @return New Wiki markup created by splicing the replacement text into the
	 *  old Wiki markup text.
	 */
	public abstract String parseSplice(String rawtext, String topicName, int targetSection, String replacementText) throws Exception;

	/**
	 * For setting general information about this parser.
	 *
	 * @param parserInfo General information about this parser.
	 */
	public void setParserInfo(ParserInfo parserInfo) {
		this.parserInfo = parserInfo;
	}
}