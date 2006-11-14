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
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import org.jamwiki.Environment;
import org.jamwiki.UserHandler;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class LdapUserHandler implements UserHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(LdapUserHandler.class.getName());

	private static String[] SEARCH_ATTRIBUTES = new String[3];

	static {
		SEARCH_ATTRIBUTES[0] = Environment.PROP_LDAP_FIELD_EMAIL;
		SEARCH_ATTRIBUTES[1] = Environment.PROP_LDAP_FIELD_FIRST_NAME;
		SEARCH_ATTRIBUTES[2] = Environment.PROP_LDAP_FIELD_LAST_NAME;
	}

	/**
	 *
	 */
	public boolean authenticate(String login, String password) {
		DirContext ctx = null;
		try {
			ctx = getContext(login, password);
			Attributes matchAttrs = new BasicAttributes(true);
			matchAttrs.put(new BasicAttribute(Environment.PROP_LDAP_FIELD_LOGIN, login));
			NamingEnumeration answer = ctx.search(Environment.PROP_LDAP_CONTEXT, matchAttrs, SEARCH_ATTRIBUTES);
			return true;
		} catch (Exception e) {
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
	 * @throws NamingException if an exception occured during connecting.
	 */
	private DirContext getContext(String username, String password) throws NamingException {
		// Set up the environment for creating the initial context
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, Environment.getValue(Environment.PROP_LDAP_FACTORY_CLASS));
		env.put(Context.PROVIDER_URL, Environment.getValue(Environment.PROP_LDAP_URL));
		// "simple" "DIGEST-MD5"
		env.put(Context.SECURITY_AUTHENTICATION, Environment.getValue(Environment.PROP_LDAP_SECURITY_AUTHENTICATION));
		// cn=login, ou=NewHires, o=JNDITutorial
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		DirContext ctx = new InitialDirContext(env);
		return ctx;
	}
}
