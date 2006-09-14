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

import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class DiffServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(DiffServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			diff(request, next, pageInfo);
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	protected void diff(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String diffType = request.getParameter("type");
		if (diffType != null && diffType.equals("arbitrary")) {
			// FIXME - used with history.jsp, this is ugly
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
				next.addObject("badinput", "true");
			} else {
				Vector diffs = WikiBase.getHandler().diff(virtualWiki, topicName, Math.max(firstVersion, secondVersion), Math.min(firstVersion, secondVersion));
				next.addObject("diffs", diffs);
			}
		} else {
			int topicVersionId1 = 0;
			if (StringUtils.hasText(request.getParameter("version1"))) {
				topicVersionId1 = new Integer(request.getParameter("version1")).intValue();
			}
			int topicVersionId2 = 0;
			if (StringUtils.hasText(request.getParameter("version2"))) {
				topicVersionId2 = new Integer(request.getParameter("version2")).intValue();
			}
			Vector diffs = WikiBase.getHandler().diff(virtualWiki, topicName, topicVersionId1, topicVersionId2);
			next.addObject("diffs", diffs);
		}
		pageInfo.setPageTitle(new WikiMessage("diff.title", topicName));
		pageInfo.setTopicName(topicName);
		pageInfo.setAction(WikiPageInfo.ACTION_DIFF);
	}
}
