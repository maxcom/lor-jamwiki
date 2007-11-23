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
	/** Namespace prefix for the link. */
	private String namespace = null;
	/** Link query paramters. */
	private String query = null;
	/** Link section (ie #section). */
	private String section = null;
	/** Link text. */
	private String text = null;

	/**
	 *
	 */
	public WikiLink() {
	}

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
	public String getNamespace() {
		return this.namespace;
	}

	/**
	 *
	 */
	public void setNamespace(String namespace) {
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
}
