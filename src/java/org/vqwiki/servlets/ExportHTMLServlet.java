/**
 * @author Tobias Schulz-Hess (sourceforge@schulz-hess.de)
 *  12/04/2003 20:33:31
 */
package org.vqwiki.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.vqwiki.Change;
import org.vqwiki.ChangeLog;
import org.vqwiki.Environment;
import org.vqwiki.PseudoTopicHandler;
import org.vqwiki.SearchEngine;
import org.vqwiki.SearchResultEntry;
import org.vqwiki.Topic;
import org.vqwiki.WikiBase;
import org.vqwiki.servlets.beans.SitemapLineBean;
import org.vqwiki.utils.JSPUtils;
import org.vqwiki.utils.Utilities;
import TemplateEngine.Template;

/*
 * Very Quick Wiki - WikiWikiWeb clone Copyright (C) 2001-2002 Gareth Cronin
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the latest version of the GNU Lesser General Public License as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

/**
 * This servlet exports all pages as static HTML-pages. You can add a parameter
 * "virutal-wiki", which then generates the HTML pages on a particular virtual
 * wiki.
 * <p>
 *
 * @author Tobias Schulz-Hess (sourceforge@schulz-hess.de)
 */
public class ExportHTMLServlet extends LongLastingOperationServlet {

    /** The serial UID of this servlet */
    private static final long serialVersionUID = "$Id: ExportHTMLServlet.java 680 2006-05-27 16:17:18Z wrh2 $".hashCode();
    private File tempFile;
    private String tempdir;
    /** Logging */
    private static final Logger logger = Logger.getLogger(ExportHTMLServlet.class);
    private String virtualWiki = null;
    private String imageDir = null;
    private Exception exception;

    /**
     * Handle post request. Generate a node file and send it back as text.
     *
     * @param request
     *            The current http request
     * @param response
     *            What the servlet will send back as response
     *
     * @throws ServletException
     *             If something goes wrong during servlet execution
     * @throws IOException
     *             If the output stream cannot be accessed
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (virtualWiki == null) {
            try {
                virtualWiki = Utilities.extractVirtualWiki(request);
            } catch (Exception e) {
                e.printStackTrace();
                virtualWiki = WikiBase.DEFAULT_VWIKI;
            }
            ServletContext ctx = getServletContext();
            try {
                File tmpDir = (File) ctx.getAttribute("javax.servlet.context.tempdir");
                tempdir = tmpDir.getPath();
            } catch (Throwable t) {
                logger.warn(
                    "'javax.servlet.context.tempdir' attribute undefined or invalid, using java.io.tmpdir",
                    t);
                tempdir = System.getProperty("java.io.tmpdir");
            }
        }
        imageDir = getServletContext().getRealPath("/images");
        super.doPost(request, response);
    }

    /**
     * Do the long lasting operation
     */
    public void run() {
        exception = null;
        BufferedOutputStream fos = null;
        try {
            tempFile = File.createTempFile("htmlexport", "zip", new File(tempdir));
            fos = new BufferedOutputStream(new FileOutputStream(tempFile));
            // bos = new ByteArrayOutputStream();
            ZipOutputStream zipout = new ZipOutputStream(fos);
            zipout.setMethod(ZipOutputStream.DEFLATED);
            addAllTopics(zipout, 0, 80);
            addAllSpecialPages(zipout, 80, 10);
            addAllUploadedFiles(zipout, 90, 5);
            addAllImages(zipout, 95, 5);
            zipout.close();
            logger.debug("Closing zip and sending to user");
        } catch (Exception e) {
            logger.fatal("Exception", e);
            exception = e;
        }
        progress = PROGRESS_DONE;
    }

