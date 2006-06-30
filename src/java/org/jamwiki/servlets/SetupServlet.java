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
 * along with this program (gpl.txt); if not, write to the Free Software
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
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.utils.Encryption;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class SetupServlet extends JAMWikiServlet implements Controller {

	private static Logger logger = Logger.getLogger(SetupServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMWikiServlet.buildLayout(request, next);
		String function = request.getParameter("function");
		if (function == null) function = "";
		// FIXME - hard coding of "function" values
		if (function == null || function.length() == 0) {
			view(request, next);
		} else {
			initialize(request, next);
		}
		return next;
	}

	/**
	 *
	 */
	private void initialize(HttpServletRequest request, ModelAndView next) throws Exception {
		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_SETUP);
		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Setup");
		setProperties(request, next);
		Vector errors = validate(request, next);
		if (errors.size() > 0) {
			next.addObject("errors", errors);
		} else {
			Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, true);
			Environment.saveProperties();
			WikiBase.initialise(request.getLocale());
		}
	}

	/**
	 *
	 */
	private void setProperties(HttpServletRequest request, ModelAndView next) throws Exception {
		Environment.setValue(Environment.PROP_BASE_FILE_DIR, request.getParameter(Environment.PROP_BASE_FILE_DIR));
		int persistenceType = Integer.parseInt(request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));
		if (persistenceType == WikiBase.FILE) {
			Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "FILE");
		} else if (persistenceType == WikiBase.DATABASE) {
			Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
		}
		if (request.getParameter(Environment.PROP_DB_DRIVER) != null) {
			Environment.setValue(Environment.PROP_DB_DRIVER, request.getParameter(Environment.PROP_DB_DRIVER));
			Environment.setValue(Environment.PROP_DB_URL, request.getParameter(Environment.PROP_DB_URL));
			Environment.setValue(Environment.PROP_DB_USERNAME, request.getParameter(Environment.PROP_DB_USERNAME));
			Encryption.setEncryptedProperty(Environment.PROP_DB_PASSWORD, request.getParameter(Environment.PROP_DB_PASSWORD));
		}
		Encryption.setEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD, request.getParameter(Environment.PROP_BASE_ADMIN_PASSWORD));
	}

	/**
	 *
	 */
	private Vector validate(HttpServletRequest request, ModelAndView next) throws Exception {
		Vector errors = new Vector();
		File baseDir = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR));
		if (!baseDir.exists()) {
			// invalid base directory
			// FIXME - hard coding
			errors.add(Environment.getValue(Environment.PROP_BASE_FILE_DIR) + " is not a valid directory");
		}
		String newPassword = request.getParameter(Environment.PROP_BASE_ADMIN_PASSWORD);
		String confirmPassword = request.getParameter("confirmPassword");
		if (!newPassword.equals(confirmPassword)) {
			// admin password invalid
			errors.add(JAMWikiServlet.getMessage("admin.message.passwordsnomatch", request.getLocale()));
		}
		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("DATABASE")) {
			// test database
			if (!DatabaseHandler.testDatabase()) {
				errors.add("A connection could not be established with the database; please re-check the settings");
			}
		}
		return errors;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_SETUP);
		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Setup");
	}
}
