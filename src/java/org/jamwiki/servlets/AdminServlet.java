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
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.db.DatabaseConnection;
import org.jamwiki.db.DatabaseHandler;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * The <code>AdminServlet</code> servlet is the servlet which allows the administrator
 * to perform administrative actions on the wiki.
 */
public class AdminServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(AdminServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			if (!Utilities.isAdmin(request)) {
				WikiMessage errorMessage = new WikiMessage("admin.message.loginrequired");
				return viewLogin(request, "Special:Admin", errorMessage);
			}
			String function = request.getParameter("function");
			if (function == null) function = "";
			if (!StringUtils.hasText(function)) {
				view(request, next, pageInfo, null);
			}
			if (function.equals("refreshIndex")) {
				refreshIndex(request, next, pageInfo);
			}
			if (function.equals("properties")) {
				properties(request, next, pageInfo);
			}
			if (function.equals("addVirtualWiki")) {
				addVirtualWiki(request, next, pageInfo);
			}
			if (function.equals("recentChanges")) {
				recentChanges(request, next, pageInfo);
			}
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void addVirtualWiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		WikiUser user = Utilities.currentUser(request);
		try {
			VirtualWiki virtualWiki = new VirtualWiki();
			if (StringUtils.hasText(request.getParameter("virtualWikiId"))) {
				virtualWiki.setVirtualWikiId(new Integer(request.getParameter("virtualWikiId")).intValue());
			}
			virtualWiki.setName(request.getParameter("name"));
			virtualWiki.setDefaultTopicName(request.getParameter("defaultTopicName"));
			WikiBase.getHandler().writeVirtualWiki(virtualWiki);
			WikiBase.getHandler().setupSpecialPages(request.getLocale(), user, virtualWiki);
			next.addObject("message", new WikiMessage("admin.message.virtualwikiadded"));
		} catch (Exception e) {
			logger.severe("Failure while adding virtual wiki", e);
			String message = "Failure while adding virtual wiki: " + e.getMessage();
			next.addObject("message", new WikiMessage("admin.message.virtualwikifail", e.getMessage()));
		}
		view(request, next, pageInfo, null);
	}

	/**
	 *
	 */
	private void properties(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		Properties props = new Properties();
		try {
			setProperty(props, request, Environment.PROP_BASE_DEFAULT_TOPIC);
			setProperty(props, request, Environment.PROP_BASE_LOGO_IMAGE);
			setBooleanProperty(props, request, Environment.PROP_TOPIC_VERSIONING_ON);
			setBooleanProperty(props, request, Environment.PROP_TOPIC_NON_ADMIN_TOPIC_MOVE);
			setBooleanProperty(props, request, Environment.PROP_TOPIC_FORCE_USERNAME);
			setProperty(props, request, Environment.PROP_IMAGE_RESIZE_INCREMENT);
			setProperty(props, request, Environment.PROP_RECENT_CHANGES_NUM);
			setBooleanProperty(props, request, Environment.PROP_TOPIC_USE_PREVIEW);
			setProperty(props, request, Environment.PROP_PARSER_CLASS);
			setBooleanProperty(props, request, Environment.PROP_PARSER_TOC);
			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_HTML);
			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
			setProperty(props, request, Environment.PROP_PARSER_SIGNATURE_USER_PATTERN);
			setProperty(props, request, Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN);
			setProperty(props, request, Environment.PROP_BASE_FILE_DIR);
			int persistenceType = Integer.parseInt(request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));

			if (persistenceType == WikiBase.PERSISTENCE_INTERNAL_DB) {
				DatabaseHandler.setupDefaultDatabase(props);
			} else if (persistenceType == WikiBase.PERSISTENCE_EXTERNAL_DB) {
				props.setProperty(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
				setProperty(props, request, Environment.PROP_DB_DRIVER);
				setProperty(props, request, Environment.PROP_DB_TYPE);
				setProperty(props, request, Environment.PROP_DB_URL);
				setProperty(props, request, Environment.PROP_DB_USERNAME);
				if (StringUtils.hasText(request.getParameter(Environment.PROP_DB_PASSWORD))) {
					setEncryptedProperty(props, request, Environment.PROP_DB_PASSWORD);
					next.addObject("dbPassword", request.getParameter(Environment.PROP_DB_PASSWORD));
				} else {
					props.setProperty(Environment.PROP_DB_PASSWORD, Environment.getValue(Environment.PROP_DB_PASSWORD));
				}
			}
			setProperty(props, request, Environment.PROP_DBCP_MAX_ACTIVE);
			setProperty(props, request, Environment.PROP_DBCP_MAX_IDLE);
			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_BORROW);
			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_RETURN);
			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_WHILE_IDLE);
			setProperty(props, request, Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME);
			setProperty(props, request, Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS);
			setProperty(props, request, Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN);
			setProperty(props, request, Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION);
			setBooleanProperty(props, request, Environment.PROP_DBCP_REMOVE_ABANDONED);
			setProperty(props, request, Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT);
			int maxFileSizeInKB = Integer.parseInt(request.getParameter(Environment.PROP_FILE_MAX_FILE_SIZE));
			props.setProperty(
				Environment.PROP_FILE_MAX_FILE_SIZE,
				new Integer(maxFileSizeInKB * 1000).toString()
			);
			setProperty(props, request, Environment.PROP_FILE_DIR_FULL_PATH);
			setProperty(props, request, Environment.PROP_FILE_DIR_RELATIVE_PATH);
			/*
			setProperty(props, request, Environment.PROP_EMAIL_SMTP_HOST);
			setProperty(props, request, Environment.PROP_EMAIL_SMTP_USERNAME);
			if (StringUtils.hasText(request.getParameter(Environment.PROP_EMAIL_SMTP_PASSWORD))) {
				setEncryptedProperty(props, request, Environment.PROP_EMAIL_SMTP_PASSWORD);
				next.addObject("smtpPassword", request.getParameter(Environment.PROP_EMAIL_SMTP_PASSWORD));
			} else {
				props.setProperty(Environment.PROP_EMAIL_SMTP_PASSWORD, Environment.getValue(Environment.PROP_EMAIL_SMTP_PASSWORD));
			}
			setProperty(props, request, Environment.PROP_EMAIL_REPLY_ADDRESS);
			*/
			/*
			int membershipType = Integer.parseInt(request.getParameter(Environment.PROP_USERGROUP_TYPE));
			String usergroupType;
			if (membershipType == WikiBase.LDAP) {
				usergroupType = "LDAP";
			} else if (membershipType == WikiBase.PERSISTENCE_EXTERNAL_DB) {
				usergroupType = "DATABASE";
			} else {
				usergroupType = "0";
			}
			props.setProperty(Environment.PROP_USERGROUP_TYPE, usergroupType);
			String[] autoFill = {
				Environment.PROP_USERGROUP_FACTORY,
				Environment.PROP_USERGROUP_URL,
				Environment.PROP_USERGROUP_USERNAME,
				Environment.PROP_USERGROUP_PASSWORD,
				Environment.PROP_USERGROUP_BASIC_SEARCH,
				Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS,
				Environment.PROP_USERGROUP_USERID_FIELD,
				Environment.PROP_USERGROUP_FULLNAME_FIELD,
				Environment.PROP_USERGROUP_MAIL_FIELD,
				Environment.PROP_USERGROUP_DETAILVIEW
			};
			for (int i = 0; i < autoFill.length; i++) {
				if (autoFill[i].equals(Environment.PROP_USERGROUP_PASSWORD) && StringUtils.hasText(request.getParameter(autoFill[i]))) {
					setEncryptedProperty(props, request, Environment.PROP_USERGROUP_PASSWORD);
					next.addObject("userGroupPassword", request.getParameter(autoFill[i]));
				} else {
					setProperty(props, request, autoFill[i]);
				}
			}
			*/
			Vector errors = Utilities.validateSystemSettings(props);
			if (errors.size() > 0) {
				next.addObject("errors", errors);
				next.addObject("message", new WikiMessage("admin.message.changesnotsaved"));
			} else {
				// all is well, save the properties
				Iterator iterator = props.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String)iterator.next();
					String value = props.getProperty(key);
					Environment.setValue(key, value);
				}
				// re-initialize to reset DatabaseHandler settings (if needed)
				WikiBase.reset(request.getLocale(), Utilities.currentUser(request));
				Environment.saveProperties();
				next.addObject("message", new WikiMessage("admin.message.changessaved"));
			}
		} catch (Exception e) {
			logger.severe("Failure while processing property values", e);
			next.addObject("message", new WikiMessage("admin.message.propertyfailure", e.getMessage()));
		}
		view(request, next, pageInfo, props);
	}

	/**
	 *
	 */
	private void recentChanges(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			// FIXME - database specific
			if (WikiBase.getHandler() instanceof DatabaseHandler) {
				WikiBase.getHandler().reloadRecentChanges();
				next.addObject("message", new WikiMessage("admin.message.recentchanges"));
			} else {
				next.addObject("message", new WikiMessage("admin.caption.recentchangesdb"));
			}
		} catch (Exception e) {
			logger.severe("Failure while loading recent changes", e);
			next.addObject("errorMessage", new WikiMessage("admin.caption.recentchangesdb", e.getMessage()));
		}
		view(request, next, pageInfo, null);
	}

	/**
	 *
	 */
	private void refreshIndex(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			WikiBase.getSearchEngine().refreshIndex();
			next.addObject("message", new WikiMessage("admin.message.indexrefreshed"));
		} catch (Exception e) {
			logger.severe("Failure while refreshing search index", e);
			next.addObject("message", new WikiMessage("admin.message.searchrefresh", e.getMessage()));
		}
		view(request, next, pageInfo, null);
	}

	/**
	 *
	 */
	private static void setBooleanProperty(Properties props, HttpServletRequest request, String parameter) {
		boolean value = (request.getParameter(parameter) != null);
		props.setProperty(parameter, new Boolean(value).toString());
	}

	/**
	 *
	 */
	private static void setEncryptedProperty(Properties props, HttpServletRequest request, String parameter) throws Exception {
		String value = request.getParameter(parameter);
		Encryption.setEncryptedProperty(parameter, value, props);
	}

	/**
	 *
	 */
	private static void setProperty(Properties props, HttpServletRequest request, String parameter) {
		String value = request.getParameter(parameter);
		if (value == null) value = "";
		props.setProperty(parameter, value);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Properties props) throws Exception {
		pageInfo.setAction(WikiPageInfo.ACTION_ADMIN);
		pageInfo.setAdmin(true);
		pageInfo.setPageTitle(new WikiMessage("admin.title"));
		Collection virtualWikiList = WikiBase.getHandler().getVirtualWikiList();
		next.addObject("wikis", virtualWikiList);
		if (props == null) {
			props = Environment.getInstance();
		}
		next.addObject("props", props);
	}
}
