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
 * @author garethc
 * Date: 7/03/2003
 */
public class RecentChangesServlet extends VQWikiServlet {

	Collection all = null;
	private Object lock = new Object();
	private long lastReloadTime = 0;
	private static final Logger logger = Logger.getLogger(RecentChangesServlet.class);

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response)
						 throws ServletException, IOException
	{
		if (mustReload()) {
			try {
				String virtualWiki = (String)request.getAttribute("virtual-wiki");
				reload(virtualWiki);
			} catch (Exception e) {
				error(request, response, e);
				return;
			}
		}
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources",
														   request.getLocale());
		request.setAttribute("title", messages.getString("recentchanges.title"));
		request.setAttribute("changes", all);
		dispatch("/WEB-INF/jsp/recentChanges.jsp", request, response);
	}

	/**
	 *
	 */
	private void reload(String virtualWiki) throws Exception {
		synchronized (lock) {
			logger.debug("Reloading recent changes");
			Calendar cal = Calendar.getInstance();
			ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
			int n = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
			if (n == 0) {
				n = 5;
			}
			all = new ArrayList();
			for (int i = 0; i < n; i++) {
				Collection col = cl.getChanges(virtualWiki, cal.getTime());
				if (col != null)
				  all.addAll(col);
				cal.add(Calendar.DATE, -1);
			}
			lastReloadTime = System.currentTimeMillis();
		}
	}

	/**
	 *
	 */
	private long getDeltaMillisReload() {
		return Environment.getIntValue(Environment.PROP_RECENT_CHANGES_REFRESH_INTERVAL) * 1000 * 60;
	}

	/**
	 *
	 */
	private boolean mustReload() {
		long delta = getDeltaMillisReload();
		logger.debug("delta = " + delta);
		return ((System.currentTimeMillis() - lastReloadTime) > delta);
	}
}
