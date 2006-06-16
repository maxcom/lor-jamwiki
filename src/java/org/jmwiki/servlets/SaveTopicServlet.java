/*
 * Copyright 2002 Gareth Cronin
 * This software is subject to the GNU Public Licence
 */
package org.jmwiki.servlets;

import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmwiki.Change;
import org.jmwiki.ChangeLog;
import org.jmwiki.SearchEngine;
import org.jmwiki.Topic;
import org.jmwiki.WikiBase;
import org.jmwiki.persistency.file.FileChangeLog;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;
import org.apache.log4j.Logger;

public class SaveTopicServlet extends JMWikiServlet {

	private static final Logger logger = Logger.getLogger(SaveTopicServlet.class);

	/**
	 *
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("Request for save");
		String virtualWiki = null;
		String user = request.getRemoteAddr();
		logger.debug("user: " + user);
		if (Utilities.getUserFromRequest(request) != null) {
			user = Utilities.getUserFromRequest(request);
		}
		String topic = request.getParameter("topic");
		logger.debug("Saving topic: " + topic);
		if (topic == null) {
			WikiServletException err = new WikiServletException("Topic must be specified");
			error(request, response, err);
			return;
		}
		WikiBase base = null;
		try {
			base = WikiBase.getInstance();
			virtualWiki = (String) request.getAttribute("virtual-wiki");
			logger.debug("vwiki is " + virtualWiki);
		} catch (Exception err) {
			error(request, response, new WikiServletException(err.getMessage()));
			return;
		}
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", request.getLocale());
		String append = messages.getString("edit.action.append");
		if (append == null) append = "append";
		// First try the above templatelist.
		String templatevalue = request.getParameter("templateabove");
		String notemplate = Utilities.resource("edit.notemplate", request.getLocale());
		if (templatevalue != null) {
			// if it is not set try the below templatelist.
			if (templatevalue.equals(notemplate)) {
				templatevalue = request.getParameter("templatebelow");
			}
		}
		if ((templatevalue != null) && append.equals(request.getParameter("action"))) {
			logger.debug("appending template contents from template: " + templatevalue);
			if (!templatevalue.equals(notemplate)) {
				String templateContents = null;
				Collection templateNames = null;
				try {
					templateContents = base.getTemplate(virtualWiki, templatevalue);
					templateNames = base.getTemplates(virtualWiki);
				} catch (Exception e) {
					error(request, response, e);
					return;
				}
				StringBuffer buffer = new StringBuffer();
				buffer.append(request.getParameter("contents"));
				buffer.append(templateContents);
				request.setAttribute("contents", buffer.toString());
				String title = Utilities.resource("edit", request.getLocale()) + " " + topic;
				request.setAttribute("title", title);
				request.setAttribute("templateNames", templateNames);
				request.setAttribute("topic", topic);
				request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_EDIT);
				dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
				return;
			} else {
				Collection templateNames = null;
				try {
					templateNames = base.getTemplates(virtualWiki);
				} catch (Exception e) {
					error(request, response, e);
					return;
				}
				StringBuffer buffer = new StringBuffer();
				buffer.append(request.getParameter("contents"));
				request.setAttribute("contents", buffer.toString());
				String title = Utilities.resource("edit", request.getLocale()) + " " + topic;
				request.setAttribute("title", title);
				request.setAttribute("templateNames", templateNames);
				request.setAttribute("topic", topic);
				request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_EDIT);
				dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
				return;
			}
		}
		try {
			Topic t = new Topic(topic);
			if (t.isReadOnlyTopic(virtualWiki)) {
				throw new WikiServletException(WikiServletException.READ_ONLY);
			}
			String key = request.getSession().getId();
			if (!base.holdsLock(virtualWiki, topic, key)) {
				logger.debug("No lock for " + virtualWiki + "/" + topic + "/" + key);
				error(request, response, new WikiServletException(WikiServletException.LOCK_TIMEOUT));
				return;
			}
			String contents = request.getParameter("contents");
			if (contents == null) {
				error(request, response, new WikiServletException("Contents must be supplied"));
				return;
			}
			base.write(virtualWiki, contents, topic, user);
			if (request.getParameter("minorEdit") == null) {
				Change change = new Change();
				change.setTopic(topic);
				change.setUser(user);
				change.setTime(new java.util.Date());
				change.setVirtualWiki(virtualWiki);
				ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
				cl.logChange(change, request);
			}
			SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
			sedb.indexText(virtualWiki, topic, request.getParameter("contents"));
		} catch (Exception err) {
			logger.debug("Exception saving: " + err.toString());
			if (err instanceof java.io.InvalidClassException) {
			try {
				((FileChangeLog) WikiBase.getInstance().getChangeLogInstance()).deleteChangeTableFile(virtualWiki);
			} catch (Exception ignore) {
			}
			request.setAttribute("exception", new WikiServletException("Due to a change to the RecentChanges mechanism, " +
				"this upgrade is incompatible with previous recent changes, the offending file (wikihome/recent.hashtable) should now have been deleted, " +
				"please restart the app server"));
			} else {
				request.setAttribute("exception", new WikiServletException(err.toString()));
			}
			err.printStackTrace();
			RequestDispatcher dispatch = request.getRequestDispatcher("/WEB-INF/jsp/servlet-error.jsp");
			dispatch.forward(request, response);
		}
		try {
			base.unlockTopic(virtualWiki, topic);
		} catch (Exception err) {
			request.setAttribute("exception", new WikiServletException(err.toString()));
			err.printStackTrace();
			RequestDispatcher dispatch = request.getRequestDispatcher("/WEB-INF/jsp/servlet-error.jsp");
			dispatch.forward(request, response);
			return;
		}
		String next = null;
		response.setLocale(request.getLocale());
		next = JSPUtils.createRedirectURL(request, "Wiki?" + JSPUtils.encodeURL(topic));
		logger.debug("Creating redirect: " + next);
		logger.debug("Redirect URL: " + response.encodeRedirectURL(next));
		response.sendRedirect(response.encodeRedirectURL(next));
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "GET requests are not serviced in saves");
	}
}
