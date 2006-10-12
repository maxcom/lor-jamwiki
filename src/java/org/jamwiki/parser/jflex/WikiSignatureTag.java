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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class WikiSignatureTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(WikiSignatureTag.class.getName());

	/**
	 *
	 */
	private String buildWikiSignature(ParserInput parserInput, ParserDocument parserDocument, int mode, boolean includeUser, boolean includeDate) {
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
				// parse signature as link in order to store link metadata
				WikiLinkTag wikiLinkTag = new WikiLinkTag();
				wikiLinkTag.parse(parserInput, parserDocument, mode, signature);
				if (mode != JFlexParser.MODE_SAVE) {
					signature = ParserUtil.parseFragment(parserInput, signature, mode);
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
	 * Parse a Mediawiki signature of the form "~~~~" and return the resulting
	 * HTML output.
	 */
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		if (raw.equals("~~~")) {
			return this.buildWikiSignature(parserInput, parserDocument, mode, true, false);
		} else if (raw.equals("~~~~")) {
			return this.buildWikiSignature(parserInput, parserDocument, mode, true, true);
		} else if (raw.equals("~~~~~")) {
			return this.buildWikiSignature(parserInput, parserDocument, mode, false, true);
		}
		return raw;
	}
}
