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

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.XMLTopicFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class ImportServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(ImportServlet.class.getName());

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String contentType = ((request.getContentType() != null) ? request.getContentType().toLowerCase() : "" );
		if (contentType.indexOf("multipart") != -1) {
			importFile(request, next, pageInfo);
		} else {
			view(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void importFile(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
		Iterator iterator = ServletUtil.processMultipartRequest(request);
		WikiUser user = Utilities.currentUser(request);
		XMLTopicFactory importer = new XMLTopicFactory(virtualWiki, user, request.getRemoteAddr());
		String topicName = null;
		while (iterator.hasNext()) {
			FileItem item = (FileItem)iterator.next();
			if (item.isFormField()) {
				continue;
			}
			File xmlFile = saveFileItem(item);
			// FIXME - breaks if more than one topic imported
			topicName = importer.importWikiXml(xmlFile);
			xmlFile.delete();
		}
		if (!StringUtils.hasText(topicName)) {
			next.addObject("error", new WikiMessage("import.caption.failure"));
			view(request, next, pageInfo);
		} else {
			ServletUtil.redirect(next, virtualWiki, topicName);
		}
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setAction(WikiPageInfo.ACTION_IMPORT);
		pageInfo.setPageTitle(new WikiMessage("import.title"));
		pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private File saveFileItem(FileItem item) throws Exception {
    	// upload user file to the server
		String subdirectory = "tmp";
		File directory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), subdirectory);
		if (!directory.exists() && !directory.mkdirs()) {
			throw new WikiException(new WikiMessage("upload.error.directorycreate",  directory.getAbsolutePath()));
		}
		// use current timestamp as unique file name
		String filename = System.currentTimeMillis() + ".xml";
		File xmlFile = new File(directory, filename);
		// transfer remote file
		item.write(xmlFile);
		return xmlFile;
	}
}
