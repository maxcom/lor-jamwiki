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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jamwiki.WikiBase;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.InterWikiHandler;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiLinkHandler {

	private static WikiLogger logger = WikiLogger.getLogger(WikiLinkHandler.class.getName());
	private static Pattern WIKI_LINK_PATTERN = null;

	static {
		try {
			WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[ ]*(\\:[ ]*)?[ ]*([^\\n\\r\\|]+)([ ]*\\|[ ]*([^\\n\\r]+))?[ ]*\\]\\]");
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 *
	 */
	private String buildInternalLinkUrl(ParserInput parserInput, String raw) {
		String context = parserInput.getContext();
		String virtualWiki = parserInput.getVirtualWiki();
		try {
			WikiLink wikiLink = this.parseWikiLink(raw);
			if (wikiLink == null) {
				// invalid link
				return raw;
			}
			if (!StringUtils.hasText(wikiLink.getDestination()) && !StringUtils.hasText(wikiLink.getSection())) {
				// invalid topic
				return raw;
			}
			if (!wikiLink.getColon() && StringUtils.hasText(wikiLink.getNamespace()) && wikiLink.getNamespace().equals(WikiBase.NAMESPACE_IMAGE)) {
				// parse as an image
				return ParserUtil.parseImageLink(parserInput, wikiLink);
			}
			if (StringUtils.hasText(wikiLink.getNamespace()) && InterWikiHandler.isInterWiki(wikiLink.getNamespace())) {
				// inter-wiki link
				return LinkUtil.interWiki(wikiLink);
			}
			if (wikiLink.getColon() && StringUtils.hasText(wikiLink.getNamespace())) {
				if (WikiBase.getHandler().lookupVirtualWiki(wikiLink.getNamespace()) != null) {
					virtualWiki = wikiLink.getNamespace();
					wikiLink.setDestination(wikiLink.getDestination().substring(virtualWiki.length() + WikiBase.NAMESPACE_SEPARATOR.length()));
				}
			}
			if (!StringUtils.hasText(wikiLink.getText()) && StringUtils.hasText(wikiLink.getDestination())) {
				wikiLink.setText(wikiLink.getDestination());
				if (StringUtils.hasText(wikiLink.getSection())) {
					wikiLink.setText(wikiLink.getText() + "#" + Utilities.decodeFromURL(wikiLink.getSection()));
				}
			} else if (!StringUtils.hasText(wikiLink.getText()) && StringUtils.hasText(wikiLink.getSection())) {
				wikiLink.setText(Utilities.decodeFromURL(wikiLink.getSection()));
			} else {
				wikiLink.setText(ParserUtil.parseFragment(parserInput, wikiLink.getText(), ParserMode.MODE_NORMAL));
			}
			// do not escape text html - already done by parser
			return LinkUtil.buildInternalLinkHtml(context, virtualWiki, wikiLink, wikiLink.getText(), null, false);
		} catch (Exception e) {
			logger.severe("Failure while parsing link " + raw, e);
			return "";
		}
	}

	/**
	 * Parse a Mediawiki link of the form "[[topic|text]]" and return the
	 * resulting HTML output.
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, ParserMode mode, String raw) throws Exception {
		this.processLinkMetadata(parserInput, parserOutput, mode, raw);
		if (mode.hasMode(ParserMode.MODE_SAVE)) {
			// do not parse to HTML when in save mode
			return raw;
		}
		return this.processLinkContent(parserInput, parserOutput, mode, raw);
	}

	/**
	 * Parse a raw Wiki link of the form "[[link|text]]", and return a WikiLink
	 * object representing the link.
	 *
	 * @param raw The raw Wiki link text.
	 * @return A WikiLink object that represents the link.
	 */
	private WikiLink parseWikiLink(String raw) {
		if (!StringUtils.hasText(raw)) {
			return new WikiLink();
		}
		Matcher m = WIKI_LINK_PATTERN.matcher(raw.trim());
		if (!m.matches()) {
			return new WikiLink();
		}
		String url = m.group(2);
		WikiLink wikiLink = LinkUtil.parseWikiLink(url);
		wikiLink.setColon((m.group(1) != null));
		wikiLink.setText(m.group(4));
		return wikiLink;
	}

	/**
	 *
	 */
	private String processLinkContent(ParserInput parserInput, ParserOutput parserOutput, ParserMode mode, String raw) {
		WikiLink wikiLink = this.parseWikiLink(raw);
		if (!StringUtils.hasText(wikiLink.getDestination()) && !StringUtils.hasText(wikiLink.getSection())) {
			// no destination or section
			return raw;
		}
		if (!wikiLink.getColon() && wikiLink.getNamespace() != null && wikiLink.getNamespace().equals(WikiBase.NAMESPACE_CATEGORY)) {
			// category tag, but not a category link
			return "";
		}
		return this.buildInternalLinkUrl(parserInput, raw);
	}

	/**
	 *
	 */
	private void processLinkMetadata(ParserInput parserInput, ParserOutput parserOutput, ParserMode mode, String raw) {
		WikiLink wikiLink = this.parseWikiLink(raw);
		if (!StringUtils.hasText(wikiLink.getDestination()) && !StringUtils.hasText(wikiLink.getSection())) {
			return;
		}
		if (!wikiLink.getColon() && wikiLink.getNamespace() != null && wikiLink.getNamespace().equals(WikiBase.NAMESPACE_CATEGORY)) {
			parserOutput.addCategory(wikiLink.getDestination(), wikiLink.getText());
		}
		if (StringUtils.hasText(wikiLink.getDestination())) {
			parserOutput.addLink(wikiLink.getDestination());
		}
	}
}
