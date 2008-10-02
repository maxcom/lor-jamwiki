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
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * The <code>WikiPageInfo</code> class provides an object containing common
 * data used for generating wiki page display.
 */
public class WikiPageInfo {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiPageInfo.class.getName());
	protected static final String JSP_TOPIC = "topic.jsp";
	private boolean admin = false;
	private String contentJsp = JSP_TOPIC;
	private WikiMessage pageTitle = null;
	private String redirectName = null;
	private String redirectUrl = null;
	private boolean special = false;
	private LinkedHashMap tabMenu = new LinkedHashMap();
	private String topicName = "";
	private LinkedHashMap userMenu = new LinkedHashMap();
	private String virtualWikiName = null;

	/**
	 *
	 */
	protected WikiPageInfo(HttpServletRequest request) {
		this.virtualWikiName = WikiUtil.getVirtualWikiFromURI(request);
		if (this.virtualWikiName == null) {
			logger.severe("No virtual wiki available for page request " + request.getRequestURI());
			this.virtualWikiName = WikiBase.DEFAULT_VWIKI;
		}
	}

	/**
	 * Reset all parameters of the current <code>WikiPageInfo</code> object
	 * to default values.
	 */
	protected void reset() {
		this.admin = false;
		this.contentJsp = JSP_TOPIC;
		this.pageTitle = null;
		this.redirectName = null;
		this.special = false;
		this.tabMenu = new LinkedHashMap();
		this.topicName = "";
		this.userMenu = new LinkedHashMap();
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
	 * Return a description for the current page that can be used in an HTML
	 * meta tag.
	 *
	 * @return A description for the current page that can be used in an HTML
	 *  meta tag.
	 */
	public String getMetaDescription() {
		String pattern = Environment.getValue(Environment.PROP_BASE_META_DESCRIPTION);
		if (StringUtils.isBlank(pattern)) {
			return "";
		}
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
	 * another topic, return the full (relative) URL back to the redirection
	 * topic.
	 *
	 * @return The full (relative) URL of the topic being redirected from, or
	 *  <code>null</code> if the current page is not the result of a redirect.
	 */
	public String getRedirectUrl() {
		return this.redirectUrl;
	}

	/**
	 * If the topic currently being displayed is the result of a redirect from
	 * another topic, set the name and full (relative) URL of the topic that is
	 * being redirected from.
	 *
	 * @param redirectUrl The full (relative) URL of the topic being redirected
	 *  from, or <code>null</code> if the current page is not the result of a redirect.
	 * @param redirectName The name of the topic being redirected from, or
	 *  <code>null</code> if the current page is not the result of a redirect.
	 */
	public void setRedirectInfo(String redirectUrl, String redirectName) {
		this.redirectName = redirectName;
		this.redirectUrl = redirectUrl;
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
	 * Return the namespace of the topic displayed by the current page.  The
	 * namespace is the part of topic name, up to the colon.  For regular
	 * articles the namespace is an empty string.
	 *
	 * The namespace cannot be set directly, only the topic name can be set.
	 *
	 * @return The wiki namespace of this page, or an empty string for pages
	 *  in the main namespace.
	 * @see #getPagename
	 * @see #getTopicName
	 */
	public String getNamespace() {
		WikiLink wikiLink = LinkUtil.parseWikiLink(this.getTopicName());
		return wikiLink.getNamespace();
	}

	/**
	 * Return the name of the page, i.e. the name of the topic displayed by
	 * the current page, without the namespace.
	 *
	 * @return Name of the page.
	 */
	public String getPagename() {
		WikiLink wikiLink = LinkUtil.parseWikiLink(this.getTopicName());
		return wikiLink.getArticle();
	}

	/**
	 * Return a LinkedHashMap containing the topic and text for all links
	 * that should appear for the tab menu.
	 *
	 * @return A LinkedHashMap containing the topic and text for all links
	 *  that should appear for the tab menu.
	 */
	public LinkedHashMap getTabMenu() {
		return this.tabMenu;
	}

	/**
	 * Set a LinkedHashMap containing the topic and text for all links
	 * that should appear for the tab menu.
	 *
	 * @param tabMenu A LinkedHashMap containing the topic and text for all
	 *  links that should appear for the tab menu.
	 */
	public void setTabMenu(LinkedHashMap tabMenu) {
		this.tabMenu = tabMenu;
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
	 * Return a LinkedHashMap containing the topic and text for all links
	 * that should appear for the user menu.
	 *
	 * @return A LinkedHashMap containing the topic and text for all links
	 *  that should appear for the user menu.
	 */
	public LinkedHashMap getUserMenu() {
		return this.userMenu;
	}

	/**
	 * Set a LinkedHashMap containing the topic and text for all links
	 * that should appear for the user menu.
	 *
	 * @param userMenu A LinkedHashMap containing the topic and text for all
	 *  links that should appear for the user menu.
	 */
	public void setUserMenu(LinkedHashMap userMenu) {
		this.userMenu = userMenu;
	}

	/**
	 * Return the name of the virtual wiki associated with the page info being
	 * created.  This will normally be taken directly from the request and default
	 * to the wiki default virtual wiki, although in rare cases (such as redirects
	 * to other virtual wikis) it may differ.
	 *
	 * @param virtualWikiName The name of the virtual wiki currently associated
	 *  with this page info object.
	 */
	public String getVirtualWikiName() {
		if (StringUtils.isBlank(virtualWikiName)) {
			throw new IllegalArgumentException("Cannot pass a null or empty virtual wiki name");
		}
		return this.virtualWikiName;
	}

	/**
	 * Return the name of the virtual wiki associated with the page info being
	 * created.  This will normally be taken directly from the request and default
	 * to the wiki default virtual wiki, although in rare cases (such as redirects
	 * to other virtual wikis) it may differ.
	 *
	 * @param virtualWikiName The name of the virtual wiki to set.
	 */
	public void setVirtualWikiName(String virtualWikiName) {
		this.virtualWikiName = virtualWikiName;
	}

	/**
	 * If the page currently being viewed is a user page or a user comments
	 * page return <code>true</code>
	 *
	 * @return <code>true</code> if the page currently being viewed is a
	 *  user page, otherwise <code>false</code>.
	 */
	public boolean isUserPage() {
		WikiLink wikiLink = LinkUtil.parseWikiLink(this.getTopicName());
		if (wikiLink.getNamespace().equals(NamespaceHandler.NAMESPACE_USER)) {
			return true;
		}
		if (wikiLink.getNamespace().equals(NamespaceHandler.NAMESPACE_USER_COMMENTS)) {
			return true;
		}
		return false;
	}
}
