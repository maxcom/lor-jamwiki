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

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to display all topics that contain links to the current topic.
 */
public class LinkToServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(LinkToServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_LINKTO = "linkto.jsp";

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException, WikiException {
		this.linksTo(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void linksTo(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException, WikiException {
		String virtualWiki = pageInfo.getVirtualWikiName();
		String topicName = WikiUtil.getTopicFromRequest(request);
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		WikiMessage pageTitle = new WikiMessage("linkto.title", topicName);
		pageInfo.setPageTitle(pageTitle);
		// retrieve topic names for topics that link to this one
		List<String> linkTopics = WikiBase.getDataHandler().lookupTopicLinks(virtualWiki, topicName);
		next.addObject("linkTopics", linkTopics);
		next.addObject("link", topicName);
		pageInfo.setContentJsp(JSP_LINKTO);
		pageInfo.setTopicName(topicName);
	}
}
