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
package org.jmwiki.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.jmwiki.WikiMember;
import org.jmwiki.WikiMembers;
import org.jmwiki.model.Topic;
import org.jmwiki.model.TopicVersion;
import org.jmwiki.persistency.file.FileHandler;
import org.jmwiki.persistency.file.FileNotify;
import org.jmwiki.persistency.file.FileSearchEngine;
import org.jmwiki.persistency.file.FileWikiMembers;
import org.jmwiki.persistency.db.DatabaseHandler;
import org.jmwiki.persistency.db.DatabaseWikiMembers;
import org.jmwiki.persistency.db.DatabaseNotify;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class ImportServlet extends JMController implements Controller {

	private static final Logger logger = Logger.getLogger(ImportServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		importFiles(request, next);
		return next;
	}

	/**
	 *
	 */
	private void importFiles(HttpServletRequest request, ModelAndView next) throws Exception {
		try {
			next.addObject("results", importAll(request));
		} catch (Exception e) {
			logger.error("Failure while importing files", e);
			throw new Exception("Failure while importing files" + e.getMessage());
		}
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_IMPORT);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
	private String importAll(HttpServletRequest request) throws Exception {
		StringBuffer buffer = new StringBuffer();
		// language does not matter here
		FileHandler fileHandler = new FileHandler();
		DatabaseHandler databaseHandler = new DatabaseHandler();
		FileSearchEngine fileSearchEngine = FileSearchEngine.getInstance();
		Collection virtualWikis = fileHandler.getVirtualWikiList();
		for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
			String virtualWiki = (String) virtualWikiIterator.next();
			logger.info("importing for virtual wiki " + virtualWiki);
			buffer.append("imported for virtual wiki " + virtualWiki);
			buffer.append("<br/>");
			databaseHandler.addVirtualWiki(virtualWiki);
			// Versions
			Collection topics = fileSearchEngine.getAllTopicNames(virtualWiki);
			for (Iterator topicIterator = topics.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				List versions = fileHandler.getAllVersions(virtualWiki, topicName);
				logger.info("importing " + versions.size() + " versions of topic " + topicName);
				buffer.append("imported " + versions.size() + " versions of topic " + topicName);
				buffer.append("<br/>");
				for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
					TopicVersion topicVersion = (TopicVersion) topicVersionIterator.next();
					databaseHandler.addTopicVersion(
						virtualWiki,
						topicVersion.getTopicName(),
						topicVersion.getVersionContent(),
						topicVersion.getRevisionDate(),
						topicVersion.getAuthorIpAddress()
					);
				}
			}
			// Topics
			for (Iterator topicIterator = topics.iterator(); topicIterator.hasNext();) {
				String topicName = (String) topicIterator.next();
				logger.info("importing topic " + topicName);
				buffer.append("imported topic " + topicName);
				buffer.append("<br/>");
				databaseHandler.write(virtualWiki, fileHandler.read(virtualWiki, topicName), topicName, request.getRemoteAddr());
			}
			// Read-only topics
			Collection readOnlys = fileHandler.getReadOnlyTopics(virtualWiki);
			for (Iterator readOnlyIterator = readOnlys.iterator(); readOnlyIterator.hasNext();) {
				String topicName = (String) readOnlyIterator.next();
				logger.info("import read-only topicname " + topicName);
				buffer.append("imported read-only topicname " + topicName);
				buffer.append("<br/>");
				databaseHandler.addReadOnlyTopic(virtualWiki, topicName);
			}
			// Members
			WikiMembers fileMembers = new FileWikiMembers(virtualWiki);
			WikiMembers databaseMembers = new DatabaseWikiMembers(virtualWiki);
			Collection members = fileMembers.getAllMembers();
			for (Iterator memberIterator = members.iterator(); memberIterator.hasNext();) {
				WikiMember wikiMember = (WikiMember) memberIterator.next();
				logger.info("importing member " + wikiMember);
				buffer.append("imported member " + wikiMember);
				buffer.append("<br/>");
				databaseMembers.addMember(
					wikiMember.getUserName(),
					wikiMember.getEmail(),
					wikiMember.getKey()
				);
			}
			// Notifications
			Collection fileNotifications = FileNotify.getAll(virtualWiki);
			for (Iterator iterator = fileNotifications.iterator(); iterator.hasNext();) {
				FileNotify fileNotify = (FileNotify) iterator.next();
				logger.info("importing notification " + fileNotify);
				buffer.append("imported notification " + fileNotify);
				buffer.append("<br/>");
				DatabaseNotify databaseNotify = new DatabaseNotify(virtualWiki, fileNotify.getTopicName());
				Collection notifyMembers = fileNotify.getMembers();
				for (Iterator notifyMemberIterator = notifyMembers.iterator(); notifyMemberIterator.hasNext();) {
					String memberName = (String) notifyMemberIterator.next();
					databaseNotify.addMember(memberName);
				}
			}
		}
		return buffer.toString();
	}
}
