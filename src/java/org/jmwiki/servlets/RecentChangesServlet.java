package org.jmwiki.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.ChangeLog;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;

/**
 *
 */
public class RecentChangesServlet extends JMWikiServlet {

	private static final Logger logger = Logger.getLogger(RecentChangesServlet.class);

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
		if (request.getParameter("num") != null) {
			// FIXME - verify it's a number
			num = new Integer(request.getParameter("num")).intValue();
		}
		ArrayList all = null;
		try {
			String virtualWiki = (String)request.getAttribute("virtual-wiki");
			all = reload(virtualWiki, num);
		} catch (Exception e) {
			error(request, response, e);
			return;
		}
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", request.getLocale());
		request.setAttribute("title", messages.getString("recentchanges.title"));
		request.setAttribute("changes", all);
		request.setAttribute("num", new Integer(num));
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_RECENT_CHANGES);
		request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
	}

	/**
	 *
	 */
	private ArrayList reload(String virtualWiki, int num) throws Exception {
		// FIXME - this is hugely inefficient as it gets too many changes at once
		Calendar cal = Calendar.getInstance();
		ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
		ArrayList all = new ArrayList();
		for (int i = 0; i < num; i++) {
			Collection col = cl.getChanges(virtualWiki, cal.getTime());
			if (col != null) {
				all.addAll(col);
			}
			cal.add(Calendar.DATE, -1);
		}
		return (all.size() > num) ? new ArrayList(all.subList(0, num)) : all;
	}
}
