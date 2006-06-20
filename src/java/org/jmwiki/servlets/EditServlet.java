package org.jmwiki.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.Change;
import org.jmwiki.ChangeLog;
import org.jmwiki.Environment;
import org.jmwiki.PseudoTopicHandler;
import org.jmwiki.SearchEngine;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiException;
import org.jmwiki.model.Topic;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class EditServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(EditServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
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
		String topic = JMController.getTopicFromRequest(request);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
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
		String topic = JMController.getTopicFromRequest(request);
		if (topic == null || topic.length() == 0) {
			// FIXME - hard coding
			throw new Exception("Invalid or missing topic");
		}
		if (PseudoTopicHandler.getInstance().isPseudoTopic(topic)) {
			throw new Exception(topic + " " + JMController.getMessage("edit.exception.pseudotopic", request.getLocale()));
		}
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		Topic t = new Topic(topic);
		if (t.isReadOnlyTopic(virtualWiki)) {
			// FIXME - hard coding
			throw new Exception("The topic " + topic + " is read only");
		}
		if (WikiBase.getInstance().isAdminOnlyTopic(request.getLocale(), virtualWiki, topic)) {
			if (!Utilities.isAdmin(request)) {
				next.addObject("title", JMController.getMessage("login.title", request.getLocale()));
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
		buffer.append(JMController.getMessage("edit", request.getLocale()));
		buffer.append(" ");
		buffer.append(topic);
		next.addObject("title", buffer.toString());
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
		String topic = request.getParameter(JMController.PARAMETER_TOPIC);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
		String redirect = Utilities.buildInternalLink(
			request.getContextPath(),
			virtualWiki,
			"Special:Edit"
		);
		redirect += "?topic=" + JSPUtils.encodeURL(topic);
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
		String topic = request.getParameter(JMController.PARAMETER_TOPIC);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
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
		WikiBase.getInstance().write(virtualWiki, contents, topic, user);
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
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String topicName = request.getParameter(JMController.PARAMETER_TOPIC);
		Topic topic = new Topic(topicName);
		topic.loadTopic(virtualWiki);
		next.addObject("title", topicName);
		String contents = WikiBase.getInstance().cook(request.getContextPath(), virtualWiki, new BufferedReader(new StringReader(topic.getRenderedContent())));
		next.addObject("contents", contents);
	}
}
