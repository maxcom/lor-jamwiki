package org.jmwiki.servlets;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jmwiki.persistency.TopicVersion;
import org.jmwiki.VersionManager;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;

/**
 * @author garethc
 * Date: Jan 10, 2003
 */
public class HistoryServlet extends JMWikiServlet {

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		VersionManager manager;
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		String topicName = request.getParameter("topic");
		try {
			manager = WikiBase.getInstance().getVersionManagerInstance();
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		String type = request.getParameter("type");
		if (type.equals("all")) {
			request.setAttribute("title", "History for " + topicName);
			try {
				Collection versions = manager.getAllVersions(virtualWiki, topicName);
				request.setAttribute("versions", versions);
				request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
				dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
			} catch (Exception e) {
				error(request, response, e);
				return;
			}
		} else if (type.equals("version")) {
			int versionNumber = Integer.parseInt(request.getParameter("versionNumber"));
			try {
				int numberOfVersions = manager.getNumberOfVersions(virtualWiki, topicName);
				TopicVersion topicVersion = manager.getTopicVersion(
					virtualWiki,
					topicName,
					versionNumber
				);
				request.setAttribute("topicVersion", topicVersion);
				request.setAttribute("numberOfVersions", new Integer(numberOfVersions));
				request.setAttribute("title", topicName + " @" + Utilities.formatDateTime(topicVersion.getRevisionDate()));
			} catch (Exception e) {
				error(request, response, e);
				return;
			}
			request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_HISTORY);
			dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
		}
	}
}
