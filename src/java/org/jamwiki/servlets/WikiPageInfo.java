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
import org.jamwiki.WikiMessage;

/**
 *
 */
public class WikiPageInfo {

	private static final Logger logger = Logger.getLogger(WikiPageInfo.class);
	private boolean admin = false;
	private int action = -1;
	private WikiMessage pageTitle = null;
	private String redirectName = null;
	private String topicName = "";
	private boolean special = false;
	// constants used as the action parameter in calls to this servlet
	public static final int ACTION_ADMIN = 1;
	public static final int ACTION_ADMIN_CONVERT = 2;
	public static final int ACTION_ADMIN_MANAGE = 3;
	public static final int ACTION_ADMIN_TRANSLATION = 4;
	public static final int ACTION_ALL_PAGES = 5;
	public static final int ACTION_CATEGORIES = 6;
	public static final int ACTION_CONTRIBUTIONS = 7;
	public static final int ACTION_DIFF = 8;
	public static final int ACTION_EDIT = 9;
	public static final int ACTION_EDIT_PREVIEW = 10;
	public static final int ACTION_EDIT_RESOLVE = 11;
	public static final int ACTION_ERROR = 12;
	public static final int ACTION_EXPORT = 13;
	public static final int ACTION_HISTORY = 14;
	public static final int ACTION_IMPORT = 15;
	public static final int ACTION_LINK_TO = 16;
	public static final int ACTION_LOGIN = 17;
	public static final int ACTION_MOVE = 18;
	public static final int ACTION_RECENT_CHANGES = 19;
	public static final int ACTION_REGISTER = 20;
	public static final int ACTION_SEARCH = 21;
	public static final int ACTION_SEARCH_RESULTS = 22;
	public static final int ACTION_SETUP = 23;
	public static final int ACTION_UPGRADE = 24;
	public static final int ACTION_UPLOAD = 25;

	/**
	 *
	 */
	protected WikiPageInfo() {
	}

	/**
	 *
	 */
	public boolean getActionAdmin() {
		return (this.action == ACTION_ADMIN);
	}

	/**
	 *
	 */
	public boolean getActionAdminConvert() {
		return (this.action == ACTION_ADMIN_CONVERT);
	}

	/**
	 *
	 */
	public boolean getActionAdminManage() {
		return (this.action == ACTION_ADMIN_MANAGE);
	}

	/**
	 *
	 */
	public boolean getActionAdminTranslation() {
		return (this.action == ACTION_ADMIN_TRANSLATION);
	}

	/**
	 *
	 */
	public boolean getActionAllPages() {
		return (this.action == ACTION_ALL_PAGES);
	}

	/**
	 *
	 */
	public boolean getActionCategories() {
		return (this.action == ACTION_CATEGORIES);
	}

	/**
	 *
	 */
	public boolean getActionContributions() {
		return (this.action == ACTION_CONTRIBUTIONS);
	}

	/**
	 *
	 */
	public boolean getActionDiff() {
		return (this.action == ACTION_DIFF);
	}

	/**
	 *
	 */
	public boolean getActionEdit() {
		return (this.action == ACTION_EDIT);
	}

	/**
	 *
	 */
	public boolean getActionEditPreview() {
		return (this.action == ACTION_EDIT_PREVIEW);
	}

	/**
	 *
	 */
	public boolean getActionEditResolve() {
		return (this.action == ACTION_EDIT_RESOLVE);
	}

	/**
	 *
	 */
	public boolean getActionError() {
		return (this.action == ACTION_ERROR);
	}

	/**
	 *
	 */
	public boolean getActionExport() {
		return (this.action == ACTION_EXPORT);
	}

	/**
	 *
	 */
	public boolean getActionHistory() {
		return (this.action == ACTION_HISTORY);
	}

	/**
	 *
	 */
	public boolean getActionImport() {
		return (this.action == ACTION_IMPORT);
	}

	/**
	 *
	 */
	public boolean getActionLinkTo() {
		return (this.action == ACTION_LINK_TO);
	}

	/**
	 *
	 */
	public boolean getActionLogin() {
		return (this.action == ACTION_LOGIN);
	}

	/**
	 *
	 */
	public boolean getActionMove() {
		return (this.action == ACTION_MOVE);
	}

	/**
	 *
	 */
	public boolean getActionRecentChanges() {
		return (this.action == ACTION_RECENT_CHANGES);
	}

	/**
	 *
	 */
	public boolean getActionRegister() {
		return (this.action == ACTION_REGISTER);
	}

	/**
	 *
	 */
	public boolean getActionSearch() {
		return (this.action == ACTION_SEARCH);
	}

	/**
	 *
	 */
	public boolean getActionSearchResults() {
		return (this.action == ACTION_SEARCH_RESULTS);
	}

	/**
	 *
	 */
	public boolean getActionSetup() {
		return (this.action == ACTION_SETUP);
	}

	/**
	 *
	 */
	public boolean getActionUpgrade() {
		return (this.action == ACTION_UPGRADE);
	}

	/**
	 *
	 */
	public boolean getActionUpload() {
		return (this.action == ACTION_UPLOAD);
	}

	/**
	 *
	 */
	public void setAction(int action) {
		this.action = action;
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
}
