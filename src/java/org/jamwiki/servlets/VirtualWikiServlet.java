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
import org.jamwiki.servlets.WikiServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class VirtualWikiServlet extends JAMController implements Controller {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(TopicController.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		list(request, next);
		return next;
	}

	/**
	 *
	 */
	private void list(HttpServletRequest request, ModelAndView next) throws Exception {
		Collection virtualWikiList = WikiBase.getInstance().getVirtualWikiList();
		next.addObject("wikis", virtualWikiList);
		// FIXME - hard coding
		next.addObject(JAMController.PARAMETER_TITLE, "Special:VirtualWikiList");
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_VIRTUAL_WIKI_LIST);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}
}