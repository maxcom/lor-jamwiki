package org.jmwiki.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.ChangeLog;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class RecentChangesServlet extends JMController implements Controller {

	private static final Logger logger = Logger.getLogger(RecentChangesServlet.class);

	/**
	 *
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		recentChanges(request, next);
		return next;
	}

	/**
	 *
	 */
	private void recentChanges(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		next.addObject("title", JMController.getMessage("recentchanges.title", request.getLocale()));
		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
		if (request.getParameter("num") != null) {
			// FIXME - verify it's a number
			num = new Integer(request.getParameter("num")).intValue();
		}
		ArrayList all = null;
		try {
			all = reload(virtualWiki, num);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		request.setAttribute("changes", all);
		request.setAttribute("num", new Integer(num));
		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_RECENT_CHANGES);
		request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
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
