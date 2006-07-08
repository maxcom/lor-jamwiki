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

import org.jamwiki.model.WikiUser;

/**
 *
 */
public class ParserInfo {

	public static final int MODE_NORMAL = 1;
	/** Preview mode indicates that the topic is being edited but not saved yet. */
	public static final int MODE_PREVIEW = 2;
	/** Save mode indicates that the topic was edited and is being saved. */
	public static final int MODE_SAVE = 3;
	private String context = null;
	private int mode = MODE_NORMAL;
	/** IP address of the current user. */
	private String userIpAddress = null;
	private String virtualWiki = null;
	/** Current WikiUser (if any). */
	private WikiUser wikiUser = null;
	private TableOfContents tableOfContents = new TableOfContents();

	public ParserInfo() {
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
	public int getMode() {
		return this.mode;
	}

	/**
	 *
	 */
	public void setMode(int mode) {
		this.mode = mode;
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
