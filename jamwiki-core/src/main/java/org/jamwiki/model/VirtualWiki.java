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
import org.jamwiki.Environment;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a virtual wiki.
 */
public class VirtualWiki implements Serializable {

	private static final WikiLogger logger = WikiLogger.getLogger(VirtualWiki.class.getName());
	private String defaultTopicName = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
	private String logoImageUrl = Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE);
	private String metaDescription = Environment.getValue(Environment.PROP_BASE_META_DESCRIPTION);
	private String name = null;
	private String siteName = Environment.getValue(Environment.PROP_SITE_NAME);
	private int virtualWikiId = -1;

	/**
	 *
	 */
	public VirtualWiki() {
	}

	/**
	 *
	 */
	public String getDefaultTopicName() {
		return this.defaultTopicName;
	}

	/**
	 *
	 */
	public void setDefaultTopicName(String defaultTopicName) {
		this.defaultTopicName = defaultTopicName;
	}

	/**
	 *
	 */
	public String getLogoImageUrl() {
		return this.logoImageUrl;
	}

	/**
	 *
	 */
	public void setLogoImageUrl(String logoImageUrl) {
		this.logoImageUrl = logoImageUrl;
	}

	/**
	 *
	 */
	public String getMetaDescription() {
		return this.metaDescription;
	}

	/**
	 *
	 */
	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	/**
	 * Determine if this virtual wiki matches the system default.
	 */
	public boolean isDefaultVirtualWiki() {
		return (this.name != null && this.name.equals(Environment.getValue(Environment.PROP_VIRTUAL_WIKI_DEFAULT)));
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public String getSiteName() {
		return this.siteName;
	}

	/**
	 *
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	/**
	 *
	 */
	public int getVirtualWikiId() {
		return this.virtualWikiId;
	}

	/**
	 *
	 */
	public void setVirtualWikiId(int virtualWikiId) {
		this.virtualWikiId = virtualWikiId;
	}
}