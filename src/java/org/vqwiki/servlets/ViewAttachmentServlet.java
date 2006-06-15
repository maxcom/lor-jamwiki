/**
 * Delivers an attachment to the browser. Sends as a binary stream along with a content-type
 * determined by the mime.types file in the classpath.
 *
 * @author garethc
 * 25/10/2002 14:18:32
 */
package org.vqwiki.servlets;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.vqwiki.Environment;
import org.vqwiki.utils.Utilities;

public class ViewAttachmentServlet extends VQWikiServlet {

    private static final Logger logger = Logger.getLogger(ViewAttachmentServlet.class);

    /**
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String attachmentName = request.getParameter("attachment");
        String virtualWiki = (String) request.getAttribute("virtual-wiki");
        File uploadPath = Utilities.uploadPath(virtualWiki, attachmentName);
        response.reset();
        // attachments can be "inline" or "attachment"
        response.setHeader(
            "Content-Disposition", Environment.getValue(Environment.PROP_ATTACH_TYPE) +
            ";filename=" + attachmentName + ";"
        );
        if (attachmentName.indexOf('.') >= 0) {
            if (attachmentName.indexOf('.') < attachmentName.length() - 1) {
                String extension = attachmentName.substring(attachmentName.lastIndexOf('.') + 1);
                logger.debug("Extension: " + extension);
                try {
                    String mimetype = (String) getMimeByExtension().get(extension.toLowerCase());
                    logger.debug("MIME: " + mimetype);
                    if (mimetype != null) {
                        logger.debug("Setting content type to: " + mimetype);
                        response.setContentType(mimetype);
                    }
                } catch (Exception e) {
                    error(request, response, new WikiServletException(e.getMessage()));
                    return;
                }
            }
        }
        FileInputStream in = new FileInputStream(uploadPath);
        ServletOutputStream out = response.getOutputStream();
        int bytesRead = 0;
        byte byteArray[] = new byte[4096];
        // Read in bytes through file stream, and write out through servlet stream
        while ((bytesRead = in.read(byteArray)) != -1) {
            out.write(byteArray, 0, bytesRead);
        }
        in.close();
        out.flush();
        out.close();
    }

    /**
     *
     */
    protected HashMap getMimeByExtension() throws Exception {
        HashMap map = new HashMap();
        InputStream resourceAsStream = getClass().getResourceAsStream("/mime.types");
        if (resourceAsStream == null) {
            logger.warn("couldn't find the MIME types file mime.types");
            return map;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(
            resourceAsStream
        ));
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            if (!line.startsWith("#") && !line.trim().equals("")) {
                StringTokenizer tokens = new StringTokenizer(line);
                if (tokens.hasMoreTokens()) {
                    String type = tokens.nextToken();
                    while (tokens.hasMoreTokens()) {
                        map.put(tokens.nextToken(), type);
                    }
                }
            }
        }
        return map;
    }
}
