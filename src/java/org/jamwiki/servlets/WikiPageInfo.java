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

import java.text.MessageFormat;
import org.jamwiki.Environment;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiPageInfo {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiPageInfo.class.getName());
	protected static final String JSP_TOPIC = "topic.jsp";
	private boolean admin = false;
	private String contentJsp = JSP_TOPIC;
	private boolean moveable = false;
	private WikiMessage pageTitle = null;
	private String redirectName = null;
	private String topicName = "";
	private boolean special = false;
	private boolean watched = false;

	/**
	 *
	 */
	protected WikiPageInfo() {
	}

	/**
	 *
	 */
	protected void reset() {
		this.admin = false;
		this.contentJsp = JSP_TOPIC;
		this.moveable = false;
		this.pageTitle = null;
		this.redirectName = null;
		this.topicName = "";
		this.special = false;
		this.watched = false;
	}

	/**
	 * If a page is a part of the admin tool then this method will return
	 * <code>true</code>.
	 *
	 * @return <code>true</code> if a page is part of the admin tool,
	 *  <code>false</code> otherwise.
	 */
	public boolean getAdmin() {
		return this.admin;
	}

	/**
	 * Set a flag indicating whether or not the page being displayed is a part
	 * of the admin tool.
	 *
	 * @param admin <code>true</code> if a page is part of the admin tool,
	 *  <code>false</code> otherwise.
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Retrieve the name of the JSP page that will be used to display the
	 * results of this page request.
	 *
	 * @return The name of the JSP page that will be used to display the
	 *  results of the page request.
	 */
	public String getContentJsp() {
		return this.contentJsp;
	}

	/**
	 * Set the JSP page that will display the results of this page request.
	 * If no value is specified then the default is to display the request
	 * using the topic display JSP.
	 *
	 * @param contentJsp The JSP page that should be used to display the
	 *  results of the page request.
	 */
	public void setContentJsp(String contentJsp) {
		this.contentJsp = contentJsp;
	}

	/**
	 *
	 */
	public String getDefaultTopic() {
		return Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
	}

	/**
	 *
	 */
	public boolean getMoveable() {
		return this.moveable;
	}

	/**
	 *
	 */
	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	/**
	 *
	 */
	public String getMetaDescription() {
		String pattern = Environment.getValue(Environment.PROP_BASE_META_DESCRIPTION);
		if (!StringUtils.hasText(pattern)) return "";
		MessageFormat formatter = new MessageFormat(pattern);
		Object params[] = new Object[1];
		params[0] = (this.topicName == null) ? "" : this.topicName;
		return formatter.format(params);
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
	public String getPrintTarget() {
		return (Environment.getBooleanValue(Environment.PROP_PRINT_NEW_WINDOW)) ? "_blank" : "";
	}

	/**
	 *
	 */
	public String getRedirectName() {
		return this.redirectName;
	}

	/**
	 *
	 */
	public void setRedirectName(String redirectName) {
		this.redirectName = redirectName;
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

	/**
	 *
	 */
	public boolean getWatched() {
		return this.watched;
	}

	/**
	 *
	 */
	public void setWatched(boolean watched) {
		this.watched = watched;
	}
}
