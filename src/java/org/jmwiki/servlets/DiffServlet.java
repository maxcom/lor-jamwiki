package org.jmwiki.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import org.apache.log4j.Logger;
import org.jmwiki.persistency.Topic;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class DiffServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(DiffServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		diff(request, next);
		return next;
	}

	/**
	 *
	 */
	protected void diff(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String topic = request.getParameter("topic");
		next.addObject("title", "Diff " + topic);
		next.addObject("topic", topic);
		try {
			Topic t = new Topic(topic);
			String diffType = request.getParameter("type");
			if (diffType != null && diffType.equals("arbitrary")) {
				int firstVersion = -1;
				int secondVersion = -1;
				Enumeration e = request.getParameterNames();
				while (e.hasMoreElements()) {
					String name = (String) e.nextElement();
					if (name.startsWith("diff:")) {
						int version = Integer.parseInt(name.substring(name.indexOf(":") + 1));
						if (firstVersion >= 0) {
							secondVersion = version;
						} else {
							firstVersion = version;
						}
					}
				}
				if (firstVersion == -1 || secondVersion == -1) {
					next.addObject("badinput", "true");
				} else {
					String diff = t.getDiff(virtualWiki, Math.min(firstVersion, secondVersion), Math.max(firstVersion, secondVersion), true);
					next.addObject("diff", diff);
				}
			} else {
				next.addObject("diff", t.mostRecentDiff(virtualWiki, true));
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_DIFF);
	}
}