    /**
     *
     */
    private void addAllTopics(ZipOutputStream zipout,
        int progressStart, int progressLength) throws Exception, IOException {
        HashMap containingTopics = new HashMap();
        if (virtualWiki == null || virtualWiki.length() < 1) {
            virtualWiki = WikiBase.DEFAULT_VWIKI;
        }
        SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
        Collection all = sedb.getAllTopicNames(virtualWiki);
        String defaultTopic = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
        if (defaultTopic == null || defaultTopic.length() < 2) {
            defaultTopic = "StartingPoints";
        }
        Template tpl;
        logger.debug("Logging Wiki " + virtualWiki + " starting at " + defaultTopic);
        List ignoreTheseTopicsList = new ArrayList();
        ignoreTheseTopicsList.add("WikiSearch");
        ignoreTheseTopicsList.add("RecentChanges");
        ignoreTheseTopicsList.add("WikiSiteMap");
        ignoreTheseTopicsList.add("WikiSiteMapIE");
        ignoreTheseTopicsList.add("WikiSiteMapNS");
        Iterator allIterator = all.iterator();
        int count = 0;
        while (allIterator.hasNext()) {
            progress = Math.min(progressStart
                + (int) ((double) count
                * (double) progressLength / (double) all.size()), 99
            );
            count++;
            String topicname = (String) allIterator.next();
            try {
                addTopicToZip(
                    zipout, containingTopics, sedb, defaultTopic,
                    ignoreTheseTopicsList, topicname
                );
            } catch (Exception e) {
                logger.fatal("Exception while adding a topic ", e);
            }
        }
        logger.debug("Done adding all topics.");
        // now build sitemap
        List sitemapLines = new ArrayList();
        Vector visitedPages = new Vector();
        List startingList = new ArrayList(1);
        startingList.add(SitemapServlet.LAST_IN_LIST);
        parsePages(defaultTopic, containingTopics, startingList, "1",
            sitemapLines, visitedPages);
        // create sitemap view
        StringBuffer ieView = new StringBuffer();
        StringBuffer nsView = new StringBuffer();
        // IE view
        Vector childNodes = new Vector();
        // ieView.append(" <b><f:message key="sitemap.head"><f:param
        // value="${vwiki.name}"/><f:param
        // value="${vwiki.numpages}"/></f:message></b>\n");
        for (Iterator lineIterator = sitemapLines.iterator(); lineIterator.hasNext();) {
            SitemapLineBean line = (SitemapLineBean) lineIterator.next();
            if (childNodes.size() > 0) {
                String myGroup = line.getGroup();
                String lastNode = (String) childNodes.get(childNodes.size() - 1);
                while (myGroup.length() <= (lastNode.length() + 1)
                        && childNodes.size() > 0) {
                    ieView.append("</div><!-- " + lastNode + "-->");
                    childNodes.remove(childNodes.size() - 1);
                    if (childNodes.size() > 0) {
                        lastNode = (String) childNodes.get(childNodes.size() - 1);
                    }
                }
            }
            ieView.append("<div id=\"node_" + line.getGroup()
                    + "_Parent\" class=\"parent\">");
            for (Iterator levelsIterator = line.getLevels().iterator(); levelsIterator
                    .hasNext();) {
                String level = (String) levelsIterator.next();
                if (line.isHasChildren()) {
                    if ("x".equalsIgnoreCase(level)) {
                        ieView.append("<a href=\"#\" onClick=\"expandIt('node_"
                            + line.getGroup()
                            + "_'); return false;\"><img src=\"images/x-.png\" widht=\"30\" height=\"30\" align=\"top\"  name=\"imEx\" border=\"0\"></a>"
                        );
                    } else if ("e".equalsIgnoreCase(level)) {
                        ieView.append("<a href=\"#\" onClick=\"expandItE('node_"
                            + line.getGroup()
                            + "_'); return false;\"><img src=\"images/e-.png\" widht=\"30\" height=\"30\" align=\"top\"  name=\"imEx\" border=\"0\"></a>"
                        );
                    } else {
                        ieView.append("<img src=\"images/"
                            + level
                            + ".png\" widht=\"30\" height=\"30\" align=\"top\">"
                        );
                    }
                } else {
                    ieView.append("<img src=\"images/"
                        + level
                        + ".png\" widht=\"30\" height=\"30\" align=\"top\">"
                    );
                }
            }
            ieView.append("<a href=\"" + safename(line.getTopic()) + ".html\">"
                    + line.getTopic() + "</a></div>\n");
            if (line.isHasChildren()) {
                ieView.append("<div id=\"node_" + line.getGroup()
                        + "_Child\" class=\"child\">");
                childNodes.add(line.getGroup());
            }
        }
        for (int i = childNodes.size() - 1; i >= 0; i--) {
            ieView.append("</div><!-- " + (String) childNodes.get(i) + "-->");
        }
        // Netscape or non scripting
        nsView.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n");
        // nsView.append(" <tr><td><b><f:message key="sitemap.head"><f:param
        // value="${vwiki.name}"/><f:param
        // value="${vwiki.numpages}"/></f:message></b></td></tr>\n");
        for (Iterator lineIterator = sitemapLines.iterator(); lineIterator.hasNext();) {
            SitemapLineBean line = (SitemapLineBean) lineIterator.next();
            nsView.append("<tr><td height=\"30\" valign=\"top\">");
            for (Iterator levelsIterator = line.getLevels().iterator(); levelsIterator
                    .hasNext();) {
                String level = (String) levelsIterator.next();
                nsView.append("<img src=\"images/" + level
                        + ".png\" widht=\"30\" height=\"30\" align=\"top\">");
            }
            nsView.append("<a href=\"" + safename(line.getTopic()) + ".html\">"
                    + line.getTopic() + "</a></td></tr>\n");
        }
        nsView.append("</table>\n");
        // add sitemap to zip
        tpl = getTemplateFilledWithContent("sitemap");
        tpl.setFieldGlobal("TOPICNAME", "WikiSiteMap");
        ZipEntry entry = new ZipEntry("WikiSiteMap.html");
        StringReader strin = new StringReader(Utilities.replaceString(tpl
                .getContent(), "@@NSVIEW@@", nsView.toString()));
        // Add new entry to zip archive
        zipout.putNextEntry(entry);
        // Add data to new entry
        int read;
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry(); // Close this entry
        zipout.flush();
        tpl = getTemplateFilledWithContent("sitemap_ie");
        tpl.setFieldGlobal("TOPICNAME", "WikiSiteMap");
        entry = new ZipEntry("WikiSiteMapIE.html");
        strin = new StringReader(Utilities.replaceString(tpl.getContent(),
                "@@IEVIEW@@", ieView.toString()));
        // Add new entry to zip archive
        zipout.putNextEntry(entry);
        // Add data to new entry
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry(); // Close this entry
        zipout.flush();
        tpl = getTemplateFilledWithContent("sitemap_ns");
        tpl.setFieldGlobal("TOPICNAME", "WikiSiteMap");
        entry = new ZipEntry("WikiSiteMapNS.html");
        strin = new StringReader(Utilities.replaceString(tpl.getContent(),
                "@@NSVIEW@@", nsView.toString()));
        // Add new entry to zip archive
        zipout.putNextEntry(entry);
        // Add data to new entry
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry(); // Close this entry
        zipout.flush();
    }

