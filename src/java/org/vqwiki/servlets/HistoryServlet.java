package org.vqwiki.servlets;

import org.vqwiki.TopicVersion;
import org.vqwiki.VersionManager;
import org.vqwiki.WikiBase;
import org.vqwiki.utils.Utilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author garethc
 * Date: Jan 10, 2003
 */
public class HistoryServlet extends VQWikiServlet {

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
                dispatch("/WEB-INF/jsp/history.jsp", request, response);
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
            dispatch("/WEB-INF/jsp/history.jsp", request, response);
        }
    }
}
