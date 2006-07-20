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
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
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
		ModelAndView next = new ModelAndView("wiki");
		try {
			if (!Utilities.isUpgrade()) {
				// FIXME - hard coding
				throw new Exception("No upgrade is currently required.");
			}
			// FIXME - hard coding of "function" values
			String function = request.getParameter("function");
			if (!StringUtils.hasText(function)) {
				view(request, next);
			} else if (function.equals("upgrade")) {
				upgrade(request, next);
			}
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void upgrade(HttpServletRequest request, ModelAndView next) throws Exception {
		Vector messages = new Vector();
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(0, 0, 8)) {
			messages = upgrade008(messages);
		}
		Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiBase.WIKI_VERSION);
		Environment.saveProperties();
		next.addObject("messages", messages);
		// FIXME - hard coding
		next.addObject("message", "Upgrade complete.  See below for any failure messages.");
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_UPGRADE);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageTitle("Special:Upgrade");
	}

	/**
	 *
	 */
	private Vector upgrade008(Vector messages) {
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
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_UPGRADE);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageTitle("Special:Upgrade");
	}
}
