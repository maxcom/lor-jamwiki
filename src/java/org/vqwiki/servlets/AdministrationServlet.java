/*
 * Copyright 2002 Gareth Cronin
 * This software is subject to the GNU Lesser General Public Licence (LGPL)
 */
package org.vqwiki.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.vqwiki.ChangeLog;
import org.vqwiki.Environment;
import org.vqwiki.Topic;
import org.vqwiki.WikiBase;
import org.vqwiki.WikiMembers;
import org.vqwiki.persistency.db.DBDate;
import org.vqwiki.persistency.db.DatabaseConnection;
import org.vqwiki.utils.Encryption;
import org.vqwiki.utils.JSPUtils;
import org.vqwiki.utils.Utilities;

public class AdministrationServlet extends VQWikiServlet {

    private static final Logger logger = Logger.getLogger(AdministrationServlet.class);

    /**
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse httpServletResponse) throws ServletException,
        IOException {
        if (!Utilities.isAdmin(request)) {
            request.setAttribute("title", Utilities.resource("login.title", request.getLocale()));
            logger.debug("Current URL: " + request.getRequestURL());
            String rootPath = JSPUtils.createLocalRootPath(request, (String) request.getAttribute("virtualWiki"));
            StringBuffer buffer = new StringBuffer();
            buffer.append(rootPath);
            buffer.append("Wiki?action=" + WikiServlet.ACTION_ADMIN + "&username=admin");
            request.setAttribute("redirect", buffer.toString());
            dispatch("/jsp/login.jsp", request, httpServletResponse);
            return;
        }
        if (request.getParameter("function") != null) {
            if (request.getParameter("function").equals("logout")) {
                request.getSession().removeAttribute("admin");
                redirect(
                    JSPUtils.createRedirectURL(request, "Wiki?StartingPoints"),
                    httpServletResponse
                );
                return;
            }
        }
        dispatch("/jsp/admin.jsp", request, httpServletResponse);
    }

    /**
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Admin request");
        String message = "";
        try {
            ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", request.getLocale());
            String virtualWiki = (String) request.getAttribute("virtual-wiki");
            String functionType = request.getParameter("function");
            if (request.getParameter("function") == null) {
                // nothing
            } else if (functionType.equals("refreshIndex")) {
                WikiBase.getInstance().getSearchEngineInstance().refreshIndex();
                message = messages.getString("admin.message.indexrefreshed");
            } else if (request.getParameter("function").equals("logout")) {
                request.getSession().removeAttribute("admin");
                redirect("Wiki?StartingPoints", response);
                return;
            } else if (functionType.equals("purge")) {
                Collection purged = WikiBase.getInstance().purgeDeletes(request.getParameter("virtual-wiki"));
                StringBuffer buffer = new StringBuffer();
                ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
                cl.removeChanges(virtualWiki, purged);
                buffer.append("Purged: ");
                for (Iterator iterator = purged.iterator(); iterator.hasNext();) {
                    String topicName = (String) iterator.next();
                    buffer.append(topicName);
                    buffer.append("; ");
                }
                message = buffer.toString();
            } else if (functionType.equals("purge-versions")) {
                DateFormat dateFormat = DateFormat.getInstance();
                DBDate date = new DBDate(dateFormat.parse(request.getParameter("purgedate")));
                WikiBase.getInstance().purgeVersionsOlderThan(virtualWiki, date);
            } else if (functionType.equals("properties")) {
                Encryption.togglePropertyEncryption(request.getParameter(Environment.PROP_BASE_ENCODE_PASSWORDS) != null);
                Environment.setIntValue(
                    Environment.PROP_TOPIC_EDIT_TIME_OUT,
                    Integer.parseInt(request.getParameter("editTimeout"))
                );
                Environment.setValue(Environment.PROP_RECENT_CHANGES_DAYS, request.getParameter("recentChangesDays"));
                Environment.setValue(Environment.PROP_TOPIC_MAXIMUM_BACKLINKS, request.getParameter("maximumBacklinks"));
                Environment.setIntValue(
                    Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL,
                    Integer.parseInt(request.getParameter("indexInterval"))
                );
                Environment.setIntValue(
                    Environment.PROP_RECENT_CHANGES_REFRESH_INTERVAL,
                    Integer.parseInt(request.getParameter("recentChangesInterval"))
                );
                Environment.setValue(Environment.PROP_EMAIL_SMTP_HOST, request.getParameter("smtp"));
                Environment.setValue(Environment.PROP_EMAIL_SMTP_USERNAME, request.getParameter("smtpUsername"));
                Encryption.setEncryptedProperty(Environment.PROP_EMAIL_SMTP_PASSWORD, request.getParameter("smtpPassword"));
                Environment.setValue(Environment.PROP_EMAIL_REPLY_ADDRESS, request.getParameter("replyAddress"));
                Environment.setValue(Environment.PROP_PARSER_NEW_LINE_BREAKS, request.getParameter("newLineBreaks"));
                Environment.setBooleanValue(
                    Environment.PROP_TOPIC_VERSIONING_ON,
                    request.getParameter("versioning") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_PARSER_ALLOW_HTML,
                    request.getParameter("allowHtml") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_TOPIC_FORCE_USERNAME,
                    request.getParameter("forceUserName") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_PARSER_ALLOW_BACK_TICK,
                    request.getParameter("allowBackTick") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_TOPIC_ALLOW_VWIKI_LIST,
                    request.getParameter("allowVirtualWikiList") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_SEARCH_ATTACHMENT_INDEXING_ENABLED,
                    request.getParameter("indexAttachments") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_SEARCH_EXTLINKS_INDEXING_ENABLED,
                    request.getParameter("indexExtLinks") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_ATTACH_TIMESTAMP,
                    request.getParameter("attachmentTimestamp") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_PARSER_FRANZ_NEWTOPIC,
                    request.getParameter("franzNewTopicStyle") != null
                );
                Environment.setValue(Environment.PROP_ATTACH_UPLOAD_DIR, request.getParameter("uploadDir"));
                Environment.setValue(Environment.PROP_FILE_HOME_DIR, request.getParameter("homeDir"));
                int persistenceType = Integer.parseInt(request.getParameter("persistenceType"));
                if (persistenceType == WikiBase.FILE) {
                    Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "FILE");
                } else if (persistenceType == WikiBase.DATABASE) {
                    Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
                }
                if (request.getParameter("driver") != null) {
                    Environment.setValue(Environment.PROP_DB_DRIVER, request.getParameter("driver"));
                    Environment.setValue(Environment.PROP_DB_URL, request.getParameter("url"));
                    Environment.setValue(Environment.PROP_DB_USERNAME, request.getParameter("dbUsername"));
                    Encryption.setEncryptedProperty(Environment.PROP_DB_PASSWORD, request.getParameter("dbPassword"));
                    Environment.setIntValue(
                        Environment.PROP_DBCP_MAX_ACTIVE, Integer.parseInt(request.getParameter("dbcp_max_active"))
                    );
                    Environment.setIntValue(Environment.PROP_DBCP_MAX_IDLE, Integer.parseInt(request.getParameter("dbcp_max_idle")));
                    Environment.setBooleanValue(Environment.PROP_DBCP_TEST_ON_BORROW, request.getParameter("dbcp_test_on_borrow") != null);
                    Environment.setBooleanValue(Environment.PROP_DBCP_TEST_ON_RETURN, request.getParameter("dbcp_test_on_return") != null);
                    Environment.setBooleanValue(
                        Environment.PROP_DBCP_TEST_WHILE_IDLE, request.getParameter("dbcp_test_while_idle") != null
                    );
                    Environment.setIntValue(
                        Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME,
                        Integer.parseInt(request.getParameter("dbcp_min_evictable_idle_time"))
                    );
                    Environment.setIntValue(
                        Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS,
                        Integer.parseInt(request.getParameter("dbcp_time_between_eviction_runs"))
                    );
                    Environment.setIntValue(
                        Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN,
                        Integer.parseInt(request.getParameter("dbcp_num_tests_per_eviction_run"))
                    );
                    Environment.setIntValue(
                        Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION,
                        Integer.parseInt(request.getParameter("dbcp_when_exhausted_action"))
                    );
                    Environment.setValue(Environment.PROP_DBCP_VALIDATION_QUERY, request.getParameter("dbcp_validation_query"));
                    Environment.setBooleanValue(
                        Environment.PROP_DBCP_REMOVE_ABANDONED, request.getParameter("dbcp_remove_abandoned") != null
                    );
                    Environment.setIntValue(
                        Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT,
                        Integer.parseInt(request.getParameter("dbcp_remove_abandoned_timeout"))
                    );
                    Environment.setBooleanValue(
                        Environment.PROP_DBCP_LOG_ABANDONED,
                        request.getParameter("dbcp_log_abandoned") != null
                    );
                }
                Environment.setBooleanValue(
                    Environment.PROP_TOPIC_ALLOW_TEMPLATES,
                    request.getParameter("allowTemplates") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_TOPIC_USE_PREVIEW,
                    request.getParameter("usePreview") != null
                );
                Environment.setValue(Environment.PROP_BASE_DEFAULT_TOPIC, request.getParameter("defaultTopic"));
                Environment.setValue(Environment.PROP_PARSER_CLASS, request.getParameter(Environment.PROP_PARSER_CLASS));
                Environment.setValue(Environment.PROP_PARSER_FORMAT_LEXER, request.getParameter("formatLexer"));
                Environment.setValue(Environment.PROP_PARSER_LINK_LEXER, request.getParameter("linkLexer"));
                Environment.setValue(Environment.PROP_PARSER_LAYOUT_LEXER, request.getParameter("layoutLexer"));
                int maxFileSizeInKB = Integer.parseInt(request.getParameter("maximumFileSize"));
                Environment.setIntValue(Environment.PROP_ATTACH_MAX_FILE_SIZE, maxFileSizeInKB * 1000);
                Environment.setValue(Environment.PROP_ATTACH_TYPE, request.getParameter("attachmentType"));
                Environment.setBooleanValue(Environment.PROP_TOPIC_CONVERT_TABS, (request.getParameter("convertTabs") != null));
                if (request.getParameter("databaseType") != null) {
                    Environment.setValue(Environment.PROP_DB_TYPE, request.getParameter("databaseType"));
                }
                if (request.getParameter("wikiServerHostname") !=  null && !request.getParameter("wikiServerHostname").equals("")) {
                    Environment.setValue(Environment.PROP_BASE_SERVER_HOSTNAME, request.getParameter("wikiServerHostname"));
                } else {
                    Environment.setValue(Environment.PROP_BASE_SERVER_HOSTNAME,"");
                }
                Environment.setValue(Environment.PROP_FILE_ENCODING, request.getParameter("fileEncoding"));
                Environment.setBooleanValue(
                    Environment.PROP_PARSER_SEPARATE_WIKI_TITLE_WORDS,
                    request.getParameter("separateWikiTitleWords") != null
                );
                Environment.setBooleanValue(
                    Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY,
                    request.getParameter("suppressNotifyWithinSameDay") != null
                );
                int membershipType = Integer.parseInt(request.getParameter("usergroupType"));
                String usergroupType;
                if (membershipType == WikiBase.LDAP) {
                    usergroupType = "LDAP";
                } else if (membershipType == WikiBase.DATABASE) {
                    usergroupType = "DATABASE";
                } else {
                    usergroupType = "";
                }
                Environment.setValue(Environment.PROP_USERGROUP_TYPE, usergroupType);
                String[] autoFill = {
                    Environment.PROP_USERGROUP_FACTORY,
                    Environment.PROP_USERGROUP_URL,
                    Environment.PROP_USERGROUP_USERNAME,
                    Environment.PROP_USERGROUP_PASSWORD,
                    Environment.PROP_USERGROUP_BASIC_SEARCH,
                    Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS,
                    Environment.PROP_USERGROUP_USERID_FIELD,
                    Environment.PROP_USERGROUP_FULLNAME_FIELD,
                    Environment.PROP_USERGROUP_MAIL_FIELD,
                    Environment.PROP_USERGROUP_DETAILVIEW
                };
                for (int i = 0; i < autoFill.length; i++) {
                    if (request.getParameter(autoFill[i]) != null) {
                        if (autoFill[i].equals(Environment.PROP_USERGROUP_PASSWORD)) {
                            Encryption.setEncryptedProperty(
                                Environment.PROP_USERGROUP_PASSWORD,
                                request.getParameter(autoFill[i])
                            );
                        } else {
                            Environment.setValue(autoFill[i], request.getParameter(autoFill[i]));
                        }
                    }
                }
                // FIXME (PARSER_TEMP) - temporary property until conversion is complete
                Environment.setBooleanValue(
                    Environment.PROP_PARSER_NEW,
                    request.getParameter(Environment.PROP_PARSER_NEW) != null
                );
                if (Environment.getValue(Environment.PROP_FILE_HOME_DIR) == null) {
                    // if home directory set empty, use system home directory
                    String dir = System.getProperty("user.home") + System.getProperty("file.separator") + "wiki";
                    Environment.setValue(Environment.PROP_FILE_HOME_DIR, dir);
                }
                if (WikiBase.getPersistenceType() == WikiBase.DATABASE) {
                    DatabaseConnection.setPoolInitialized(false);
                }
                Environment.saveProperties();
                WikiBase.initialise();
                message = messages.getString("admin.message.changessaved");
            } else if (functionType.equals("clearEditLock")) {
                WikiBase base = WikiBase.getInstance();
                base.unlockTopic(request.getParameter("virtual-wiki"), request.getParameter("topic"));
                message = Utilities.resource("admin.message.lockcleared", request.getLocale());
            } else if (functionType.equals("removeUser")) {
                String user = request.getParameter("userName");
                WikiMembers members = WikiBase.getInstance().getWikiMembersInstance(virtualWiki);
                if (members.removeMember(user)) {
                    message = user + messages.getString("admin.message.userremoved.success");
                } else {
                    message = user + messages.getString("admin.message.userremoved.failure");
                }
            } else if (functionType.equals("addVirtualWiki")) {
                String newWiki = request.getParameter("newVirtualWiki");
                logger.debug("Adding new Wiki: " + newWiki);
                WikiBase.getInstance().addVirtualWiki(newWiki);
                message = messages.getString("admin.message.virtualwikiadded");
                WikiBase.initialise();
            } else if (functionType.equals("changePassword")) {
                String oldPassword = request.getParameter("oldPassword");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");
                if (!Encryption.getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD).equals(oldPassword)) {
                    message = messages.getString("admin.message.oldpasswordincorrect");
                } else if (!newPassword.equals(confirmPassword)) {
                    message = messages.getString("admin.message.passwordsnomatch");
                } else {
                    Encryption.setEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD, newPassword);
                    Environment.saveProperties();
                    message = messages.getString("admin.message.passwordchanged");
                }
            } else if ("panic".equals(functionType)) {
                WikiBase.getInstance().panic();
            }
        } catch (Exception err) {
            if (err instanceof java.sql.SQLException) {
                logger.warn("SQL-Exception in the admin console catched", err);
                message = err.getMessage();
            } else {
                error(request, response, err);
                return;
            }
        }
        try {
            String virtualWiki = (String) request.getAttribute("virtualWiki");
            if (request.getParameter("addReadOnly") != null) {
                Topic t = new Topic(request.getParameter("readOnlyTopic"));
                t.makeTopicReadOnly(virtualWiki);
            }
            if (request.getParameter("removeReadOnly") != null) {
                String[] topics = request.getParameterValues("markRemove");
                for (int i = 0; i < topics.length; i++) {
                    Topic t = new Topic(topics[i]);
                    t.makeTopicWritable(virtualWiki);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        String next = JSPUtils.createRedirectURL(request, "Wiki?action=" + WikiServlet.ACTION_ADMIN + "&message=" + message);
        response.sendRedirect(response.encodeRedirectURL(next));
    }
}
