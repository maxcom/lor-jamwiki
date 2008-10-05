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
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.WikiUserDetails;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle moving a topic to a new name.
 */
public class MoveServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(MoveServlet.class.getName());
	protected static final String JSP_MOVE = "move.jsp";

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		WikiUserDetails user = ServletUtil.currentUser();
		if (!user.hasRole(Role.ROLE_MOVE)) {
			WikiMessage messageObject = new WikiMessage("login.message.move");
			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
		}
		if (request.getParameter("move") == null) {
			view(request, next, pageInfo);
		} else {
			move(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void move(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
		pageInfo.setPageTitle(pageTitle);
		pageInfo.setTopicName(topicName);
		String moveDestination = request.getParameter("moveDestination");
		if (!movePage(request, next, pageInfo, topicName, moveDestination)) {
			return;
		}
		if (!StringUtils.isBlank(request.getParameter("moveCommentsPage"))) {
			String moveCommentsPage = Utilities.decodeTopicName(request.getParameter("moveCommentsPage"), true);
			String commentsDestination = WikiUtil.extractCommentsLink(moveDestination);
			if (WikiUtil.isCommentsPage(moveCommentsPage) && !moveCommentsPage.equals(topicName) && !commentsDestination.equals(moveDestination)) {
				if (!movePage(request, next, pageInfo, moveCommentsPage, commentsDestination)) {
					return;
				}
			}
		}
		String virtualWiki = pageInfo.getVirtualWikiName();
		ServletUtil.redirect(next, virtualWiki, moveDestination);
	}

	/**
	 *
	 */
	private boolean movePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String moveFrom, String moveDestination) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		Topic fromTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, moveFrom, false, null);
		if (fromTopic == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		if (StringUtils.isBlank(moveDestination)) {
			pageInfo.setContentJsp(JSP_MOVE);
			next.addObject("messageObject", new WikiMessage("move.exception.nodestination"));
			return false;
		}
		WikiUserDetails user = ServletUtil.currentUser();
		if (!ServletUtil.isMoveable(virtualWiki, moveFrom, user)) {
			pageInfo.setContentJsp(JSP_MOVE);
			next.addObject("messageObject", new WikiMessage("move.exception.permission", moveFrom));
			return false;
		}
		if (!WikiBase.getDataHandler().canMoveTopic(fromTopic, moveDestination)) {
			pageInfo.setContentJsp(JSP_MOVE);
			next.addObject("messageObject", new WikiMessage("move.exception.destinationexists", moveDestination));
			next.addObject("moveDestination", moveDestination);
			next.addObject("moveComment", request.getParameter("moveComment"));
			return false;
		}
		String moveComment = Utilities.formatMessage("move.editcomment", request.getLocale(), new String[]{moveFrom, moveDestination});
		if (!StringUtils.isBlank(request.getParameter("moveComment"))) {
			moveComment += " (" + request.getParameter("moveComment") + ")";
		}
		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), moveComment, fromTopic.getTopicContent(), 0);
		topicVersion.setEditType(TopicVersion.EDIT_MOVE);
		WikiBase.getDataHandler().moveTopic(fromTopic, topicVersion, moveDestination, null);
		return true;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		String commentsPage = WikiUtil.extractCommentsLink(topicName);
		Topic commentsTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, commentsPage, false, null);
		if (commentsTopic != null) {
			// add option to also move comments page
			next.addObject("moveCommentsPage", commentsPage);
		}
		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
		pageInfo.setPageTitle(pageTitle);
		pageInfo.setContentJsp(JSP_MOVE);
		pageInfo.setTopicName(topicName);
	}
}
