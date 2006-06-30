/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
//import org.jamwiki.ChangeLog;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class RecentChangesServlet extends JAMWikiServlet implements Controller {

	private static final Logger logger = Logger.getLogger(RecentChangesServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMWikiServlet.buildLayout(request, next);
		recentChanges(request, next);
		return next;
	}

	/**
	 *
	 */
	private void recentChanges(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		next.addObject(JAMWikiServlet.PARAMETER_TITLE, JAMWikiServlet.getMessage("recentchanges.title", request.getLocale()));
		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
		if (request.getParameter("num") != null) {
			// FIXME - verify it's a number
			num = new Integer(request.getParameter("num")).intValue();
		}
		Collection all = null;
		try {
			all = WikiBase.getInstance().getHandler().getRecentChanges(virtualWiki, num);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		request.setAttribute("changes", all);
		request.setAttribute("num", new Integer(num));
		request.setAttribute(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_RECENT_CHANGES);
		request.setAttribute(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
//	private ArrayList getRecentChanges(String virtualWiki, int num) throws Exception {
//		// FIXME - this is hugely inefficient as it gets too many changes at once
//		Calendar cal = Calendar.getInstance();
//		ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
//		ArrayList all = new ArrayList();
//		for (int i = 0; i < num; i++) {
//			Collection col = cl.getChanges(virtualWiki, cal.getTime());
//			if (col != null) {
//				all.addAll(col);
//			}
//			cal.add(Calendar.DATE, -1);
//		}
//		return (all.size() > num) ? new ArrayList(all.subList(0, num)) : all;
//	}
}
