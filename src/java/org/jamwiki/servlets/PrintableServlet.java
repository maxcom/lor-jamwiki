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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class PrintableServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(PrintableServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("printable");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			print(request, next, pageInfo);
		} catch (Exception e) {
			return ServletUtil.viewError(request, e);
		}
		ServletUtil.loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void print(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = ServletUtil.getTopicFromRequest(request);
		if (!StringUtils.hasText(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		// FIXME - full URLs should be printed, need some sort of switch
		String virtualWiki = ServletUtil.getVirtualWikiFromURI(request);
		if (!StringUtils.hasText(virtualWiki)) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		WikiMessage pageTitle = new WikiMessage("topic.title", topicName);
		ServletUtil.viewTopic(request, next, pageInfo, pageTitle, topic, false);
	}
}
