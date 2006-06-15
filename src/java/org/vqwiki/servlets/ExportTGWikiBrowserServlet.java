/**
 * @author Tobias Schulz-Hess (sourceforge@schulz-hess.de)
 *  12/04/2003 20:33:31
 */
package org.vqwiki.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.vqwiki.SearchEngine;
import org.vqwiki.WikiBase;
import org.vqwiki.utils.Utilities;

/**
 * This servlet exports the node file for the TGWikiBrowser.
 * You can add a parameter "virutal-wiki", which then generates
 * the node file on a particular virtual wiki.<p>
 *
 * For more details on TGWikiBrowser see:
 * http://touchgraph.sourceforge.net
 * <p>
 *
 * @author Tobias Schulz-Hess (sourceforge@schulz-hess.de)
 *
 * TODO: Create a zip containing the browser, a batch file plus the node file
 */
public class ExportTGWikiBrowserServlet extends HttpServlet {

    /** Logging */
    private static final Logger logger = Logger.getLogger(ExportTGWikiBrowserServlet.class);

    /**
     * Handle post request.
     * Generate a node file and send it back as text.
     *
     * @param httpServletRequest  The current http request
     * @param httpServletResponse What the servlet will send back as response
     *
     * @throws ServletException If something goes wrong during servlet execution
     * @throws IOException If the output stream cannot be accessed
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String virtualWiki = null;
        try {
            virtualWiki = (String) request.getAttribute("virtual-wiki");
            if (virtualWiki == null || virtualWiki.length() < 1) {
                virtualWiki = WikiBase.DEFAULT_VWIKI;
            }
            StringBuffer result = new StringBuffer();
            WikiBase wb = WikiBase.getInstance();
            SearchEngine sedb = wb.getSearchEngineInstance();
            Collection all = sedb.getAllTopicNames(virtualWiki);
            Iterator allIterator = all.iterator();
            while (allIterator.hasNext()) {
                StringBuffer oneline = new StringBuffer();
                String topicname = (String) allIterator.next();
                oneline.append(topicname);
                String content = wb.readCooked(virtualWiki, topicname);
                String searchfor = "href=\"Wiki?";
                int iPos = content.indexOf(searchfor);
                int iEndPos = content.indexOf(Utilities.resource("topic.ismentionedon", request.getLocale()));
                if (iEndPos == -1) iEndPos = Integer.MAX_VALUE;
                while (iPos > -1 && iPos < iEndPos) {
                    String link = content.substring(iPos + searchfor.length(),
                        content.indexOf('"', iPos + searchfor.length())
                    );
                    if (link.indexOf('&') > -1) {
                        link = link.substring(0, link.indexOf('&'));
                    }
                    if (link.length() > 3 && !link.startsWith("topic=") &&
                        !link.startsWith("action=") && !topicname.equals(link)) {
                        oneline.append(" ").append(link);
                    }
                    iPos = content.indexOf(searchfor, iPos + 10);
                }
                logger.debug(oneline.toString());
                result.append(oneline).append("\n");
            }
            response.setContentType("text/plain");
            response.setHeader("Expires", "0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Keep-Alive", "timeout=15, max=100");
            response.setHeader("Connection", "Keep-Alive");
            response.setContentLength(result.length());
            OutputStream out = response.getOutputStream();
            StringReader source = new StringReader(result.toString());
            int copied;
            while ((copied = source.read()) != -1) {
                out.write(copied);
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    /**
     * Handle get request.
     * The request is handled the same way as the post request.
     *
     * @see doPost()
     *
     * @param httpServletRequest  The current http request
     * @param httpServletResponse What the servlet will send back as response
     *
     * @throws ServletException If something goes wrong during servlet execution
     * @throws IOException If the output stream cannot be accessed
     *
     */
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
        this.doPost(httpServletRequest, httpServletResponse);
    }
}
