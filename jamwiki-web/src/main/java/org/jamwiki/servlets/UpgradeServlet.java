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

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
import org.jamwiki.authentication.JAMWikiAuthenticationConfiguration;
import org.jamwiki.db.DatabaseUpgrades;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to automatically handle JAMWiki upgrades, including configuration and
 * data modifications.
 *
 * @see org.jamwiki.servlets.SetupServlet
 */
public class UpgradeServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(UpgradeServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_UPGRADE = "upgrade.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (!WikiUtil.isUpgrade()) {
			throw new WikiException(new WikiMessage("upgrade.error.notrequired"));
		}
		String function = request.getParameter("function");
		pageInfo.setPageTitle(new WikiMessage("upgrade.title", Environment.getValue(Environment.PROP_BASE_WIKI_VERSION), WikiVersion.CURRENT_WIKI_VERSION));
		if (!StringUtils.isBlank(function) && function.equals("upgrade")) {
			upgrade(request, next, pageInfo);
		} else {
			view(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	protected void initParams() {
		this.layout = false;
		this.displayJSP = "upgrade";
	}

	/**
	 * Special login method - it cannot be assumed that the database schema
	 * is unchanged, so do not use standard methods.
	 */
	private boolean login(HttpServletRequest request) throws Exception {
		String password = request.getParameter("password");
		String username = request.getParameter("username");
		return DatabaseUpgrades.login(username, password);
	}

	/**
	 *
	 */
	private void upgrade(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		try {
			if (!this.login(request)) {
				next.addObject("error", new WikiMessage("error.login"));
				this.view(request, next, pageInfo);
				return;
			}
			WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
			if (oldVersion.before(0, 6, 0)) {
				List<WikiMessage> errors = new ArrayList<WikiMessage>();
				errors.add(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "0.6.0"));
				next.addObject("errors", errors);
				return;
			}
			List<String> messages = new ArrayList<String>();
			boolean success = true;
			// first perform database upgrades
			this.upgradeDatabase(true, messages);
			// upgrade stylesheet
			if (this.upgradeStyleSheetRequired()) {
				if (!upgradeStyleSheet(request, messages)) {
					success = false;
				}
			}
			// perform any additional upgrades required
			if (oldVersion.before(0, 7, 0)) {
				Environment.setValue(Environment.PROP_FILE_SERVER_URL, Utilities.getServerUrl(request));
				Environment.setValue(Environment.PROP_SERVER_URL, Utilities.getServerUrl(request));
			}
			List<WikiMessage> errors = ServletUtil.validateSystemSettings(Environment.getInstance());
			if (!errors.isEmpty()) {
				next.addObject("errors", errors);
				success = false;
			}
			if (success) {
				Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
				Environment.saveProperties();
				// reset data handler and other instances.  this probably hides a bug
				// elsewhere since no reset should be needed, but it's anyone's guess
				// where that might be...
				WikiBase.reload();
				VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(WikiBase.DEFAULT_VWIKI);
				WikiLink wikiLink = new WikiLink();
				wikiLink.setDestination(virtualWiki.getDefaultTopicName());
				String htmlLink = LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki.getName(), wikiLink, virtualWiki.getDefaultTopicName(), null, null, true);
				WikiMessage wm = new WikiMessage("upgrade.caption.upgradecomplete");
				// do not escape the HTML link
				wm.setParamsWithoutEscaping(new String[]{htmlLink});
				next.addObject("successMessage", wm);
				// force logout to ensure current user will be re-validated.  this is
				// necessary because the upgrade may have changed underlying data structures.
				SecurityContextHolder.clearContext();
				// force group permissions to reset
				JAMWikiAuthenticationConfiguration.resetDefaultGroupRoles();
				JAMWikiAuthenticationConfiguration.resetJamwikiAnonymousAuthorities();
			} else {
				next.addObject("error", new WikiMessage("upgrade.caption.upgradefailed"));
				next.addObject("failure", "true");
			}
			next.addObject("messages", messages);
		} catch (Exception e) {
			next.addObject("error", new WikiMessage("error.unknown", e.toString()));
			logger.severe("Unable to complete upgrade to new JAMWiki version.", e);
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private boolean upgradeDatabase(boolean performUpgrade, List<String> messages) throws Exception {
		boolean upgradeRequired = false;
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(0, 6, 1)) {
			upgradeRequired = true;
			if (performUpgrade) {
				messages = DatabaseUpgrades.upgrade061(messages);
			}
		}
		if (oldVersion.before(0, 6, 3)) {
			upgradeRequired = true;
			if (performUpgrade) {
				messages = DatabaseUpgrades.upgrade063(messages);
			}
		}
		if (oldVersion.before(0, 7, 0)) {
			upgradeRequired = true;
			if (performUpgrade) {
				messages = DatabaseUpgrades.upgrade070(messages);
			}
		}
		return upgradeRequired;
	}

	/**
	 *
	 */
	private boolean upgradeStyleSheet(HttpServletRequest request, List<String> messages) throws Exception {
		try {
			List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
			for (VirtualWiki virtualWiki : virtualWikis) {
				WikiBase.getDataHandler().updateSpecialPage(request.getLocale(), virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, ServletUtil.getIpAddress(request));
				messages.add("Updated stylesheet for virtual wiki " + virtualWiki.getName());
			}
			return true;
		} catch (Exception e) {
			// FIXME - hard coding
			String msg = "Unable to complete upgrade to new JAMWiki version.";
			logger.severe(msg, e);
			messages.add(msg + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 *
	 */
	private boolean upgradeStyleSheetRequired() {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(0, 6, 1)) {
			return true;
		}
		if (oldVersion.before(0, 6, 2)) {
			return true;
		}
		if (oldVersion.before(0, 6, 3)) {
			return true;
		}
		if (oldVersion.before(0, 6, 6)) {
			return true;
		}
		if (oldVersion.before(0, 6, 7)) {
			return true;
		}
		if (oldVersion.before(0, 7, 0)) {
			return true;
		}
		if (oldVersion.before(0, 8, 0)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(0, 6, 0)) {
			List<WikiMessage> errors = new ArrayList<WikiMessage>();
			errors.add(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "0.6.0"));
			next.addObject("errors", errors);
		}
		List<WikiMessage> upgradeDetails = new ArrayList<WikiMessage>();
		try {
			if (this.upgradeDatabase(false, null)) {
				upgradeDetails.add(new WikiMessage("upgrade.caption.database"));
			}
		} catch (Exception e) {
			// never thrown when the first parameter is false
		}
		if (this.upgradeStyleSheetRequired()) {
			upgradeDetails.add(new WikiMessage("upgrade.caption.stylesheet"));
		}
		upgradeDetails.add(new WikiMessage("upgrade.caption.releasenotes"));
		upgradeDetails.add(new WikiMessage("upgrade.caption.manual"));
		next.addObject("upgradeDetails", upgradeDetails);
		pageInfo.setContentJsp(JSP_UPGRADE);
		pageInfo.setSpecial(true);
	}
}
