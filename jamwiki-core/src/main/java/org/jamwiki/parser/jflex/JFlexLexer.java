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

import java.util.Stack;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the JFlex lexers.  This class primarily
 * contains utility methods useful during parsing.
 */
public abstract class JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(JFlexLexer.class.getName());

	/** Member variable used to keep track of the state history for the lexer. */
	protected Stack<Integer> states = new Stack<Integer>();
	/** Parser configuration information. */
	protected ParserInput parserInput = null;
	/** Parser parsing results. */
	protected ParserOutput parserOutput = null;
	/** Parser mode, which provides input to the parser about what steps to take. */
	protected int mode = JFlexParser.MODE_POSTPROCESS;
	/** Stack of currently parsed tag content. */
	protected Stack<JFlexTagItem> tagStack = new Stack<JFlexTagItem>();

	protected static final int TAG_TYPE_HTML_HEADING = 5;
	protected static final int TAG_TYPE_HTML_LINK = 10;
	protected static final int TAG_TYPE_IMAGE_LINK = 15;
	protected static final int TAG_TYPE_INCLUDE_ONLY = 20;
	protected static final int TAG_TYPE_JAVASCRIPT = 25;
	protected static final int TAG_TYPE_NO_INCLUDE = 30;
	protected static final int TAG_TYPE_ONLY_INCLUDE = 33;
	protected static final int TAG_TYPE_REDIRECT = 34;
	protected static final int TAG_TYPE_TEMPLATE = 35;
	protected static final int TAG_TYPE_WIKI_BOLD_ITALIC = 40;
	protected static final int TAG_TYPE_WIKI_HEADING = 45;
	protected static final int TAG_TYPE_WIKI_LINK = 50;
	protected static final int TAG_TYPE_WIKI_REFERENCE = 55;
	protected static final int TAG_TYPE_WIKI_REFERENCES = 60;
	protected static final int TAG_TYPE_WIKI_SIGNATURE = 65;
	private static final HtmlHeadingTag TAG_HTML_HEADING = new HtmlHeadingTag();
	private static final HtmlLinkTag TAG_HTML_LINK = new HtmlLinkTag();
	private static final ImageLinkTag TAG_IMAGE_LINK = new ImageLinkTag();
	private static final IncludeOnlyTag TAG_INCLUDE_ONLY = new IncludeOnlyTag();
	private static final JavascriptTag TAG_JAVASCRIPT = new JavascriptTag();
	private static final NoIncludeTag TAG_NO_INCLUDE = new NoIncludeTag();
	private static final OnlyIncludeTag TAG_ONLY_INCLUDE = new OnlyIncludeTag();
	private static final RedirectTag TAG_REDIRECT = new RedirectTag();
	private static final TemplateTag TAG_TEMPLATE = new TemplateTag();
	private static final WikiBoldItalicTag TAG_WIKI_BOLD_ITALIC = new WikiBoldItalicTag();
	private static final WikiHeadingTag TAG_WIKI_HEADING = new WikiHeadingTag();
	private static final WikiLinkTag TAG_WIKI_LINK = new WikiLinkTag();
	private static final WikiReferenceTag TAG_WIKI_REFERENCE = new WikiReferenceTag();
	private static final WikiReferencesTag TAG_WIKI_REFERENCES = new WikiReferencesTag();
	private static final WikiSignatureTag TAG_WIKI_SIGNATURE = new WikiSignatureTag();

	/**
	 * Utility method used to indicate whether HTML tags are allowed in wiki syntax
	 * or not.
	 */
	protected boolean allowHTML() {
		return Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
	}

	/**
	 * Utility method used to indicate whether Javascript is allowed in wiki syntax
	 * or not.  Note that enabling Javascript opens a site up to cross-site-scripting
	 * attacks.
	 */
	protected boolean allowJavascript() {
		return Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
	}

	/**
	 * Append content to the current tag in the tag stack.
	 */
	protected void append(String content) {
		this.tagStack.peek().getTagContent().append(content);
	}

	/**
	 * Begin a new parser state and store the old state onto the stack.
	 *
	 * @param state The new parsing state that is being entered.
	 */
	protected void beginState(int state) {
		// store current state
		states.push(yystate());
		// switch to new state
		yybegin(state);
	}

	/**
	 * End processing of a parser state and switch to the previous parser state.
	 */
	protected void endState() {
		// revert to previous state
		if (states.empty()) {
			logger.warn("Attempt to call endState for an empty stack with text: " + yytext());
			return;
		}
		int next = states.pop();
		yybegin(next);
	}

	/**
	 * Return the current lexer mode (defined in the lexer specification file).
	 */
	protected int getMode() {
		return this.mode;
	}

	/**
	 * This method is used to retrieve information used about parser configuration settings.
	 *
	 * @return Parser configuration information.
	 */
	public ParserInput getParserInput() {
		return this.parserInput;
	}

	/**
	 * This method is used to set the ParserOutput field, which is used to retrieve
	 * parsed information from the parser.
	 *
	 * @return Parsed information generated by the parser
	 */
	public ParserOutput getParserOutput() {
		return this.parserOutput;
	}

	/**
	 *
	 */
	protected Stack<JFlexTagItem> getTagStack() {
		return this.tagStack;
	}

	/**
	 * Initialize the parser settings.  This functionality should be done
	 * from the constructor, but since JFlex generates code it is not possible
	 * to modify the constructor parameters.
	 *
	 * @param parserInput The ParserInput object containing parser parameters
	 *  required for successful parsing.
	 * @param parserOutput The current parsed document.  When parsing is done
	 *  in multiple stages that output values are also built in stages.
	 * @param mode The parser mode to use when parsing.  Mode affects what
	 *  type of parsing actions are taken when processing raw text.
	 */
	public final void init(ParserInput parserInput, ParserOutput parserOutput, int mode) throws ParserException {
		this.parserInput = parserInput;
		this.parserOutput = parserOutput;
		this.mode = mode;
		this.tagStack.push(new JFlexTagItem(JFlexTagItem.ROOT_TAG, null));
	}

	/**
	 * Utility method to walk the current tag stack to determine if the top of the stack
	 * contains list tags followed by a tag of a specific type.
	 */
	private boolean isNextAfterListTags(String tagType) {
		JFlexTagItem nextTag;
		for (int i = (this.tagStack.size() - 1); i > 0; i--) {
			nextTag = this.tagStack.get(i);
			if (nextTag.getTagType().equals(tagType)) {
				return true;
			}
			if (!nextTag.isListTag()) {
				return false;
			}
		}
		return false;
	}

	/**
	 *
	 */
	protected String parse(int type, String raw, Object... args) {
		if (this.getParserInput().getInfiniteLoopCount() >= Environment.getIntValue(Environment.PROP_PARSER_MAXIMUM_INFINITE_LOOP_LIMIT)) {
			// do not attempt any further parsing
			return raw;
		}
		JFlexParserTag jflexParserTag = null;
		switch (type) {
			case TAG_TYPE_HTML_HEADING:
				jflexParserTag = TAG_HTML_HEADING;
				break;
			case TAG_TYPE_HTML_LINK:
				jflexParserTag = TAG_HTML_LINK;
				break;
			case TAG_TYPE_IMAGE_LINK:
				jflexParserTag = TAG_IMAGE_LINK;
				break;
			case TAG_TYPE_INCLUDE_ONLY:
				jflexParserTag = TAG_INCLUDE_ONLY;
				break;
			case TAG_TYPE_JAVASCRIPT:
				jflexParserTag = TAG_JAVASCRIPT;
				break;
			case TAG_TYPE_NO_INCLUDE:
				jflexParserTag = TAG_NO_INCLUDE;
				break;
			case TAG_TYPE_ONLY_INCLUDE:
				jflexParserTag = TAG_ONLY_INCLUDE;
				break;
			case TAG_TYPE_REDIRECT:
				jflexParserTag = TAG_REDIRECT;
				break;
			case TAG_TYPE_TEMPLATE:
				jflexParserTag = TAG_TEMPLATE;
				break;
			case TAG_TYPE_WIKI_BOLD_ITALIC:
				jflexParserTag = TAG_WIKI_BOLD_ITALIC;
				break;
			case TAG_TYPE_WIKI_HEADING:
				jflexParserTag = TAG_WIKI_HEADING;
				break;
			case TAG_TYPE_WIKI_LINK:
				jflexParserTag = TAG_WIKI_LINK;
				break;
			case TAG_TYPE_WIKI_REFERENCE:
				jflexParserTag = TAG_WIKI_REFERENCE;
				break;
			case TAG_TYPE_WIKI_REFERENCES:
				jflexParserTag = TAG_WIKI_REFERENCES;
				break;
			case TAG_TYPE_WIKI_SIGNATURE:
				jflexParserTag = TAG_WIKI_SIGNATURE;
				break;
			default:
				throw new IllegalArgumentException("Invalid tag type: " + type);
		}
		try {
			return jflexParserTag.parse(this, raw, args);
		} catch (Throwable t) {
			logger.info("Unable to parse " + raw, t);
			return raw;
		}
	}

	/**
	 * Peek at the current tag from the lexer stack and see if it matches
	 * the given tag type.
	 */
	protected JFlexTagItem peekTag() {
		return this.tagStack.peek();
	}

	/**
	 * Pop the most recent HTML tag from the lexer stack.
	 */
	protected JFlexTagItem popTag(String tagType) {
		if (this.tagStack.size() <= 1) {
			logger.warn("popTag called on an empty tag stack or on the root stack element.  Please report this error on jamwiki.org, and provide the wiki syntax for the topic being parsed.");
		}
		// verify that the tag being closed is the tag that is currently open.  if not
		// there are two options - first is that the user entered unbalanced HTML such
		// as "<u><strong>text</u></strong>" and it should be re-balanced, and second
		// is that this is just a random close tag such as "<div>text</div></div>" and
		// it should be escaped without modifying the tag stack.
		if (!this.peekTag().getTagType().equals(tagType)) {
			// check to see if a close tag override was previously set, which happens
			// from the inner tag of unbalanced HTML.  Example: "<u><strong>text</u></strong>"
			// would set a close tag override when the "</u>" is parsed to indicate that
			// the "</strong>" should actually be parsed as a "</u>".
			if (StringUtils.equals(this.peekTag().getTagType(), this.peekTag().getCloseTagOverride())) {
				return this.popTag(this.peekTag().getCloseTagOverride());
			}
			// check to see if the parent tag is a list and the current tag is in the tag
			// stack.  if so close the list and pop the current tag.
			if (!JFlexTagItem.isListTag(tagType) && this.peekTag().isListItemTag() && this.isNextAfterListTags(tagType)) {
				this.popAllListTags();
				return this.popTag(tagType);
			}
			// check to see if the parent tag matches the current close tag.  if so then
			// this is unbalanced HTML of the form "<u><strong>text</u></strong>" and
			// it should be parsed as "<u><strong>text</strong></u>".
			JFlexTagItem parent = null;
			if (this.tagStack.size() > 2) {
				parent = this.tagStack.get(this.tagStack.size() - 2);
			}
			if (parent != null && parent.getTagType().equals(tagType)) {
				parent.setCloseTagOverride(tagType);
				return this.popTag(this.peekTag().getTagType());
			}
			// if the above checks fail then this is an attempt to pop a tag that is not
			// currently open, so append the escaped close tag to the current tag
			// content without modifying the tag stack.
			JFlexTagItem currentTag = this.tagStack.peek();
			currentTag.getTagContent().append("&lt;/" + tagType + "&gt;");
			return null;
		}
		JFlexTagItem currentTag = this.tagStack.peek();
		if (this.tagStack.size() > 1) {
			// only pop if not the root tag
			currentTag = this.tagStack.pop();
		}
		JFlexTagItem previousTag = this.tagStack.peek();
		if (!currentTag.isInlineTag() || currentTag.getTagType().equals("pre")) {
			// if the current tag is not an inline tag, make sure it is on its own lines
			String trimmedContent = StringUtils.stripEnd(previousTag.getTagContent().toString(), null);
			previousTag.getTagContent().replace(0, previousTag.getTagContent().length(), trimmedContent);
			previousTag.getTagContent().append('\n');
			previousTag.getTagContent().append(currentTag.toHtml());
			previousTag.getTagContent().append('\n');
		} else {
			previousTag.getTagContent().append(currentTag.toHtml());
		}
		return currentTag;
	}

	/**
	 * Pop the most recent HTML tag from the lexer stack.
	 */
	protected JFlexTagItem popTag(String tagType, String closeTagRaw) throws ParserException {
		if (tagType != null) {
			return this.popTag(tagType);
		}
		HtmlTagItem htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(closeTagRaw);
		return this.popTag(htmlTagItem.getTagType());
	}

	/**
	 * Pop all tags off of the stack and return a string representation.
	 */
	protected String popAllTags() {
		// pop the stack down to (but not including) the root tag
		while (this.tagStack.size() > 1) {
			JFlexTagItem currentTag = this.tagStack.peek();
			this.popTag(currentTag.getTagType());
		}
		// now pop the root tag
		JFlexTagItem currentTag = this.tagStack.pop();
		return (this.mode >= JFlexParser.MODE_LAYOUT) ? currentTag.toHtml().trim() : currentTag.toHtml();
	}

	/**
	 * Push a new HTML tag onto the lexer stack.
	 */
	protected void pushTag(String tagType, String openTagRaw) throws ParserException {
		JFlexTagItem tag = new JFlexTagItem(tagType, openTagRaw);
		// many HTML tags cannot nest (ie "<li><li></li></li>" is invalid), so if a non-nesting
		// tag is being added and the previous tag is of the same type, close the previous tag
		if (tag.isNonNestingTag() && this.peekTag().getTagType().equals(tag.getTagType())) {
			this.popTag(tag.getTagType());
		}
		this.tagStack.push(tag);
	}

	/**
	 * Utility method used when parsing list tags to determine the current
	 * list nesting level.
	 */
	protected int currentListDepth() {
		int depth = 0;
		int currentPos = this.tagStack.size() - 1;
		while (currentPos >= 0) {
			JFlexTagItem tag = this.tagStack.get(currentPos);
			if (!StringUtils.equals(tag.getTagType(), "li") && !StringUtils.equals(tag.getTagType(), "dd") && !StringUtils.equals(tag.getTagType(), "dt")) {
				break;
			}
			// move back in the stack two since each list item has a parent list type
			currentPos -= 2;
			depth++;
		}
		return depth;
	}

	/**
	 *
	 */
	protected void popAllListTags() {
		// before clearing a list, first make sure that any open inline tags or paragraph tags
		// have been closed (example: "<i><ul>" is invalid.  close the <i> first).
		while (!this.peekTag().isRootTag() && (this.peekTag().getTagType().equals("p") || this.peekTag().isInlineTag())) {
			this.popTag(this.peekTag().getTagType());
		}
		this.popListTags(this.currentListDepth());
	}

	/**
	 *
	 */
	protected void popListTags(int depth) {
		if (depth < 0) {
			throw new IllegalArgumentException("Cannot pop a negative number: " + depth);
		}
		String tagType;
		for (int i=0; i < depth; i++) {
			// pop twice since lists have a list tag and a list item tag ("<ul><li></li></ul>")
			tagType = (this.tagStack.peek()).getTagType();
			popTag(tagType);
			tagType = (this.tagStack.peek()).getTagType();
			popTag(tagType);
		}
	}

	/**
	 * JFlex internal method used to change the lexer state values.
	 */
	public abstract void yybegin(int newState);

	/**
	 * JFlex internal method used to parse the next token.
	 */
	public abstract String yylex() throws Exception;

	/**
	 * JFlex internal method used to push text back onto the parser stack.
	 */
	public abstract void yypushback(int number);

	/**
	 * JFlex internal method used to retrieve the current lexer state value.
	 */
	public abstract int yystate();

	/**
	 * JFlex internal method used to retrieve the current text matched by the
	 * yylex() method.
	 */
	public abstract String yytext();
}
