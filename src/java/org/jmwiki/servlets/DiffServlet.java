package org.jmwiki.servlets;

import org.jmwiki.persistency.Topic;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * @author garethc
 *		 Date: Jan 8, 2003
 */
public class DiffServlet extends JMWikiServlet {

	/**
	 *
	 */
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		doGet(httpServletRequest, httpServletResponse);
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String virtualWiki = (String) request.getAttribute("virtualWiki");
		String topic = request.getParameter("topic");
		request.setAttribute("title", "Diff " + topic);
		request.setAttribute("topic", topic);
		try {
			Topic t = new Topic(topic);
			String diffType = request.getParameter("type");
			if (diffType != null && "arbitrary".equals(diffType)) {
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
					request.setAttribute("badinput", "true");
				} else {
					request.setAttribute(
						"diff", t.getDiff(
							virtualWiki,
							Math.min(firstVersion, secondVersion),
							Math.max(firstVersion, secondVersion),
							true
						)
					);
				}
			} else {
				request.setAttribute("diff", t.mostRecentDiff(virtualWiki, true));
			}
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_DIFF);
		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
	}
}
