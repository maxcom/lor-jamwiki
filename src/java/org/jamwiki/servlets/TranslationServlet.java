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

import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class TranslationServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(TranslationServlet.class.getName());
	private Properties translations = new Properties();

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView next = new ModelAndView("wiki");
		try {
			String function = request.getParameter("function");
			if (!StringUtils.hasText(function)) {
				view(request, next);
			} else {
				translate(request, next);
			}
			next.addObject("translations", new TreeMap(this.translations));
			if (request.getParameter("language") != null) next.addObject("language", request.getParameter("language"));
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private String filename(HttpServletRequest request) {
		String filename = "ApplicationResources.properties";
		String language = request.getParameter("language");
		if (StringUtils.hasText(language)) {
			// FIXME - should also check for valid language code
			filename = "ApplicationResources_" + language + ".properties";
		}
		return filename;
	}

	/**
	 *
	 */
	private void translate(HttpServletRequest request, ModelAndView next) throws Exception {
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ADMIN_TRANSLATION);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageTitle("Special:Translation");
		Enumeration names = request.getParameterNames();
		String name;
		while (names.hasMoreElements()) {
			name = (String)names.nextElement();
			if (!name.startsWith("translations[") || !name.endsWith("]")) {
				continue;
			}
			String key = name.substring("translations[".length(), name.length() - 1);
			String value = request.getParameter(name);
			this.translations.setProperty(key, value);
		}
		Environment.saveProperties(filename(request), this.translations, null);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
		String language = request.getParameter("language");
		String filename = filename(request);
		this.translations = Environment.loadProperties("ApplicationResources.properties");
		if (StringUtils.hasText(language)) {
			filename = filename(request);
			this.translations.putAll(Environment.loadProperties(filename));
		}
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_ADMIN_TRANSLATION);
		this.pageInfo.setSpecial(true);
		this.pageInfo.setPageTitle("Special:Translation");
	}
}
