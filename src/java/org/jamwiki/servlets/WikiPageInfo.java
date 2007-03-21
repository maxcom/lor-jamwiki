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
 * The <code>WikiPageInfo</code> class provides an object containing common
 * data used for generating wiki page display.
 */
public class WikiPageInfo {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiPageInfo.class.getName());
	protected static final String JSP_TOPIC = "topic.jsp";
	private boolean admin = false;
	private String contentJsp = JSP_TOPIC;
	private boolean editable = false;
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
	 * Reset all parameters of the current <code>WikiPageInfo</code> object
	 * to default values.
	 */
	protected void reset() {
		this.admin = false;
		this.contentJsp = JSP_TOPIC;
		this.editable = false;
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
	 * Every virtual wiki has a default topic, and this method returns the
	 * default topic for the current virtual wiki.
	 *
	 * @return The default topic for the current virtual wiki.
	 */
	public String getDefaultTopic() {
		return Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
	}

	/**
	 * Return a flag indicating whether or not the current user can edit the
	 * current topic.
	 *
	 * @return <code>true</code> if the current user can edit the current
	 *  page, <code>false</code> otherwise.
	 */
	public boolean getEditable() {
		return this.editable;
	}

	/**
	 * Set a flag indicating whether or not the current user can edit the
	 * current topic.
	 *
	 * @param editable Set to <code>true</code> if the current user can edit
	 *  the current page, <code>false</code> otherwise.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * Return a flag indicating whether or not the current user can move the
	 * current topic.
	 *
	 * @return <code>true</code> if the current user can move the current
	 *  page, <code>false</code> otherwise.
	 */
	public boolean getMoveable() {
		return this.moveable;
	}

	/**
	 * Set a flag indicating whether or not the current user can move the
	 * current topic.
	 *
	 * @param moveable Set to <code>true</code> if the current user can move
	 *  the current page, <code>false</code> otherwise.
	 */
	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	/**
	 * Return a description for the current page that can be used in an HTML
	 * meta tag.
	 *
	 * @return A description for the current page that can be used in an HTML
	 *  meta tag.
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
	 * Return the title for the current page.
	 *
	 * @return The title for the current page.
	 */
	public WikiMessage getPageTitle() {
		return this.pageTitle;
	}

	/**
	 * Set the title for the current page.
	 *
	 * @param pageTitle A <code>WikiMessage</code> object that contains a
	 *  translatable page title value.
	 */
	public void setPageTitle(WikiMessage pageTitle) {
		this.pageTitle = pageTitle;
	}

	/**
	 * If printable pages should open in a new window then this method will
	 * return the HTML target "_blank", otherwise this method returns an
	 * empty String.
	 *
	 * @return The HTML target "_blank" if printable pages should open in a
	 *  new window, otherwise an empty String.
	 */
	public String getPrintTarget() {
		return (Environment.getBooleanValue(Environment.PROP_PRINT_NEW_WINDOW)) ? "_blank" : "";
	}

	/**
	 * If the topic currently being displayed is the result of a redirect from
	 * another topic, return the name of the topic that is being redirected
	 * from.
	 *
	 * @return The name of the topic being redirected from, or
	 *  <code>null</code> if the current page is not the result of a redirect.
	 */
	public String getRedirectName() {
		return this.redirectName;
	}

	/**
	 * If the topic currently being displayed is the result of a redirect from
	 * another topic, set the name of the topic that is being redirected
	 * from.
	 *
	 * @param redirectName The name of the topic being redirected from, or
	 *  <code>null</code> if the current page is not the result of a redirect.
	 */
	public void setRedirectName(String redirectName) {
		this.redirectName = redirectName;
	}

	/**
	 * Return the base title used with RSS feeds.
	 *
	 * @return The base title used with RSS feeds.
	 */
	public String getRSSTitle() {
		return Environment.getValue("rss-title");
	}

	/**
	 * Return a flag indicating whether or not the current page is a "Special:"
	 * page, as opposed to a standard topic.
	 *
	 * @return <code>true</code> if the current page is a "Special:" page,
	 *  <code>false</code> otherwise.
	 */
	public boolean getSpecial() {
		return this.special;
	}

	/**
	 * Set a flag indicating whether or not the current page is a "Special:"
	 * page, as opposed to a standard topic.
	 *
	 * @param special Set to <code>true</code> if the current page is a
	 *  "Special:" page, <code>false</code> otherwise.
	 */
	public void setSpecial(boolean special) {
		this.special = special;
	}

	/**
	 * Return the name of the topic being displayed by the current page.
	 *
	 * @return The name of the topic being displayed by the current page.
	 */
	public String getTopicName() {
		return this.topicName;
	}

	/**
	 * Set the name of the topic being displayed by the current page.
	 *
	 * @param topicName The name of the topic being displayed by the current
	 *  page.
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * Return a flag indicating whether or not the current page is in the
	 * current user's watchlist.
	 *
	 * @return <code>true</code> if the current page is in the current user's
	 *  watchlist, <code>false</code> otherwise.
	 */
	public boolean getWatched() {
		return this.watched;
	}

	/**
	 * Set a flag indicating whether or not the current page is in the
	 * current user's watchlist.
	 *
	 * @param watched Set to <code>true</code> if the current page is in the
	 *  current user's watchlist, <code>false</code> otherwise.
	 */
	public void setWatched(boolean watched) {
		this.watched = watched;
	}
}
