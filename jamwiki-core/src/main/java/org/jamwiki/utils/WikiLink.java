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

import org.jamwiki.model.Interwiki;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.VirtualWiki;

/**
 * Utility method used in processing Wiki links.
 */
public class WikiLink {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiLink.class.getName());
	/** Indicator that the link requires special handling, such as links starting with a colon. */
	private boolean colon = false;
	/** Article name, not including namespace. */
	private String article = null;
	/** Link destination, including namespace. */
	private String destination = null;
	/** Interwiki link prefix. */
	private Interwiki interwiki = null;
	/** Namespace prefix for the link. */
	private Namespace namespace = Namespace.namespace(Namespace.MAIN_ID);
	/** Link query paramters. */
	private String query = null;
	/** Link section (ie #section). */
	private String section = null;
	/** Link text. */
	private String text = null;
	/** Virtual wiki link prefix. */
	private VirtualWiki virtualWiki = null;

	/**
	 *
	 */
	public String getArticle() {
		return this.article;
	}

	/**
	 *
	 */
	public void setArticle(String article) {
		this.article = article;
	}

	/**
	 *
	 */
	public boolean getColon() {
		return this.colon;
	}

	/**
	 *
	 */
	public void setColon(boolean colon) {
		this.colon = colon;
	}

	/**
	 *
	 */
	public String getDestination() {
		return this.destination;
	}

	/**
	 *
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 *
	 */
	public Interwiki getInterwiki() {
		return this.interwiki;
	}

	/**
	 *
	 */
	public void setInterwiki(Interwiki interwiki) {
		this.interwiki = interwiki;
	}

	/**
	 *
	 */
	public Namespace getNamespace() {
		return this.namespace;
	}

	/**
	 *
	 */
	public void setNamespace(Namespace namespace) {
		if (namespace == null) {
			throw new IllegalArgumentException("Namespace cannot be null");
		}
		this.namespace = namespace;
	}

	/**
	 *
	 */
	public String getQuery() {
		return this.query;
	}

	/**
	 *
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 *
	 */
	public String getSection() {
		return this.section;
	}

	/**
	 *
	 */
	public void setSection(String section) {
		this.section = section;
	}

	/**
	 *
	 */
	public String getText() {
		return this.text;
	}

	/**
	 *
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 *
	 */
	public VirtualWiki getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(VirtualWiki virtualWiki) {
		this.virtualWiki = virtualWiki;
	}
}
