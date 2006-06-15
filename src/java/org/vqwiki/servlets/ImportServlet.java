package org.vqwiki.servlets;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import org.vqwiki.persistency.file.*;
import org.vqwiki.persistency.db.DatabaseHandler;
import org.vqwiki.persistency.db.DatabaseWikiMembers;
import org.vqwiki.persistency.db.DatabaseNotify;
import org.vqwiki.persistency.db.DatabaseVersionManager;
import org.vqwiki.WikiMembers;
import org.vqwiki.WikiMember;
import org.vqwiki.VersionManager;
import org.vqwiki.TopicVersion;

/**
 * Servlet for migrating a file-based wiki into a database-based one
 *
 * @author garethc
 * Date: Apr 28, 2003
 */
public class ImportServlet extends VQWikiServlet {

    private static final Logger logger = Logger.getLogger(ImportServlet.class);

    /**
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Importing...");
        try {
            request.setAttribute("results", importAll());
        } catch (Exception e) {
            error(request, response, e);
            return;
        }
        dispatch("/WEB-INF/jsp/afterImport.jsp", request, response);
    }

    /**
     *
     */
    private String importAll() throws Exception {
        StringBuffer buffer = new StringBuffer();
        // language does not matter here
        FileHandler fileHandler = new FileHandler();
        DatabaseHandler databaseHandler = new DatabaseHandler();
        FileSearchEngine fileSearchEngine = FileSearchEngine.getInstance();
        VersionManager fileVersionManager = FileVersionManager.getInstance();
        VersionManager databaseVersionManager = DatabaseVersionManager.getInstance();
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
                List versions = fileVersionManager.getAllVersions(virtualWiki, topicName);
                logger.info("importing " + versions.size() + " versions of topic " + topicName);
                buffer.append("imported " + versions.size() + " versions of topic " + topicName);
                buffer.append("<br/>");
                for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
                    TopicVersion topicVersion = (TopicVersion) topicVersionIterator.next();
                    databaseVersionManager.addVersion(
                        virtualWiki,
                        topicVersion.getTopicName(),
                        topicVersion.getRawContents(),
                        topicVersion.getRevisionDate()
                    );
                }
            }
            // Topics
            for (Iterator topicIterator = topics.iterator(); topicIterator.hasNext();) {
                String topicName = (String) topicIterator.next();
                logger.info("importing topic " + topicName);
                buffer.append("imported topic " + topicName);
                buffer.append("<br/>");
                databaseHandler.write(
                    virtualWiki,
                    fileHandler.read(virtualWiki, topicName),
                    false,
                    topicName
                );
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
            // Templates
            Collection templates = fileHandler.getTemplateNames(virtualWiki);
            for (Iterator templateIterator = templates.iterator(); templateIterator.hasNext();) {
                String templateName = (String) templateIterator.next();
                logger.info("importing template " + templateName);
                buffer.append("imported template " + templateName);
                buffer.append("<br/>");
                databaseHandler.saveAsTemplate(
                    virtualWiki,
                    templateName,
                    fileHandler.getTemplate(virtualWiki, templateName)
                );
            }
        }
        return buffer.toString();
    }
}
