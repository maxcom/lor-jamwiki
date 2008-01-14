package org.jamwiki.parser.bliki;

import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;

public class BlikiParser extends JFlexParser {
	public BlikiParser(ParserInput parserInput) {
		super(parserInput);
	}

	/**
	 * Parse text for online display.
	 */
	public String parseHTML(String raw) throws Exception {
		ParserOutput doc = new ParserOutput();

		String titlePrefix = "/jamwiki/" + parserInput.getVirtualWiki() + '/';
		JAMWikiModel wikiModel = new JAMWikiModel(parserInput, doc, titlePrefix + "${image}", titlePrefix + "${title}");
		String htmlStr = wikiModel.render(raw);
		htmlStr = htmlStr == null ? "" : htmlStr;
		return htmlStr;
	}

}
