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
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.migrate.MediaWikiXmlTopicFactory;
import org.jamwiki.migrate.MigrationException;
import org.jamwiki.migrate.Migrator;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.WikiUser;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to import an XML file (in MediaWiki format), creating or updating a
 * topic as a result.
 */
public class ImportServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(ImportServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_IMPORT = "import.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String contentType = ((request.getContentType() == null) ? "" : request.getContentType().toLowerCase());
		if (contentType.indexOf("multipart") == -1) {
			view(request, next, pageInfo);
		} else {
			importFile(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void importFile(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		Iterator iterator = ServletUtil.processMultipartRequest(request, Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), Environment.getLongValue(Environment.PROP_FILE_MAX_FILE_SIZE));
		WikiUser user = ServletUtil.currentWikiUser();
		Migrator migrator = new MediaWikiXmlTopicFactory(user, ServletUtil.getIpAddress(request));
		ParserOutput parserOutput = null;
		List<TopicVersion> topicVersions;
		try {
			while (iterator.hasNext()) {
				FileItem item = (FileItem)iterator.next();
				if (item.isFormField()) {
					continue;
				}
				File file = saveFileItem(item);
				Map<Topic, List<TopicVersion>> parsedTopics = migrator.importFromFile(file);
				for (Topic topic : parsedTopics.keySet()) {
					topic.setVirtualWiki(virtualWiki);
					topicVersions = parsedTopics.get(topic);
					if (topicVersions.isEmpty()) {
						throw new WikiException(new WikiMessage("import.caption.failure"));
					}
					for (TopicVersion topicVersion : topicVersions) {
						parserOutput = ParserUtil.parserOutput(topicVersion.getVersionContent(), virtualWiki, topic.getName());
						WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks(), true);
					}
					// create a dummy version to indicate that the topic was imported
					// FIXME - hard coding
					TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), "Imported by " + user.getUsername(), topic.getTopicContent(), 0);
					WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks(), true);
				}
				file.delete();
				break;
			}
		} catch (MigrationException e) {
			logger.severe("Failure while importing from file", e);
			next.addObject("error", new WikiMessage("import.caption.failure", e.getMessage()));
		} catch (DataAccessException e) {
			logger.severe("Failure while importing from file", e);
			next.addObject("error", new WikiMessage("import.caption.failure", e.getMessage()));
		} catch (WikiException e) {
			logger.severe("Failure while importing from file", e);
			next.addObject("error", e.getWikiMessage());
		}
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setContentJsp(JSP_IMPORT);
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
			throw new WikiException(new WikiMessage("upload.error.directorycreate", directory.getAbsolutePath()));
		}
		// use current timestamp as unique file name
		String filename = System.currentTimeMillis() + ".xml";
		File xmlFile = new File(directory, filename);
		// transfer remote file
		item.write(xmlFile);
		return xmlFile;
	}
}
