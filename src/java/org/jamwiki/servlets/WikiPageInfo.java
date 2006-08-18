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
package org.jamwiki.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 */
public class WikiPageInfo {

	private static final Logger logger = Logger.getLogger(WikiPageInfo.class);
	protected boolean admin = false;
	protected String pageAction = "";
	protected WikiMessage pageTitle = null;
	protected String topicName = "";
	protected boolean special = false;

	/**
	 *
	 */
	protected WikiPageInfo() {
	}

	/**
	 *
	 */
	public boolean getAdmin() {
		return this.admin;
	}

	/**
	 *
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 *
	 */
	public String getPageAction() {
		return this.pageAction;
	}

	/**
	 *
	 */
	public void setPageAction(String pageAction) {
		this.pageAction = pageAction;
	}

	/**
	 *
	 */
	public WikiMessage getPageTitle() {
		return this.pageTitle;
	}

	/**
	 *
	 */
	public void setPageTitle(WikiMessage pageTitle) {
		this.pageTitle = pageTitle;
	}

	/**
	 *
	 */
	public boolean getSpecial() {
		return this.special;
	}

	/**
	 *
	 */
	public void setSpecial(boolean special) {
		this.special = special;
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
}
