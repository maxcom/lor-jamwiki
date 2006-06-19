package org.jmwiki.servlets;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.persistency.TopicVersion;
import org.jmwiki.VersionManager;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class HistoryServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(HistoryServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		history(request, next);
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next) throws Exception {
		VersionManager manager;
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		String topicName = request.getParameter("topic");
		try {
			manager = WikiBase.getInstance().getVersionManagerInstance();
			String type = request.getParameter("type");
			if (type.equals("all")) {
				next.addObject("title", "History for " + topicName);
				Collection versions = manager.getAllVersions(virtualWiki, topicName);
				next.addObject("versions", versions);
				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
			} else if (type.equals("version")) {
				int versionNumber = Integer.parseInt(request.getParameter("versionNumber"));
				int numberOfVersions = manager.getNumberOfVersions(virtualWiki, topicName);
				TopicVersion topicVersion = manager.getTopicVersion(
					request.getContextPath(),
					virtualWiki,
					topicName,
					versionNumber
				);
				next.addObject("topicVersion", topicVersion);
				next.addObject("numberOfVersions", new Integer(numberOfVersions));
				next.addObject("title", topicName + " @" + Utilities.formatDateTime(topicVersion.getRevisionDate()));
				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
