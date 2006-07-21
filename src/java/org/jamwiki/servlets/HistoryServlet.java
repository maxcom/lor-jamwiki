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
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import java.util.Collection;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserInfo;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class HistoryServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(HistoryServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			history(request, next);
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next) throws Exception {
		PersistencyHandler handler;
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_HISTORY);
		this.pageInfo.setTopicName(topicName);
		try {
			handler = WikiBase.getHandler();
			String type = request.getParameter("type");
			if (type.equals("all")) {
				this.pageInfo.setPageTitle("History for " + topicName);
				Vector changes = handler.getRecentChanges(virtualWiki, topicName, true);
				next.addObject("changes", changes);
			} else if (type.equals("version")) {
				int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
				TopicVersion topicVersion = handler.lookupTopicVersion(virtualWiki, topicName, topicVersionId);
				String displayName = request.getRemoteAddr();
				WikiUser user = Utilities.currentUser(request);
				ParserInfo parserInfo = new ParserInfo();
				parserInfo.setContext(request.getContextPath());
				parserInfo.setWikiUser(user);
				parserInfo.setTopicName(topicName);
				parserInfo.setUserIpAddress(request.getRemoteAddr());
				parserInfo.setVirtualWiki(virtualWiki);
				String cookedContents = WikiBase.parse(parserInfo, topicVersion.getVersionContent(), topicName);
				next.addObject("topicVersion", topicVersion);
				next.addObject("cookedContents", cookedContents);
				this.pageInfo.setPageTitle(topicName + " @" + Utilities.formatDateTime(topicVersion.getEditDate()));
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
