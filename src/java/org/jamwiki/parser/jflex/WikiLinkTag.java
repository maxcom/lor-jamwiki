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

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jamwiki.WikiBase;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.InterWikiHandler;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiLinkTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(WikiLinkTag.class.getName());
	private static Pattern WIKI_LINK_PATTERN = null;
	private static Pattern IMAGE_SIZE_PATTERN = null;
	// FIXME - make configurable
	private static final int DEFAULT_THUMBNAIL_SIZE = 180;

	static {
		try {
			WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[ ]*(\\:[ ]*)?[ ]*([^\\n\\r\\|]+)([ ]*\\|[ ]*([^\\n\\r]+))?[ ]*\\]\\]");
			// look for image size info in image tags
			IMAGE_SIZE_PATTERN = Pattern.compile("([0-9]+)[ ]*px", Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 *
	 */
	private String buildInternalLinkUrl(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) {
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
			if (!wikiLink.getColon() && StringUtils.hasText(wikiLink.getNamespace()) && wikiLink.getNamespace().equals(NamespaceHandler.NAMESPACE_IMAGE)) {
				// parse as an image
				return this.parseImageLink(parserInput, mode, wikiLink);
			}
			if (StringUtils.hasText(wikiLink.getNamespace()) && InterWikiHandler.isInterWiki(wikiLink.getNamespace())) {
				// inter-wiki link
				return LinkUtil.interWiki(wikiLink);
			}
			if (wikiLink.getColon() && StringUtils.hasText(wikiLink.getNamespace())) {
				if (WikiBase.getDataHandler().lookupVirtualWiki(wikiLink.getNamespace()) != null) {
					virtualWiki = wikiLink.getNamespace();
					wikiLink.setDestination(wikiLink.getDestination().substring(virtualWiki.length() + NamespaceHandler.NAMESPACE_SEPARATOR.length()));
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
				wikiLink.setText(ParserUtil.parseFragment(parserInput, wikiLink.getText(), mode));
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
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		this.processLinkMetadata(parserInput, parserDocument, raw);
		if (mode <= JFlexParser.MODE_PREPROCESS) {
			// do not parse to HTML when in preprocess mode
			return raw;
		}
		return this.processLinkContent(parserInput, parserDocument, mode, raw);
	}

	/**
	 *
	 */
	private String parseImageLink(ParserInput parserInput, int mode, WikiLink wikiLink) throws Exception {
		String context = parserInput.getContext();
		String virtualWiki = parserInput.getVirtualWiki();
		boolean thumb = false;
		boolean frame = false;
		String caption = null;
		String align = null;
		int maxDimension = -1;
		if (StringUtils.hasText(wikiLink.getText())) {
			StringTokenizer tokens = new StringTokenizer(wikiLink.getText(), "|");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if (!StringUtils.hasText(token)) continue;
				if (token.equalsIgnoreCase("noframe")) {
					frame = false;
				} else if (token.equalsIgnoreCase("frame")) {
					frame = true;
				} else if (token.equalsIgnoreCase("thumb")) {
					thumb = true;
				} else if (token.equalsIgnoreCase("right")) {
					align = "right";
				} else if (token.equalsIgnoreCase("left")) {
					align = "left";
				} else if (token.equalsIgnoreCase("center")) {
					align = "center";
				} else {
					Matcher m = IMAGE_SIZE_PATTERN.matcher(token);
					if (m.find()) {
						maxDimension = new Integer(m.group(1)).intValue();
					} else {
						// FIXME - this is a hack.  images may contain piped links, so if
						// there was previous caption info append the new info.
						if (!StringUtils.hasText(caption)) {
							caption = token;
						} else {
							caption += "|" + token;
						}
					}
				}
			}
			if (thumb && maxDimension <= 0) {
				maxDimension = DEFAULT_THUMBNAIL_SIZE;
			}
			caption = ParserUtil.parseFragment(parserInput, caption, mode);
		}
		// do not escape html for caption since parser does it above
		return LinkUtil.buildImageLinkHtml(context, virtualWiki, wikiLink.getDestination(), frame, thumb, align, caption, maxDimension, false, null, false);
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
	private String processLinkContent(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) {
		WikiLink wikiLink = this.parseWikiLink(raw);
		if (!StringUtils.hasText(wikiLink.getDestination()) && !StringUtils.hasText(wikiLink.getSection())) {
			// no destination or section
			return raw;
		}
		if (!wikiLink.getColon() && wikiLink.getNamespace() != null && wikiLink.getNamespace().equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
			// category tag, but not a category link
			return "";
		}
		return this.buildInternalLinkUrl(parserInput, parserDocument, mode, raw);
	}

	/**
	 *
	 */
	private void processLinkMetadata(ParserInput parserInput, ParserDocument parserDocument, String raw) {
		WikiLink wikiLink = this.parseWikiLink(raw);
		if (!StringUtils.hasText(wikiLink.getDestination()) && !StringUtils.hasText(wikiLink.getSection())) {
			return;
		}
		if (!wikiLink.getColon() && wikiLink.getNamespace() != null && wikiLink.getNamespace().equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
			parserDocument.addCategory(wikiLink.getDestination(), wikiLink.getText());
		}
		if (StringUtils.hasText(wikiLink.getDestination())) {
			parserDocument.addLink(wikiLink.getDestination());
		}
	}
}
