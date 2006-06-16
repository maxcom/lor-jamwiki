package org.jmwiki.servlets;

import org.jmwiki.Topic;
import org.jmwiki.WikiBase;
import org.jmwiki.WikiException;
import org.jmwiki.utils.Utilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author garethc
 * Date: Jan 8, 2003
 */
public class AttachServlet extends JMWikiServlet {

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String topic = request.getParameter("topic");
		request.setAttribute("title", "Attach Files to " + topic);
		request.setAttribute("topic", topic);
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		String user = request.getRemoteAddr();
		if (Utilities.getUserFromRequest(request) != null) {
			user = Utilities.getUserFromRequest(request);
		}
		request.setAttribute("user", user);
		try {
			Topic t = new Topic(topic);
			if (t.isReadOnlyTopic(virtualWiki)) {
				error(request, response, new WikiException(WikiException.READ_ONLY));
				return;
			}
			if (topic == null || topic.equals("")) {
				throw new WikiException("Topic must be specified");
			}
			WikiBase base = WikiBase.getInstance();
			String key = request.getSession().getId();
			if (!base.lockTopic(virtualWiki, topic, key)) {
				throw new WikiException(WikiException.TOPIC_LOCKED);
			}
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		dispatch("/WEB-INF/jsp/attach.jsp", request, response);
	}
}
