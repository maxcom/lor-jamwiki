/**
 * @author garethc
 *  15/10/2002 15:06:53
 */
package org.vqwiki.servlets;

import org.vqwiki.Notify;
import org.vqwiki.WikiBase;
import org.vqwiki.WikiException;
import org.vqwiki.utils.JSPUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NotifyServlet extends HttpServlet {

    /**
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String virtualWiki = null;
        String topic = null;
        try {
            virtualWiki = (String) request.getAttribute("virtual-wiki");
            String action = request.getParameter("notify_action");
            topic = request.getParameter("topic");
            if (topic == null || topic.equals("")) {
                throw new WikiException("Topic must be specified");
            }
            String user = "";
            
            user = request.getParameter("username");
            
            if (user == null || user.equals("")) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    if (cookies.length > 0) {
                        for (int i = 0; i < cookies.length; i++) {
                            if (cookies[i].getName().equals("username")) {
                                user = cookies[i].getValue();
                            }
                        }
                    }
                }
            }
            if (user == null || user.equals("")) {
                throw new WikiException("User name not found.");
            }
            Notify notifier = WikiBase.getInstance().getNotifyInstance(virtualWiki, topic);
            if (action == null || action.equals("notify_on")) {
                notifier.addMember(user);
            } else {
                notifier.removeMember(user);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
        String next = JSPUtils.createLocalRootPath(request, virtualWiki) + "Wiki?" + topic;
        response.sendRedirect(response.encodeRedirectURL(next));
    }

    /**
     *
     */
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
        this.doPost(httpServletRequest, httpServletResponse);
    }
}
