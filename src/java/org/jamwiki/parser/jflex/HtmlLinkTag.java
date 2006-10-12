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

import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class HtmlLinkTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(HtmlLinkTag.class.getName());

	/**
	 * Given a String that represents a Wiki HTML link (a URL with an optional
	 * link text that is enclosed in brackets), return a formatted HTML anchor tag.
	 *
	 * @param raw The raw Wiki syntax that is to be converted into an HTML link.
	 * @return A formatted HTML link for the Wiki syntax.
	 */
	private String buildHtmlLink(String raw) {
		if (raw.length() <= 2) {
			// no link, display the raw text
			return raw;
		}
		// strip the first and last brackets
		String link = raw.substring(1, raw.length() - 1).trim();
		return this.buildHtmlLinkRaw(link);
	}

	/**
	 * Given a String that represents a raw HTML link (a URL link that is
	 * not enclosed in brackets), return a formatted HTML anchor tag.
	 *
	 * @param raw The raw HTML link that is to be converted into an HTML link.
	 * @return A formatted HTML link.
	 */
	private String buildHtmlLinkRaw(String raw) {
		String link = raw.trim();
		// search for link text (space followed by text)
		String punctuation = Utilities.extractTrailingPunctuation(link);
		String text = null;
		int pos = link.indexOf(' ');
		if (pos == -1) {
			pos = link.indexOf('\t');
		}
		if (pos > 0) {
			text = link.substring(pos+1).trim();
			link = link.substring(0, pos).trim();
			punctuation = "";
		} else {
			link = link.substring(0, link.length() - punctuation.length()).trim();
		}
		String html = this.linkHtml(link, text, punctuation);
		return (html != null) ? html : raw;
	}

	/**
	 *
	 */
	private String linkHtml(String link, String text, String punctuation) {
		String html = null;
		// in case of script attack, replace script tags (cannot use escapeHTML due
		// to the possibility of ampersands in the link)
		link = StringUtils.replace(link, "<", "&lt;");
		link = StringUtils.replace(link, ">", "&gt;");
		link = StringUtils.replace(link, "\"", "&quot;");
		link = StringUtils.replace(link, "'", "&#39;");
		String linkLower = link.toLowerCase();
		if (linkLower.startsWith("mailto://")) {
			// fix bad mailto syntax
			link = "mailto:" + link.substring("mailto://".length());
		}
		if (!StringUtils.hasText(text)) {
			text = link;
		}
		text = Utilities.escapeHTML(text);
		if (linkLower.startsWith("http://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if  (linkLower.startsWith("https://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("ftp://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("mailto:")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("news://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("telnet://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("file://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + text + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		}
		return html;
	}

	/**
	 * Parse a Mediawiki HTML link of the form "[http://www.site.com/ text]" or
	 * "http://www.site.com/" and return the resulting HTML output.
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) throws Exception {
		if (raw == null || !StringUtils.hasText(raw)) {
			// no link to display
			return raw;
		}
		if (raw.startsWith("[") && raw.endsWith("]")) {
			return this.buildHtmlLink(raw);
		} else {
			return this.buildHtmlLinkRaw(raw);
		}
	}
}
