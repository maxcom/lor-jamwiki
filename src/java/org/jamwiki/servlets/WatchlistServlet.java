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
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class WatchlistServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static WikiLogger logger = WikiLogger.getLogger(WatchlistServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			String topic = request.getParameter("topic");
			if (!StringUtils.hasText(topic)) {
				view(request, next, pageInfo);
			} else {
				update(request, next, pageInfo);
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
	private void update(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		WikiUser user = Utilities.currentUser(request);
		if (user == null) {
			throw new WikiException(new WikiMessage("watchlist.error.loginrequired"));
		}
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		int topicId = 0;
		if (topic != null) {
			topicId = topic.getTopicId();
		}
		// get watchlist for user
		Watchlist watchlist = WikiBase.getHandler().getWatchlist(virtualWiki, user.getUserId());
		if (watchlist.containsTopic(topicId) || watchlist.containsTopic(topicName)) {
			// remove from watchlist
			WikiBase.getHandler().deleteWatchlistEntry(virtualWiki, topicId, topicName, user.getUserId());
			next.addObject("message", new WikiMessage("watchlist.caption.removed", topicName));
		} else {
			// add to watchlist
			WikiBase.getHandler().writeWatchlistEntry(virtualWiki, topicId, topicName, user.getUserId());
			next.addObject("message", new WikiMessage("watchlist.caption.added", topicName));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Pagination pagination = JAMWikiServlet.buildPagination(request, next);
		WikiUser user = Utilities.currentUser(request);
		if (user == null) {
			throw new WikiException(new WikiMessage("watchlist.error.loginrequired"));
		}
		Collection changes = WikiBase.getHandler().getWatchlist(virtualWiki, user.getUserId(), pagination);
		next.addObject("numChanges", new Integer(changes.size()));
		next.addObject("changes", changes);
		pageInfo.setPageTitle(new WikiMessage("watchlist.title"));
		pageInfo.setAction(WikiPageInfo.ACTION_WATCHLIST);
		pageInfo.setSpecial(true);
	}
}