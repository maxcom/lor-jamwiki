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

import org.jamwiki.migrate.MediaWikiConstants;

/**
 * Namespace constants. Namespaces allow the organization of wiki topics
 * by dividing topics into groups.  A namespace will precede the topic, such
 * as "Namespace:Topic".  Namespaces can be customized by modifying using
 * configuration tools.
 */
public enum Namespace {

	// default namespaces, used during setup.  additional namespaces may be added after setup.
	// namespace IDs should match Mediawiki to maximize compatibility.
	NAMESPACE_MEDIA_ID                (MediaWikiConstants.MEDIAWIKI_MEDIA_NAMESPACE_ID, "Media:"),
	NAMESPACE_SPECIAL_ID              (MediaWikiConstants.MEDIAWIKI_SPECIAL_NAMESPACE_ID, "Special"),
	NAMESPACE_MAIN_ID                 (MediaWikiConstants.MEDIAWIKI_MAIN_NAMESPACE_ID, ""),
	NAMESPACE_COMMENTS_ID             (MediaWikiConstants.MEDIAWIKI_TALK_NAMESPACE_ID, "Comments"),
	NAMESPACE_USER_ID                 (MediaWikiConstants.MEDIAWIKI_USER_NAMESPACE_ID, "User"),
	NAMESPACE_USER_COMMENTS_ID        (MediaWikiConstants.MEDIAWIKI_USER_TALK_NAMESPACE_ID, "User comments"),
	NAMESPACE_SITE_CUSTOM_ID          (MediaWikiConstants.MEDIAWIKI_SITE_CUSTOM_NAMESPACE_ID, "Project"),
	NAMESPACE_SITE_CUSTOM_COMMENTS_ID (MediaWikiConstants.MEDIAWIKI_SITE_CUSTOM_TALK_NAMESPACE_ID, "Project comments"),
	NAMESPACE_FILE_ID                 (MediaWikiConstants.MEDIAWIKI_FILE_NAMESPACE_ID, "Image"),
	NAMESPACE_FILE_COMMENTS_ID        (MediaWikiConstants.MEDIAWIKI_FILE_TALK_NAMESPACE_ID, "Image comments"),
	NAMESPACE_JAMWIKI_ID              (MediaWikiConstants.MEDIAWIKI_MEDIAWIKI_NAMESPACE_ID, "JAMWiki"),
	NAMESPACE_JAMWIKI_COMMENTS_ID     (MediaWikiConstants.MEDIAWIKI_MEDIAWIKI_TALK_NAMESPACE_ID, "JAMWiki comments"),
	NAMESPACE_TEMPLATE_ID             (MediaWikiConstants.MEDIAWIKI_TEMPLATE_NAMESPACE_ID, "Template"),
	NAMESPACE_TEMPLATE_COMMENTS_ID    (MediaWikiConstants.MEDIAWIKI_TEMPLATE_TALK_NAMESPACE_ID, "Template comments"),
	NAMESPACE_HELP_ID                 (MediaWikiConstants.MEDIAWIKI_HELP_NAMESPACE_ID, "Help"),
	NAMESPACE_HELP_COMMENTS_ID        (MediaWikiConstants.MEDIAWIKI_HELP_TALK_NAMESPACE_ID, "Help comments"),
	NAMESPACE_CATEGORY_ID             (MediaWikiConstants.MEDIAWIKI_CATEGORY_NAMESPACE_ID, "Category"),
	NAMESPACE_CATEGORY_COMMENTS_ID    (MediaWikiConstants.MEDIAWIKI_CATEGORY_TALK_NAMESPACE_ID, "Category comments");

	private final int id;
	private final String label;

	/**
	 *
	 */
	Namespace(int id, String label) {
		this.id = id;
		this.label = label;
	}

	/**
	 *
	 */
	public int id() {
		return this.id;
	}

	/**
	 *
	 */
	public String label() {
		return this.label;
	}
}
