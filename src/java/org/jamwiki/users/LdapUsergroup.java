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
package org.jamwiki.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
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
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.Encryption;

/**
 * Use an LDAP server as usergroup.
 */
public class LdapUsergroup extends Usergroup {

	private static final WikiLogger logger = WikiLogger.getLogger(LdapUsergroup.class.getName());

	/**
	 * Get the email address of an user by its user-ID.
	 *
	 * @param id The user-ID of this user
	 * @return The email address of this user
	 */
	public String getKnownEmailById(String id) {
		if (id == null) {
			return null;
		}
		return getAttributeByID(id, Environment.getValue(Environment.PROP_USERGROUP_MAIL_FIELD));
	}

	/**
	 * Get the full name of an user by its user-ID.
	 *
	 * @param id The user-ID of this user
	 * @return The full name of this user
	 */
	public String getFullnameById(String id) {
		if (id == null) {
			return null;
		}
		String returnValue = getAttributeByID(id, Environment.getValue(Environment.PROP_USERGROUP_FULLNAME_FIELD));
		if (returnValue == null || "".equals(returnValue)) {
			returnValue = id;
		}
		return returnValue;
	}

	/**
	 * Get an instance of the user group class.
	 *
	 * @return Instance to the user group class
	 */
	public static Usergroup getInstance() {
		LdapUsergroup lug = new LdapUsergroup();
		return lug;
	}

	/**
	 * Get a list of all users.
	 *
	 * @return List of all users. The list contains SelectorBeans with the
	 *  user-ID as key and the full username as label.
	 */
	public List getListOfAllUsers() {
		List list = new ArrayList();
		String userIdField = Environment.getValue(Environment.PROP_USERGROUP_USERID_FIELD);
		String fullnameField = Environment.getValue(Environment.PROP_USERGROUP_FULLNAME_FIELD);
		try {
			DirContext ctx = connect();
			String searchString = Environment.getValue(Environment.PROP_USERGROUP_BASIC_SEARCH);
			Attributes matchAttrs = new BasicAttributes(true);
			fillSearchRestrictions(matchAttrs);
			NamingEnumeration answer = ctx.search(searchString,matchAttrs);
			for (; answer.hasMoreElements();) {
				try {
					SearchResult searchResult = (SearchResult)answer.nextElement();
					Attributes sr = searchResult.getAttributes();
					SelectorBean bean = new SelectorBean();
					bean.setKey(sr.get(userIdField).get().toString());
					bean.setLabel(sr.get(fullnameField).get().toString());
					list.add(bean);
				} catch (Exception e) {
					logger.severe("Cannot add a member", e);
				}
			}
		} catch (Exception e) {
			logger.severe("Cannot create member list", e);
		}
		logger.fine("List created with " + list.size()+ " entries");
		Collections.sort(list,new Comparator() {
			public int compare(Object o1, Object o2) {
				SelectorBean bean1 = (SelectorBean) o1;
				SelectorBean bean2 = (SelectorBean) o2;
				return bean1.getLabel().compareToIgnoreCase(bean2.getLabel());
			}
		});
		return list;
	}

	/**
	 * Get the user details of this user by its user-ID. The user details
	 * is a string, which is set in the admin section. It contains some
	 * placeholders, which are replaced by values from the user repository.
	 *
	 * @param id The user-ID of this user
	 * @return The user details section
	 */
	public String getUserDetails(String id) {
		if (id == null) {
			return null;
		}
		String details = Environment.getValue(Environment.PROP_USERGROUP_DETAILVIEW);
		// get attributes from user
		Attributes searchAttributes;
		try {
			DirContext ctx = connect();
			searchAttributes = getAttributesOfUser(id, ctx);
			if (searchAttributes == null) return "Cannot find user";
		} catch (NamingException ne) {
			return "Cannot find user";
		}
		// go through text for all the attrib
		String searchChar = "@@";
		int beginning = details.indexOf(searchChar);
		int end;
		int lastCopied = 0;
		StringBuffer result = new StringBuffer();
		while (beginning != -1) {
			end = details.indexOf(searchChar, beginning+1);
			if (end != -1) {
				String token = details.substring(beginning + searchChar.length(), end);
				String replacement;
				try {
					replacement = searchAttributes.get(token).get().toString();
				} catch (NamingException ne) {
					replacement = "";
				} catch (NullPointerException ne) {
					replacement = "";
				}
				result.append(details.substring(lastCopied, beginning));
				result.append(replacement);
				lastCopied = end + searchChar.length();
				beginning = details.indexOf(searchChar, end + 1);
			} else {
				beginning = -1;
			}
		}
		result.append(details.substring(lastCopied));
		return result.toString();
	}