    /**
     * Add a single topic to the Zip stream
     *
     * @param zipout The Zip to add the topic to
     * @param containingTopics List of all containing topic
     * @param sedb The search engine
     * @param defaultTopic The default topics
     * @param ignoreTheseTopicsList Ignore these topics
     * @param topicname The name of this topic
     * @throws Exception
     * @throws IOException
     */
    private void addTopicToZip(ZipOutputStream zipout,
            HashMap containingTopics, SearchEngine sedb,
            String defaultTopic, List ignoreTheseTopicsList,
            String topicname) throws Exception, IOException {
        WikiBase wb = WikiBase.getInstance();
        Template tpl;
        // FIXME: hard coding is evil
        tpl = new Template(this.getServletContext().getRealPath(
                "/WEB-INF/classes/export2html/mastertemplate.tpl"));
        tpl.setFieldGlobal("VERSION", WikiBase.WIKI_VERSION);
        StringBuffer oneline = new StringBuffer();
        if (!ignoreTheseTopicsList.contains(topicname)) {
            oneline.append(topicname);
            tpl.setFieldGlobal("TOPICNAME", topicname);
            Topic topicObject = new Topic(topicname);
            logger.debug("Adding topic " + topicname);
            String author = null;
            java.util.Date lastRevisionDate = null;
            if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
                lastRevisionDate = topicObject
                        .getMostRecentRevisionDate(virtualWiki);
                author = topicObject.getMostRecentAuthor(virtualWiki);
                if (author != null || lastRevisionDate != null) {
                    tpl.setField("SHOWVERSIONING1", "-->");
                    if (author != null)
                        tpl.setField("AUTHOR", author);
                    if (lastRevisionDate != null)
                        tpl.setField("MODIFYDATE", Utilities
                                .formatDate(lastRevisionDate));
                    tpl.setField("SHOWVERSIONING2", "<!--");
                }
            }
            // get content
            StringBuffer content = new StringBuffer();
            // FIXME (PARSER_TEMP) - update after parser conversion is complete
            content.append(wb.readCooked(virtualWiki, topicname,
                Environment.getValue(Environment.PROP_PARSER_FORMAT_LEXER),
                Environment.getValue(Environment.PROP_PARSER_LAYOUT_LEXER),
                "vqwiki.lex.HTMLLinkLex", true)
            );
            // handle redirects
            String redirect = "redirect:";
            if (content.toString().startsWith(redirect)) {
                StringBuffer link = new StringBuffer(content.toString()
                        .substring(redirect.length()).trim());
                // remove all tags:
                while (link.toString().indexOf("<") != -1) {
                    int startpos = link.toString().indexOf("<");
                    int endpos = link.toString().indexOf(">");
                    if (endpos == -1) {
                        endpos = link.length();
                    } else {
                        endpos++;
                    }
                    link.delete(startpos, endpos);
                }
                link = new StringBuffer(safename(link.toString().trim()));
                link = link.append(".html");
                String nl = System.getProperty("line.separator");
                tpl.setFieldGlobal("REDIRECT", "<script>" + nl
                        + "location.replace(\"" + link.toString() + "\");" + nl
                        + "</script>" + nl
                        + "<meta http-equiv=\"refresh\" content=\"1; "
                        + link.toString() + "\">" + nl);
            } else {
                tpl.setFieldGlobal("REDIRECT", "");
            }
            // add referrer pages
            Collection searchresult = sedb.find(virtualWiki, topicname, false);
            if (searchresult != null && searchresult.size() > 0) {
                Iterator it = searchresult.iterator();
                String divider = "";
                StringBuffer backlinks = new StringBuffer();
                for (; it.hasNext();) {
                    SearchResultEntry result = (SearchResultEntry) it.next();
                    if (!result.getTopic().equals(topicname)) {
                        backlinks.append(divider);
                        backlinks.append("<a href=\"");
                        backlinks.append(safename(result.getTopic()));
                        backlinks.append(".html\">");
                        backlinks.append(result.getTopic());
                        backlinks.append("</a>");
                        divider = " | ";
                        // add this topic to the containingTopics Map:
                        List l = (List) containingTopics.get(result.getTopic());
                        if (l == null) {
                            l = new ArrayList();
                        }
                        if (!l.contains(topicname)) {
                            l.add(topicname);
                        }
                        containingTopics.put(result.getTopic(), l);
                    }
                }
                if (backlinks.length() > 0) {
                    ResourceBundle messages = ResourceBundle.getBundle(
                            "ApplicationResources", locale);
                    content.append("<br /><br /><span class=\"backlinks\">");
                    content.append(topicname);
                    content.append(" ");
                    content.append(messages.getString("topic.ismentionedon"));
                    content.append(" ");
                    content.append(backlinks.toString());
                    content.append("</span>");
                }
            }
            tpl.setFieldGlobal("CONTENTS", content.toString());
            ZipEntry entry = new ZipEntry(safename(topicname) + ".html");
            StringReader in = new StringReader(tpl.getContent());
            // Add new entry to zip archive
            zipout.putNextEntry(entry);
            // Hinzuf?gen der Daten zum neuen Eintrag
            int read = 0;
            while ((read = in.read()) != -1) {
                zipout.write(read);
            }
            zipout.closeEntry(); // Neuen Eintrag abschlie?en
            zipout.flush();
            if (topicname.equals(defaultTopic)) {
                entry = new ZipEntry("index.html");
                in = new StringReader(tpl.getContent());
                zipout.putNextEntry(entry);
                read = 0;
                while ((read = in.read()) != -1) {
                    zipout.write(read);
                }
                zipout.closeEntry(); // Neuen Eintrag abschlie?en
                zipout.flush();
            }
        }
    }

    /**
     * Create a safe name of this topic for the file system.
     *
     * @param topic
     *            The original topic name
     * @return The safe topic name
     */
    private String safename(String topic) {
        return Utilities.encodeSafeExportFileName(topic);
    }

    /**
     * Parse the pages starting with startTopic. The results are stored in the
     * list sitemapLines. This functions is called recursivly, but the list is
     * filled in the correct order.
     *
     * @param currentWiki
     *            name of the wiki to refer to
     * @param startTopic
     *            Start with this page
     * @param level
     *            A list indicating the images to use to represent certain
     *            levels
     * @param group
     *            The group, we are representing
     * @param sitemapLines
     *            A list of all lines, which results in the sitemap
     * @param visitedPages
     *            A vector of all pages, which already have been visited
     * @param endString
     *            Beyond this text we do not search for links
     */
    private void parsePages(String topic, HashMap wiki, List levelsIn,
            String group, List sitemapLines, Vector visitedPages) {
        try {
            List result = new ArrayList();
            List levels = new ArrayList(levelsIn.size());
            for (int i = 0; i < levelsIn.size(); i++) {
                if ((i + 1) < levelsIn.size()) {
                    if (SitemapServlet.MORE_TO_COME.equals((String) levelsIn.get(i))) {
                        levels.add(SitemapServlet.HORIZ_LINE);
                    } else if (SitemapServlet.LAST_IN_LIST.equals((String) levelsIn.get(i))) {
                        levels.add(SitemapServlet.NOTHING);
                    } else {
                        levels.add(levelsIn.get(i));
                    }
                } else {
                    levels.add(levelsIn.get(i));
                }
            }
            List l = (List) wiki.get(topic);
            if (l == null) {
                // topic is empty, but need to be shown!
                l = new ArrayList();
            }
            for (Iterator listIterator = l.iterator(); listIterator.hasNext();) {
                String link = (String) listIterator.next();
                if (link.indexOf('&') > -1) {
                    link = link.substring(0, link.indexOf('&'));
                }
                if (link.length() > 3
                        && !link.startsWith("topic=")
                        && !link.startsWith("action=")
                        && !visitedPages.contains(link)
                        && !PseudoTopicHandler.getInstance()
                                .isPseudoTopic(link)) {
                    result.add(link);
                    visitedPages.add(link);
                }
            }
            // add a sitemap line
            SitemapLineBean slb = new SitemapLineBean();
            slb.setTopic(topic);
            slb.setLevels(new ArrayList(levels));
            slb.setGroup(group);
            slb.setHasChildren(result.size() > 0);
            sitemapLines.add(slb);
            for (int i = 0; i < result.size(); i++) {
                String link = (String) result.get(i);
                String newGroup = group + "_" + String.valueOf(i);
                boolean isLast = ((i + 1) == result.size());
                if (isLast) {
                    levels.add(SitemapServlet.LAST_IN_LIST);
                } else {
                    levels.add(SitemapServlet.MORE_TO_COME);
                }
                parsePages(link, wiki, levels, newGroup, sitemapLines,
                        visitedPages);
                levels.remove(levels.size() - 1);
            }
        } catch (Exception e) {
            logger.fatal("Exception", e);
        }
    }

    /**
     *
     */
    private void addAllSpecialPages(ZipOutputStream zipout,
            int progressStart, int progressLength) throws Exception,
            IOException {
        if (virtualWiki == null || virtualWiki.length() < 1) {
            virtualWiki = WikiBase.DEFAULT_VWIKI;
        }
        ResourceBundle messages = ResourceBundle.getBundle(
                "ApplicationResources", locale);

        WikiBase wb = WikiBase.getInstance();
        SearchEngine sedb = wb.getSearchEngineInstance();
        Template tpl;
        int count = 0;
        int numberOfSpecialPages = 7;
        int bytesRead = 0;
        byte[] byteArray = new byte[4096];
        // -----------------------------------------------------------
        // add style sheet
        progress = Math.min(
            progressStart
            + (int) ((double) count
            * (double) progressLength / (double) numberOfSpecialPages),
            99
        );
        count++;
        ZipEntry entry = new ZipEntry("vqwiki.css");
        zipout.putNextEntry(entry);
        InputStream in = new BufferedInputStream(new FileInputStream(this
                .getServletContext().getRealPath("/vqwiki.css")));
        // Read in bytes through file stream, and write out through servlet
        // stream
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        // -----------------------------------------------------------
        // add search page
        progress = Math.min(
            progressStart
            + (int) ((double) count
            * (double) progressLength / (double) numberOfSpecialPages),
            99
        );
        count++;
        tpl = getTemplateFilledWithContent("search");
        tpl.setFieldGlobal("TOPICNAME", "WikiSearch");
        entry = new ZipEntry("WikiSearch.html");
        StringReader strin = new StringReader(tpl.getContent());
        // Add new entry to zip archive
        zipout.putNextEntry(entry);
        // Add data to new entry
        while ((bytesRead = strin.read()) != -1) {
            zipout.write(bytesRead);
        }
        zipout.closeEntry(); // Close this entry
        zipout.flush();
        // -----------------------------------------------------------
        // add index page
        progress = Math.min(
            progressStart
            + (int) ((double) count
            * (double) progressLength / (double) numberOfSpecialPages),
            99
        );
        count++;
        entry = new ZipEntry("applets/vqapplets.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(this
                .getServletContext().getRealPath(
                        "/WEB-INF/classes/export2html/vqapplets.jar")));
        // Read in bytes through file stream, and write out through servlet
        // stream
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        entry = new ZipEntry("applets/log4j.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(this
                .getServletContext().getRealPath("/WEB-INF/lib/log4j.jar")));
        // Read in bytes through file stream, and write out through servlet
        // stream
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        entry = new ZipEntry("applets/lucene-1.2a.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(this
                .getServletContext()
                .getRealPath("/WEB-INF/lib/lucene-1.2a.jar")));
        // Read in bytes through file stream, and write out through servlet
        // stream
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        entry = new ZipEntry("applets/commons-httpclient-2.0.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(this
                .getServletContext()
                .getRealPath("/WEB-INF/lib/commons-httpclient-2.0.jar")));
        // Read in bytes through file stream, and write out through servlet
        // stream
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JarOutputStream indexjar = new JarOutputStream(bos);
            JarEntry jarEntry;
            File searchDir = new File(WikiBase.getInstance()
                    .getSearchEngineInstance().getSearchIndexPath(virtualWiki));
            String files[] = searchDir.list();
            StringBuffer listOfAllFiles = new StringBuffer();
            for (int i = 0; i < files.length; i++) {
                if (listOfAllFiles.length() > 0) {
                    listOfAllFiles.append(",");
                }
                listOfAllFiles.append(files[i]);
                jarEntry = new JarEntry("lucene/index/" + files[i]);
                indexjar.putNextEntry(jarEntry);
                in = new FileInputStream(new File(searchDir, files[i]));
                // Read in bytes through file stream, and write out through
                // servlet stream
                while (in.available() > 0) {
                    bytesRead = in.read(byteArray, 0, Math.min(4096, in
                            .available()));
                    indexjar.write(byteArray, 0, bytesRead);
                }
                indexjar.closeEntry();
            }
            indexjar.flush();
            jarEntry = new JarEntry("lucene/index.dir");
            strin = new StringReader(listOfAllFiles.toString());
            indexjar.putNextEntry(jarEntry);
            while ((bytesRead = strin.read()) != -1) {
                indexjar.write(bytesRead);
            }
            indexjar.closeEntry();
            indexjar.flush();
            indexjar.close();
            entry = new ZipEntry("applets/index.jar");
            zipout.putNextEntry(entry);
            zipout.write(bos.toByteArray());
            zipout.closeEntry();
            zipout.flush();
            bos.reset();
        } catch (Exception e) {
            logger.debug("Exception while adding lucene index: ", e);
        }
        // -----------------------------------------------------------
        // add recent changes
        progress = Math.min(
            progressStart
            + (int) ((double) count
            * (double) progressLength / (double) numberOfSpecialPages),
            99
        );
        count++;
        // FIXME - hard coding is evil
        tpl = new Template(this.getServletContext().getRealPath(
                "/WEB-INF/classes/export2html/mastertemplate.tpl"));
        tpl.setFieldGlobal("VERSION", WikiBase.WIKI_VERSION);
        StringBuffer content = new StringBuffer();
        content.append("<table><tr><th>" + messages.getString("common.date")
                + "</th><th>" + messages.getString("common.topic")
                + "</th><th>" + messages.getString("common.user")
                + "</th></tr>\n");
        Collection all = null;
        try {
            Calendar cal = Calendar.getInstance();
            ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
            int n = Environment.getIntValue(
                    Environment.PROP_RECENT_CHANGES_DAYS);
            if (n == 0) {
                n = 5;
            }
            all = new ArrayList();
            for (int i = 0; i < n; i++) {
                Collection col = cl.getChanges(virtualWiki, cal.getTime());
                if (col != null) {
                    all.addAll(col);
                }
                cal.add(Calendar.DATE, -1);
            }
        } catch (Exception e) {
            ;
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, locale);
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            Change change = (Change) iter.next();
            content.append("<tr><td class=\"recent\">"
                    + df.format(change.getTime())
                    + "</td><td class=\"recent\"><a href=\""
                    + safename(change.getTopic()) + ".html\">" + change.getTopic()
                    + "</a></td><td class=\"recent\">" + change.getUser()
                    + "</td></tr>");
        }
        content.append("</table>\n");
        tpl.setFieldGlobal("TOPICNAME", "RecentChanges");
        tpl.setFieldGlobal("VERSION", WikiBase.WIKI_VERSION);
        tpl.setFieldGlobal("CONTENTS", content.toString());
        entry = new ZipEntry("RecentChanges.html");
        strin = new StringReader(tpl.getContent());
        // Add new entry to zip archive
        zipout.putNextEntry(entry);
        // Add data to new entry
        int read;
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry(); // Close this entry
        zipout.flush();
        logger.debug("Done adding all special topics.");
    }

    /**
     *
     */
    private void addAllImages(ZipOutputStream zipout,
            int progressStart, int progressLength) throws IOException {
        // add images
        String[] files = new File(imageDir).list();
        int bytesRead = 0;
        byte byteArray[] = new byte[4096];
        FileInputStream in = null;
        for (int i = 0; i < files.length; i++) {
            progress = Math.min(
                progressStart
                + (int) ((double) i
                * (double) progressLength / (double) files.length),
                99
            );
            File fileToHandle = new File(imageDir, files[i]);
            if (fileToHandle.isFile() && fileToHandle.canRead()) {
                try {
                    logger.debug("Adding image file " + files[i]);
                    ZipEntry entry = new ZipEntry("images/" + files[i]);
                    zipout.putNextEntry(entry);
                    in = new FileInputStream(fileToHandle);
                    // Read in bytes through file stream, and write out through
                    // servlet stream
                    while (in.available() > 0) {
                        bytesRead = in.read(byteArray, 0, Math.min(4096, in
                                .available()));
                        zipout.write(byteArray, 0, bytesRead);
                    }
                } catch (FileNotFoundException e) {
                    ;
                } catch (IOException e) {
                    ;
                } finally {
                    try {
                        zipout.closeEntry();
                    } catch (IOException e1) {
                        ;
                    }
                    try {
                        zipout.flush();
                    } catch (IOException e1) {
                        ;
                    }
                    try {
                        if (in != null) {
                            in.close();
                            in = null;
                        }
                    } catch (IOException e1) {
                        ;
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void addAllUploadedFiles(ZipOutputStream zipout,
            int progressStart, int progressLength) throws IOException,
            FileNotFoundException {
        // add all attachments
        File uploadPath = Utilities.uploadPath(virtualWiki, "");
        String[] files = uploadPath.list();
        int bytesRead = 0;
        byte byteArray[] = new byte[4096];
        for (int i = 0; i < files.length; i++) {
            progress = Math.min(
                progressStart
                + (int) ((double) i
                * (double) progressLength / (double) files.length),
                99
            );
            logger.debug("Adding uploaded file " + files[i]);
            ZipEntry entry = new ZipEntry(files[i]);
            try {
                FileInputStream in = new FileInputStream(Utilities.uploadPath(
                        virtualWiki, files[i]));
                zipout.putNextEntry(entry);
                // Read in bytes through file stream, and write out through
                // servlet stream
                while (in.available() > 0) {
                    bytesRead = in.read(byteArray, 0, Math.min(4096, in
                            .available()));
                    zipout.write(byteArray, 0, bytesRead);
                }
                zipout.closeEntry();
                zipout.flush();
            } catch (FileNotFoundException e) {
                logger.warn("Could not open file!", e);
            } catch (IOException e) {
                logger.warn("IOException!", e);
                try {
                    zipout.closeEntry();
                    zipout.flush();
                } catch (IOException e1) {
                    ;
                }
            }
        }
    }

    /**
     *
     */
    private Template getTemplateFilledWithContent(String contentName)
            throws Exception {
        // FIXME - hard coding is evil
        Template tpl = new Template(this.getServletContext().getRealPath(
                "/WEB-INF/classes/export2html/mastertemplate.tpl"));
        tpl.setFieldGlobal("VERSION", WikiBase.WIKI_VERSION);
        StringBuffer content = readFile("/WEB-INF/classes/export2html/"
                + contentName + ".content");
        tpl.setFieldGlobal("CONTENTS", content.toString());
        return tpl;
    }

    /**
     * Read a file from a resource inside the classpath
     *
     * @param filename
     *            The file to read
     * @return The content of the file as StringBuffer
     */
    private StringBuffer readFile(String filename) {
        // get content
        return Utilities.readFile(new File(this.getServletContext()
                .getRealPath(filename)));
    }

    /**
     * We are done. Go to result page.
     *
     * @see vqwiki.servlets.LongLastingOperationServlet#dispatchDone(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void dispatchDone(HttpServletRequest request, HttpServletResponse response) {
        if (exception != null) {
            error(request, response, new ServletException(exception
                    .getMessage(), exception));
            return;
        }
        try {
            response.setContentType("application/zip");
            response.setHeader("Expires", "0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Keep-Alive", "timeout=15, max=100");
            response.setHeader("Connection", "Keep-Alive");
            response.setHeader("Content-Disposition", "attachment"
                    + ";filename=HTMLExportOf" + virtualWiki + ".zip;");
            FileInputStream in = new FileInputStream(tempFile);
            response.setContentLength((int) tempFile.length());
            OutputStream out = response.getOutputStream();
            int bytesRead = 0;
            byte byteArray[] = new byte[4096];
            while (in.available() > 0) {
                bytesRead = in.read(byteArray, 0, Math
                        .min(4096, in.available()));
                out.write(byteArray, 0, bytesRead);
            }
            out.flush();
            out.close();
            tempFile.delete();
        } catch (Exception e) {
            logger.fatal("Exception", e);
            error(request, response, new ServletException(e.getMessage(), e));
        }
    }
}
