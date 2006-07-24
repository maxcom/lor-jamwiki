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
package org.jamwiki.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiFile;
import org.springframework.util.StringUtils;

/**
 *
 */
public class LinkUtil {

	private static final Logger logger = Logger.getLogger(LinkUtil.class);

	/**
	 *
	 */
	public static String buildImageLink(String context, String virtualWiki, String topicName) throws Exception {
		return LinkUtil.buildImageLink(context, virtualWiki, topicName, false, false, null, null, true);
	}

	/**
	 *
	 */
	public static String buildImageLink(String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, boolean suppressLink) throws Exception {
		WikiFile wikiFile = WikiBase.getHandler().lookupWikiFile(virtualWiki, topicName);
		if (wikiFile == null) {
			// doesn't exist, return topic name as text IF it's an image
			return (topicName.startsWith(WikiBase.NAMESPACE_IMAGE)) ? topicName : "";
		}
		String html = "";
		if (!suppressLink) html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildWikiLink(context, virtualWiki, topicName) + "\">";
		if (frame || thumb || StringUtils.hasText(align) || StringUtils.hasText(caption)) {
			html += "<div ";
			if (thumb) {
				html += "class=\"imgthumb\"";
			} else if (align != null && align.equalsIgnoreCase("right")) {
				html += "class=\"imgright\"";
			} else if (align != null && align.equalsIgnoreCase("left")) {
				html += "class=\"imgleft\"";
			} else if (frame) {
				html += "class=\"imgleft\"";
			}
			html += "\">";
		}
		html += "<img class=\"wikiimg\" src=\"";
		if (!Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH).startsWith("/")) html += "/";
		html += Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH);
		String url = wikiFile.getUrl();
		if (!html.endsWith("/") && !url.startsWith("/")) {
			url = "/" + url;
		} else if (html.endsWith("/") && url.startsWith("/")) {
			url = url.substring(1);
		}
		html += url;
		html += "\" />";
		if (frame || thumb || StringUtils.hasText(align) || StringUtils.hasText(caption)) {
			if (StringUtils.hasText(caption)) {
				html += "<div class=\"imgcaption\">" + caption + "</div>";
			}
			html += "</div>";
		}
		if (!suppressLink) html += "</a>";
		return html;
	}

	/**
	 *
	 */
	public static String buildInternalLink(String context, String virtualWiki, String page) {
		return buildInternalLink(context, virtualWiki, page, null, null);
	}

	/**
	 *
	 */
	public static String buildInternalLink(String context, String virtualWiki, String page, String section) {
		return buildInternalLink(context, virtualWiki, page, section, null);
	}

	/**
	 *
	 */
	public static String buildInternalLink(String context, String virtualWiki, String page, String section, String query) {
		String url = context;
		// context never ends with a "/" per servlet specification
		url += "/";
		// get the virtual wiki, which should have been set by the parent servlet
		url += Utilities.encodeURL(virtualWiki);
		url += "/";
		url += Utilities.encodeURL(page);
		if (StringUtils.hasText(section)) {
			if (section.startsWith("#")) {
				section = section.substring(1);
			}
			url += "#" + Utilities.encodeURL(section);
		}
		if (StringUtils.hasText(query)) {
			url += query;
		}
		return url;
	}

	/**
	 *
	 */
	public static String buildWikiLink(String context, String virtualWiki, String topic) throws Exception {
		if (!StringUtils.hasText(topic)) {
			return null;
		}
		// search for hash mark
		String section = "";
		String query = "";
		int pos = topic.indexOf('?');
		if (pos > 0) {
			query = topic.substring(pos).trim();
			topic = topic.substring(0, pos).trim();
		}
		pos = topic.indexOf('#');
		if (pos > 0) {
			section = topic.substring(pos+1).trim();
			topic = topic.substring(0, pos).trim();
		}
		String url = LinkUtil.buildInternalLink(context, virtualWiki, topic, section, query);
		if (!WikiBase.exists(virtualWiki, topic)) {
			url = LinkUtil.buildWikiEditLink(context, virtualWiki, topic);
		}
		return url;
	}

	/**
	 *
	 */
	public static String buildWikiEditLink(String context, String virtualWiki, String topic) {
		return LinkUtil.buildWikiEditLink(context, virtualWiki, topic, -1);
	}

	/**
	 *
	 */
	public static String buildWikiEditLink(String context, String virtualWiki, String topic, int section) {
		String url = LinkUtil.buildInternalLink(context, virtualWiki, "Special:Edit");
		url += "?topic=" + Utilities.encodeURL(topic);
		if (section > 0) {
			url += "&section=" + section;
		}
		return url;
	}
}
