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

/**
 *
 */
public class ParserInfo {

	private String context = null;
	/** User name to display for syntax such as the MediaWiki "~~~~" syntax */
	private String userDisplay = null;
	private String virtualWiki = null;
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
	public String getUserDisplay() {
		return userDisplay;
	}

	/**
	 *
	 */
	public void setUserDisplay(String userDisplay) {
		this.userDisplay = userDisplay;
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
}
