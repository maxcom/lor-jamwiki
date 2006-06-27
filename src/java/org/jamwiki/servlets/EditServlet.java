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

import java.io.IOException;
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
		WikiBase.removeCachedContents();
		view(request, next);
	}

	/**
	 *
	 */
	private void edit(HttpServletRequest request, ModelAndView next) throws Exception {
		request.getSession().setMaxInactiveInterval(60 * Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT));
		String topicName = JAMController.getTopicFromRequest(request);
		if (topicName == null || topicName.length() == 0) {
			// FIXME - hard coding
			throw new Exception("Invalid or missing topic name");
		}
		if (PseudoTopicHandler.getInstance().isPseudoTopic(topicName)) {
			throw new Exception(topicName + " " + JAMController.getMessage("edit.exception.pseudotopic", request.getLocale()));
		}
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			topic = new Topic();
			topic.setName(topicName);
		}
		if (topic.getReadOnly()) {
			// FIXME - hard coding
			throw new Exception("The topic " + topicName + " is read only");
		}
		if (topic.getAdminOnly()) {
			if (!Utilities.isAdmin(request)) {
				next.addObject(JAMController.PARAMETER_TITLE, JAMController.getMessage("login.title", request.getLocale()));
				String redirect = Utilities.buildInternalLink(
					request.getContextPath(),
					virtualWiki,
					"Special:Edit"
				);
				next.addObject("redirect", redirect);
				next.addObject(JAMController.PARAMETER_ACTION, JAMController.ACTION_LOGIN);
				next.addObject(JAMController.PARAMETER_SPECIAL, new Boolean(true));
				return;
			}
		}
		String key = request.getSession().getId();
		if (!WikiBase.getInstance().lockTopic(virtualWiki, topicName, key)) {
			// FIXME - hard coding
			throw new Exception("The topic " + topicName + " is locked");
		}
		String contents = null;
		String preview = null;
		if (isPreview(request)) {
			WikiBase.removeCachedContents();
			contents = (String)request.getParameter("contents");
		} else {
			contents = WikiBase.getInstance().readRaw(virtualWiki, topicName);
		}
		preview = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, contents);
		StringBuffer buffer = new StringBuffer();
		buffer.append(JAMController.getMessage("edit", request.getLocale()));
		buffer.append(" ");
		buffer.append(topicName);
		next.addObject(JAMController.PARAMETER_TITLE, buffer.toString());
		next.addObject("contents", contents);
		next.addObject("preview", preview);
		if (request.getAttribute(JAMController.ACTION_PREVIEW) != null) {
			next.addObject(JAMController.PARAMETER_ACTION, JAMController.ACTION_PREVIEW);
		} else {
			next.addObject(JAMController.PARAMETER_ACTION, JAMController.ACTION_EDIT);
		}
	}

	/**
	 *
	 */
	private boolean isCancel(HttpServletRequest request) {
		return isAction(request, "edit.action.cancel", JAMController.ACTION_CANCEL);
	}

	/**
	 *
	 */
	private boolean isPreview(HttpServletRequest request) {
		return isAction(request, "edit.action.preview", JAMController.ACTION_PREVIEW);
	}

	/**
	 *
	 */
	private boolean isSave(HttpServletRequest request) {
		return isAction(request, "edit.action.save", JAMController.ACTION_SAVE);
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
		next.addObject(JAMController.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(JAMController.PARAMETER_ACTION, JAMController.ACTION_LOGIN);
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
		WikiBase.removeCachedContents();
		String topicName = request.getParameter(JAMController.PARAMETER_TOPIC);
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String user = request.getRemoteAddr();
		if (Utilities.getUserFromRequest(request) != null) {
			user = Utilities.getUserFromRequest(request);
		}
		if (topicName == null) {
			logger.warn("Attempt to save null topic");
			// FIXME - hard coding
			throw new Exception("Topic must be specified");
		}
		Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			topic = new Topic();
			topic.setName(topicName);
		}
		if (topic.getReadOnly()) {
			logger.warn("The topic " + topicName + " is read only and cannot be saved");
			// FIXME - hard coding
			throw new Exception("The topic " + topicName + " is read only and cannot be saved");
		}
		String key = request.getSession().getId();
		if (!WikiBase.getInstance().holdsLock(virtualWiki, topicName, key)) {
			logger.warn("The lock on " + topicName + " has timed out");
			// FIXME - hard coding
			throw new Exception("The lock on " + topicName + " has timed out");
		}
		String contents = request.getParameter("contents");
		if (contents == null) {
			logger.warn("The topic " + topicName + " has no content");
			// FIXME - hard coding
			throw new Exception("The topic " + topicName + " has no content");
		}
		topic.setTopicContent(contents);
		WikiBase.getInstance().write(virtualWiki, contents, topicName, user, request.getRemoteAddr(), topic);
		if (request.getParameter("minorEdit") == null) {
			Change change = new Change();
			change.setTopic(topicName);
			change.setUser(user);
			change.setTime(new java.util.Date());
			change.setVirtualWiki(virtualWiki);
			ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
			cl.logChange(change, request);
		}
		SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
		sedb.indexText(virtualWiki, topicName, request.getParameter("contents"));
		WikiBase.getInstance().unlockTopic(virtualWiki, topicName);
		view(request, next);
	}

	/**
	 *
	 */
	// FIXME - duplicates the functionality in ViewController
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String topicName = request.getParameter(JAMController.PARAMETER_TOPIC);
		Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
		next.addObject(JAMController.PARAMETER_TITLE, topicName);
		// FIXME - what should the default be for topics that don't exist?
		String contents = "";
		if (topic != null) {
			contents = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, topic.getTopicContent());
		}
		next.addObject("contents", contents);
	}
}
