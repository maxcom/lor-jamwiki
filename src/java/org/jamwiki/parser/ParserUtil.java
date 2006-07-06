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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.parser;

import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;

/**
 * Utility methods used with the Mediawiki lexers.
 */
public class ParserUtil {

	private static Logger logger = Logger.getLogger(ParserUtil.class.getName());

	/**
	 * Given a String that represents a Wiki HTML link (a URL with an optional
	 * link text that is enclosed in brackets), return a formatted HTML anchor tag.
	 *
	 * @param raw The raw Wiki syntax that is to be converted into an HTML link.
	 * @return A formatted HTML link for the Wiki syntax.
	 */
	protected static String buildHtmlLink(String raw) {
		if (raw == null || raw.length() <= 2) {
			// no link, display the raw text
			return raw;
		}
		// strip the first and last brackets
		String link = raw.substring(1, raw.length() - 1).trim();
		return buildHtmlLinkRaw(link);
	}

	/**
	 * Given a String that represents a raw HTML link (a URL link that is
	 * not enclosed in brackets), return a formatted HTML anchor tag.
	 *
	 * @param raw The raw HTML link that is to be converted into an HTML link.
	 * @return A formatted HTML link.
	 */
	protected static String buildHtmlLinkRaw(String raw) {
		if (raw == null) return raw;
		String link = raw.trim();
		if (link.length() <= 0) {
			// no link to display
			return raw;
		}
		// search for link text (space followed by text)
		String punctuation = Utilities.extractTrailingPunctuation(link);
		String text = "";
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
			text = link;
		}
		String html = linkHtml(link, text, punctuation);
		return (html != null) ? html : raw;
	}

	/**
	 *
	 */
	protected static String buildWikiLink(String context, String virtualWiki, String raw) {
		try {
			if (raw == null || raw.length() <= 4) {
				// no topic, display the raw text
				return raw;
			}
			// strip the first and last brackets
			String topic = raw.substring(2, raw.length() - 2).trim();
			if (topic.length() <= 0) {
				// empty brackets, no topic to display
				return raw;
			}
			// search for topic text ("|" followed by text)
			String text = topic.trim();
			int pos = topic.indexOf('|');
			if (pos > 0) {
				text = topic.substring(pos+1).trim();
				topic = topic.substring(0, pos).trim();
			}
			String url = Utilities.buildWikiLink(context, virtualWiki, topic);
			String css = "";
			// FIXME - exists triggers a database call, bad for performance
			if (!WikiBase.exists(virtualWiki, topic)) {
				css = " class=\"edit\"";
			}
			return "<a title=\"" + text + "\" href=\"" + url + "\"" + css + ">" + text + "</a>";
		} catch (Exception e) {
			logger.error("Failure while parsing link " + raw);
			return "";
		}
	}

	/**
	 *
	 */
	public static String escapeHtml(String html) {
		if (html == null) return html;
		StringBuffer escaped = new StringBuffer();
		for (int i=0; i < html.length(); i++) {
			if (html.charAt(i) == '<') {
				escaped.append("&lt;");
			} else if (html.charAt(i) == '>') {
				escaped.append("&gt;");
			} else {
				escaped.append(html.charAt(i));
			}
		}
		return escaped.toString();
	}

	/**
	 *
	 */
	protected static String linkHtml(String link, String text, String punctuation) {
		String html = null;
		String linkLower = link.toLowerCase();
		if (linkLower.startsWith("http://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if  (linkLower.startsWith("https://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("ftp://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("mailto://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("news://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("telnet://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		} else if (linkLower.startsWith("file://")) {
			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
				 + link + "\" href=\"" + link + "\">" + text + "</a>"
				 + punctuation;
		}
		return html;
	}
}
