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
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * This servlet provides the ability to generate a list of admin-only topics
 * for display.
 */
public class ImagesServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(ImagesServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.view(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
		Pagination pagination = Utilities.buildPagination(request, next);
		Collection images = WikiBase.getDataHandler().lookupTopicByType(virtualWiki, Topic.TYPE_IMAGE, pagination);
		next.addObject("imageCount", new Integer(images.size()));
		next.addObject("images", images);
		pageInfo.setPageTitle(new WikiMessage("allimages.title"));
		pageInfo.setAction(WikiPageInfo.ACTION_IMAGES);
		pageInfo.setSpecial(true);
	}
}
