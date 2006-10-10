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
package org.jamwiki.parser;

import java.util.Hashtable;
import java.util.Locale;
import org.jamwiki.model.WikiUser;

/**
 *
 */
public class ParserInput {

	private boolean allowSectionEdit = true;
	private String context = null;
	/** Depth is used to prevent infinite nesting of templates and other objects. */
	private int depth = 0;
	private Locale locale = null;
	private TableOfContents tableOfContents = new TableOfContents();
	/** Hashtable of generic temporary objects used during parsing. */
	private Hashtable tempParams = new Hashtable();
	private String topicName = null;
	/** IP address of the current user. */
	private String userIpAddress = null;
	private String virtualWiki = null;
	/** Current WikiUser (if any). */
	private WikiUser wikiUser = null;

	/**
	 *
	 */
	public ParserInput() {
	}

	/**
	 *
	 */
	public boolean getAllowSectionEdit() {
		return allowSectionEdit;
	}

	/**
	 *
	 */
	public void setAllowSectionEdit(boolean allowSectionEdit) {
		this.allowSectionEdit = allowSectionEdit;
	}

	/**
	 *
	 */
	public String getContext() {
		return context;
	}

	/**
	 *
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 *
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 *
	 */
	public void incrementDepth() {
		this.depth++;
	}

	/**
	 *
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 *
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 *
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 *
	 */
	public TableOfContents getTableOfContents() {
		return this.tableOfContents;
	}

	/**
	 *
	 */
	public void setTableOfContents(TableOfContents tableOfContents) {
		this.tableOfContents = tableOfContents;
	}

	/**
	 *
	 */
	public Hashtable getTempParams() {
		return this.tempParams;
	}

	/**
	 *
	 */
	public String getTopicName() {
		return this.topicName;
	}

	/**
	 *
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 *
	 */
	public String getUserIpAddress() {
		return this.userIpAddress;
	}

	/**
	 *
	 */
	public void setUserIpAddress(String userIpAddress) {
		this.userIpAddress = userIpAddress;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}

	/**
	 *
	 */
	public WikiUser getWikiUser() {
		return this.wikiUser;
	}

	/**
	 *
	 */
	public void setWikiUser(WikiUser wikiUser) {
		this.wikiUser = wikiUser;
	}
}
