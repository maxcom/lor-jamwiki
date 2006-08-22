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
public class ManageServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(ManageServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			if (!Utilities.isAdmin(request)) {
				WikiMessage errorMessage = new WikiMessage("admin.message.loginrequired");
				return viewLogin(request, "Special:Manage", errorMessage);
			}
			if (StringUtils.hasText(request.getParameter("delete"))) {
				delete(request, next, pageInfo);
			} else if (StringUtils.hasText(request.getParameter("permissions"))) {
				permissions(request, next, pageInfo);
			} else {
				view(request, next, pageInfo);
			}
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void delete(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		if (topicName == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
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
		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void permissions(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		if (topicName == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		WikiUser user = Utilities.currentUser(request);
		// FIXME - should content be null?
		topic.setReadOnly(request.getParameter("readOnly") != null);
		topic.setAdminOnly(request.getParameter("adminOnly") != null);
		TopicVersion previousVersion = WikiBase.getHandler().lookupLastTopicVersion(virtualWiki, topicName);
		TopicVersion topicVersion = new TopicVersion();
		topicVersion.setVersionContent(previousVersion.getVersionContent());
		topicVersion.setEditComment(Utilities.getMessage("manage.message.permissions", request.getLocale()));
		topicVersion.setAuthorIpAddress(request.getRemoteAddr());
		topicVersion.setEditType(TopicVersion.EDIT_PERMISSION);
		if (user != null) {
			topicVersion.setAuthorId(new Integer(user.getUserId()));
		}
		WikiBase.getHandler().writeTopic(topic, topicVersion, null);
		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		if (topic == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		next.addObject("readOnly", new Boolean(topic.getReadOnly()));
		next.addObject("adminOnly", new Boolean(topic.getAdminOnly()));
		pageInfo.setTopicName(topicName);
		pageInfo.setAction(WikiPageInfo.ACTION_ADMIN_MANAGE);
		pageInfo.setPageTitle(new WikiMessage("manage.title", topicName));
	}
}
