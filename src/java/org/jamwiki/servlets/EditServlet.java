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
import org.jamwiki.Environment;
import org.jamwiki.PseudoTopicHandler;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.search.SearchEngine;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class EditServlet extends JAMWikiServlet implements Controller {

	private static Logger logger = Logger.getLogger(EditServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMWikiServlet.buildLayout(request, next);
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
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
		try {
			WikiBase.getInstance().getHandler().unlockTopic(topic);
		} catch (Exception err) {
			// FIXME - hard coding
			throw new Exception("Unable to unlock topic " + virtualWiki + "/" + topicName);
		}
		// FIXME - the caching needs to be simplified
		JAMWikiServlet.removeCachedContents();
		// refresh layout
		JAMWikiServlet.buildLayout(request, next);
		view(request, next);
	}

	/**
	 *
	 */
	private void edit(HttpServletRequest request, ModelAndView next) throws Exception {
		request.getSession().setMaxInactiveInterval(60 * Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT));
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		if (!StringUtils.hasText(topicName)) {
			// FIXME - hard coding
			throw new Exception("Invalid or missing topic name");
		}
		if (PseudoTopicHandler.getInstance().isPseudoTopic(topicName)) {
			throw new Exception(topicName + " " + Utilities.getMessage("edit.exception.pseudotopic", request.getLocale()));
		}
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
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
				next.addObject(JAMWikiServlet.PARAMETER_TITLE, Utilities.getMessage("login.title", request.getLocale()));
				String redirect = Utilities.buildInternalLink(
					request.getContextPath(),
					virtualWiki,
					"Special:Edit"
				);
				next.addObject("redirect", redirect);
				next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_LOGIN);
				next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
				return;
			}
		}
		String key = request.getSession().getId();
		if (!WikiBase.getInstance().getHandler().lockTopic(virtualWiki, topicName, key)) {
			// FIXME - hard coding
			throw new Exception("The topic " + topicName + " is locked");
		}
		String contents = null;
		String editComment = null;
		boolean minorEdit = false;
		String preview = null;
		if (isPreview(request)) {
			JAMWikiServlet.removeCachedContents();
			// refresh layout
			JAMWikiServlet.buildLayout(request, next);
			contents = (String)request.getParameter("contents");
			editComment = (String)request.getParameter("editComment");
			minorEdit = (request.getParameter("minorEdit") != null);
		} else {
			contents = WikiBase.getInstance().readRaw(virtualWiki, topicName);
		}
		preview = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, contents);
		StringBuffer buffer = new StringBuffer();
		buffer.append(Utilities.getMessage("edit", request.getLocale()));
		buffer.append(" ");
		buffer.append(topicName);
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, buffer.toString());
		next.addObject("contents", contents);
		next.addObject("editComment", editComment);
		next.addObject("minorEdit", new Boolean(minorEdit));
		next.addObject("preview", preview);
		if (request.getAttribute(JAMWikiServlet.ACTION_PREVIEW) != null) {
			next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_PREVIEW);
		} else {
			next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_EDIT);
		}
	}

	/**
	 *
	 */
	private boolean isCancel(HttpServletRequest request) {
		return isAction(request, "edit.action.cancel", JAMWikiServlet.ACTION_CANCEL);
	}

	/**
	 *
	 */
	private boolean isPreview(HttpServletRequest request) {
		return isAction(request, "edit.action.preview", JAMWikiServlet.ACTION_PREVIEW);
	}

	/**
	 *
	 */
	private boolean isSave(HttpServletRequest request) {
		return isAction(request, "edit.action.save", JAMWikiServlet.ACTION_SAVE);
	}

	/**
	 *
	 */
	private void login(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String page = JAMWikiServlet.getTopicFromURI(request);
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, Utilities.getMessage("login.title", request.getLocale()));
		String redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, page);
		if (request.getQueryString() != null) {
			redirect += "?" + request.getQueryString();
		}
		next.addObject("redirect", redirect);
		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_LOGIN);
		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Login");
	}

	/**
	 *
	 */
	private boolean mustLogin(HttpServletRequest request) {
		return (Environment.getBooleanValue(Environment.PROP_TOPIC_FORCE_USERNAME) && Utilities.currentUser(request) == null);
	}


	/**
	 *
	 */
	private void save(HttpServletRequest request, ModelAndView next) throws Exception {
		String topicName = request.getParameter(JAMWikiServlet.PARAMETER_TOPIC);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		if (topicName == null) {
			logger.warn("Attempt to save null topic");
			// FIXME - hard coding
			throw new Exception("Topic must be specified");
		}
		Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
		TopicVersion topicVersion = new TopicVersion();
		if (topic == null) {
			topic = new Topic();
			topic.setName(topicName);
			topic.setVirtualWiki(virtualWiki);
		}
		if (topic.getReadOnly()) {
			logger.warn("The topic " + topicName + " is read only and cannot be saved");
			// FIXME - hard coding
			throw new Exception("The topic " + topicName + " is read only and cannot be saved");
		}
		String key = request.getSession().getId();
		if (!WikiBase.getInstance().getHandler().holdsLock(virtualWiki, topicName, key)) {
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
		topicVersion.setVersionContent(contents);
		topicVersion.setEditComment(request.getParameter("editComment"));
		topicVersion.setAuthorIpAddress(request.getRemoteAddr());
		WikiUser user = Utilities.currentUser(request);
		if (user != null) {
			topicVersion.setAuthorId(new Integer(user.getUserId()));
		}
		WikiBase.getInstance().getHandler().write(topic, topicVersion);
		// a save request has been made
		JAMWikiServlet.removeCachedContents();
		// refresh layout
		JAMWikiServlet.buildLayout(request, next);
		SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
		sedb.indexText(virtualWiki, topicName, request.getParameter("contents"));
		view(request, next);
	}

	/**
	 *
	 */
	// FIXME - duplicates the functionality in ViewController
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String topicName = request.getParameter(JAMWikiServlet.PARAMETER_TOPIC);
		Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, topicName);
		// FIXME - what should the default be for topics that don't exist?
		String contents = "";
		if (topic != null) {
			contents = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, topic.getTopicContent());
		}
		next.addObject("contents", contents);
	}
}
