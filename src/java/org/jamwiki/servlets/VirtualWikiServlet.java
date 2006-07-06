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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class VirtualWikiServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(TopicServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		list(request, next);
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void list(HttpServletRequest request, ModelAndView next) throws Exception {
		Collection virtualWikiList = WikiBase.getVirtualWikiList();
		next.addObject("wikis", virtualWikiList);
		// FIXME - hard coding
		this.pageInfo.setPageTitle("Special:VirtualWikiList");
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_VIRTUAL_WIKI_LIST);
		this.pageInfo.setSpecial(true);
	}
}