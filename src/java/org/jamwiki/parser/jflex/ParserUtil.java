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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * Utility methods used with the Mediawiki lexers.
 */
public class ParserUtil {

	private static WikiLogger logger = WikiLogger.getLogger(ParserUtil.class.getName());
	private static Pattern TAG_PATTERN = null;
	private static Pattern JAVASCRIPT_PATTERN1 = null;
	private static Pattern JAVASCRIPT_PATTERN2 = null;
	private static Pattern IMAGE_SIZE_PATTERN = null;
	// FIXME - make configurable
	private static final int DEFAULT_THUMBNAIL_SIZE = 180;

	static {
		try {
			TAG_PATTERN = Pattern.compile("<[ ]*([^\\ />]+)([ ]*(.*?))([/]?[ ]*>)");
			// catch script insertions of the form "onsubmit="
			JAVASCRIPT_PATTERN1 = Pattern.compile("( on[^=]{3,}=)+", Pattern.CASE_INSENSITIVE);
			// catch script insertions that use a javascript url
			JAVASCRIPT_PATTERN2 = Pattern.compile("(javascript[ ]*\\:)+", Pattern.CASE_INSENSITIVE);
			// look for image size info in image tags
			IMAGE_SIZE_PATTERN = Pattern.compile("([0-9]+)[ ]*px", Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 *
	 */
	protected static String buildSectionEditLink(ParserInput parserInput, int section) {
		if (!parserInput.getAllowSectionEdit()) return "";
		String output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[";
		String url = "";
		try {
			url = LinkUtil.buildEditLinkUrl(parserInput.getContext(), parserInput.getVirtualWiki(), parserInput.getTopicName(), null, section);
		} catch (Exception e) {
			logger.severe("Failure while building link for topic " + parserInput.getVirtualWiki() + " / " + parserInput.getTopicName(), e);
		}
		output += "<a href=\"" + url + "\">";
		output += Utilities.formatMessage("common.sectionedit", parserInput.getLocale());
		output += "</a>]</div>";
		return output;
	}

	/**
	 *
	 */
	public static String buildWikiSignature(ParserInput parserInput, boolean includeUser, boolean includeDate, ParserMode mode) {
		try {
			String signature = "";
			if (includeUser) {
				String context = parserInput.getContext();
				String virtualWiki = parserInput.getVirtualWiki();
				String login = parserInput.getUserIpAddress();
				String email = parserInput.getUserIpAddress();
				String displayName = parserInput.getUserIpAddress();
				String userId = "-1";
				if (parserInput.getWikiUser() != null) {
					WikiUser user = parserInput.getWikiUser();
					login = user.getLogin();
					displayName = (user.getDisplayName() != null) ? user.getDisplayName() : user.getLogin();
					email = user.getEmail();
					userId = new Integer(user.getUserId()).toString();
				}
				String text = parserInput.getUserIpAddress();
				MessageFormat formatter = new MessageFormat(Environment.getValue(Environment.PROP_PARSER_SIGNATURE_USER_PATTERN));
				Object params[] = new Object[7];
				params[0] = WikiBase.NAMESPACE_USER + WikiBase.NAMESPACE_SEPARATOR + login;
				// FIXME - hard coding
				params[1] = WikiBase.NAMESPACE_SPECIAL + WikiBase.NAMESPACE_SEPARATOR + "Contributions?contributor=" + login;
				params[2] = WikiBase.NAMESPACE_USER_COMMENTS + WikiBase.NAMESPACE_SEPARATOR + login;
				params[3] = login;
				params[4] = displayName;
				params[5] = email;
				params[6] = userId;
				signature = formatter.format(params);
				if (!mode.hasMode(ParserMode.MODE_SAVE)) {
					signature = ParserUtil.parseFragment(parserInput, signature, mode.getMode());
				}
			}
			if (includeUser && includeDate) {
				signature += " ";
			}
			if (includeDate) {
				SimpleDateFormat format = new SimpleDateFormat();
				format.applyPattern(Environment.getValue(Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN));
				signature += format.format(new java.util.Date());
			}
			return signature;
		} catch (Exception e) {
			logger.severe("Failure while building wiki signature", e);
			// FIXME - return empty or a failure indicator?
			return "";
		}
	}

	/**
	 *
	 */
	public static String parseImageLink(ParserInput parserInput, WikiLink wikiLink) throws Exception {
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
			caption = ParserUtil.parseFragment(parserInput, caption, ParserMode.MODE_NORMAL);
		}
		// do not escape html for caption since parser does it above
		return LinkUtil.buildImageLinkHtml(context, virtualWiki, wikiLink.getDestination(), frame, thumb, align, caption, maxDimension, false, null, false);
	}

	/**
	 * Provide a way to run the pre-processor against a fragment of text, such
	 * as an image caption.  This method should be used sparingly since it is
	 * not very efficient.
	 */
	protected static String parseFragment(ParserInput parserInput, String fragment, int mode) throws Exception {
		// FIXME - consider yypushstream() and yypopstream() as potentially more efficient
		// ways to handle this functionality
		if (!StringUtils.hasText(fragment)) return fragment;
		JFlexParser parser = new JFlexParser(parserInput);
		StringReader raw = new StringReader(fragment);
		ParserMode parserMode = new ParserMode(mode);
		ParserOutput parserOutput = parser.parsePreProcess(raw, parserMode);
		return parserOutput.getContent();
	}

	/**
	 * Clean up HTML tags to make them XHTML compliant (lowercase, no
	 * unnecessary spaces).
	 */
	protected static String sanitizeHtmlTag(String tag) {
		tag = StringUtils.deleteAny(tag, " ").toLowerCase();
		if (tag.endsWith("/>")) {
			// spaces were stripped, so make sure tag is of the form "<br />"
			tag = tag.substring(0, tag.length() - 2) + " />";
		}
		return tag;
	}

	/**
	 * Strip Wiki markup from text
	 */
	protected static String stripMarkup(String text) {
		// FIXME - this could be a bit more thorough and also strip HTML
		text = StringUtils.delete(text, "'''");
		text = StringUtils.delete(text, "''");
		text = StringUtils.delete(text, "[[");
		text = StringUtils.delete(text, "]]");
		return text;
	}

	/**
	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String validateHtmlTag(String tag) {
		Matcher m = TAG_PATTERN.matcher(tag);
		if (!m.find()) {
			logger.severe("Failure while attempting to match html tag for pattern " + tag);
			return tag;
		}
		String tagOpen = m.group(1);
		String attributes = m.group(2);
		String tagClose = m.group(4);
		attributes = ParserUtil.validateHtmlTagAttributes(attributes);
		tag = "<" + tagOpen.toLowerCase().trim();
		tag += attributes;
		if (!attributes.endsWith(" ")) tag += " ";
		if (tagClose.indexOf("/") != -1) {
			tagClose = "/>";
		}
		tag += tagClose.trim();
		return tag;
	}

	/**
	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String validateHtmlTagAttributes(String attributes) {
		if (!StringUtils.hasText(attributes)) return attributes;
		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT)) {
			// FIXME - can these two patterns be combined into one?
			// pattern requires a space prior to the "onFoo", so make sure one exists
			Matcher m = JAVASCRIPT_PATTERN1.matcher(" " + attributes);
			if (m.find()) {
				logger.warning("Attempt to include Javascript in Wiki syntax " + attributes);
				return "";
			}
			m = JAVASCRIPT_PATTERN2.matcher(attributes);
			if (m.find()) {
				logger.warning("Attempt to include Javascript in Wiki syntax " + attributes);
				return "";
			}
		}
		return attributes;
	}
}
