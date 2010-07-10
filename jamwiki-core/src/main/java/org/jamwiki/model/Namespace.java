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
package org.jamwiki.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;

/**
 * Namespaces allow the organization of wiki topics by dividing topics into
 * groups.  A namespace will precede the topic, such as "Namespace:Topic".
 * Namespaces can be customized by modifying using configuration tools, but
 * the namesapces defined as constants always exist and are required for wiki
 * operation.
 */
public class Namespace implements Serializable {

	public static final String SEPARATOR = ":";
	// IDs must match Mediawiki - see http://www.mediawiki.org/wiki/Help:Namespaces
	public static final int MEDIA_ID                = -2;
	public static final int SPECIAL_ID              = -1;
	public static final int MAIN_ID                 = 0;
	public static final int COMMENTS_ID             = 1;
	public static final int USER_ID                 = 2;
	public static final int USER_COMMENTS_ID        = 3;
	public static final int SITE_CUSTOM_ID          = 4;
	public static final int SITE_CUSTOM_COMMENTS_ID = 5;
	public static final int FILE_ID                 = 6;
	public static final int FILE_COMMENTS_ID        = 7;
	public static final int JAMWIKI_ID              = 8;
	public static final int JAMWIKI_COMMENTS_ID     = 9;
	public static final int TEMPLATE_ID             = 10;
	public static final int TEMPLATE_COMMENTS_ID    = 11;
	public static final int HELP_ID                 = 12;
	public static final int HELP_COMMENTS_ID        = 13;
	public static final int CATEGORY_ID             = 14;
	public static final int CATEGORY_COMMENTS_ID    = 15;
	// default namespaces, used during setup.  additional namespaces may be added after setup.
	// namespace IDs should match Mediawiki to maximize compatibility.
	private static final Namespace MEDIA                = new Namespace(MEDIA_ID, "Media", null);
	private static final Namespace SPECIAL              = new Namespace(SPECIAL_ID, "Special", null);
	private static final Namespace MAIN                 = new Namespace(MAIN_ID, "", null);
	private static final Namespace COMMENTS             = new Namespace(COMMENTS_ID, "Comments", Namespace.MAIN);
	private static final Namespace USER                 = new Namespace(USER_ID, "User", null);
	private static final Namespace USER_COMMENTS        = new Namespace(USER_COMMENTS_ID, "User comments", Namespace.USER);
	private static final Namespace SITE_CUSTOM          = new Namespace(SITE_CUSTOM_ID, "Project", null);
	private static final Namespace SITE_CUSTOM_COMMENTS = new Namespace(SITE_CUSTOM_COMMENTS_ID, "Project comments", Namespace.SITE_CUSTOM);
	private static final Namespace FILE                 = new Namespace(FILE_ID, "Image", null);
	private static final Namespace FILE_COMMENTS        = new Namespace(FILE_COMMENTS_ID, "Image comments", Namespace.FILE);
	private static final Namespace JAMWIKI              = new Namespace(JAMWIKI_ID, "JAMWiki", null);
	private static final Namespace JAMWIKI_COMMENTS     = new Namespace(JAMWIKI_COMMENTS_ID, "JAMWiki comments", Namespace.JAMWIKI);
	private static final Namespace TEMPLATE             = new Namespace(TEMPLATE_ID, "Template", null);
	private static final Namespace TEMPLATE_COMMENTS    = new Namespace(TEMPLATE_COMMENTS_ID, "Template comments", Namespace.TEMPLATE);
	private static final Namespace HELP                 = new Namespace(HELP_ID, "Help", null);
	private static final Namespace HELP_COMMENTS        = new Namespace(HELP_COMMENTS_ID, "Help comments", Namespace.HELP);
	private static final Namespace CATEGORY             = new Namespace(CATEGORY_ID, "Category", null);
	private static final Namespace CATEGORY_COMMENTS    = new Namespace(CATEGORY_COMMENTS_ID, "Category comments", Namespace.CATEGORY);
	public static Map<Integer, Namespace> DEFAULT_NAMESPACES = new LinkedHashMap<Integer, Namespace>();
	private Integer id;
	private String label;
	private Namespace mainNamespace;
	private Map<String, String> namespaceTranslations = new HashMap<String, String>();

	static {
		DEFAULT_NAMESPACES.put(Namespace.MEDIA.getId(), Namespace.MEDIA);
		DEFAULT_NAMESPACES.put(Namespace.SPECIAL.getId(), Namespace.SPECIAL);
		DEFAULT_NAMESPACES.put(Namespace.MAIN.getId(), Namespace.MAIN);
		DEFAULT_NAMESPACES.put(Namespace.COMMENTS.getId(), Namespace.COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.USER.getId(), Namespace.USER);
		DEFAULT_NAMESPACES.put(Namespace.USER_COMMENTS.getId(), Namespace.USER_COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.SITE_CUSTOM.getId(), Namespace.SITE_CUSTOM);
		DEFAULT_NAMESPACES.put(Namespace.SITE_CUSTOM_COMMENTS.getId(), Namespace.SITE_CUSTOM_COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.FILE.getId(), Namespace.FILE);
		DEFAULT_NAMESPACES.put(Namespace.FILE_COMMENTS.getId(), Namespace.FILE_COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.JAMWIKI.getId(), Namespace.JAMWIKI);
		DEFAULT_NAMESPACES.put(Namespace.JAMWIKI_COMMENTS.getId(), Namespace.JAMWIKI_COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.TEMPLATE.getId(), Namespace.TEMPLATE);
		DEFAULT_NAMESPACES.put(Namespace.TEMPLATE_COMMENTS.getId(), Namespace.TEMPLATE_COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.HELP.getId(), Namespace.HELP);
		DEFAULT_NAMESPACES.put(Namespace.HELP_COMMENTS.getId(), Namespace.HELP_COMMENTS);
		DEFAULT_NAMESPACES.put(Namespace.CATEGORY.getId(), Namespace.CATEGORY);
		DEFAULT_NAMESPACES.put(Namespace.CATEGORY_COMMENTS.getId(), Namespace.CATEGORY_COMMENTS);
	}
	private static final WikiLogger logger = WikiLogger.getLogger(Namespace.class.getName());

