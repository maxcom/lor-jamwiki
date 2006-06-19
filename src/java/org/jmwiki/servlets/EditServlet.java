package org.jmwiki.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jmwiki.Environment;
import org.jmwiki.PseudoTopicHandler;
import org.jmwiki.persistency.Topic;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiException;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author garethc
 *		 Date: Jan 8, 2003
 */
public class EditServlet extends JMController implements Controller {

	/**
	 *
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		if (request.getMethod() != null && request.getMethod().equalsIgnoreCase("GET")) {
			this.doGet(request, response);
		} else {
			this.doPost(request, response);
		}
		return null;
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().setMaxInactiveInterval(60 * Environment.getIntValue(Environment.PROP_TOPIC_EDIT_TIME_OUT));
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", request.getLocale());
		String topic = request.getParameter("topic");
		if (topic == null) {
			if (request.getAttribute("topic") != null) {
				topic = (String) request.getAttribute("topic");
			}
		}
		if (PseudoTopicHandler.getInstance().isPseudoTopic(topic)) {
			error(
				request,
				response,
				new WikiServletException(topic + " " + messages.getString("edit.exception.pseudotopic"))
			);
			return;
		}
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		try {
			if (virtualWiki == null) {
				virtualWiki = Utilities.extractVirtualWiki(request);
			}
			Topic t = new Topic(topic);
			if (t.isReadOnlyTopic(virtualWiki)) {
				error(request, response, new WikiServletException(WikiException.READ_ONLY));
				return;
			}
			if (WikiBase.getInstance().isAdminOnlyTopic(request.getLocale(), virtualWiki, topic)) {
				if (!Utilities.isAdmin(request)) {
					request.setAttribute("title", Utilities.resource("login.title", request.getLocale()));
					String rootPath = JSPUtils.createLocalRootPath(request, virtualWiki);
					StringBuffer buffer = new StringBuffer();
					buffer.append(rootPath);
					buffer.append("Wiki?" + topic);
					request.setAttribute(
						"redirect",
						buffer.toString()
					);
					request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
					dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
					return;
				}
			}
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		if (topic == null || topic.equals("")) {
			error(
				request, response,
				new WikiServletException(messages.getString("edit.exception.notopic"))
			);
			return;
		}
		WikiBase base = null;
		try {
			base = WikiBase.getInstance();
			String key = request.getSession().getId();
			if (!base.lockTopic(virtualWiki, topic, key)) {
				error(request, response, new WikiServletException(WikiException.TOPIC_LOCKED));
				return;
			}
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		String contents = null;
		String preview = null;
		Collection templateNames = null;
		try {
			if (request.getAttribute(WikiServlet.ACTION_PREVIEW) != null) {
				request.removeAttribute(WikiServlet.ACTION_PREVIEW);
				contents = (String) request.getParameter("contents");
			} else {
				contents = base.readRaw(virtualWiki, topic);
			}
			templateNames = base.getTemplates(virtualWiki);
			preview = base.cook(new BufferedReader(new StringReader(contents)), virtualWiki);
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(messages.getString("edit"));
		buffer.append(" ");
		buffer.append(topic);
		request.setAttribute("title", buffer.toString());
		request.setAttribute("templateNames", templateNames);
		request.setAttribute("contents", contents);
		request.setAttribute("preview", preview);
		request.setAttribute("topic", topic);
		if (request.getAttribute(WikiServlet.ACTION_PREVIEW) != null) {
			request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_PREVIEW);
		} else {
			request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_EDIT);
		}
		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
	}

	/**
	 *
	 */
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		this.doGet(httpServletRequest, httpServletResponse);
	}
}
