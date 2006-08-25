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

import java.io.File;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.persistency.db.DatabaseConnection;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class SetupServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(SetupServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("setup");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			if (!Utilities.isFirstUse()) {
				throw new WikiException(new WikiMessage("setup.error.notrequired"));
			}
			String function = request.getParameter("function");
			if (function == null) function = "";
			try {
				if (!StringUtils.hasText(function)) {
					setup(request, next, pageInfo);
				} else if (initialize(request, next, pageInfo)) {
					next = new ModelAndView("wiki");
					viewTopic(request, next, pageInfo, Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC));
					loadDefaults(request, next, pageInfo);
				}
			} catch (Exception e) {
				handleSetupError(request, next, pageInfo, e);
			}
		} catch (WikiException e) {
			return viewError(request, e);
		}
		return next;
	}

	/**
	 *
	 */
	private void handleSetupError(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Exception e) {
		// reset properties
		Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, false);
		if (!(e instanceof WikiException)) {
			logger.error("Setup error", e);
		}
		pageInfo.setAction(WikiPageInfo.ACTION_SETUP);
		pageInfo.setSpecial(true);
		pageInfo.setPageTitle(new WikiMessage("setup.title"));
		if (e instanceof WikiException) {
			WikiException we = (WikiException)e;
			next.addObject("errorMessage", we.getWikiMessage());
		} else {
			next.addObject("errorMessage", new WikiMessage("error.unknown", e.getMessage()));
		}
	}

	/**
	 *
	 */
	private boolean initialize(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		setProperties(request, next);
		WikiUser user = new WikiUser();
		setAdminUser(request, user);
		Vector errors = validate(request, user);
		if (errors.size() > 0) {
			pageInfo.setAction(WikiPageInfo.ACTION_SETUP);
			pageInfo.setSpecial(true);
			pageInfo.setPageTitle(new WikiMessage("setup.title"));
			next.addObject("errors", errors);
			next.addObject("login", user.getLogin());
			return false;
		} else {
			Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, true);
			Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
			WikiBase.reset(request.getLocale(), user);
			request.getSession().setAttribute(JAMWikiServlet.PARAMETER_USER, user);
			Environment.saveProperties();
			return true;
		}
	}

	/**
	 *
	 */
	private void setAdminUser(HttpServletRequest request, WikiUser user) throws Exception {
		user.setLogin(request.getParameter("login"));
		user.setEncodedPassword(Encryption.encrypt(request.getParameter("newPassword")));
		user.setCreateIpAddress(request.getRemoteAddr());
		user.setLastLoginIpAddress(request.getRemoteAddr());
		user.setAdmin(true);
	}

	/**
	 *
	 */
	private void setProperties(HttpServletRequest request, ModelAndView next) throws Exception {
		Environment.setValue(Environment.PROP_BASE_FILE_DIR, request.getParameter(Environment.PROP_BASE_FILE_DIR));
		Environment.setValue(Environment.PROP_FILE_DIR_FULL_PATH, request.getParameter(Environment.PROP_FILE_DIR_FULL_PATH));
		Environment.setValue(Environment.PROP_FILE_DIR_RELATIVE_PATH, request.getParameter(Environment.PROP_FILE_DIR_RELATIVE_PATH));
		int persistenceType = Integer.parseInt(request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));
		if (persistenceType == WikiBase.FILE) {
			Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "FILE");
		} else if (persistenceType == WikiBase.DATABASE) {
			Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
		}
		if (request.getParameter(Environment.PROP_DB_DRIVER) != null) {
			Environment.setValue(Environment.PROP_DB_TYPE, request.getParameter(Environment.PROP_DB_TYPE));
			Environment.setValue(Environment.PROP_DB_DRIVER, request.getParameter(Environment.PROP_DB_DRIVER));
			Environment.setValue(Environment.PROP_DB_URL, request.getParameter(Environment.PROP_DB_URL));
			Environment.setValue(Environment.PROP_DB_USERNAME, request.getParameter(Environment.PROP_DB_USERNAME));
			Encryption.setEncryptedProperty(Environment.PROP_DB_PASSWORD, request.getParameter(Environment.PROP_DB_PASSWORD));
			next.addObject("dbPassword", request.getParameter(Environment.PROP_DB_PASSWORD));
			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE)) {
				// oracle must use a different validation query
				Environment.setValue(Environment.PROP_DBCP_VALIDATION_QUERY, "select 1 from dual");
			}
		}
	}

	/**
	 *
	 */
	private void setup(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setAction(WikiPageInfo.ACTION_SETUP);
		pageInfo.setSpecial(true);
		pageInfo.setPageTitle(new WikiMessage("setup.title"));
	}

	/**
	 *
	 */
	private Vector validate(HttpServletRequest request, WikiUser user) throws Exception {
		Vector errors = new Vector();
		File baseDir = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR));
		if (!baseDir.exists()) {
			// invalid base directory
			errors.add(new WikiMessage("error.directoryinvalid", Environment.getValue(Environment.PROP_BASE_FILE_DIR)));
		}
		File fullDir = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH));
		if (!fullDir.exists()) {
			// invalid base directory
			errors.add(new WikiMessage("error.directoryinvalid", Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH)));
		}
		if (!StringUtils.hasText(user.getLogin())) {
			user.setLogin("");
			errors.add(new WikiMessage("error.loginempty"));
		}
		String oldPassword = request.getParameter("oldPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		if (newPassword != null || confirmPassword != null) {
			if (newPassword == null) {
				errors.add(new WikiMessage("error.newpasswordempty"));
			} else if (confirmPassword == null) {
				errors.add(new WikiMessage("error.passwordconfirm"));
			} else if (!newPassword.equals(confirmPassword)) {
				errors.add(new WikiMessage("admin.message.passwordsnomatch"));
			}
		}
		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("DATABASE")) {
			// test database
			if (!DatabaseConnection.testDatabase()) {
				errors.add(new WikiMessage("error.databaseconnection"));
			}
		}
		return errors;
	}
}