	/**
	 * Create a namespace.
	 */
	public Namespace(Integer id, String label) {
		this.id = id;
		this.label = label;
	}

	/**
	 * Create a namespace and add it to the global list of namespaces.
	 */
	private Namespace(Integer id, String label, Namespace mainNamespace) {
		this.id = id;
		this.label = label;
		this.mainNamespace = mainNamespace;
	}

	/**
	 *
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 *
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 *
	 */
	public String getDefaultLabel() {
		return this.label;
	}

	/**
	 * Setter method for the namespace label.
	 */
	public void setDefaultLabel(String label) {
		this.label = label;
	}

	/**
	 * Return the virtual-wiki specific namespace, or if one has not been defined
	 * return the default namespace label
	 */
	public String getLabel(String virtualWiki) {
		return (virtualWiki != null && this.namespaceTranslations.get(virtualWiki) != null) ? this.namespaceTranslations.get(virtualWiki) : this.label;
	}

	/**
	 *
	 */
	public Namespace getMainNamespace() {
		return this.mainNamespace;
	}

	/**
	 *
	 */
	public void setMainNamespace(Namespace mainNamespace) {
		this.mainNamespace = mainNamespace;
	}

	/**
	 *
	 */
	public Map<String, String> getNamespaceTranslations() {
		return this.namespaceTranslations;
	}

	/**
	 * Certain namespaces are case sensitive (such as the main namespace) while
	 * others (such as the user namespace) are not.
	 */
	public boolean isCaseSensitive() {
		// user/template/category namespaces are not case-insensitive
		if (this.getId().equals(Namespace.SPECIAL_ID)) {
			return false;
		}
		if (this.getId().equals(Namespace.TEMPLATE_ID) || this.getId().equals(Namespace.TEMPLATE_COMMENTS_ID)) {
			return false;
		}
		if (this.getId().equals(Namespace.USER_ID) || this.getId().equals(Namespace.USER_COMMENTS_ID)) {
			return false;
		}
		if (this.getId().equals(Namespace.CATEGORY_ID) || this.getId().equals(Namespace.CATEGORY_COMMENTS_ID)) {
			return false;
		}
		return true;
	}

	/**
	 * Given a namespace, return the Namespace for the corresponding "comments"
	 * namespace.  If no match exists return <code>null</code>.  Example: if this
	 * method is called with Namespace.USER_COMMENTS or Namespace.USER as an
	 * argument, Namespace.USER_COMMENTS will be returned.
	 */
	public static Namespace findCommentsNamespace(Namespace namespace) throws DataAccessException {
		if (namespace == null) {
			return null;
		}
		if (namespace.mainNamespace != null) {
			// the submitted namespace IS a comments namespace, so return it.
			return namespace;
		}
		// otherwise loop through all namespaces looking for a comments namespace that points
		// to this namespace.
		List<Namespace> namespaces = WikiBase.getDataHandler().lookupNamespaces();
		for (Namespace candidateNamespace : namespaces) {
			if (candidateNamespace.mainNamespace != null && candidateNamespace.mainNamespace.equals(namespace)) {
				return candidateNamespace;
			}
		}
		// no match found
		return null;
	}

	/**
	 * Given a namespace, return the Namespace for the "main" (ie, not comments)
	 * namespace.  If no match exists return <code>null</code>.  Example: if this
	 * method is called with Namespace.USER_COMMENTS or Namespace.USER as an
	 * argument, Namespace.USER will be returned.
	 */
	public static Namespace findMainNamespace(Namespace namespace) {
		if (namespace == null) {
			return null;
		}
		return (namespace.mainNamespace == null) ? namespace : namespace.mainNamespace;
	}

	/**
	 * Utility method for retrieving a namespace given the ID.  Note that this method
	 * will suppress any database exceptions, so if the caller must know if the
	 * retrieval failed then DataHandler.lookupNamespaceById() should be used instead.
	 *
	 * @param namespaceId The ID of the namespace being retrieved.
	 * @return The Namespace object that matches the ID, or <code>null</code> if no
	 *  match is found or if an error is returned.
	 */
	public static Namespace namespace(int namespaceId) {
		try {
			return WikiBase.getDataHandler().lookupNamespaceById(namespaceId);
		} catch (DataAccessException e) {
			logger.severe("Failure while retrieving namespace for ID: " + namespaceId, e);
		}
		return null;
	}

	/**
	 * Standard equals method.  Two namespaces are equal if they have the same ID.
	 */
	public boolean equals(Namespace namespace) {
		return (namespace != null && this.label.equals(namespace.getDefaultLabel()));
	}
}
