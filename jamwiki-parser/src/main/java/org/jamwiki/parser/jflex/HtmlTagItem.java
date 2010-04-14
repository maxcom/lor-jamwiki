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
package org.jamwiki.parser.jflex;

/**
 * Wrapper for an HTML tag of the form <tag attribute1="value1" attribute2="value2">.  Used solely
 * for utility purposes with the JFlex parser.
 */
public class HtmlTagItem {

	private String tagType;
	private String html;

	/**
	 *
	 */
	protected HtmlTagItem(String tagType, String html) {
		this.tagType = tagType;
		this.html = html;
	}

	/**
	 *
	 */
	protected String getHtml() {
		return this.html;
	}

	/**
	 *
	 */
	protected String getTagType() {
		return this.tagType;
	}
}
