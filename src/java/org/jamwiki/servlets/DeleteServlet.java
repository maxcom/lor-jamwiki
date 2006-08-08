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
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class DeleteServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(DeleteServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			if (!Utilities.isAdmin(request)) {
				next.addObject("errorMessage", new WikiMessage("admin.message.loginrequired"));
				viewLogin(request, next, "Special:Delete");
				loadDefaults(request, next, this.pageInfo);
				return next;
			}
			if (StringUtils.hasText(request.getParameter("delete"))) {
				delete(request, next);
			} else {
				deleteView(request, next);
			}
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void delete(HttpServletRequest request, ModelAndView next) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setTopicName(topicName);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ADMIN_DELETE);
		this.pageInfo.setPageTitle(new WikiMessage("delete.title", topicName));
		try {
			if (topicName == null) {
				next.addObject("errorMessage", new WikiMessage("delete.error.notopic"));
				return;
			}
			Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
			TopicVersion topicVersion = new TopicVersion();
			WikiUser user = Utilities.currentUser(request);
			// FIXME - should content be null?
			topic.setTopicContent("");
			topic.setDeleted(true);
			topicVersion.setVersionContent("");
			topicVersion.setEditComment(request.getParameter("deleteComment"));
			topicVersion.setAuthorIpAddress(request.getRemoteAddr());
			topicVersion.setEditType(TopicVersion.EDIT_DELETE);
			if (user != null) {
				topicVersion.setAuthorId(new Integer(user.getUserId()));
			}
			WikiBase.getHandler().deleteTopic(topic, topicVersion);
			next.addObject("message", new WikiMessage("delete.success", topicName));
		} catch (Exception e) {
			logger.error("Failure while deleting topic " + topicName, e);
			next.addObject("errorMessage", new WikiMessage("delete.failure", topicName, e.getMessage()));
		}
	}

	/**
	 *
	 */
	private void deleteView(HttpServletRequest request, ModelAndView next) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		if (topicName == null) {
			next.addObject("errorMessage", new WikiMessage("delete.error.notopic"));
		}
		this.pageInfo.setTopicName(topicName);
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ADMIN_DELETE);
		this.pageInfo.setPageTitle(new WikiMessage("delete.title", topicName));
		this.pageInfo.setSpecial(true);
	}
}
