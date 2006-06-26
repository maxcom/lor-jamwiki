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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Change;
import org.jamwiki.ChangeLog;
import org.jamwiki.Environment;
import org.jamwiki.PseudoTopicHandler;
import org.jamwiki.SearchEngine;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.model.Topic;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class EditServlet extends JAMController implements Controller {

	private static Logger logger = Logger.getLogger(EditServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		if (mustLogin(request)) {
			login(request, next);
		} else if (isSave(request)) {
			save(request, next);
		} else if (isCancel(request)) {
			cancel(request, next);
		} else if (isPreview(request)) {
			edit(request, next);
		} else {
			edit(request, next);
		}
		return next;
	}

	/**
	 *
	 */
	private void cancel(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JAMController.getTopicFromRequest(request);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		try {
			WikiBase.getInstance().unlockTopic(virtualWiki, topic);
		} catch (Exception err) {
			// FIXME - hard coding
			throw new Exception("Unable to unlock topic " + virtualWiki + "/" + topic);
		}
		// FIXME - the caching needs to be simplified
		WikiServlet.removeCachedContents();
		view(request, next);
	}

	/**
	 *
	 */
	private void edit(HttpServletRequest request, ModelAndView next) throws Exception {
		request.getSession().setMaxInactiveInterval(60 * Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT));
		String topic = JAMController.getTopicFromRequest(request);
		if (topic == null || topic.length() == 0) {
			// FIXME - hard coding
			throw new Exception("Invalid or missing topic");
		}
		if (PseudoTopicHandler.getInstance().isPseudoTopic(topic)) {
			throw new Exception(topic + " " + JAMController.getMessage("edit.exception.pseudotopic", request.getLocale()));
		}
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		Topic t = new Topic(topic);
		if (t.isReadOnlyTopic(virtualWiki)) {
			// FIXME - hard coding
			throw new Exception("The topic " + topic + " is read only");
		}
		if (WikiBase.getInstance().isAdminOnlyTopic(request.getLocale(), virtualWiki, topic)) {
			if (!Utilities.isAdmin(request)) {
				next.addObject(JAMController.PARAMETER_TITLE, JAMController.getMessage("login.title", request.getLocale()));
				String redirect = Utilities.buildInternalLink(
					request.getContextPath(),
					virtualWiki,
					"Special:Edit"
				);
				next.addObject("redirect", redirect);
				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
				next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
				return;
			}
		}
		String key = request.getSession().getId();
		if (!WikiBase.getInstance().lockTopic(virtualWiki, topic, key)) {
			// FIXME - hard coding
			throw new Exception("The topic " + topic + " is locked");
		}
		String contents = null;
		String preview = null;
		if (isPreview(request)) {
			WikiServlet.removeCachedContents();
			contents = (String)request.getParameter("contents");
		} else {
			contents = WikiBase.getInstance().readRaw(virtualWiki, topic);
		}
		preview = WikiBase.getInstance().cook(
			request.getContextPath(),
			virtualWiki,
			new BufferedReader(new StringReader(contents))
		);
		StringBuffer buffer = new StringBuffer();
		buffer.append(JAMController.getMessage("edit", request.getLocale()));
		buffer.append(" ");
		buffer.append(topic);
		next.addObject(JAMController.PARAMETER_TITLE, buffer.toString());
		next.addObject("contents", contents);
		next.addObject("preview", preview);
		if (request.getAttribute(WikiServlet.ACTION_PREVIEW) != null) {
			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_PREVIEW);
		} else {
			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_EDIT);
		}
	}

	/**
	 *
	 */
	private boolean isCancel(HttpServletRequest request) {
		return isAction(request, "edit.action.cancel", WikiServlet.ACTION_CANCEL);
	}

	/**
	 *
	 */
	private boolean isPreview(HttpServletRequest request) {
		return isAction(request, "edit.action.preview", WikiServlet.ACTION_PREVIEW);
	}

	/**
	 *
	 */
	private boolean isSave(HttpServletRequest request) {
		return isAction(request, "edit.action.save", WikiServlet.ACTION_SAVE);
	}

	/**
	 *
	 */
	private void login(HttpServletRequest request, ModelAndView next) throws Exception {
		try {
			List users = WikiBase.getInstance().getUsergroupInstance().getListOfAllUsers();
			next.addObject("userList", users);
		} catch (Exception e) { }
		String topic = request.getParameter(JAMController.PARAMETER_TOPIC);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
		String redirect = Utilities.buildInternalLink(
			request.getContextPath(),
			virtualWiki,
			"Special:Edit"
		);
		redirect += "?topic=" + Utilities.encodeURL(topic);
		next.addObject("redirect", redirect);
		return;
	}

	/**
	 *
	 */
	private boolean mustLogin(HttpServletRequest request) {
		return (Environment.getBooleanValue(Environment.PROP_TOPIC_FORCE_USERNAME) && Utilities.getUserFromRequest(request) == null);
	}


	/**
	 *
	 */
	private void save(HttpServletRequest request, ModelAndView next) throws Exception {
		// a save request has been made
		WikiServlet.removeCachedContents();
		String topic = request.getParameter(JAMController.PARAMETER_TOPIC);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String user = request.getRemoteAddr();
		if (Utilities.getUserFromRequest(request) != null) {
			user = Utilities.getUserFromRequest(request);
		}
		if (topic == null) {
			logger.warn("Attempt to save null topic");
			// FIXME - hard coding
			throw new Exception("Topic must be specified");
		}
		Topic t = new Topic(topic);
		if (t.isReadOnlyTopic(virtualWiki)) {
			logger.warn("The topic " + topic + " is read only and cannot be saved");
			// FIXME - hard coding
			throw new Exception("The topic " + topic + " is read only and cannot be saved");
		}
		String key = request.getSession().getId();
		if (!WikiBase.getInstance().holdsLock(virtualWiki, topic, key)) {
			logger.warn("The lock on " + topic + " has timed out");
			// FIXME - hard coding
			throw new Exception("The lock on " + topic + " has timed out");
		}
		String contents = request.getParameter("contents");
		if (contents == null) {
			logger.warn("The topic " + topic + " has no content");
			// FIXME - hard coding
			throw new Exception("The topic " + topic + " has no content");
		}
		WikiBase.getInstance().write(virtualWiki, contents, topic, user, request.getRemoteAddr());
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
		WikiBase.getInstance().unlockTopic(virtualWiki, topic);
		view(request, next);
	}

	/**
	 *
	 */
	// FIXME - duplicates the functionality in ViewController
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String topicName = request.getParameter(JAMController.PARAMETER_TOPIC);
		Topic topic = new Topic(topicName);
		topic.loadTopic(virtualWiki);
		next.addObject(JAMController.PARAMETER_TITLE, topicName);
		String contents = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, new BufferedReader(new StringReader(topic.getRenderedContent())));
		next.addObject("contents", contents);
	}
}
