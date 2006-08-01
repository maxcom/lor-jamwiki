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

import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class ImportServlet extends JAMWikiServlet {

	private static Logger logger = Logger.getLogger(ImportServlet.class.getName());

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
			String contentType = ((request.getContentType() != null) ? request.getContentType().toLowerCase() : "" );
			if (contentType.indexOf("multipart") != -1) {
				importFile(request, next);
			} else {
				importView(request, next);
			}
		} catch (Exception e) {
			viewError(request, next, e);
		}
		loadDefaults(request, next, this.pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void importFile(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		Iterator iterator = Utilities.processMultipartRequest(request);
		while (iterator.hasNext()) {
			FileItem item = (FileItem)iterator.next();
			String fieldName = item.getFieldName();
			if (item.isFormField()) {
				// process form fields
			} else {
				String xml = item.getString("UTF-8");
				// FIXME - process XML
			}
		}
		// FIXME - redirect where?
		importView(request, next);
	}

	/**
	 *
	 */
	private void importView(HttpServletRequest request, ModelAndView next) throws Exception {
		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_IMPORT);
		this.pageInfo.setPageTitle(new WikiMessage("import.title"));
		this.pageInfo.setSpecial(true);
	}
}
