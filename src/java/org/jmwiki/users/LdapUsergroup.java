/*
 *
 */
package org.jmwiki.users;

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
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.utils.Encryption;

/**
 * Use an LDAP server as usergroup.
 */
public class LdapUsergroup extends Usergroup {

	private static final Logger logger = Logger.getLogger(LdapUsergroup.class);

	/**
	 * Get the email address of an user by its user-ID
	 * @param uid The user-ID of this user
	 * @return The email address of this user
	 * @see jmwiki.users.Usergroup#getKnownEmailById(java.lang.String)
	 */
	public String getKnownEmailById(String id) {
		if (id == null) {
			return null;
		}
		return getAttributeByID(id, Environment.getValue(Environment.PROP_USERGROUP_MAIL_FIELD));
	}

	/**
	 * Get the full name of an user by its user-ID
	 * @param uid The user-ID of this user
	 * @return The full name of this user
	 * @see jmwiki.users.Usergroup#getFullnameById(java.lang.String)
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
	 * @return Instance to the user group class
	 * @see jmwiki.users.Usergroup#getInstance()
	 */
	public static Usergroup getInstance() {
		LdapUsergroup lug = new LdapUsergroup();
		return lug;
	}

	/**
	 * Get a list of all users.
	 * @return List of all users. The list contains SelectorBeans with the user-ID as key and the full
	 * username as label.
	 * @see jmwiki.servlets.beans.SelectorBean
	 * @see jmwiki.users.Usergroup#getListOfAllUsers()
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
					logger.error("Cannot add a member", e);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot create member list", e);
		}
		logger.debug("List created with " + list.size()+ " entries");
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
	 * Get the user details of this user by its user-ID. The user details is a string, which is
	 * set in the admin section. It contains some placeholders, which are replaced by values from
	 * the user repository
	 * @param uid The user-ID of this user
	 * @return The user details section
	 * @see jmwiki.users.Usergroup#getUserDetails(java.lang.String)
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
	 * Contains the repository valid (already confirmed) email addresses?
	 * If yes, then we can skip the registration process and the user is automatically registered.
	 * @return true, if so. false otherwise.
	 * @see jmwiki.users.Usergroup#isEmailValidated()
	 */
	public boolean isEmailValidated() {
		return true;
	}

	/**
	 * Get an (LDAP) Attribute by its name
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
	 * Get all Attributes of a user
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
	 * Connect to the LDAP server and return a context
	 * @return The Context to work with
	 * @throws NamingException if an exception occured during connecting
	 */
	private DirContext connect() throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, Environment.getValue(Environment.PROP_USERGROUP_FACTORY));
		env.put(Context.PROVIDER_URL, Environment.getValue(Environment.PROP_USERGROUP_URL));
		String username = Environment.getValue(Environment.PROP_USERGROUP_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_USERGROUP_PASSWORD);
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
