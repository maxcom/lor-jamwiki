package org.jamwiki.parser.bliki;

import info.bliki.wiki.filter.WikiModel;

import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;

public class BlikiParser extends JFlexParser {
	public BlikiParser(ParserInput parserInput) {
		super(parserInput);
	}

	/**
	 * Parse text for online display.
	 */
	public ParserDocument parseHTML(String raw) throws Exception {
		ParserDocument doc = new ParserDocument();

		String titlePrefix = "/jamwiki/" + parserInput.getVirtualWiki() + '/';
		JAMWikiModel wikiModel = new JAMWikiModel(parserInput, doc, "/jamwiki/en/${image}", titlePrefix + "${title}");
		String htmlStr = wikiModel.render(raw);
		htmlStr = htmlStr == null ? "" : htmlStr;
		doc.setContent(htmlStr);
		return doc;
	}

}
