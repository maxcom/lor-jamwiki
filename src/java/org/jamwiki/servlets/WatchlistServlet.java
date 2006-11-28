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
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topic = request.getParameter("topic");
		if (!StringUtils.hasText(topic)) {
			view(request, next, pageInfo);
		} else {
			update(request, next, pageInfo);
		}
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
		String topicName = Utilities.getTopicFromRequest(request);
		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
		Watchlist watchlist = Utilities.currentWatchlist(request);
		WikiBase.getHandler().writeWatchlistEntry(watchlist, virtualWiki, topicName, user.getUserId(), null);
		String article = Utilities.extractTopicLink(topicName);
		if (watchlist.containsTopic(topicName)) {
			// added to watchlist
			next.addObject("message", new WikiMessage("watchlist.caption.added", article));
		} else {
			// removed from watchlist
			next.addObject("message", new WikiMessage("watchlist.caption.removed", article));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
		Pagination pagination = Utilities.buildPagination(request, next);
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