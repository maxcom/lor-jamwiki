/**
 * @author garethc
 *  5/09/2002 13:03:32
 */
package org.vqwiki.servlets;

import org.apache.log4j.Logger;
import org.vqwiki.WikiBase;
import org.vqwiki.utils.JSPUtils;
import org.vqwiki.utils.Utilities;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SaveTemplateServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SaveTemplateServlet.class);

    /**
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Request for save template");
        String virtualWiki = null;
        String topic = request.getParameter("topic");
        String templateName = request.getParameter("save-template");
        logger.debug("Saving template: " + templateName);
        if (templateName == null || templateName.equals("")) {
            throw new WikiServletException("Template name must be specified");
        }
        WikiBase base = null;
        try {
            virtualWiki = (String) request.getAttribute("virtual-wiki");
            if (virtualWiki == null) {
                virtualWiki = Utilities.extractVirtualWiki(request.getRequestURI());
            }
            logger.debug("vwiki is " + virtualWiki);
            base = WikiBase.getInstance();
            base.unlockTopic(virtualWiki, topic);
        } catch (Exception err) {
            logger.error("error unlocking topic: " + err);
            request.setAttribute("exception", new WikiServletException(err.toString()));
            RequestDispatcher dispatch = request.getRequestDispatcher("/jsp/servlet-error.jsp");
            dispatch.forward(request, response);
        }
        try {
            base.saveAsTemplate(virtualWiki, templateName, request.getParameter("contents"));
            String next = JSPUtils.createLocalRootPath(request, virtualWiki) + "Wiki?" + topic;
            logger.debug("Creating redirect: " + next);
            logger.debug("Redirect URL: " + response.encodeRedirectURL(next));
            response.sendRedirect(response.encodeRedirectURL(next));
        } catch (Exception err) {
            request.setAttribute("exception", new WikiServletException(err.toString()));
            err.printStackTrace();
            RequestDispatcher dispatch = request.getRequestDispatcher("/jsp/servlet-error.jsp");
            dispatch.forward(request, response);
        }
    }
}
