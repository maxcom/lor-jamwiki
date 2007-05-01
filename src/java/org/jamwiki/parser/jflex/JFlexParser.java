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

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jamwiki.WikiBase;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.springframework.util.StringUtils;

/**
 * Implementation of {@link org.jamwiki.parser.AbstractParser} that uses
 * <a href="http://jflex.de/">JFlex</a> as a lexer to convert Wiki syntax into
 * HTML or other formats.
 */
public class JFlexParser extends AbstractParser {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexParser.class.getName());

	/** Splice mode is used when inserting an edited topic section back into the full topic content. */
	protected static final int MODE_SPLICE = 1;
	/** Slice mode is used when retrieving a section of a topic for editing. */
	protected static final int MODE_SLICE = 2;
	/** Minimal mode is used to do a bare minimum of parsing, usually just converting signature tags, prior to saving to the database. */
	protected static final int MODE_MINIMAL = 3;
	/** Pre-process mode is currently equivalent to metadata mode and indicates that that the JFlex pre-processor parser should be run in full. */
	protected static final int MODE_PREPROCESS = 4;
	/** Processing mode indicates that the pre-processor and processor should be run in full, parsing all Wiki syntax into formatted output. */
	protected static final int MODE_PROCESS = 5;
	/** Layout mode indicates that the pre-processor, processor and post-processor should be run in full, parsing all Wiki syntax into formatted output and adding layout tags such as paragraphs. */
	protected static final int MODE_LAYOUT = 6;

	private static Pattern REDIRECT_PATTERN = null;

	static {
		try {
			// is the topic a redirect?
			REDIRECT_PATTERN = Pattern.compile("#REDIRECT[ ]+\\[\\[([^\\n\\r\\]]+)\\]\\]", Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 * The constructor creates a parser instance, initialized with the
	 * specified parser input settings.
	 *
	 * @param parserInput Input configuration settings for this parser
	 *  instance.
	 */
	public JFlexParser(ParserInput parserInput) {
		super(parserInput);
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
	public String buildRedirectContent(String topicName) {
		return "#REDIRECT [[" + topicName + "]]";
	}

	/**
	 *
	 */
	private String isRedirect(String content) {
		if (!StringUtils.hasText(content)) return null;
		Matcher m = REDIRECT_PATTERN.matcher(content.trim());
		return (m.matches()) ? Utilities.decodeFromURL(m.group(1).trim()) : null;
	}

	/**
	 * Utility method for executing a lexer parse.
	 */
	private String lex(AbstractLexer lexer, String raw, ParserDocument parserDocument, int mode) throws Exception {
		lexer.init(this.parserInput, parserDocument, mode);
		validate(lexer);
		this.parserInput.incrementDepth();
		// avoid infinite loops
		if (this.parserInput.getDepth() > 100) {
			String topicName = (StringUtils.hasText(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
			throw new Exception("Infinite parsing loop - over " + this.parserInput.getDepth() + " parser iterations while parsing topic " + topicName);
		}
		StringBuffer content = new StringBuffer();
		while (true) {
			String line = lexer.yylex();
			if (line == null) break;
			content.append(line);
		}
		this.parserInput.decrementDepth();
		String redirect = this.isRedirect(raw);
		if (StringUtils.hasText(redirect)) {
			parserDocument.setRedirect(redirect);
		}
		return content.toString();
	}

	/**
	 * This method parses content, performing all transformations except for
	 * layout changes such as adding paragraph tags.  It is suitable to be used
	 * when parsing the contents of a link or performing similar internal
	 * manipulation.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @param mode The parser mode to use when parsing.  Mode affects what
	 *  type of parsing actions are taken when processing raw text.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	public String parseFragment(ParserDocument parserDocument, String raw, int mode) throws Exception {
		// maintain the original output, which has all of the category and link info
		int preMode = (mode > JFlexParser.MODE_PREPROCESS) ? JFlexParser.MODE_PREPROCESS : mode;
		String output = raw;
		output = this.parsePreProcess(parserDocument, output, preMode);
		if (mode >= JFlexParser.MODE_PROCESS) {
			// layout should not be done while parsing fragments
			preMode = JFlexParser.MODE_PROCESS;
			output = this.parseProcess(parserDocument, output, preMode);
		}
		return output;
	}

	/**
	 * Returns a HTML representation of the given wiki raw text for online representation.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	public String parseHTML(ParserDocument parserDocument, String raw) throws Exception {
		long start = System.currentTimeMillis();
		// some parser expressions require that lines end in a newline, so add a newline
		// to the end of the content for good measure
		String output = raw + '\n';
		output = this.parsePreProcess(parserDocument, output, JFlexParser.MODE_PREPROCESS);
		output = this.parseProcess(parserDocument, output, JFlexParser.MODE_PROCESS);
		output = this.parsePostProcess(parserDocument, output, JFlexParser.MODE_LAYOUT);
		if (StringUtils.hasText(this.isRedirect(raw))) {
			// redirects are parsed differently
			output = this.parseRedirect(parserDocument, raw);
		}
		String topicName = (StringUtils.hasText(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
		logger.info("Parse time (parseHTML) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
		return output;
	}

	/**
	 * This method provides a way to parse content and set all output metadata,
	 * such as link values used by the search engine.
	 *
	 * @return A ParserDocument object containing results of the parsing process.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 */
	public void parseMetadata(ParserDocument parserDocument, String raw) throws Exception {
		long start = System.currentTimeMillis();
		// FIXME - set a bogus context value to avoid parser errors
		if (this.parserInput.getContext() == null) this.parserInput.setContext("/wiki");
		// some parser expressions require that lines end in a newline, so add a newline
		// to the end of the content for good measure
		String output = raw + '\n';
		output = this.parsePreProcess(parserDocument, output, JFlexParser.MODE_PREPROCESS);
		output = this.parseProcess(parserDocument, output, JFlexParser.MODE_PROCESS);
		String topicName = (StringUtils.hasText(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
		logger.info("Parse time (parseMetadata) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
	}

	/**
	 * Perform a bare minimum of parsing as required prior to saving a topic
	 * to the database.  In general this method will simply parse signature
	 * tags are return.
	 *
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	public String parseMinimal(String raw) throws Exception {
		long start = System.currentTimeMillis();
		String output = raw;
		ParserDocument parserDocument = new ParserDocument();
		output = this.parsePreProcess(parserDocument, output, JFlexParser.MODE_MINIMAL);
		String topicName = (StringUtils.hasText(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
		logger.info("Parse time (parseHTML) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
		return output;
	}

	/**
	 * First stage of the parser, this method parses templates and signatures
	 * and builds metadata.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	private String parsePreProcess(ParserDocument parserDocument, String raw, int mode) throws Exception {
		StringReader reader = new StringReader(raw);
		JAMWikiPreProcessor lexer = new JAMWikiPreProcessor(reader);
		int preMode = (mode > JFlexParser.MODE_PREPROCESS) ? JFlexParser.MODE_PREPROCESS : mode;
		return this.lex(lexer, raw, parserDocument, preMode);
	}

	/**
	 * Second stage of the parser, this method parses most Wiki syntax, validates
	 * HTML, and performs the majority of the parser conversion.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	private String parseProcess(ParserDocument parserDocument, String raw, int mode) throws Exception {
		StringReader reader = new StringReader(raw);
		JAMWikiProcessor lexer = new JAMWikiProcessor(reader);
		return this.lex(lexer, raw, parserDocument, JFlexParser.MODE_PROCESS);
	}

	/**
	 * In most cases this method is the second and final stage of the parser,
	 * adding paragraph tags and other layout elements that for various reasons
	 * cannot be added during the first parsing stage.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	private String parsePostProcess(ParserDocument parserDocument, String raw, int mode) throws Exception {
		StringReader reader = new StringReader(raw);
		JAMWikiPostProcessor lexer = new JAMWikiPostProcessor(reader);
		return this.lex(lexer, raw, parserDocument, JFlexParser.MODE_LAYOUT);
	}

	/**
	 * Parse a topic that is a redirect.  Ordinarily the contents of the redirected
	 * topic would be displayed, but in some cases (such as when explicitly viewing
	 * a redirect) the redirect page contents need to be displayed.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	private String parseRedirect(ParserDocument parserDocument, String raw) throws Exception {
		String redirect = this.isRedirect(raw);
		String style = "redirect";
		if (!WikiBase.exists(this.parserInput.getVirtualWiki(), redirect.trim())) {
			style = "edit redirect";
		}
		WikiLink wikiLink = new WikiLink();
		wikiLink.setDestination(redirect);
		String output = LinkUtil.buildInternalLinkHtml(this.parserInput.getContext(), this.parserInput.getVirtualWiki(), wikiLink, null, style, null, false);
		return output;
	}

	/**
	 * This method provides the capability for retrieving a section of Wiki markup
	 * from an existing document.  It is used primarily when editing a section of
	 * a topic.  This method will return all content from the specified section, up
	 * to the either the next section of the same or greater level or the end of the
	 * document.  For example, if the specified section is an &lt;h3&gt;, all content
	 * up to the next &lt;h1&gt;, &lt;h2&gt;, &lt;h3&gt; or the end of the document
	 * will be returned.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki text that is to be parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @return Returns the raw topic content for the target section.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	public String parseSlice(ParserDocument parserDocument, String raw, int targetSection) throws Exception {
		long start = System.currentTimeMillis();
		StringReader reader = new StringReader(raw);
		JAMWikiSpliceProcessor lexer = new JAMWikiSpliceProcessor(reader);
		lexer.setTargetSection(targetSection);
		String output = this.lex(lexer, raw, parserDocument, JFlexParser.MODE_SLICE);
		String topicName = (StringUtils.hasText(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
		logger.fine("Parse time (parseSlice) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
		return output;
	}

	/**
	 * This method provides the capability for splicing a section of new content back
	 * into a document.  It is used primarily when editing a section of a topic.  This
	 * method will replace all content in a specified section, up to the either the next
	 * section of the same or greater level or the end of the document.  For example, if
	 * the specified section is an &lt;h3&gt;, all content up to the next &lt;h1&gt;,
	 * &lt;h2&gt;, &lt;h3&gt; or the end of the document will be replaced with the
	 * specified text.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param raw The raw Wiki text that is to be parsed.
	 * @param targetSection The section (counted from zero) that is to be returned.
	 * @param replacementText The text to replace the target section text with.
	 * @return The raw topic content including the new replacement text.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	public String parseSplice(ParserDocument parserDocument, String raw, int targetSection, String replacementText) throws Exception {
		long start = System.currentTimeMillis();
		StringReader reader = new StringReader(raw);
		JAMWikiSpliceProcessor lexer = new JAMWikiSpliceProcessor(reader);
		lexer.setReplacementText(replacementText);
		lexer.setTargetSection(targetSection);
		String output = this.lex(lexer, raw, parserDocument, JFlexParser.MODE_SPLICE);
		String topicName = (StringUtils.hasText(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
		logger.fine("Parse time (parseSplice) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
		return output;
	}

	/**
	 * Validate that all settings required for the parser have been set, and if
	 * not throw an exception.
	 *
	 * @throws Exception Thrown if the parser is not initialized properly,
	 *  usually due to a parser input field not being set.
	 */
	private static void validate(AbstractLexer lexer) throws Exception {
		// validate parser settings
		boolean validated = true;
		if (lexer.mode == JFlexParser.MODE_SPLICE || lexer.mode == JFlexParser.MODE_SLICE) {
			if (lexer.parserInput.getTopicName() == null) {
				validated = false;
			}
		} else if (lexer.mode == JFlexParser.MODE_LAYOUT) {
			if (lexer.parserInput == null) {
				validated = false;
			}
			if (lexer.parserInput.getTableOfContents() == null) {
				validated = false;
			}
		} else if (lexer.mode == JFlexParser.MODE_PROCESS) {
			if (lexer.parserInput.getTableOfContents() == null) {
				validated = false;
			}
			if (lexer.parserInput.getTopicName() == null) {
				validated = false;
			}
			if (lexer.parserInput.getContext() == null) {
				validated = false;
			}
			if (lexer.parserInput.getVirtualWiki() == null) {
				validated = false;
			}
		} else if (lexer.mode <= JFlexParser.MODE_PREPROCESS) {
			if (lexer.mode >= JFlexParser.MODE_MINIMAL) {
				if (lexer.parserInput.getVirtualWiki() == null) {
					validated = false;
				}
				if (lexer.parserInput.getTopicName() == null) {
					validated = false;
				}
			}
		}
		if (!validated) {
			throw new Exception("Parser info not properly initialized");
		}
	}
}
