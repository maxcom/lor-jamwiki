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
package org.jamwiki.ldap;

import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import org.jamwiki.Environment;
import org.jamwiki.UserHandler;
import org.jamwiki.model.WikiUserInfo;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class LdapUserHandler implements UserHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(LdapUserHandler.class.getName());

	private static String[] SEARCH_ATTRIBUTES = new String[4];

	static {
		SEARCH_ATTRIBUTES[0] = Environment.getValue(Environment.PROP_LDAP_FIELD_EMAIL);
		SEARCH_ATTRIBUTES[1] = Environment.getValue(Environment.PROP_LDAP_FIELD_FIRST_NAME);
		SEARCH_ATTRIBUTES[2] = Environment.getValue(Environment.PROP_LDAP_FIELD_LAST_NAME);
		SEARCH_ATTRIBUTES[3] = Environment.getValue(Environment.PROP_LDAP_FIELD_USERID);
	}

	/**
	 *
	 */
	private void addUserContext(BasicAttributes matchAttrs) {
		String userContext = Environment.getValue(Environment.PROP_LDAP_USER_CONTEXT);
		if (!StringUtils.hasText(userContext)) return;
		StringTokenizer tokens = new StringTokenizer(userContext, ",");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			int pos = token.indexOf("=");
			if (pos != -1 && (pos + 1) < token.length()) {
				String key = token.substring(0, pos);
				String value = token.substring(pos + 1);
				matchAttrs.put(new BasicAttribute(key, value));
			}
		}
	}

	/**
	 *
	 */
	public void addWikiUserInfo(WikiUserInfo userInfo) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public boolean authenticate(String login, String password) throws Exception {
		InitialDirContext ctx = null;
		try {
			// convert login to full path
			login = Environment.getValue(Environment.PROP_LDAP_FIELD_USERID) + "=" + login;
			String context = Environment.getValue(Environment.PROP_LDAP_CONTEXT);
			if (StringUtils.hasText(context)) {
				// add context
				login += "," + context;
			}
			ctx = getContext(login, password);
			return true;
		} catch (Exception e) {
			// could not authenticate, return false
			return false;
		} finally {
			try {
				ctx.close();
			} catch (Exception e) {}
		}
	}

	/**
	 *
	 */
	public boolean canUpdate() {
		return false;
	}

	/**
	 * Connect to the LDAP server and return a context.
	 *
	 * @return The LDAP context to use when retrieving user information.
	 */
	private InitialDirContext getContext(String username, String password) throws Exception {
		// Set up the environment for creating the initial context
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, Environment.getValue(Environment.PROP_LDAP_FACTORY_CLASS));
		env.put(Context.PROVIDER_URL, Environment.getValue(Environment.PROP_LDAP_URL));
		if (StringUtils.hasText(username)) {
			// "simple" "DIGEST-MD5"
			env.put(Context.SECURITY_AUTHENTICATION, Environment.getValue(Environment.PROP_LDAP_SECURITY_AUTHENTICATION));
			// cn=login, ou=NewHires, o=JNDITutorial
			env.put(Context.SECURITY_PRINCIPAL, username);
			env.put(Context.SECURITY_CREDENTIALS, password);
		}
		InitialDirContext ctx = new InitialDirContext(env);
		return ctx;
	}

	/**
	 *
	 */
	private WikiUserInfo initWikiUserInfo(NamingEnumeration answer) throws Exception {
		WikiUserInfo userInfo = new WikiUserInfo();
		SearchResult sr = (SearchResult)answer.next();
		Attributes attributes = sr.getAttributes();
		userInfo.setEmail((String)attributes.get(Environment.getValue(Environment.PROP_LDAP_FIELD_EMAIL)).get());
		userInfo.setFirstName((String)attributes.get(Environment.getValue(Environment.PROP_LDAP_FIELD_FIRST_NAME)).get());
		userInfo.setLastName((String)attributes.get(Environment.getValue(Environment.PROP_LDAP_FIELD_LAST_NAME)).get());
		return userInfo;
	}

	/**
	 *
	 */
	public WikiUserInfo lookupWikiUserInfo(String login) throws Exception {
		InitialDirContext ctx = null;
		try {
			ctx = getContext(Environment.getValue(Environment.PROP_LDAP_LOGIN), Encryption.getEncryptedProperty(Environment.PROP_LDAP_PASSWORD, null));
			BasicAttributes matchAttrs = new BasicAttributes(true);
			matchAttrs.put(new BasicAttribute(Environment.PROP_LDAP_FIELD_USERID, login));
			this.addUserContext(matchAttrs);
			NamingEnumeration answer = ctx.search(Environment.PROP_LDAP_CONTEXT, matchAttrs, SEARCH_ATTRIBUTES);
			return this.initWikiUserInfo(answer);
		} finally {
			try {
				ctx.close();
			} catch (Exception e) {}
		}
	}

	/**
	 *
	 */
	public void updateWikiUserInfo(WikiUserInfo userInfo) throws Exception {
		throw new UnsupportedOperationException();
	}
}
