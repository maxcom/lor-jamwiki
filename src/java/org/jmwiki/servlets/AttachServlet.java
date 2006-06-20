/**
 *
 */
package org.jmwiki.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.model.Topic;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class AttachServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(AttachServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		attach(request, next);
		return next;
	}

	/**
	 *
	 */
	private void attach(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JMController.getTopicFromRequest(request);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		next.addObject("title", "Attach Files to " + topic);
		next.addObject("topic", topic);
		String user = request.getRemoteAddr();
		if (Utilities.getUserFromRequest(request) != null) {
			user = Utilities.getUserFromRequest(request);
		}
		next.addObject("user", user);
		try {
			Topic t = new Topic(topic);
			if (t.isReadOnlyTopic(virtualWiki)) {
				logger.warn("Topic " + topic + " is read only");
				// FIXME - hard coding
				throw new Exception("Topic " + topic + " is read only");
			}
			if (topic == null || topic.equals("")) {
				throw new Exception("Topic must be specified");
			}
			String key = request.getSession().getId();
			if (!WikiBase.getInstance().lockTopic(virtualWiki, topic, key)) {
				// FIXME - hard coding
				throw new Exception("Topic " + topic + " is locked");
			}
		} catch (Exception e) {
			logger.error("Failure while getting attachment topic info for " + topic, e);
			// FIXME - hard coding
			throw new Exception("Failure while getting attachment topic info for " + topic + " " + e.getMessage());
		}
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_ATTACH);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}
}
