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

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.persistency.db.DatabaseUpgrades;
import org.jamwiki.persistency.file.FileUpgrades;
import org.jamwiki.search.LuceneSearchEngine;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * The <code>AdminServlet</code> servlet is the servlet which allows the administrator
 * to perform administrative actions on the wiki.
 */
public class UpgradeServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(UpgradeServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("upgrade");
		try {
			if (!Utilities.isUpgrade()) {
				throw new WikiException(new WikiMessage("upgrade.error.notrequired"));
			}
			String function = request.getParameter("function");
			if (!StringUtils.hasText(function)) {
				view(request, next);
			} else if (function.equals("upgrade")) {
				upgrade(request, next);
			}
		} catch (Exception e) {
			next = new ModelAndView("wiki");
			viewError(request, next, e);
			loadDefaults(request, next, this.pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void upgrade(HttpServletRequest request, ModelAndView next) throws Exception {
		Vector messages = new Vector();
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(0, 0, 8)) {
			messages = upgrade008(request, messages);
		}
		if (oldVersion.before(0, 1, 0)) {
			messages = upgrade010(request, messages);
		}
		if (oldVersion.before(0, 2, 0)) {
			messages = upgrade020(request, messages);
		}
		Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiBase.WIKI_VERSION);
		Environment.saveProperties();
		next.addObject("messages", messages);
		VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(WikiBase.DEFAULT_VWIKI);
		String htmlLink = LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki.getName(), virtualWiki.getDefaultTopicName(), virtualWiki.getDefaultTopicName(), null, true);
		WikiMessage wm = new WikiMessage("upgrade.caption.upgradecomplete");
		// do not escape the HTML link
		wm.setParamsWithoutEscaping(new String[]{htmlLink});
		next.addObject("message", wm);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_UPGRADE);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageTitle(new WikiMessage("upgrade.title"));
	}

	/**
	 *
	 */
	private Vector upgrade008(HttpServletRequest request, Vector messages) {
		Collection userNames = null;
		try {
			userNames = WikiBase.getHandler().getAllWikiUserLogins();
		} catch (Exception e) {
			// FIXME - hard coding
			String msg = "Unable to retrieve user logins";
			logger.error(msg, e);
			messages.add(msg + ": " + e.getMessage());
			return messages;
		}
		for (Iterator userIterator = userNames.iterator(); userIterator.hasNext();) {
			String userName = (String)userIterator.next();
			try {
				WikiUser user = WikiBase.getHandler().lookupWikiUser(userName);
				if (!StringUtils.hasText(user.getEncodedPassword()) || (user.getEncodedPassword().length() != 12 && user.getEncodedPassword().length() != 24)) {
					messages.add("User password for user " + userName + " is not Base64 encoded");
					continue;
				}
				String password = Encryption.decrypt64(user.getEncodedPassword());
				user.setEncodedPassword(Encryption.encrypt(password));
				WikiBase.getHandler().writeWikiUser(user);
				// FIXME - hard coding
				messages.add("Converted user password to SHA-512 for user " + userName);
			} catch (Exception e) {
				// FIXME - hard coding
				String msg = "Unable to convert user password to SHA-512 for user " + userName;
				logger.error(msg, e);
				messages.add(msg + ": " + e.getMessage());
			}
		}
		return messages;
	}

	/**
	 *
	 */
	private Vector upgrade010(HttpServletRequest request, Vector messages) {
		// update virtual wiki
		try {
			if (WikiBase.getHandler() instanceof DatabaseHandler) {
				messages = DatabaseUpgrades.upgrade010(messages);
			} else {
				messages = FileUpgrades.upgrade010(messages);
			}
		} catch (Exception e) {
			// FIXME - hard coding
			String msg = "Unable to update virtual wiki table";
			logger.error(msg, e);
			messages.add(msg + ": " + e.getMessage());
		}
		return messages;
	}

	/**
	 *
	 */
	private Vector upgrade020(HttpServletRequest request, Vector messages) {
		try {
			// rebuild search index
			LuceneSearchEngine.refreshIndex();
			messages.add("Refreshed search index");
			// update stylesheet
			WikiUser user = Utilities.currentUser(request);
			Collection virtualWikis = WikiBase.getHandler().getVirtualWikiList();
			for (Iterator iterator = virtualWikis.iterator(); iterator.hasNext();) {
				VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
				WikiBase.getHandler().updateSpecialPage(request.getLocale(), virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user);
				messages.add("Updated stylesheet for virtual wiki " + virtualWiki.getName());
			}
		} catch (Exception e) {
			// FIXME - hard coding
			String msg = "Unable to update virtual wiki table";
			logger.error(msg, e);
			messages.add(msg + ": " + e.getMessage());
		}
		return messages;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_UPGRADE);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageTitle(new WikiMessage("upgrade.title"));
	}
}
