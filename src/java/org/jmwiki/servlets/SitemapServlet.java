/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jmwiki.servlets;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jmwiki.Environment;
import org.jmwiki.PseudoTopicHandler;
import org.jmwiki.WikiBase;
import org.jmwiki.servlets.beans.SitemapBean;
import org.jmwiki.servlets.beans.SitemapLineBean;
import org.jmwiki.servlets.beans.StatisticsVWikiBean;
import org.jmwiki.utils.Utilities;

/**
 * This servlet provides some general statisitcs for the usage of wiki
 *
 * This class was created on 09:34:30 19.07.2003
 *
 * @author $Author: wrh2 $
 */
public class SitemapServlet extends LongLastingOperationServlet {

	/** Logging */
	private static final Logger logger = Logger.getLogger(SitemapServlet.class);
	public static final String LAST_IN_LIST = "e";
	public static final String MORE_TO_COME = "x";
	public static final String HORIZ_LINE = "a";
	public static final String NOTHING = "s";
	private StatisticsVWikiBean vwikis;
	private int allWikiSize;
	private int allWikiCount;
	private int numPages;
	private int pageCount;

	/**
	 * Constructor
	 *
	 *
	 */
	public SitemapServlet() {
		super();
	}

	/**
	 * Handle post request.
	 * Generate a RSS feed and send it back as XML.
	 *
	 * @param request  The current http request
	 * @param response What the servlet will send back as response
	 *
	 * @throws ServletException If something goes wrong during servlet execution
	 * @throws IOException If the output stream cannot be accessed
	 *
	 */
	public void run() {
		vwikis = new StatisticsVWikiBean();
		NumberFormat nf = NumberFormat.getInstance(locale);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(1);
		Collection allWikis;
		try {
			allWikis = WikiBase.getInstance().getVirtualWikiList();
		} catch (Exception e) {
			allWikis = Collections.EMPTY_LIST;
		}
		if (!allWikis.contains(WikiBase.DEFAULT_VWIKI)) {
			allWikis.add(WikiBase.DEFAULT_VWIKI);
		}
		String endString = Utilities.resource("topic.ismentionedon", locale);
		allWikiCount = 0;
		allWikiSize = allWikis.size();
		numPages = 0;
		for (Iterator iterator = allWikis.iterator(); iterator.hasNext(); allWikiCount++) {
			pageCount = 0;
			setProgress();
			String currentWiki = (String) iterator.next();
			List sitemapLines = new ArrayList();
			Vector visitedPages = new Vector();
			try {
				numPages = WikiBase.getInstance().getSearchEngineInstance().getAllTopicNames(currentWiki).size();
			} catch (Exception e1) {
				numPages = 1;
			}
			// get starting point
			String startTopic = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
			if (startTopic == null || startTopic.length() < 2) {
				startTopic = "StartingPoints";
			}
			List startingList = new ArrayList(1);
			startingList.add(LAST_IN_LIST);
			parsePages(currentWiki, startTopic, startingList, "1", sitemapLines, visitedPages, endString);
			SitemapBean onewiki = new SitemapBean();
			onewiki.setName(currentWiki);
			onewiki.setPages(sitemapLines);
			vwikis.getVwiki().add(onewiki);
		}
		progress = PROGRESS_DONE;
	}

	/**
	 * Set the progress
	 * @param allWikiCount Current wiki, we are processing
	 * @param allWikiSize  Number of wikis (overall)
	 * @param pageCount Current page we are processing
	 * @param pageSize Number of pages of this wiki
	 */
	private void setProgress() {
		if (numPages == 0) numPages = 1;
		double one = 100.0 / (double) allWikiSize;
		progress = Math.min((int) ((double) allWikiCount * one + (double) pageCount * one / (double) numPages), 99);
	}

