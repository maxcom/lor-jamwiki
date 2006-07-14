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
package org.jamwiki.persistency.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.PersistencyHandler;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.XMLUtil;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class FileHandler extends PersistencyHandler {

	private static final Logger logger = Logger.getLogger(FileHandler.class);

	private static final String CONTRIBUTIONS_DIR = "contributions";
	private static final String DELETE_DIR = "deletes";
	private final static String EXT = ".xml";
	private static final String LOCK_DIR = "locks";
	private static int NEXT_TOPIC_ID = 0;
	private static final String NEXT_TOPIC_ID_FILE = "topic.id";
	private static int NEXT_TOPIC_VERSION_ID = 0;
	private static final String NEXT_TOPIC_VERSION_ID_FILE = "topic_version.id";
	private static int NEXT_WIKI_USER_ID = 0;
	private static int NEXT_WIKI_FILE_ID = 0;
	private static final String NEXT_WIKI_FILE_ID_FILE = "wiki_file.id";
	private static int NEXT_WIKI_FILE_VERSION_ID = 0;
	private static final String NEXT_WIKI_FILE_VERSION_ID_FILE = "wiki_file_version.id";
	private static final String NEXT_WIKI_USER_ID_FILE = "wiki_user.id";
	private static final String READ_ONLY_DIR = "readonly";
	private static final String RECENT_CHANGE_DIR = "changes";
	private static final String TOPIC_DIR = "topics";
	private static final String TOPIC_VERSION_DIR = "versions";
	private static final String VIRTUAL_WIKI_LIST = "virtualwikis";
	private static final String WIKI_FILE_DIR = "files";
	private static final String WIKI_FILE_VERSION_DIR = "fileversions";
	private static final String WIKI_USER_DIR = "wikiusers";
	private static Properties WIKI_USER_ID_HASH = null;
	private static final String WIKI_USER_ID_HASH_FILE = "wiki_user.hash";
	protected static final String XML_RECENT_CHANGE_ROOT = "change";
	protected static final String XML_RECENT_CHANGE_TOPIC_ID = "topicid";
	protected static final String XML_RECENT_CHANGE_TOPIC_NAME = "topicname";
	protected static final String XML_RECENT_CHANGE_TOPIC_VERSION_ID = "topicversionid";
	protected static final String XML_RECENT_CHANGE_PREVIOUS_TOPIC_VERSION_ID = "previoustopicversionid";
	protected static final String XML_RECENT_CHANGE_AUTHOR_ID = "authorid";
	protected static final String XML_RECENT_CHANGE_AUTHOR_NAME = "authorname";
	protected static final String XML_RECENT_CHANGE_EDIT_COMMENT = "editcomment";
	protected static final String XML_RECENT_CHANGE_EDIT_DATE = "editdate";
	protected static final String XML_RECENT_CHANGE_EDIT_TYPE = "edittype";
	protected static final String XML_RECENT_CHANGE_VIRTUAL_WIKI = "virtualwiki";
	protected static final String XML_TOPIC_ROOT = "page";
	protected static final String XML_TOPIC_TITLE = "title";
	protected static final String XML_TOPIC_ID = "id";
	protected static final String XML_TOPIC_VIRTUAL_WIKI = "virtualwiki";
	protected static final String XML_TOPIC_TEXT = "text";
	protected static final String XML_TOPIC_ADMIN_ONLY = "admin";
	protected static final String XML_TOPIC_LOCKED_BY = "lockedby";
	protected static final String XML_TOPIC_LOCK_DATE = "lockdate";
	protected static final String XML_TOPIC_LOCK_KEY = "lockkey";
	protected static final String XML_TOPIC_READ_ONLY = "readonly";
	protected static final String XML_TOPIC_DELETED = "deleted";
	protected static final String XML_TOPIC_TYPE = "type";
	protected static final String XML_TOPIC_VERSION_ROOT = "revision";
	protected static final String XML_TOPIC_VERSION_ID = "id";
	protected static final String XML_TOPIC_VERSION_TOPIC_ID = "topicid";
	protected static final String XML_TOPIC_VERSION_AUTHOR = "contributor";
	protected static final String XML_TOPIC_VERSION_AUTHOR_ID = "id";
	protected static final String XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS = "ip";
	protected static final String XML_TOPIC_VERSION_EDIT_COMMENT = "comment";
	protected static final String XML_TOPIC_VERSION_EDIT_DATE = "timestamp";
	protected static final String XML_TOPIC_VERSION_EDIT_TYPE = "edittype";
	protected static final String XML_TOPIC_VERSION_PREVIOUS_TOPIC_VERSION_ID = "previoustopicversionid";
	protected static final String XML_TOPIC_VERSION_TEXT = "text";
	protected static final String XML_WIKI_FILE_ROOT = "wikifile";
	protected static final String XML_WIKI_FILE_NAME = "filename";
	protected static final String XML_WIKI_FILE_ID = "fileid";
	protected static final String XML_WIKI_FILE_TOPIC_ID = "topicid";
	protected static final String XML_WIKI_FILE_VIRTUAL_WIKI = "virtualwiki";
	protected static final String XML_WIKI_FILE_URL = "url";
	protected static final String XML_WIKI_FILE_ADMIN_ONLY = "admin";
	protected static final String XML_WIKI_FILE_READ_ONLY = "readonly";
	protected static final String XML_WIKI_FILE_DELETED = "deleted";
	protected static final String XML_WIKI_FILE_MIME_TYPE = "mimetype";
	protected static final String XML_WIKI_FILE_SIZE = "filesize";
	protected static final String XML_WIKI_FILE_VERSION_ROOT = "revision";
	protected static final String XML_WIKI_FILE_VERSION_ID = "id";
	protected static final String XML_WIKI_FILE_VERSION_FILE_ID = "fileid";
	protected static final String XML_WIKI_FILE_VERSION_UPLOAD_COMMENT = "comment";
	protected static final String XML_WIKI_FILE_VERSION_URL = "url";
	protected static final String XML_WIKI_FILE_VERSION_AUTHOR = "contributor";
	protected static final String XML_WIKI_FILE_VERSION_AUTHOR_ID = "id";
	protected static final String XML_WIKI_FILE_VERSION_AUTHOR_IP_ADDRESS = "ip";
	protected static final String XML_WIKI_FILE_VERSION_UPLOAD_DATE = "timestamp";
	protected static final String XML_WIKI_FILE_VERSION_MIME_TYPE = "mimetype";
	protected static final String XML_WIKI_FILE_VERSION_SIZE = "filesize";
	protected static final String XML_WIKI_USER_ROOT = "wikiuser";
	protected static final String XML_WIKI_USER_ADMIN = "admin";
	protected static final String XML_WIKI_USER_CREATE_DATE = "createdate";
	protected static final String XML_WIKI_USER_CREATE_IP_ADDRESS = "createipaddress";
	protected static final String XML_WIKI_USER_DISPLAY_NAME = "displayname";
	protected static final String XML_WIKI_USER_EMAIL = "email";
	protected static final String XML_WIKI_USER_ENCODED_PASSWORD = "encodedpassword";
	protected static final String XML_WIKI_USER_FIRST_NAME = "firstname";
	protected static final String XML_WIKI_USER_LAST_LOGIN_DATE = "lastlogindate";
	protected static final String XML_WIKI_USER_LAST_LOGIN_IP_ADDRESS = "lastloginipaddress";
	protected static final String XML_WIKI_USER_LAST_NAME = "lastname";
	protected static final String XML_WIKI_USER_LOGIN = "login";
	protected static final String XML_WIKI_USER_ID = "userid";

	/**
	 *
	 */
	public FileHandler() {
	}

	/**
	 *
	 */
	protected void addRecentChange(RecentChange change) throws Exception {
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_RECENT_CHANGE_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_TOPIC_ID, change.getTopicId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_TOPIC_NAME, change.getTopicName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_TOPIC_VERSION_ID, change.getTopicVersionId()));
		content.append("\n");
		if (change.getPreviousTopicVersionId() != null) {
			content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_PREVIOUS_TOPIC_VERSION_ID, change.getPreviousTopicVersionId()));
			content.append("\n");
		}
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_AUTHOR_ID, change.getAuthorId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_AUTHOR_NAME, change.getAuthorName(), true));
		content.append("\n");
		if (change.getEditComment() != null) {
			content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_EDIT_COMMENT, change.getEditComment(), true));
			content.append("\n");
		}
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_EDIT_DATE, change.getEditDate()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_EDIT_TYPE, change.getEditType()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_VIRTUAL_WIKI, change.getVirtualWiki(), true));
		content.append("\n");
		content.append("</").append(XML_RECENT_CHANGE_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = recentChangeFilename(change.getTopicVersionId());
		File file = FileHandler.getPathFor(change.getVirtualWiki(), FileHandler.RECENT_CHANGE_DIR, filename);
		FileUtils.writeStringToFile(file, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
		// also write to user contributions directory
		file = FileHandler.getPathFor(change.getVirtualWiki(), FileHandler.CONTRIBUTIONS_DIR, change.getAuthorName(), filename);
		FileUtils.writeStringToFile(file, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	protected void addTopic(Topic topic) throws Exception {
		if (topic.getTopicId() <= 0) {
			topic.setTopicId(nextTopicId());
		}
		if (topic.getTopicId() > NEXT_TOPIC_ID) {
			NEXT_TOPIC_ID = topic.getTopicId();
			nextFileWrite(NEXT_TOPIC_ID, getPathFor(null, null, NEXT_TOPIC_ID_FILE));
		}
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_TOPIC_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_TITLE, topic.getName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_ID, topic.getTopicId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VIRTUAL_WIKI, topic.getVirtualWiki(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_TEXT, topic.getTopicContent(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_ADMIN_ONLY, topic.getAdminOnly()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_LOCKED_BY, topic.getLockedBy()));
		content.append("\n");
		if (topic.getLockedDate() != null) {
			content.append(XMLUtil.buildTag(XML_TOPIC_LOCK_DATE, topic.getLockedDate()));
			content.append("\n");
		}
		if (topic.getLockSessionKey() != null) {
			content.append(XMLUtil.buildTag(XML_TOPIC_LOCK_KEY, topic.getLockSessionKey(), true));
			content.append("\n");
		}
		content.append(XMLUtil.buildTag(XML_TOPIC_READ_ONLY, topic.getReadOnly()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_DELETED, topic.getDeleted()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_TYPE, topic.getTopicType()));
		content.append("\n");
		content.append("</").append(XML_TOPIC_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = topicFilename(topic.getName());
		File file = FileHandler.getPathFor(topic.getVirtualWiki(), FileHandler.TOPIC_DIR, filename);
		FileUtils.writeStringToFile(file, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	protected void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
		if (topicVersion.getTopicVersionId() <= 0) {
			topicVersion.setTopicVersionId(nextTopicVersionId());
		}
		if (topicVersion.getTopicVersionId() > NEXT_TOPIC_VERSION_ID) {
			NEXT_TOPIC_VERSION_ID = topicVersion.getTopicVersionId();
			nextFileWrite(NEXT_TOPIC_VERSION_ID, getPathFor(null, null, NEXT_TOPIC_VERSION_ID_FILE));
		}
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_TOPIC_VERSION_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_ID, topicVersion.getTopicVersionId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_TOPIC_ID, topicVersion.getTopicId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_EDIT_DATE, topicVersion.getEditDate()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_TEXT, topicVersion.getVersionContent(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_EDIT_TYPE, topicVersion.getEditType()));
		content.append("\n");
		content.append("<").append(XML_TOPIC_VERSION_AUTHOR).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_AUTHOR_ID, topicVersion.getAuthorId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS, topicVersion.getAuthorIpAddress(), true));
		content.append("\n");
		content.append("</").append(XML_TOPIC_VERSION_AUTHOR).append(">");
		content.append("\n");
		if (topicVersion.getEditComment() != null) {
			content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_EDIT_COMMENT, topicVersion.getEditComment(), true));
			content.append("\n");
		}
		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_PREVIOUS_TOPIC_VERSION_ID, topicVersion.getPreviousTopicVersionId()));
		content.append("\n");
		content.append("</").append(XML_TOPIC_VERSION_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = topicVersionFilename(topicVersion.getTopicVersionId());
		File versionFile = FileHandler.getPathFor(virtualWiki, FileHandler.TOPIC_VERSION_DIR, topicName, filename);
		FileUtils.writeStringToFile(versionFile, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	protected void addVirtualWiki(String virtualWiki) throws Exception {
		Collection all = new ArrayList();
		File file = getPathFor("", null, VIRTUAL_WIKI_LIST);
		if (file.exists()) {
			List lines = FileUtils.readLines(file, Environment.getValue(Environment.PROP_FILE_ENCODING));
			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
				String line = (String)iterator.next();
				all.add(line);
			}
		}
		all.add(virtualWiki);
		FileUtils.writeLines(file, Environment.getValue(Environment.PROP_FILE_ENCODING), all);
	}

	/**
	 *
	 */
	protected void addWikiFile(String topicName, WikiFile wikiFile) throws Exception {
		if (wikiFile.getFileId() <= 0) {
			wikiFile.setFileId(nextWikiFileId());
		}
		if (wikiFile.getFileId() > NEXT_WIKI_FILE_ID) {
			NEXT_WIKI_FILE_ID = wikiFile.getFileId();
			nextFileWrite(NEXT_WIKI_FILE_ID, getPathFor(null, null, NEXT_WIKI_FILE_ID_FILE));
		}
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_WIKI_FILE_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_NAME, wikiFile.getFileName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_ID, wikiFile.getFileId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_TOPIC_ID, wikiFile.getTopicId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VIRTUAL_WIKI, wikiFile.getVirtualWiki(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_URL, wikiFile.getUrl(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_ADMIN_ONLY, wikiFile.getAdminOnly()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_READ_ONLY, wikiFile.getReadOnly()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_DELETED, wikiFile.getDeleted()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_MIME_TYPE, wikiFile.getMimeType(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_SIZE, wikiFile.getFileSize()));
		content.append("\n");
		content.append("</").append(XML_WIKI_FILE_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		// name file after its parent topic
		String filename = topicFilename(topicName);
		File file = FileHandler.getPathFor(wikiFile.getVirtualWiki(), FileHandler.WIKI_FILE_DIR, filename);
		FileUtils.writeStringToFile(file, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	protected void addWikiFileVersion(String virtualWiki, String topicName, WikiFileVersion wikiFileVersion) throws Exception {
		if (wikiFileVersion.getFileVersionId() <= 0) {
			wikiFileVersion.setFileVersionId(nextWikiFileVersionId());
		}
		if (wikiFileVersion.getFileVersionId() > NEXT_WIKI_FILE_VERSION_ID) {
			NEXT_WIKI_FILE_VERSION_ID = wikiFileVersion.getFileVersionId();
			nextFileWrite(NEXT_WIKI_FILE_VERSION_ID, getPathFor(null, null, NEXT_WIKI_FILE_VERSION_ID_FILE));
		}
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_WIKI_FILE_VERSION_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_ID, wikiFileVersion.getFileVersionId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_FILE_ID, wikiFileVersion.getFileId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_UPLOAD_COMMENT, wikiFileVersion.getUploadComment(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_URL, wikiFileVersion.getUrl(), true));
		content.append("\n");
		content.append("<").append(XML_WIKI_FILE_VERSION_AUTHOR).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_AUTHOR_ID, wikiFileVersion.getAuthorId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_AUTHOR_IP_ADDRESS, wikiFileVersion.getAuthorIpAddress(), true));
		content.append("\n");
		content.append("</").append(XML_WIKI_FILE_VERSION_AUTHOR).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_UPLOAD_DATE, wikiFileVersion.getUploadDate()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_MIME_TYPE, wikiFileVersion.getMimeType(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_VERSION_SIZE, wikiFileVersion.getFileSize()));
		content.append("\n");
		content.append("</").append(XML_WIKI_FILE_VERSION_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = wikiFileVersionFilename(wikiFileVersion.getFileVersionId());
		File versionFile = FileHandler.getPathFor(virtualWiki, FileHandler.WIKI_FILE_VERSION_DIR, topicName, filename);
		FileUtils.writeStringToFile(versionFile, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	protected void addWikiUser(WikiUser user) throws Exception {
		if (user.getUserId() < 1) {
			user.setUserId(nextWikiUserId());
		}
		if (user.getUserId() > NEXT_WIKI_USER_ID) {
			NEXT_WIKI_USER_ID = user.getUserId();
			nextFileWrite(NEXT_WIKI_USER_ID, getPathFor(null, null, NEXT_WIKI_USER_ID_FILE));
		}
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_WIKI_USER_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_ID, user.getUserId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_ADMIN, user.getAdmin()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_CREATE_DATE, user.getCreateDate()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_CREATE_IP_ADDRESS, user.getCreateIpAddress(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_DISPLAY_NAME, user.getDisplayName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_EMAIL, user.getEmail(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_ENCODED_PASSWORD, user.getEncodedPassword(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_FIRST_NAME, user.getFirstName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_LAST_LOGIN_DATE, user.getLastLoginDate()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_LAST_LOGIN_IP_ADDRESS, user.getLastLoginIpAddress(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_LAST_NAME, user.getLastName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_WIKI_USER_LOGIN, user.getLogin(), true));
		content.append("\n");
		content.append("</").append(XML_WIKI_USER_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = wikiUserFilename(user.getLogin());
		File userFile = FileHandler.getPathFor(null, FileHandler.WIKI_USER_DIR, filename);
		FileUtils.writeStringToFile(userFile, content.toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
		File userIdHashFile = getPathFor(null, null, WIKI_USER_ID_HASH_FILE);
		if (WIKI_USER_ID_HASH == null && userIdHashFile.exists()) {
			WIKI_USER_ID_HASH.load(new FileInputStream(userIdHashFile));
		} else if (WIKI_USER_ID_HASH == null) {
			WIKI_USER_ID_HASH = new Properties();
		}
		WIKI_USER_ID_HASH.setProperty(new Integer(user.getUserId()).toString(), user.getLogin());
		WIKI_USER_ID_HASH.store(new FileOutputStream(userIdHashFile), null);
	}

	/**
	 *
	 */
	private void createVirtualWikiList(File virtualList) throws Exception {
		FileUtils.writeStringToFile(virtualList, WikiBase.DEFAULT_VWIKI, Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	public void deleteReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		super.deleteReadOnlyTopic(virtualWiki, topicName);
		String filename = readOnlyFilename(topicName);
		File readOnlyFile = getPathFor(virtualWiki, READ_ONLY_DIR, filename);
		if (!readOnlyFile.exists()) {
			logger.warn("No read only file for topic " + virtualWiki + " / " + topicName);
		}
		readOnlyFile.delete();
	}

	/**
	 *
	 */
	public void deleteTopic(Topic topic) throws Exception {
		super.deleteTopic(topic);
		String filename = topicFilename(topic.getName());
		// move file from topic directory to delete directory
		File oldFile = getPathFor(topic.getVirtualWiki(), TOPIC_DIR, filename);
		File newFile = getPathFor(topic.getVirtualWiki(), DELETE_DIR, filename);
		if (!oldFile.renameTo(newFile)) {
			throw new Exception("Unable to move file to delete directory for topic " + topic.getVirtualWiki() + " / " + topic.getName());
		}
	}

	/**
	 *
	 */
	public static String fileBase(String virtualWiki) {
		String path = Environment.getValue(Environment.PROP_BASE_FILE_DIR);
		if (StringUtils.hasText(virtualWiki)) {
			path += File.separator + virtualWiki;
		}
		return path;
	}

	/**
	 *
	 */
	public List getAllTopicNames(String virtualWiki) throws Exception {
		List all = new ArrayList();
		File[] files = retrieveTopicFiles(virtualWiki);
		for (int i = 0; i < files.length; i++) {
			String topicName = files[i].getName();
			// strip extension
			int pos = topicName.lastIndexOf(EXT);
			if (pos != -1) topicName = topicName.substring(0, pos);
			// decode
			topicName = Utilities.decodeURL(topicName);
			all.add(topicName);
		}
		return all;
	}

	/**
	 * Returns all versions of the given topic in reverse chronological order
	 * @param virtualWiki
	 * @param topicName
	 * @return
	 * @throws Exception
	 */
	public List getAllTopicVersions(String virtualWiki, String topicName) throws Exception {
		List all = new LinkedList();
		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
		for (int i = 0; i < files.length; i++) {
			TopicVersion version = initTopicVersion(files[i]);
			all.add(version);
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllWikiFileTopicNames(String virtualWiki) throws Exception {
		List all = new ArrayList();
		File[] files = retrieveWikiFileFiles(virtualWiki);
		for (int i = 0; i < files.length; i++) {
			String topicName = files[i].getName();
			// strip extension
			int pos = topicName.lastIndexOf(EXT);
			if (pos != -1) topicName = topicName.substring(0, pos);
			// decode
			topicName = Utilities.decodeURL(topicName);
			all.add(topicName);
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllWikiFileVersions(String virtualWiki, String topicName) throws Exception {
		List all = new LinkedList();
		File[] files = retrieveWikiFileVersionFiles(virtualWiki, topicName);
		for (int i = 0; i < files.length; i++) {
			WikiFileVersion version = initWikiFileVersion(files[i]);
			all.add(version);
		}
		return all;
	}

	/**
	 *
	 */
	public List getAllWikiUserLogins() throws Exception {
		List all = new ArrayList();
		File[] files = retrieveWikiUserFiles();
		for (int i = 0; i < files.length; i++) {
			String login = files[i].getName();
			// strip extension
			int pos = login.lastIndexOf(EXT);
			if (pos != -1) login = login.substring(0, pos);
			// decode
			login = Utilities.decodeURL(login);
			all.add(login);
		}
		return all;
	}

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception {
		List all = new LinkedList();
		File[] files = retrieveLockFiles(virtualWiki);
		for (int i = 0; i < files.length; i++) {
			String topicName = Utilities.decodeURL(files[i].getName());
			Topic topic = lookupTopic(virtualWiki, topicName);
			if (topic == null) {
				logger.error("Unable to find topic for locked file " + virtualWiki + " / " + topicName);
				continue;
			}
			all.add(topic);
		}
		return all;
	}

	/**
	 *
	 */
	protected static File getPathFor(String virtualWiki, String dir, String fileName) {
		return getPathFor(virtualWiki, dir, null, fileName);
	}

	/**
	 *
	 */
	protected static File getPathFor(String virtualWiki, String dir1, String dir2, String fileName) {
		if (!StringUtils.hasText(Environment.getValue(Environment.PROP_BASE_FILE_DIR))) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.hasText(virtualWiki)) {
			// this is questionable, but the virtual wiki list does it
			logger.info("Attempting to write file with empty virtual wiki for file " + fileName);
			virtualWiki = "";
		}
		buffer.append(fileBase(virtualWiki));
		buffer.append(File.separator);
		if (dir1 != null) {
			buffer.append(Utilities.encodeSafeFileName(dir1));
			buffer.append(File.separator);
		}
		if (dir2 != null) {
			buffer.append(Utilities.encodeSafeFileName(dir2));
			buffer.append(File.separator);
		}
		File directory = new File(buffer.toString());
		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (fileName != null) {
			buffer.append(Utilities.encodeSafeFileName(fileName));
		}
		return new File(buffer.toString());
	}

	/**
	 * Return a list of all read-only topics
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
		List all = new LinkedList();
		File[] files = retrieveReadOnlyFiles(virtualWiki);
		for (int i = 0; i < files.length; i++) {
			String topicName = Utilities.decodeURL(files[i].getName());
			Topic topic = lookupTopic(virtualWiki, topicName);
			if (topic == null) {
				logger.error("Unable to find topic for read only file " + virtualWiki + " / " + topicName);
				continue;
			}
			all.add(topic.getName());
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getRecentChanges(String virtualWiki, int numChanges) throws Exception {
		List all = new LinkedList();
		File[] files = retrieveRecentChangeFiles(virtualWiki);
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			if (i >= numChanges) break;
			RecentChange change = initRecentChange(files[i]);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getUserContributions(String virtualWiki, String userString, int num) throws Exception {
		List all = new LinkedList();
		File[] files = retrieveUserContributionsFiles(virtualWiki, userString);
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			if (i >= num) break;
			RecentChange change = initRecentChange(files[i]);
			all.add(change);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		Collection all = new ArrayList();
		File file = getPathFor("", null, VIRTUAL_WIKI_LIST);
		List lines = FileUtils.readLines(file, Environment.getValue(Environment.PROP_FILE_ENCODING));
		for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
			String line = (String)iterator.next();
			all.add(line);
		}
		if (!all.contains(WikiBase.DEFAULT_VWIKI)) {
			all.add(WikiBase.DEFAULT_VWIKI);
		}
		return all;
	}

	/**
	 * Checks if lock exists
	 */
	public synchronized boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception {
		String filename = lockFilename(topicName);
		File lockFile = getPathFor(virtualWiki, LOCK_DIR, filename);
		Topic topic = lookupTopic(virtualWiki, topicName);
		if (lockFile.exists() && topic != null) {
			String lockKey = topic.getLockSessionKey();
			return (lockKey != null && key.equals(lockKey));
		}
		return lockTopic(virtualWiki, topicName, key);
	}

	/**
	 * Set up defaults if necessary
	 */
	public void initialize(Locale locale, WikiUser user) throws Exception {
		// create the virtual wiki list file if necessary
		File virtualList = getPathFor("", null, VIRTUAL_WIKI_LIST);
		// get the virtual wiki list and set up the file system
		if (!virtualList.exists()) {
			createVirtualWikiList(virtualList);
		}
		super.initialize(locale, user);
	}

	/**
	 *
	 */
	protected static RecentChange initRecentChange(File file) {
		if (!file.exists()) return null;
		try {
			RecentChange change = new RecentChange();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_RECENT_CHANGE_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_RECENT_CHANGE_TOPIC_ID)) {
					change.setTopicId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_RECENT_CHANGE_TOPIC_NAME)) {
					change.setTopicName(rootChild.getTextContent());
				} else if (childName.equals(XML_RECENT_CHANGE_TOPIC_VERSION_ID)) {
					change.setTopicVersionId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_RECENT_CHANGE_PREVIOUS_TOPIC_VERSION_ID)) {
					change.setPreviousTopicVersionId(new Integer(rootChild.getTextContent()));
				} else if (childName.equals(XML_RECENT_CHANGE_AUTHOR_ID)) {
					change.setAuthorId(new Integer(rootChild.getTextContent()));
				} else if (childName.equals(XML_RECENT_CHANGE_AUTHOR_NAME)) {
					change.setAuthorName(rootChild.getTextContent());
				} else if (childName.equals(XML_RECENT_CHANGE_EDIT_COMMENT)) {
					change.setEditComment(rootChild.getTextContent());
				} else if (childName.equals(XML_RECENT_CHANGE_EDIT_DATE)) {
					change.setEditDate(Timestamp.valueOf(rootChild.getTextContent()));
				} else if (childName.equals(XML_RECENT_CHANGE_EDIT_TYPE)) {
					change.setEditType(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_RECENT_CHANGE_VIRTUAL_WIKI)) {
					change.setVirtualWiki(rootChild.getTextContent());
				}
			}
			return change;
		} catch (Exception e) {
			logger.error("Failure while initializing recent changes for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static Topic initTopic(File file) {
		if (!file.exists()) return null;
		try {
			Topic topic = new Topic();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_TOPIC_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_TOPIC_TITLE)) {
					topic.setName(rootChild.getTextContent());
				} else if (childName.equals(XML_TOPIC_ID)) {
					topic.setTopicId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_TOPIC_VIRTUAL_WIKI)) {
					topic.setVirtualWiki(rootChild.getTextContent());
				} else if (childName.equals(XML_TOPIC_TEXT)) {
					topic.setTopicContent(rootChild.getTextContent());
				} else if (childName.equals(XML_TOPIC_ADMIN_ONLY)) {
					topic.setAdminOnly(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_TOPIC_LOCKED_BY)) {
					topic.setLockedBy(new Integer(rootChild.getTextContent()));
				} else if (childName.equals(XML_TOPIC_LOCK_DATE)) {
					topic.setLockedDate(Timestamp.valueOf(rootChild.getTextContent()));
				} else if (childName.equals(XML_TOPIC_LOCK_KEY)) {
					topic.setLockSessionKey(rootChild.getTextContent());
				} else if (childName.equals(XML_TOPIC_READ_ONLY)) {
					topic.setReadOnly(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_TOPIC_DELETED)) {
					topic.setDeleted(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_TOPIC_TYPE)) {
					topic.setTopicType(new Integer(rootChild.getTextContent()).intValue());
				}
			}
			return topic;
		} catch (Exception e) {
			logger.error("Failure while initializing topic for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static TopicVersion initTopicVersion(File file) {
		if (!file.exists()) return null;
		try {
			TopicVersion topicVersion = new TopicVersion();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_TOPIC_VERSION_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_TOPIC_VERSION_ID)) {
					topicVersion.setTopicVersionId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_TOPIC_VERSION_TOPIC_ID)) {
					topicVersion.setTopicId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_COMMENT)) {
					topicVersion.setEditComment(rootChild.getTextContent());
				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_DATE)) {
					topicVersion.setEditDate(Timestamp.valueOf(rootChild.getTextContent()));
				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_TYPE)) {
					topicVersion.setEditType(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_TOPIC_VERSION_TEXT)) {
					topicVersion.setVersionContent(rootChild.getTextContent());
				} else if (childName.equals(XML_TOPIC_VERSION_AUTHOR)) {
					NodeList authorChildren = rootChild.getChildNodes();
					for (int j=0; j < authorChildren.getLength(); j++) {
						Node authorChild = authorChildren.item(j);
						if (authorChild.getNodeName().equals(XML_TOPIC_VERSION_AUTHOR_ID)) {
							topicVersion.setAuthorId(new Integer(authorChild.getTextContent()));
						} else if (childName.equals(XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS)) {
							topicVersion.setAuthorIpAddress(authorChild.getTextContent());
						}
					}
				} else if (childName.equals(XML_TOPIC_VERSION_PREVIOUS_TOPIC_VERSION_ID)) {
					topicVersion.setPreviousTopicVersionId(new Integer(rootChild.getTextContent()));
				}
			}
			return topicVersion;
		} catch (Exception e) {
			logger.error("Failure while initializing topic version for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static WikiFile initWikiFile(File file) {
		if (!file.exists()) return null;
		try {
			WikiFile wikiFile = new WikiFile();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_WIKI_FILE_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_WIKI_FILE_NAME)) {
					wikiFile.setFileName(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_ID)) {
					wikiFile.setFileId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_WIKI_FILE_TOPIC_ID)) {
					wikiFile.setTopicId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_WIKI_FILE_VIRTUAL_WIKI)) {
					wikiFile.setVirtualWiki(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_URL)) {
					wikiFile.setUrl(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_ADMIN_ONLY)) {
					wikiFile.setAdminOnly(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_WIKI_FILE_READ_ONLY)) {
					wikiFile.setReadOnly(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_WIKI_FILE_DELETED)) {
					wikiFile.setDeleted(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_WIKI_FILE_MIME_TYPE)) {
					wikiFile.setMimeType(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_SIZE)) {
					wikiFile.setFileSize(new Integer(rootChild.getTextContent()).intValue());
				}
			}
			return wikiFile;
		} catch (Exception e) {
			logger.error("Failure while initializing wiki file for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static WikiFileVersion initWikiFileVersion(File file) {
		if (!file.exists()) return null;
		try {
			WikiFileVersion wikiFileVersion = new WikiFileVersion();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_WIKI_FILE_VERSION_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_WIKI_FILE_VERSION_ID)) {
					wikiFileVersion.setFileVersionId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_FILE_ID)) {
					wikiFileVersion.setFileId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_UPLOAD_COMMENT)) {
					wikiFileVersion.setUploadComment(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_UPLOAD_DATE)) {
					wikiFileVersion.setUploadDate(Timestamp.valueOf(rootChild.getTextContent()));
				} else if (childName.equals(XML_WIKI_FILE_VERSION_MIME_TYPE)) {
					wikiFileVersion.setMimeType(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_URL)) {
					wikiFileVersion.setUrl(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_AUTHOR)) {
					NodeList authorChildren = rootChild.getChildNodes();
					for (int j=0; j < authorChildren.getLength(); j++) {
						Node authorChild = authorChildren.item(j);
						if (authorChild.getNodeName().equals(XML_WIKI_FILE_VERSION_AUTHOR_ID)) {
							wikiFileVersion.setAuthorId(new Integer(authorChild.getTextContent()));
						} else if (childName.equals(XML_WIKI_FILE_VERSION_AUTHOR_IP_ADDRESS)) {
							wikiFileVersion.setAuthorIpAddress(authorChild.getTextContent());
						}
					}
				} else if (childName.equals(XML_WIKI_FILE_VERSION_SIZE)) {
					wikiFileVersion.setFileSize(new Integer(rootChild.getTextContent()).intValue());
				}
			}
			return wikiFileVersion;
		} catch (Exception e) {
			logger.error("Failure while initializing topic version for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static WikiUser initWikiUser(File file) {
		if (!file.exists()) return null;
		try {
			WikiUser user = new WikiUser();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_WIKI_USER_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_WIKI_USER_ID)) {
					user.setUserId(new Integer(rootChild.getTextContent()).intValue());
				} else if (childName.equals(XML_WIKI_USER_ADMIN)) {
					user.setAdmin(new Boolean(rootChild.getTextContent()).booleanValue());
				} else if (childName.equals(XML_WIKI_USER_CREATE_DATE)) {
					user.setCreateDate(Timestamp.valueOf(rootChild.getTextContent()));
				} else if (childName.equals(XML_WIKI_USER_CREATE_IP_ADDRESS)) {
					user.setCreateIpAddress(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_DISPLAY_NAME)) {
					user.setDisplayName(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_EMAIL)) {
					user.setEmail(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_ENCODED_PASSWORD)) {
					user.setEncodedPassword(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_FIRST_NAME)) {
					user.setFirstName(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_LAST_LOGIN_DATE)) {
					user.setLastLoginDate(Timestamp.valueOf(rootChild.getTextContent()));
				} else if (childName.equals(XML_WIKI_USER_LAST_LOGIN_IP_ADDRESS)) {
					user.setLastLoginIpAddress(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_LAST_NAME)) {
					user.setLastName(rootChild.getTextContent());
				} else if (childName.equals(XML_WIKI_USER_LOGIN)) {
					user.setLogin(rootChild.getTextContent());
				}
			}
			return user;
		} catch (Exception e) {
			logger.error("Failure while initializing user for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	protected static String lockFilename(String topicName) {
		return topicName;
	}

	/**
	 * Locks a file for editing
	 */
	public synchronized boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception {
		if (!super.lockTopic(virtualWiki, topicName, key)) {
			return false;
		}
		String filename = lockFilename(topicName);
		File lockFile = getPathFor(virtualWiki, LOCK_DIR, filename);
		FileUtils.writeStringToFile(lockFile, topicName, Environment.getValue(Environment.PROP_FILE_ENCODING));
		return true;
	}

	/**
	 *
	 */
	protected synchronized TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception {
		// get all files, sorted.  last one is last version.
		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
		if (files == null) return null;
		File file = files[0];
		return initTopicVersion(file);
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
		String filename = topicFilename(topicName);
		File file = getPathFor(virtualWiki, FileHandler.TOPIC_DIR, filename);
		return initTopic(file);
	}

	/**
	 *
	 */
	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
		String filename = topicVersionFilename(topicVersionId);
		File file = getPathFor(virtualWiki, TOPIC_VERSION_DIR, topicName, filename);
		return initTopicVersion(file);
	}

	/**
	 *
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		// wiki file named after its associated topic
		String filename = topicFilename(topicName);
		File file = getPathFor(virtualWiki, FileHandler.WIKI_FILE_DIR, filename);
		return initWikiFile(file);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(int userId) throws Exception {
		String login = retrieveWikiUserLogin(userId);
		return lookupWikiUser(login);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login) throws Exception {
		if (login == null) return null;
		String filename = wikiUserFilename(login);
		File file = getPathFor(null, WIKI_USER_DIR, filename);
		return initWikiUser(file);
	}

	/**
	 *
	 */
	public WikiUser lookupWikiUser(String login, String password) throws Exception {
		WikiUser user = lookupWikiUser(login);
		if (user == null || password == null) return null;
		return (user.getEncodedPassword().equals(Encryption.encrypt(password))) ? user : null;
	}

	/**
	 *
	 */
	private static int nextFileWrite(int nextId, File file) throws Exception {
		FileUtils.writeStringToFile(file, new Integer(nextId).toString(), Environment.getValue(Environment.PROP_FILE_ENCODING));
		return nextId;
	}

	/**
	 *
	 */
	private static int nextTopicId() throws Exception {
		File topicIdFile = getPathFor(null, null, NEXT_TOPIC_ID_FILE);
		if (NEXT_TOPIC_ID < 1) {
			// read value from file
			if (topicIdFile.exists()) {
				String integer = FileUtils.readFileToString(topicIdFile, Environment.getValue(Environment.PROP_FILE_ENCODING));
				NEXT_TOPIC_ID = new Integer(integer).intValue();
			}
		}
		NEXT_TOPIC_ID++;
		return nextFileWrite(NEXT_TOPIC_ID, topicIdFile);
	}

	/**
	 *
	 */
	private static int nextTopicVersionId() throws Exception {
		File topicVersionIdFile = getPathFor(null, null, NEXT_TOPIC_VERSION_ID_FILE);
		if (NEXT_TOPIC_VERSION_ID < 1) {
			// read value from file
			if (topicVersionIdFile.exists()) {
				String integer = FileUtils.readFileToString(topicVersionIdFile, Environment.getValue(Environment.PROP_FILE_ENCODING));
				NEXT_TOPIC_VERSION_ID = new Integer(integer).intValue();
			}
		}
		NEXT_TOPIC_VERSION_ID++;
		return nextFileWrite(NEXT_TOPIC_VERSION_ID, topicVersionIdFile);
	}

	/**
	 *
	 */
	private static int nextWikiFileId() throws Exception {
		File wikiFileIdFile = getPathFor(null, null, NEXT_WIKI_FILE_ID_FILE);
		if (NEXT_WIKI_FILE_ID < 1) {
			// read value from file
			if (wikiFileIdFile.exists()) {
				String integer = FileUtils.readFileToString(wikiFileIdFile, Environment.getValue(Environment.PROP_FILE_ENCODING));
				NEXT_WIKI_FILE_ID = new Integer(integer).intValue();
			}
		}
		NEXT_WIKI_FILE_ID++;
		return nextFileWrite(NEXT_WIKI_FILE_ID, wikiFileIdFile);
	}

	/**
	 *
	 */
	private static int nextWikiFileVersionId() throws Exception {
		File wikiFileVersionIdFile = getPathFor(null, null, NEXT_WIKI_FILE_VERSION_ID_FILE);
		if (NEXT_WIKI_FILE_VERSION_ID < 1) {
			// read value from file
			if (wikiFileVersionIdFile.exists()) {
				String integer = FileUtils.readFileToString(wikiFileVersionIdFile, Environment.getValue(Environment.PROP_FILE_ENCODING));
				NEXT_WIKI_FILE_VERSION_ID = new Integer(integer).intValue();
			}
		}
		NEXT_WIKI_FILE_VERSION_ID++;
		return nextFileWrite(NEXT_WIKI_FILE_VERSION_ID, wikiFileVersionIdFile);
	}

	/**
	 *
	 */
	private static int nextWikiUserId() throws Exception {
		File userIdFile = getPathFor(null, null, NEXT_WIKI_USER_ID_FILE);
		if (NEXT_WIKI_USER_ID < 1) {
			// read value from file
			if (userIdFile.exists()) {
				String integer = FileUtils.readFileToString(userIdFile, Environment.getValue(Environment.PROP_FILE_ENCODING));
				NEXT_WIKI_USER_ID = new Integer(integer).intValue();
			}
		}
		NEXT_WIKI_USER_ID++;
		return nextFileWrite(NEXT_WIKI_USER_ID, userIdFile);
	}

	/**
	 *
	 */
	protected static String readOnlyFilename(String topicName) {
		return topicName;
	}

	/**
	 *
	 */
	protected static String recentChangeFilename(int topicVersionId) {
		return topicVersionId + EXT;
	}

	/**
	 *
	 */
	private File[] retrieveLockFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.LOCK_DIR);
		File[] files = file.listFiles();
		if (files == null) return null;
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveReadOnlyFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.READ_ONLY_DIR);
		File[] files = file.listFiles();
		if (files == null) return null;
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveRecentChangeFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.RECENT_CHANGE_DIR);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new WikiFileComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveTopicFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.TOPIC_DIR);
		File[] files = file.listFiles();
		if (files == null) return null;
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveTopicVersionFiles(String virtualWiki, String topicName) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.TOPIC_VERSION_DIR, topicName);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new WikiFileComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveUserContributionsFiles(String virtualWiki, String userString) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.CONTRIBUTIONS_DIR, userString);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new WikiFileComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveWikiFileFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.WIKI_FILE_DIR);
		File[] files = file.listFiles();
		if (files == null) return null;
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveWikiFileVersionFiles(String virtualWiki, String topicName) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.WIKI_FILE_VERSION_DIR, topicName);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new WikiFileComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveWikiUserFiles() throws Exception {
		File file = FileHandler.getPathFor(null, null, FileHandler.WIKI_USER_DIR);
		File[] files = file.listFiles();
		if (files == null) return null;
		return files;
	}

	/**
	 *
	 */
	private static String retrieveWikiUserLogin(int userId) throws Exception {
		if (WIKI_USER_ID_HASH == null) {
			WIKI_USER_ID_HASH = new Properties();
			File userIdHashFile = getPathFor(null, null, WIKI_USER_ID_HASH_FILE);
			if (userIdHashFile.exists()) {
				WIKI_USER_ID_HASH.load(new FileInputStream(userIdHashFile));
			}
		}
		return WIKI_USER_ID_HASH.getProperty(new Integer(userId).toString());
	}

	/**
	 *
	 */
	protected static String topicFilename(String topicName) {
		return topicName + EXT;
	}

	/**
	 *
	 */
	protected static String topicVersionFilename(int topicVersionId) {
		return topicVersionId + EXT;
	}

	/**
	 *
	 */
	public synchronized void unlockTopic(Topic topic) throws Exception {
		super.unlockTopic(topic);
		String filename = lockFilename(topic.getName());
		File lockFile = getPathFor(topic.getVirtualWiki(), LOCK_DIR, filename);
		if (!lockFile.exists()) {
			logger.warn("No lockfile to unlock topic " + topic.getVirtualWiki() + " / " + topic.getName());
		}
		lockFile.delete();
	}

	/**
	 *
	 */
	protected static String wikiFileVersionFilename(int wikiFileVersionId) {
		return wikiFileVersionId + EXT;
	}

	/**
	 *
	 */
	protected static String wikiUserFilename(String login) {
		return login + EXT;
	}

	/**
	 *
	 */
	public void writeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
		super.writeReadOnlyTopic(virtualWiki, topicName);
		String filename = readOnlyFilename(topicName);
		File readOnlyFile = getPathFor(virtualWiki, READ_ONLY_DIR, filename);
		FileUtils.writeStringToFile(readOnlyFile, topicName, Environment.getValue(Environment.PROP_FILE_ENCODING));
	}

	/**
	 *
	 */
	public void writeTopic(Topic topic, TopicVersion topicVersion) throws Exception {
		super.writeTopic(topic, topicVersion);
		unlockTopic(topic);
	}

	/**
	 *
	 */
	class WikiFileComparator implements Comparator {

		/**
		 *
		 */
		public int compare(Object first, Object second) {
			String one = ((File)first).getName();
			String two = ((File)second).getName();
			int pos = one.lastIndexOf(EXT);
			int arg1 = new Integer(one.substring(0, pos)).intValue();
			pos = two.lastIndexOf(EXT);
			int arg2 = new Integer(two.substring(0, pos)).intValue();
			return arg2 - arg1;
		}
	}
}
