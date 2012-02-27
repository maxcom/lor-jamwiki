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

import java.io.*;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.authentication.WikiUserDetailsImpl;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.servlet.ModelAndView;
import sun.nio.ch.FileKey;

/**
 * Used to generate the jamwiki.css stylesheet.
 */
public class StylesheetServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(StylesheetServlet.class.getName());

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {    
    String uri = request.getRequestURI(); // jamwiki-{style}.css
    int start = uri.lastIndexOf(WikiBase.SPECIAL_URL_STYLESHEET + '-') + WikiBase.SPECIAL_URL_STYLESHEET.length() + 1;
    String style = uri.substring(start, uri.length()-4);    
		String virtualWiki = pageInfo.getVirtualWikiName();
    String styleLink = WikiBase.SPECIAL_PAGE_STYLESHEET + ':' + style;
    String stylesheet = ServletUtil.cachedContent(request.getContextPath(), request.getLocale(), virtualWiki, styleLink, false);
    if(stylesheet == null) {
      String defaultStyleLink = WikiBase.SPECIAL_PAGE_STYLESHEET + ':' + WikiBase.SPECIAL_DEFAULT_STYLESHEET;
      stylesheet = ServletUtil.cachedContent(request.getContextPath(), request.getLocale(), virtualWiki, defaultStyleLink, false);
    }
		response.setContentType("text/css");
		response.setCharacterEncoding("UTF-8");
		// cache for 30 minutes (60 * 30 = 1800)
		// FIXME - make configurable
		response.setHeader("Cache-Control", "max-age=1800");
		PrintWriter out = response.getWriter();
		out.print(stylesheet);
		out.close();
		// do not load defaults or redirect - return as raw CSS
		return null;
	}

	/**
	 *
	 */
	protected void initParams() {
		this.layout = false;
	}
}