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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jamwiki.migrate.MediaWikiConstants;

/**
 * Namespaces allow the organization of wiki topics by dividing topics into
 * groups.  A namespace will precede the topic, such as "Namespace:Topic".
 * Namespaces can be customized by modifying using configuration tools, but
 * the namesapces defined as constants always exist and are required for wiki
 * operation.
 */
public class Namespace implements Serializable {

	public static final String SEPARATOR = ":";
	private static Map<Integer, Namespace> NAMESPACES  = new HashMap<Integer, Namespace>();
	// default namespaces, used during setup.  additional namespaces may be added after setup.
	// namespace IDs should match Mediawiki to maximize compatibility.
	public static final Namespace MEDIA                = new Namespace(MediaWikiConstants.MEDIAWIKI_MEDIA_NAMESPACE_ID, "Media:");
	public static final Namespace SPECIAL              = new Namespace(MediaWikiConstants.MEDIAWIKI_SPECIAL_NAMESPACE_ID, "Special");
	public static final Namespace MAIN                 = new Namespace(MediaWikiConstants.MEDIAWIKI_MAIN_NAMESPACE_ID, "");
	public static final Namespace COMMENTS             = new Namespace(MediaWikiConstants.MEDIAWIKI_TALK_NAMESPACE_ID, "Comments");
	public static final Namespace USER                 = new Namespace(MediaWikiConstants.MEDIAWIKI_USER_NAMESPACE_ID, "User");
	public static final Namespace USER_COMMENTS        = new Namespace(MediaWikiConstants.MEDIAWIKI_USER_TALK_NAMESPACE_ID, "User comments");
	public static final Namespace SITE_CUSTOM          = new Namespace(MediaWikiConstants.MEDIAWIKI_SITE_CUSTOM_NAMESPACE_ID, "Project");
	public static final Namespace SITE_CUSTOM_COMMENTS = new Namespace(MediaWikiConstants.MEDIAWIKI_SITE_CUSTOM_TALK_NAMESPACE_ID, "Project comments");
	public static final Namespace FILE                 = new Namespace(MediaWikiConstants.MEDIAWIKI_FILE_NAMESPACE_ID, "Image");
	public static final Namespace FILE_COMMENTS        = new Namespace(MediaWikiConstants.MEDIAWIKI_FILE_TALK_NAMESPACE_ID, "Image comments");
	public static final Namespace JAMWIKI              = new Namespace(MediaWikiConstants.MEDIAWIKI_MEDIAWIKI_NAMESPACE_ID, "JAMWiki");
	public static final Namespace JAMWIKI_COMMENTS     = new Namespace(MediaWikiConstants.MEDIAWIKI_MEDIAWIKI_TALK_NAMESPACE_ID, "JAMWiki comments");
	public static final Namespace TEMPLATE             = new Namespace(MediaWikiConstants.MEDIAWIKI_TEMPLATE_NAMESPACE_ID, "Template");
	public static final Namespace TEMPLATE_COMMENTS    = new Namespace(MediaWikiConstants.MEDIAWIKI_TEMPLATE_TALK_NAMESPACE_ID, "Template comments");
	public static final Namespace HELP                 = new Namespace(MediaWikiConstants.MEDIAWIKI_HELP_NAMESPACE_ID, "Help");
	public static final Namespace HELP_COMMENTS        = new Namespace(MediaWikiConstants.MEDIAWIKI_HELP_TALK_NAMESPACE_ID, "Help comments");
	public static final Namespace CATEGORY             = new Namespace(MediaWikiConstants.MEDIAWIKI_CATEGORY_NAMESPACE_ID, "Category");
	public static final Namespace CATEGORY_COMMENTS    = new Namespace(MediaWikiConstants.MEDIAWIKI_CATEGORY_TALK_NAMESPACE_ID, "Category comments");
	private final int id;
	private final String label;

	/**
	 * Create a namespace and add it to the global list of namespaces.
	 */
	public Namespace(int id, String label) {
		this.id = id;
		this.label = label;
		NAMESPACES.put(id, this);
	}

	/**
	 * Namespace IDs are unique.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 *
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Return a collection of currently created namespaces.  Anytime a namespace
	 * is created or updated by calling the Namespace constructor then it is added
	 * to the global namespace list.
	 */
	public static Collection<Namespace> values() {
		return NAMESPACES.values();
	}
}
