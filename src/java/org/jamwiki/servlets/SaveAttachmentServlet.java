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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;
import org.jamwiki.Change;
import org.jamwiki.ChangeLog;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class SaveAttachmentServlet extends JAMController implements Controller {

	/** Logger */
	public static final Logger logger = Logger.getLogger(SaveAttachmentServlet.class);
	/** The servlet config for this servlet (initialised during the overriden servlet init()) */
	private ServletConfig config;

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		if (request.getMethod() != null && request.getMethod().equalsIgnoreCase("GET")) {
			this.doGet(request, response);
		} else {
			this.doPost(request, response);
		}
		return null;
	}

	/**
	 * Init the servlet
	 */
	final public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
	}

	/**
	 * Intercept post requests. Use the Jakarta Commons fileupload library to receive any posted files. These are
	 * stored in the file system attachments directory and the topic is modified to include a link to the attachment.
	 *
	 * @param request  request
	 * @param response response.
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FileUpload upload = new FileUpload();
		String tempDir = Utilities.relativeDirIfNecessary(Environment.getValue(Environment.PROP_ATTACH_TEMP_DIR));
		File tempDirFile = new File(tempDir);
		tempDirFile.mkdirs();
		upload.setRepositoryPath(tempDir);
		upload.setSizeMax(Environment.getIntValue(Environment.PROP_ATTACH_MAX_FILE_SIZE));
		List fileList = null;
		try {
			fileList = upload.parseRequest(request);
		} catch (FileUploadException e) {
			error(request, response, new WikiServletException(e.getMessage()));
			return;
		}
		String virtualWiki = null;
		String topicName = null;
		String user = null;
		Topic topic = null;
		TopicVersion topicVersion = null;
		boolean cancel = false;
		try {
			for (Iterator iterator = fileList.iterator(); iterator.hasNext();) {
				FileItem item = (FileItem) iterator.next();
				if (item.isFormField()) {
					if (item.getFieldName().equals("topic")) {
						topicName = item.getString();
						topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
						if (topic.getReadOnly()) {
							throw new WikiException(WikiException.READ_ONLY);
						}
					} else if (item.getFieldName().equals("user")) {
						user = item.getString();
					} else if (item.getFieldName().equals("virtualwiki")) {
						virtualWiki = item.getString();
					} else if (item.getFieldName().equals("cancel")) {
						cancel = true;
					}
				}
			}
			WikiBase base = WikiBase.getInstance();
			if (!cancel) {
				// store the files
				String[] names = storeFiles(fileList, virtualWiki);
				int nameIndex = 0;
				// Update the topic
				StringBuffer contents = new StringBuffer(base.readRaw(virtualWiki, topicName));
				for (Iterator iterator = fileList.iterator(); iterator.hasNext();) {
					FileItem item = (FileItem) iterator.next();
					if (!item.isFormField() && !item.getName().equals("")) {
						contents.append("\nattach:");
						if (item.getName().indexOf(' ') >= 0) {
							contents.append("\"");
						}
						contents.append(names[nameIndex++]);
						if (item.getName().indexOf(' ') >= 0) {
							contents.append("\"");
						}
						contents.append("\n");
					}
				}
				Change change = new Change(virtualWiki, topicName, user, new java.util.Date());
				ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
				topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
				topic.setTopicContent(contents.toString());
				topicVersion = new TopicVersion();
				topicVersion.setVersionContent(contents.toString());
				topicVersion.setAuthorIpAddress(request.getRemoteAddr());
				base.getHandler().write(topic, topicVersion);
				cl.logChange(change, request);
			}
			// Unlock and return
			base.unlockTopic(virtualWiki, topicName);
			StringBuffer next = new StringBuffer();
			next.append(Utilities.createLocalRootPath(request, virtualWiki));
			next.append("Wiki?");
			next.append(topicName);
			response.sendRedirect(response.encodeRedirectURL(next.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new WikiServletException(e.toString());
		}
	}

	/**
	 * Store the given list of files for the given virtual wiki.
	 *
	 * @param fileList	list of commons upload {@link FileItem}s
	 * @param virtualWiki virtual wiki
	 * @return the names of the files stored
	 * @throws IOException
	 */
	private String[] storeFiles(List fileList, String virtualWiki) throws IOException {
		int i = 0;
		List names = new ArrayList();
		for (Iterator iterator = fileList.iterator(); iterator.hasNext(); i++) {
			FileItem item = (FileItem) iterator.next();
			log("FileItem: " + item);
			if (!item.isFormField() && !item.getName().equals("")) {
				String name = getNameOnly(item, i);
				names.add(name);
				File uploadedFile = Utilities.uploadPath(virtualWiki, name);
				logger.debug("storing attached file to " + uploadedFile);
				InputStream stream = item.getInputStream();
				FileOutputStream fileOut = new FileOutputStream(uploadedFile);
				BufferedOutputStream out = new BufferedOutputStream(fileOut);
				try {
					while (true) {
						int nextByte = stream.read();
						if (nextByte == -1) {
							break;
						}
						out.write(nextByte);
					}
				} finally {
					try {
						if (out != null) {
							out.close();
						}
						if (fileOut != null) {
							fileOut.close();
						}
						if (stream != null) {
							stream.close();
						}
					} catch (IOException e) {
						logger.warn("error closing streams", e);
					}
				}
			}
		}
		return (String[]) names.toArray(new String[0]);
	}

	/**
	 * Return a suitable filename with not path for the file being stored.
	 * The parsing of this varies between browsers and platforms. The name includes a timestamp so subsequent
	 * uploads do not overwrite the previous one.
	 *
	 * @param item the fileitem
	 * @return suitable name
	 */
	private String getNameOnly(FileItem item, int index) {
		logger.debug("creating attachment name for " + item.getName() + ":index " + index);
		String name = item.getName();
		// the absolute path seems to get uploaded in IE
		char lastChar = name.charAt(name.length() - 1);
		if (lastChar == '/' || lastChar == '\\') {
			name = name.substring(0, name.length() - 1);
		}
		if (name.indexOf('/') >= 0) {
			name = name.substring(name.lastIndexOf('/') + 1);
		} else if (name.indexOf('\\') >= 0) {
			name = name.substring(name.lastIndexOf('\\') + 1);
		}
		logger.debug("name after platform-specific processing: " + name);
		if (Environment.getBooleanValue(Environment.PROP_ATTACH_TIMESTAMP)) {
			name = createTimeStamp() + "-" + index + "-" + name;
			logger.debug("name with timestamp and index:" + name);
		}
		logger.debug("pure name:" + name);
		return name;
	}

	/**
	 * Produce a timestamp
	 * @return timestamp
	 */
	private String createTimeStamp(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyddMMHHmmssSSS");
		String timestamp = simpleDateFormat.format(new Date());
		logger.debug("timestamp: " + timestamp);
		return timestamp;
	}
}
