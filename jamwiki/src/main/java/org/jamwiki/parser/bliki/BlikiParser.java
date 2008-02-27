package org.jamwiki.parser.bliki;

import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.jamwiki.utils.Utilities;

public class BlikiParser extends JFlexParser {
	public BlikiParser(ParserInput parserInput) {
		super(parserInput);
	}
  
	/**
	 * Parse text for online display.
	 */
	public String parseHTML(ParserDocument parserDocument, String raw) throws Exception {
		ParserDocument doc = new ParserDocument();
		String context = parserInput.getContext();
		String baseURL = "";
		if (context != null) {
			baseURL += context;
		}
		// context never ends with a "/" per servlet specification
		baseURL += "/";
		// get the virtual wiki, which should have been set by the parent servlet
		baseURL += Utilities.encodeForURL(parserInput.getVirtualWiki());
		baseURL += "/";
		JAMWikiModel wikiModel = new JAMWikiModel(parserInput, doc, baseURL + "${image}", baseURL + "${title}");
		String htmlStr = wikiModel.render(new JAMHTMLConverter(parserInput), raw);
		htmlStr = htmlStr == null ? "" : htmlStr;
		return htmlStr;
	}

}
