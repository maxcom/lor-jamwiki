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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.DiffUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to display a diff between two versions of a topic.
 */
public class DiffServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(DiffServlet.class.getName());
	protected static final String JSP_DIFF = "diff.jsp";

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.diff(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	protected void diff(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		int topicVersionId1 = 0;
		if (!StringUtils.isBlank(request.getParameter("version1"))) {
			topicVersionId1 = new Integer(request.getParameter("version1")).intValue();
		}
		int topicVersionId2 = 0;
		if (!StringUtils.isBlank(request.getParameter("version2"))) {
			topicVersionId2 = new Integer(request.getParameter("version2")).intValue();
		}
		Collection diffs = DiffUtil.diffTopicVersions(topicName, topicVersionId1, topicVersionId2);
		next.addObject("diffs", diffs);
		pageInfo.setPageTitle(new WikiMessage("diff.title", topicName));
		pageInfo.setTopicName(topicName);
		pageInfo.setContentJsp(JSP_DIFF);
	}
}
