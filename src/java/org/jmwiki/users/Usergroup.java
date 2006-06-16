/*
 * $Id: Usergroup.java 636 2006-04-20 00:14:40Z wrh2 $
 *
 * Filename  : Usergroup.java
 * Created   : 25.06.2004
 * Project   : VQWiki
 */
package org.jmwiki.users;

import java.util.List;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;

/**
 * Abstract class to handle user groups
 *
 * @version $Revision: 636 $ - $Date: 2006-04-20 02:14:40 +0200 (do, 20 apr 2006) $
 * @author SinnerSchrader (tobsch)
 */
public abstract class Usergroup {

	/**
	 * Get an instance of the user group class.
	 * @return Instance to the user group class
	 */
	public static Usergroup getInstance() {
		return null;
	}

	/**
	 * Get a list of all users.
	 * @return List of all users. The list contains SelectorBeans with the user-ID as key and the full
	 * username as label.
	 * @see jmwiki.servlets.beans.SelectorBean
	 */
	public abstract List getListOfAllUsers();

	/**
	 * Get the full name of an user by its user-ID
	 * @param uid The user-ID of this user
	 * @return The full name of this user
	 */
	public abstract String getFullnameById(String uid);

	/**
	 * Get the email address of an user by its user-ID
	 * @param uid The user-ID of this user
	 * @return The email address of this user
	 */
	public abstract String getKnownEmailById(String user);

	/**
	 * Get the user details of this user by its user-ID. The user details is a string, which is
	 * set in the admin section. It contains some placeholders, which are replaced by values from
	 * the user repository
	 * @param uid The user-ID of this user
	 * @return The user details section
	 */
	public abstract String getUserDetails(String uid);

	/**
	 * Contains the repository valid (already confirmed) email addresses?
	 * If yes, then we can skip the registration process and the user is automatically registered.
	 * @return true, if so. false otherwise.
	 */
	public boolean isEmailValidated() {
		return false;
	}

	/**
	 * @return Returns the usergroupType.
	 */
	public static int getUsergroupType() {
		String persistenceType = Environment.getValue(Environment.PROP_USERGROUP_TYPE);
		if (persistenceType.equals("LDAP")) {
			return WikiBase.LDAP;
		} else if (persistenceType.equals("DATABASE")) {
			return WikiBase.DATABASE;
		} else {
			return 0;
		}
	}

	/**
	 * @param membershipType The usergroupType to set.
	 */
	public static void setUsergroupType(int membershipType) {
		String usergroupType;
		if (membershipType == WikiBase.LDAP) {
			usergroupType = "LDAP";
		} else if (membershipType == WikiBase.DATABASE) {
			usergroupType = "DATABASE";
		} else {
			usergroupType = "";
		}
		Environment.setValue(Environment.PROP_USERGROUP_TYPE, usergroupType);
	}
}
