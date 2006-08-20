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
import java.util.LinkedHashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class CategoryServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(CategoryServlet.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		WikiPageInfo pageInfo = new WikiPageInfo();
		try {
			viewCategories(request, next, pageInfo);
		} catch (Exception e) {
			return viewError(request, e);
		}
		loadDefaults(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void viewCategories(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Collection categoryNames = WikiBase.getHandler().lookupTopicByType(virtualWiki, Topic.TYPE_CATEGORY);
		LinkedHashMap categories = new LinkedHashMap();
		for (Iterator iterator = categoryNames.iterator(); iterator.hasNext();) {
			String key = (String)iterator.next();
			String value = key.substring(WikiBase.NAMESPACE_CATEGORY.length());
			categories.put(key, value);
		}
		next.addObject("categoryCount", new Integer(categories.size()));
		next.addObject("categories", categories);
		pageInfo.setPageTitle(new WikiMessage("allcategories.title"));
		pageInfo.setAction(WikiPageInfo.ACTION_CATEGORIES);
		pageInfo.setSpecial(true);
	}
}