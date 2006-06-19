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
 *
 */
package org.jmwiki.servlets;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.ChangeLog;
import org.jmwiki.Environment;
import org.jmwiki.persistency.Topic;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiMembers;
import org.jmwiki.persistency.db.DatabaseConnection;
import org.jmwiki.persistency.db.DBDate;
import org.jmwiki.servlets.WikiServlet;
import org.jmwiki.utils.Encryption;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * The <code>AdminController</code> servlet is the servlet which allows the administrator
 * to perform administrative actions on the wiki.
 */
public class AdminController implements Controller {

	private static Logger logger = Logger.getLogger(AdminController.class.getName());
	private ResourceBundle messages = null;
	private String virtualWiki = null;
	private String message = null;

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		String function = request.getParameter("function");
		if (function == null) function = "";
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_ADMIN);
		if (!Utilities.isAdmin(request)) {
			login(request, next);
			return next;
		}
		if (function.equals("logout")) {
			logout(request, next);
			return next;
		}
		if (function == null || function.length() == 0) {
			view(request, next);
			return next;
		}
		if (function.equals("refreshIndex")) {
			refreshIndex(request, next);
		}
		if (function.equals("purge")) {
			purge(request, next);
		}
		if (function.equals("purge-versions")) {
			purgeVersions(request, next);
		}
		if (function.equals("properties")) {
			properties(request, next);
		}
		if (function.equals("clearEditLock")) {
			clearEditLock(request, next);
		}
		if (function.equals("removeUser")) {
			removeUser(request, next);
		}
		if (function.equals("addVirtualWiki")) {
			addVirtualWiki(request, next);
		}
		if (function.equals("changePassword")) {
			changePassword(request, next);
		}
		if (function.equals("panic")) {
			panic(request, next);
		}
		this.virtualWiki = (String) request.getAttribute("virtualWiki");
		if (request.getParameter("addReadOnly") != null) {
			Topic t = new Topic(request.getParameter("readOnlyTopic"));
			t.makeTopicReadOnly(this.virtualWiki);
		}
		if (request.getParameter("removeReadOnly") != null) {
			String[] topics = request.getParameterValues("markRemove");
			for (int i = 0; i < topics.length; i++) {
				Topic t = new Topic(topics[i]);
				t.makeTopicWritable(this.virtualWiki);
			}
		}
		// FIXME - put the message object in the response
		return next;
	}

	/**
	 *
	 */
	private void addVirtualWiki(HttpServletRequest request, ModelAndView next) {
		String newWiki = request.getParameter("newVirtualWiki");
		try {
			logger.debug("Adding new Wiki: " + newWiki);
			WikiBase.getInstance().addVirtualWiki(newWiki);
			this.message = this.messages.getString("admin.message.virtualwikiadded");
			WikiBase.initialise();
		} catch (Exception e) {
			logger.error("Failure while adding virtual wiki " + newWiki, e);
			this.message = "Failure while adding virtual wiki " + newWiki + ": " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void changePassword(HttpServletRequest request, ModelAndView next) {
		try {
			String oldPassword = request.getParameter("oldPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");
			if (!Encryption.getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD).equals(oldPassword)) {
				this.message = this.messages.getString("admin.message.oldpasswordincorrect");
			} else if (!newPassword.equals(confirmPassword)) {
				this.message = this.messages.getString("admin.message.passwordsnomatch");
			} else {
				Encryption.setEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD, newPassword);
				Environment.saveProperties();
				this.message = this.messages.getString("admin.message.passwordchanged");
			}
		} catch (Exception e) {
			logger.error("Failure while changing password", e);
			this.message = "Failure while changing password: " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void clearEditLock(HttpServletRequest request, ModelAndView next) {
		try {
			WikiBase base = WikiBase.getInstance();
			base.unlockTopic(request.getParameter("virtualWiki"), request.getParameter("topic"));
			this.message = Utilities.resource("admin.message.lockcleared", request.getLocale());
		} catch (Exception e) {
			logger.error("Failure while clearing locks", e);
			this.message = "Failure while clearing locks: " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void login(HttpServletRequest request, ModelAndView next) {
		request.setAttribute("title", Utilities.resource("login.title", request.getLocale()));
//		String rootPath = JSPUtils.createLocalRootPath(request, (String) request.getAttribute("virtualWiki"));
		StringBuffer buffer = new StringBuffer();
//		buffer.append(rootPath);
		buffer.append("../jsp/Special:Admin");
		request.setAttribute("redirect", buffer.toString());
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
		request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
	private void logout(HttpServletRequest request, ModelAndView next) {
		request.getSession().removeAttribute("admin");
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
		request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
	private void panic(HttpServletRequest request, ModelAndView next) {
		try {
			WikiBase.getInstance().panic();
		} catch (Exception e) {
			logger.error("Failure during panic reset", e);
			this.message = "Failure during panic reset: " + e.getMessage();
		}
	}
	/**
	 *
	 */
	private void properties(HttpServletRequest request, ModelAndView next) {
		try {
			Encryption.togglePropertyEncryption(request.getParameter(Environment.PROP_BASE_ENCODE_PASSWORDS) != null);
			Environment.setIntValue(
				Environment.PROP_TOPIC_EDIT_TIME_OUT,
				Integer.parseInt(request.getParameter(Environment.PROP_TOPIC_EDIT_TIME_OUT))
			);
			Environment.setValue(
				Environment.PROP_RECENT_CHANGES_DAYS,
				request.getParameter(Environment.PROP_RECENT_CHANGES_DAYS)
			);
			Environment.setValue(
				Environment.PROP_TOPIC_MAXIMUM_BACKLINKS,
				request.getParameter(Environment.PROP_TOPIC_MAXIMUM_BACKLINKS)
			);
			Environment.setIntValue(
				Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL,
				Integer.parseInt(request.getParameter(Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL))
			);
			Environment.setValue(Environment.PROP_SEARCH_TEMP_DIRECTORY, request.getParameter(Environment.PROP_SEARCH_TEMP_DIRECTORY));
			Environment.setIntValue(
				Environment.PROP_RECENT_CHANGES_REFRESH_INTERVAL,
				Integer.parseInt(request.getParameter(Environment.PROP_RECENT_CHANGES_REFRESH_INTERVAL))
			);
			Environment.setValue(
				Environment.PROP_EMAIL_SMTP_HOST,
				request.getParameter(Environment.PROP_EMAIL_SMTP_HOST)
			);
			Environment.setValue(
				Environment.PROP_EMAIL_SMTP_USERNAME,
				request.getParameter(Environment.PROP_EMAIL_SMTP_USERNAME)
			);
			Encryption.setEncryptedProperty(
				Environment.PROP_EMAIL_SMTP_PASSWORD,
				request.getParameter(Environment.PROP_EMAIL_SMTP_PASSWORD)
			);
			Environment.setValue(
				Environment.PROP_EMAIL_REPLY_ADDRESS,
				request.getParameter(Environment.PROP_EMAIL_REPLY_ADDRESS)
			);
			Environment.setValue(
				Environment.PROP_PARSER_NEW_LINE_BREAKS,
				request.getParameter(Environment.PROP_PARSER_NEW_LINE_BREAKS)
			);
			Environment.setBooleanValue(
				Environment.PROP_TOPIC_VERSIONING_ON,
				request.getParameter(Environment.PROP_TOPIC_VERSIONING_ON) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_PARSER_ALLOW_HTML,
				request.getParameter(Environment.PROP_PARSER_ALLOW_HTML) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_TOPIC_FORCE_USERNAME,
				request.getParameter(Environment.PROP_TOPIC_FORCE_USERNAME) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_TOPIC_ALLOW_VWIKI_LIST,
				request.getParameter(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_SEARCH_ATTACHMENT_INDEXING_ENABLED,
				request.getParameter(Environment.PROP_SEARCH_ATTACHMENT_INDEXING_ENABLED) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_SEARCH_EXTLINKS_INDEXING_ENABLED,
				request.getParameter(Environment.PROP_SEARCH_EXTLINKS_INDEXING_ENABLED) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_ATTACH_TIMESTAMP,
				request.getParameter(Environment.PROP_ATTACH_TIMESTAMP) != null
			);
			Environment.setValue(
				Environment.PROP_ATTACH_UPLOAD_DIR,
				request.getParameter(Environment.PROP_ATTACH_UPLOAD_DIR)
			);
			Environment.setValue(
				Environment.PROP_FILE_HOME_DIR,
				request.getParameter(Environment.PROP_FILE_HOME_DIR)
			);
			int persistenceType = Integer.parseInt(request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));
			if (persistenceType == WikiBase.FILE) {
				Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "FILE");
			} else if (persistenceType == WikiBase.DATABASE) {
				Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
			}
			if (request.getParameter(Environment.PROP_DB_DRIVER) != null) {
				Environment.setValue(
					Environment.PROP_DB_DRIVER,
					request.getParameter(Environment.PROP_DB_DRIVER)
				);
				Environment.setValue(
					Environment.PROP_DB_URL,
					request.getParameter(Environment.PROP_DB_URL)
				);
				Environment.setValue(
					Environment.PROP_DB_USERNAME,
					request.getParameter(Environment.PROP_DB_USERNAME)
				);
				Encryption.setEncryptedProperty(
					Environment.PROP_DB_PASSWORD,
					request.getParameter(Environment.PROP_DB_PASSWORD)
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_MAX_ACTIVE,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_MAX_ACTIVE))
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_MAX_IDLE,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_MAX_IDLE))
				);
				Environment.setBooleanValue(
					Environment.PROP_DBCP_TEST_ON_BORROW,
					request.getParameter(Environment.PROP_DBCP_TEST_ON_BORROW) != null
				);
				Environment.setBooleanValue(
					Environment.PROP_DBCP_TEST_ON_RETURN,
					request.getParameter(Environment.PROP_DBCP_TEST_ON_RETURN) != null
				);
				Environment.setBooleanValue(
					Environment.PROP_DBCP_TEST_WHILE_IDLE,
					request.getParameter(Environment.PROP_DBCP_TEST_WHILE_IDLE) != null
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME))
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS))
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN))
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION))
				);
				Environment.setValue(
					Environment.PROP_DBCP_VALIDATION_QUERY,
					request.getParameter(Environment.PROP_DBCP_VALIDATION_QUERY)
				);
				Environment.setBooleanValue(
					Environment.PROP_DBCP_REMOVE_ABANDONED,
					request.getParameter(Environment.PROP_DBCP_REMOVE_ABANDONED) != null
				);
				Environment.setIntValue(
					Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT,
					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT))
				);
				Environment.setBooleanValue(
					Environment.PROP_DBCP_LOG_ABANDONED,
					request.getParameter(Environment.PROP_DBCP_LOG_ABANDONED) != null
				);
			}
			Environment.setBooleanValue(
				Environment.PROP_TOPIC_ALLOW_TEMPLATES,
				request.getParameter(Environment.PROP_TOPIC_ALLOW_TEMPLATES) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_TOPIC_USE_PREVIEW,
				request.getParameter(Environment.PROP_TOPIC_USE_PREVIEW) != null
			);
			Environment.setValue(
				Environment.PROP_BASE_DEFAULT_TOPIC,
				request.getParameter(Environment.PROP_BASE_DEFAULT_TOPIC)
			);
			Environment.setValue(
				Environment.PROP_PARSER_CLASS,
				request.getParameter(Environment.PROP_PARSER_CLASS)
			);
			int maxFileSizeInKB = Integer.parseInt(request.getParameter(Environment.PROP_ATTACH_MAX_FILE_SIZE));
			Environment.setIntValue(
				Environment.PROP_ATTACH_MAX_FILE_SIZE,
				maxFileSizeInKB * 1000
			);
			Environment.setValue(
				Environment.PROP_ATTACH_TYPE,
				request.getParameter(Environment.PROP_ATTACH_TYPE)
			);
			if (request.getParameter(Environment.PROP_DB_TYPE) != null) {
				Environment.setValue(
					Environment.PROP_DB_TYPE,
					request.getParameter(Environment.PROP_DB_TYPE)
				);
			}
			if (request.getParameter(Environment.PROP_BASE_SERVER_HOSTNAME) !=  null && !request.getParameter(Environment.PROP_BASE_SERVER_HOSTNAME).equals("")) {
				Environment.setValue(
					Environment.PROP_BASE_SERVER_HOSTNAME,
					request.getParameter(Environment.PROP_BASE_SERVER_HOSTNAME)
				);
			} else {
				Environment.setValue(Environment.PROP_BASE_SERVER_HOSTNAME, "");
			}
			Environment.setValue(
				Environment.PROP_FILE_ENCODING,
				request.getParameter(Environment.PROP_FILE_ENCODING)
			);
			Environment.setBooleanValue(
				Environment.PROP_PARSER_SEPARATE_WIKI_TITLE_WORDS,
				request.getParameter(Environment.PROP_PARSER_SEPARATE_WIKI_TITLE_WORDS) != null
			);
			Environment.setBooleanValue(
				Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY,
				request.getParameter(Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY) != null
			);
			int membershipType = Integer.parseInt(request.getParameter(Environment.PROP_USERGROUP_TYPE));
			String usergroupType;
			if (membershipType == WikiBase.LDAP) {
				usergroupType = "LDAP";
			} else if (membershipType == WikiBase.DATABASE) {
				usergroupType = "DATABASE";
			} else {
				usergroupType = "";
			}
			Environment.setValue(Environment.PROP_USERGROUP_TYPE, usergroupType);
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
				if (request.getParameter(autoFill[i]) != null) {
					if (autoFill[i].equals(Environment.PROP_USERGROUP_PASSWORD)) {
						Encryption.setEncryptedProperty(
							Environment.PROP_USERGROUP_PASSWORD,
							request.getParameter(autoFill[i])
						);
					} else {
						Environment.setValue(autoFill[i], request.getParameter(autoFill[i]));
					}
				}
			}
			if (Environment.getValue(Environment.PROP_FILE_HOME_DIR) == null) {
				// if home directory set empty, use system home directory
				String dir = System.getProperty("user.home") + System.getProperty("file.separator") + "wiki";
				Environment.setValue(Environment.PROP_FILE_HOME_DIR, dir);
			}
			if (WikiBase.getPersistenceType() == WikiBase.DATABASE) {
				// initialize connection pool in its own try-catch to avoid an error
				// causing property values not to be saved.
				try {
					DatabaseConnection.setPoolInitialized(false);
				} catch (Exception e) {
					this.message = e.getMessage();
				}
			}
			Environment.saveProperties();
			WikiBase.initialise();
			this.message = this.messages.getString("admin.message.changessaved");
		} catch (Exception e) {
			logger.error("Failure while processing property values", e);
			this.message = "Failure while processing property values: " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void purge(HttpServletRequest request, ModelAndView next) {
		try {
			Collection purged = WikiBase.getInstance().purgeDeletes(request.getParameter("virtualWiki"));
			StringBuffer buffer = new StringBuffer();
			ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
			cl.removeChanges(this.virtualWiki, purged);
			buffer.append("Purged: ");
			for (Iterator iterator = purged.iterator(); iterator.hasNext();) {
				String topicName = (String) iterator.next();
				buffer.append(topicName);
				buffer.append("; ");
			}
			this.message = buffer.toString();
		} catch (Exception e) {
			logger.error("Failure while purging topics", e);
			this.message = "Failure while purging topics: " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void purgeVersions(HttpServletRequest request, ModelAndView next) {
		try {
			DateFormat dateFormat = DateFormat.getInstance();
			DBDate date = new DBDate(dateFormat.parse(request.getParameter("purgedate")));
			WikiBase.getInstance().purgeVersionsOlderThan(this.virtualWiki, date);
		} catch (Exception e) {
			logger.error("Failure while purging versions", e);
			this.message = "Failure while purging versions: " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void refreshIndex(HttpServletRequest request, ModelAndView next) {
		try {
			WikiBase.getInstance().getSearchEngineInstance().refreshIndex();
			this.message = this.messages.getString("admin.message.indexrefreshed");
		} catch (Exception e) {
			logger.error("Failure while refreshing search index", e);
			this.message = "Failure while refreshing search index: " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void removeUser(HttpServletRequest request, ModelAndView next) {
		String user = request.getParameter("userName");
		try {
			WikiMembers members = WikiBase.getInstance().getWikiMembersInstance(this.virtualWiki);
			if (members.removeMember(user)) {
				this.message = user + this.messages.getString("admin.message.userremoved.success");
			} else {
				this.message = user + this.messages.getString("admin.message.userremoved.failure");
			}
		} catch (Exception e) {
			logger.error("Failure while removing user " + user, e);
			this.message = "Failure while removing user " + user + ": " + e.getMessage();
		}
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) {
	}
}
