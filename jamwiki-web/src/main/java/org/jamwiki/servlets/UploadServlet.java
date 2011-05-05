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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.ImageUtil;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle file uploads.
 */
public class UploadServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(UploadServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_UPLOAD = "upload.jsp";

	/**
	 *
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String contentType = ((request.getContentType() != null) ? request.getContentType().toLowerCase() : "" );
		if (contentType.indexOf("multipart") == -1) {
			view(request, next, pageInfo);
		} else {
			upload(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private String processDestinationFilename(String virtualWiki, String destinationFilename, String filename) {
		if (StringUtils.isBlank(destinationFilename)) {
			return destinationFilename;
		}
		if (!StringUtils.isBlank(FilenameUtils.getExtension(filename)) && StringUtils.isBlank(FilenameUtils.getExtension(destinationFilename))) {
			// if original has an extension, the renamed version must as well
			destinationFilename += (!destinationFilename.endsWith(".") ? "." : "") + FilenameUtils.getExtension(filename);
		}
		// if the user entered a file name of the form "Image:Foo.jpg" strip the namespace
		return StringUtils.removeStart(destinationFilename, Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki) + Namespace.SEPARATOR);
	}

	/**
	 *
	 */
	private void upload(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// FIXME - this method is a mess and needs to be split up.
		File file = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH));
		if (!file.exists()) {
			throw new WikiException(new WikiMessage("upload.error.nodirectory"));
		}
		String virtualWiki = pageInfo.getVirtualWikiName();
		Iterator iterator = ServletUtil.processMultipartRequest(request, Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), Environment.getLongValue(Environment.PROP_FILE_MAX_FILE_SIZE));
		String filename = null;
		String destinationFilename = null;
		String contentType = null;
		long fileSize = 0;
		String contents = null;
		boolean isImage = true;
		File uploadedFile = null;
		String url = null;
		while (iterator.hasNext()) {
			FileItem item = (FileItem)iterator.next();
			String fieldName = item.getFieldName();
			if (item.isFormField()) {
				if (fieldName.equals("description")) {
					// FIXME - these should be parsed
					contents = item.getString("UTF-8");
				} else if (fieldName.equals("destination")) {
					destinationFilename = item.getString("UTF-8");
				}
				continue;
			}
			// file name can have encoding issues, so manually convert
			filename = item.getName();
			if (filename == null) {
				throw new WikiException(new WikiMessage("upload.error.filename"));
			}
			filename = ImageUtil.sanitizeFilename(filename);
			url = ImageUtil.generateFileUrl(virtualWiki, filename, null);
			if (!ImageUtil.isFileTypeAllowed(filename)) {
				String extension = FilenameUtils.getExtension(filename);
				throw new WikiException(new WikiMessage("upload.error.filetype", extension));
			}
			fileSize = item.getSize();
			contentType = item.getContentType();
			uploadedFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), url);
			item.write(uploadedFile);
			isImage = ImageUtil.isImage(uploadedFile);
		}
		if (uploadedFile == null) {
			throw new WikiException(new WikiMessage("upload.error.filenotfound"));
		}
		destinationFilename = processDestinationFilename(virtualWiki, destinationFilename, filename);
		String topicName = ImageUtil.generateFileTopicName(virtualWiki, (!StringUtils.isEmpty(destinationFilename) ? destinationFilename : filename));
		if (this.handleSpam(request, next, topicName, contents, null)) {
			// delete the spam file
			uploadedFile.delete();
			this.view(request, next, pageInfo);
			next.addObject("contents", contents);
			return;
		}
		if (!StringUtils.isEmpty(destinationFilename)) {
			// rename the uploaded file if a destination file name was specified
			filename = ImageUtil.sanitizeFilename(destinationFilename);
			url = ImageUtil.generateFileUrl(virtualWiki, filename, null);
			File renamedFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), url);
			if (!uploadedFile.renameTo(renamedFile)) {
				throw new WikiException(new WikiMessage("upload.error.filerename", destinationFilename));
			}
		}
		String ipAddress = ServletUtil.getIpAddress(request);
		WikiUser user = ServletUtil.currentWikiUser();
		Topic topic = ImageUtil.writeImageTopic(virtualWiki, topicName, contents, user, isImage, ipAddress);
		WikiFileVersion wikiFileVersion = new WikiFileVersion();
		wikiFileVersion.setUploadComment(topic.getTopicContent());
		ImageUtil.writeWikiFile(topic, wikiFileVersion, user, ipAddress, filename, url, contentType, fileSize);
		ServletUtil.redirect(next, virtualWiki, topicName);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setPageTitle(new WikiMessage("upload.title"));
		pageInfo.setContentJsp(JSP_UPLOAD);
		next.addObject("uploadDestination", request.getParameter("topic"));
		pageInfo.setSpecial(true);
	}
}
