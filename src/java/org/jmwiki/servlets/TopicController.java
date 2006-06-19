package org.jmwiki.servlets;

import java.io.BufferedReader;
import java.io.StringReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;
import org.jmwiki.persistency.Topic;
import org.jmwiki.servlets.WikiServlet;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class TopicController implements Controller {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(TopicController.class.getName());

	/**
	 * This method handles the request after its parent class receives control. It gets the topic's name and the
	 * virtual wiki name from the uri, loads the topic and returns a view to the end user.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		view(request, next);
		return next;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String topicName = JMController.getTopicFromURI(request);
		Topic topic = new Topic(topicName);
		topic.loadTopic(virtualWiki);
		next.addObject("topic", topicName);
		next.addObject("title", topicName);
		String contents = WikiBase.getInstance().cook(new BufferedReader(new StringReader(topic.getRenderedContent())), virtualWiki);
		next.addObject("contents", contents);
	}
}