	/**
	 * We are done. Go to result page.
	 * @see jmwiki.servlets.LongLastingOperationServlet#dispatchDone(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void dispatchDone(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("virtualwikis", vwikis);
		dispatch("/WEB-INF/jsp/sitemap.jsp", request, response);
	}

	/**
	 * Parse the pages starting with startTopic. The results
	 * are stored in the list sitemapLines. This functions is
	 * called recursivly, but the list is filled in the
	 * correct order.
	 *
	 * @param currentWiki  name of the wiki to refer to
	 * @param startTopic  Start with this page
	 * @param level   A list indicating the images to use to represent certain levels
	 * @param group The group, we are representing
	 * @param sitemapLines  A list of all lines, which results in the sitemap
	 * @param visitedPages  A vector of all pages, which already have been visited
	 * @param endString Beyond this text we do not search for links
	 */
	private void parsePages(String currentWiki, String topic, List levelsIn, String group, List sitemapLines, Vector visitedPages, String endString) {
		try {
			WikiBase base = WikiBase.getInstance();
			String onepage = base.readCooked(currentWiki, topic);
			List result = new ArrayList();
			List levels = new ArrayList(levelsIn.size());
			for (int i = 0; i < levelsIn.size(); i++) {
				if ((i + 1) < levelsIn.size()) {
					if (MORE_TO_COME.equals((String) levelsIn.get(i))) {
						levels.add(HORIZ_LINE);
					} else if (LAST_IN_LIST.equals((String) levelsIn.get(i))) {
						levels.add(NOTHING);
					} else {
						levels.add(levelsIn.get(i));
					}
				} else {
					levels.add(levelsIn.get(i));
				}
			}
			if (onepage != null) {
				// if we are at a page, which was already visited, forget about its children.
				//if (visitedPages.contains(topic))
				//	return;
				String searchfor = "href=\"Wiki?";
				int iPos = onepage.indexOf(searchfor);
				int iEndPos;
				if (endString == null || endString.trim().length() == 0) {
					iEndPos = Integer.MAX_VALUE;
				} else {
					iEndPos = onepage.indexOf(endString);
					if (iEndPos == -1) iEndPos = Integer.MAX_VALUE;
				}
				while (iPos > -1 && iPos < iEndPos) {
					String link = onepage.substring(iPos + searchfor.length(),
						onepage.indexOf('"', iPos + searchfor.length()));
					if (link.indexOf('&') > -1) {
						link = link.substring(0, link.indexOf('&'));
					}
					if (link.length() > 3 &&
						!link.startsWith("topic=") &&
						!link.startsWith("action=") &&
						!visitedPages.contains(link) &&
						!PseudoTopicHandler.getInstance().isPseudoTopic(link)) {
						result.add(link);
						visitedPages.add(link);
					}
					iPos = onepage.indexOf(searchfor, iPos + 10);
				}
				// add a sitemap line
				SitemapLineBean slb = new SitemapLineBean();
				slb.setTopic(topic);
				slb.setLevels(new ArrayList(levels));
				slb.setGroup(group);
				slb.setHasChildren(result.size() > 0);
				sitemapLines.add(slb);
				pageCount++;
				setProgress();
				for (int i = 0; i < result.size(); i++) {
					String link = (String) result.get(i);
					String newGroup = group + "_" + String.valueOf(i);
					boolean isLast = ((i + 1) == result.size());
					if (isLast) {
						levels.add(LAST_IN_LIST);
					} else {
						levels.add(MORE_TO_COME);
					}
					parsePages(currentWiki, link, levels, newGroup, sitemapLines, visitedPages, endString);
					levels.remove(levels.size() - 1);
				}
			}
		} catch (Exception e) {
			logger.fatal("Exception", e);
		}
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.7  2006/04/23 06:36:56  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.6  2003/10/05 05:07:32  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.5  2003/07/23 13:45:19  mrgadget4711
 * ADD: progress information
 *
 * Revision 1.4  2003/07/23 00:34:26  mrgadget4711
 * ADD: Long lasting operations
 *
 * Revision 1.3  2003/07/21 20:58:37  mrgadget4711
 * ADD: Dynamically open / close subtrees in IE (using DHTML)
 *
 * Revision 1.2  2003/07/21 09:19:39  mrgadget4711
 * Fixes
 *
 * Revision 1.1  2003/07/20 20:34:40  mrgadget4711
 * ADD: Sitemap
 *
 * Revision 1.1  2003/07/19 13:22:59  mrgadget4711
 * ADD: Statistic capabilities
 *
 * ------------END------------
 */