	/**
	 * If the repository contains confirmed validated email addresses then
	 * return <code>true</code>.  This method allows the registration process
	 * to skip email validation when addresses are known to be valid.
	 *
	 * @return <code>true</code> if the repository contains validated email
	 *  addresses, otherwise returns <code>false</code>.
	 */
	public boolean isEmailValidated() {
		return true;
	}

	/**
	 * Get an (LDAP) Attribute by its name.
	 *
	 * @param id  The uid (user-ID)
	 * @param attribute The name of the attribtue to get
	 * @return The content of that attribute (as string) or an empty string.
	 */
	private String getAttributeByID(String id, String attribute) {
		if (id == null) {
			return null;
		}
		Attributes searchAttributes = null;
		try {
			DirContext ctx = connect();
			searchAttributes = getAttributesOfUser(id, ctx);
			return searchAttributes.get(attribute).get().toString();
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Get all Attributes of a user.
	 *
	 * @param id The uid (user-ID)
	 * @param ctx The LDAP context
	 * @return The attributes of that user.
	 * @throws NamingException If an exception occured while asking for the attribtues
	 */
	private Attributes getAttributesOfUser(String id, DirContext ctx) throws NamingException {
		if (id == null) {
			return null;
		}
		Attributes searchAttributes;
		String searchString = Environment.getValue(Environment.PROP_USERGROUP_BASIC_SEARCH);
		Attributes matchAttrs = new BasicAttributes(true);
		fillSearchRestrictions(matchAttrs);
		matchAttrs.put(new BasicAttribute(Environment.getValue(Environment.PROP_USERGROUP_USERID_FIELD), id));
		NamingEnumeration answer = ctx.search(searchString,matchAttrs);
		SearchResult searchResult = (SearchResult)answer.nextElement();
		searchAttributes = searchResult.getAttributes();
		return searchAttributes;
	}

	/**
	 * Connect to the LDAP server and return a context.
	 *
	 * @return The Context to work with
	 * @throws NamingException if an exception occured during connecting
	 */
	private DirContext connect() throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, Environment.getValue(Environment.PROP_USERGROUP_FACTORY));
		env.put(Context.PROVIDER_URL, Environment.getValue(Environment.PROP_USERGROUP_URL));
		String username = Environment.getValue(Environment.PROP_USERGROUP_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_USERGROUP_PASSWORD, null);
		if (username != null && !"".equals(username)) {
			env.put(Context.SECURITY_AUTHENTICATION,"simple");
			env.put(Context.SECURITY_PRINCIPAL,username); // specify the username
			env.put(Context.SECURITY_CREDENTIALS,password);		   // specify the password
		}
		DirContext ctx = new InitialDirContext(env);
		return ctx;
	}

	/**
	 * If there are any restrictions to the search, they are filled in here.
	 *
	 * @param matchAttrs the list of attributes containing the search restrictions.
	 */
	private void fillSearchRestrictions(Attributes matchAttrs) {
		String searchRestrictions = Environment.getValue(Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS);
		StringTokenizer commaTokenizer = new StringTokenizer(searchRestrictions, ",");
		for (; commaTokenizer.hasMoreTokens();) {
			String token = commaTokenizer.nextToken();
			int ePos = token.indexOf("=");
			if (ePos != -1) {
				String key = token.substring(0, ePos);
				String value = token.substring(ePos + 1);
				matchAttrs.put(new BasicAttribute(key, value));
			}
		}
	}
}
