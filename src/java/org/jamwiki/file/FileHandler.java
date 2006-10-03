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
package org.jamwiki.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.jamwiki.Environment;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.XMLUtil;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @deprecated This class exists solely to allow upgrades to JAMWiki 0.4.0 or
 *  greater and will be replaced during the JAMWiki 0.5.x or JAMWiki 0.6.x series.
 */
public class FileHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(FileHandler.class.getName());

	private static final String DELETE_DIR = "deletes";
	private final static String EXT = ".xml";
	private static int NEXT_TOPIC_ID = 0;
	private static final String NEXT_TOPIC_ID_FILE = "topic.id";
	private static int NEXT_TOPIC_VERSION_ID = 0;
	private static final String NEXT_TOPIC_VERSION_ID_FILE = "topic_version.id";
	private static int NEXT_VIRTUAL_WIKI_ID = 0;
	private static final String NEXT_VIRTUAL_WIKI_ID_FILE = "virtual_wiki.id";
	private static int NEXT_WIKI_USER_ID = 0;
	private static int NEXT_WIKI_FILE_ID = 0;
	private static final String NEXT_WIKI_FILE_ID_FILE = "wiki_file.id";
	private static int NEXT_WIKI_FILE_VERSION_ID = 0;
	private static final String NEXT_WIKI_FILE_VERSION_ID_FILE = "wiki_file_version.id";
	private static final String NEXT_WIKI_USER_ID_FILE = "wiki_user.id";
	private static final String RECENT_CHANGE_DIR = "changes";
	private static final String TOPIC_DIR = "topics";
	private static final String TOPIC_VERSION_DIR = "versions";
	private static final String VIRTUAL_WIKI_DIR = "virtualwiki";
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
	protected static final String XML_TOPIC_READ_ONLY = "readonly";
	protected static final String XML_TOPIC_DELETE_DATE = "deleted";
	protected static final String XML_TOPIC_REDIRECT_TO = "redirectto";
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
	protected static final String XML_VIRTUAL_WIKI_ROOT = "virtualwiki";
	protected static final String XML_VIRTUAL_WIKI_NAME = "name";
	protected static final String XML_VIRTUAL_WIKI_ID = "id";
	protected static final String XML_VIRTUAL_WIKI_DEFAULT_TOPIC_NAME = "defaulttopic";
	protected static final String XML_WIKI_FILE_ROOT = "wikifile";
	protected static final String XML_WIKI_FILE_NAME = "filename";
	protected static final String XML_WIKI_FILE_ID = "fileid";
	protected static final String XML_WIKI_FILE_TOPIC_ID = "topicid";
	protected static final String XML_WIKI_FILE_VIRTUAL_WIKI = "virtualwiki";
	protected static final String XML_WIKI_FILE_URL = "url";
	protected static final String XML_WIKI_FILE_ADMIN_ONLY = "admin";
	protected static final String XML_WIKI_FILE_READ_ONLY = "readonly";
	protected static final String XML_WIKI_FILE_DELETE_DATE = "deleted";
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
	public void addTopic(Topic topic) throws Exception {
		if (topic.getTopicId() < 1) {
			topic.setTopicId(nextTopicId());
		}
		this.saveTopic(topic);
	}

	/**
	 *
	 */
	public void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
		this.saveTopicVersion(virtualWiki, topicName, topicVersion);
	}

	/**
	 *
	 */
	public void addVirtualWiki(VirtualWiki virtualWiki) throws Exception {
		if (virtualWiki.getVirtualWikiId() < 1) {
			virtualWiki.setVirtualWikiId(nextVirtualWikiId());
		}
		this.saveVirtualWiki(virtualWiki);
	}

	/**
	 *
	 */
	public void addWikiFile(String topicName, WikiFile wikiFile) throws Exception {
		if (wikiFile.getFileId() < 1) {
			wikiFile.setFileId(nextWikiFileId());
		}
		this.saveWikiFile(topicName, wikiFile);
	}

	/**
	 *
	 */
	public void addWikiFileVersion(String virtualWiki, String topicName, WikiFileVersion wikiFileVersion) throws Exception {
		this.saveWikiFileVersion(virtualWiki, topicName, wikiFileVersion);
	}

	/**
	 *
	 */
	public void addWikiUser(WikiUser user) throws Exception {
		if (user.getUserId() < 1) {
			user.setUserId(nextWikiUserId());
		}
		this.saveWikiUser(user);
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
	public Collection getAllTopicNames(String virtualWiki) throws Exception {
		Vector all = new Vector();
		File[] files = retrieveTopicFiles(virtualWiki);
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			String topicName = files[i].getName();
			// strip extension
			int pos = topicName.lastIndexOf(EXT);
			if (pos != -1) topicName = topicName.substring(0, pos);
			// decode
			topicName = Utilities.decodeFromURL(topicName);
			all.add(topicName);
		}
		return all;
	}

	/**
	 * Returns all versions of the given topic in reverse chronological order.
	 *
	 * @param virtualWiki The name of the virtual wiki for which topic versions
	 *  are being retrieved.
	 * @param topicName The name of the topic for which versions are being retrieved.
	 * @param descending Set to <code>true</code> if the results should be ordered
	 *  in reverse chronological order.
	 * @return A Collection containing all TopicVersion objects for the given topic
	 *  in reverse chronological order.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public Collection getAllTopicVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
		Vector all = new Vector();
		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName, descending);
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			TopicVersion version = initTopicVersion(files[i]);
			all.add(version);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getAllWikiFileTopicNames(String virtualWiki) throws Exception {
		Vector all = new Vector();
		File[] files = retrieveWikiFileFiles(virtualWiki);
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			String topicName = files[i].getName();
			// strip extension
			int pos = topicName.lastIndexOf(EXT);
			if (pos != -1) topicName = topicName.substring(0, pos);
			// decode
			topicName = Utilities.decodeFromURL(topicName);
			all.add(topicName);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
		Vector all = new Vector();
		File[] files = retrieveWikiFileVersionFiles(virtualWiki, topicName, descending);
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			WikiFileVersion version = initWikiFileVersion(files[i]);
			all.add(version);
		}
		return all;
	}

	/**
	 *
	 */
	public Collection getAllWikiUserLogins() throws Exception {
		Vector all = new Vector();
		File[] files = retrieveWikiUserFiles();
		if (files == null) return all;
		for (int i = 0; i < files.length; i++) {
			String login = files[i].getName();
			// strip extension
			int pos = login.lastIndexOf(EXT);
			if (pos != -1) login = login.substring(0, pos);
			// decode
			login = Utilities.decodeFromURL(login);
			all.add(login);
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
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.hasText(virtualWiki)) {
			// this is questionable, but the virtual wiki list does it
			logger.info("Attempting to write file with empty virtual wiki for file " + fileName);
			virtualWiki = "";
		}
		buffer.append(fileBase(virtualWiki));
		buffer.append(File.separator);
		if (dir1 != null) {
			buffer.append(Utilities.encodeForFilename(dir1));
			buffer.append(File.separator);
		}
		if (dir2 != null) {
			buffer.append(Utilities.encodeForFilename(dir2));
			buffer.append(File.separator);
		}
		File directory = new File(buffer.toString());
		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (fileName != null) {
			buffer.append(Utilities.encodeForFilename(fileName));
		}
		return new File(buffer.toString());
	}

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception {
		Vector results = new Vector();
		File[] files = retrieveVirtualWikiFiles();
		if (files == null) return results;
		for (int i = 0; i < files.length; i++) {
			VirtualWiki virtualWiki = initVirtualWiki(files[i]);
			results.add(virtualWiki);
		}
		return results;
	}

	/**
	 *
	 */
	private Topic initTopic(File file) {
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
					topic.setName(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_TOPIC_ID)) {
					topic.setTopicId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_TOPIC_VIRTUAL_WIKI)) {
					topic.setVirtualWiki(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_TOPIC_TEXT)) {
					topic.setTopicContent(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_TOPIC_ADMIN_ONLY)) {
					topic.setAdminOnly(new Boolean(XMLUtil.getTextContent(rootChild)).booleanValue());
				} else if (childName.equals(XML_TOPIC_READ_ONLY)) {
					topic.setReadOnly(new Boolean(XMLUtil.getTextContent(rootChild)).booleanValue());
				} else if (childName.equals(XML_TOPIC_DELETE_DATE)) {
					try {
						topic.setDeleteDate(Timestamp.valueOf(XMLUtil.getTextContent(rootChild)));
					} catch (Exception e) {
						if (XMLUtil.getTextContent(rootChild) != null && XMLUtil.getTextContent(rootChild).equals("1")) {
							// FIXME - this can be removed once the ability to upgrade from 0.3.0 is removed
							topic.setDeleteDate(new Timestamp(System.currentTimeMillis()));
						}
					}
				} else if (childName.equals(XML_TOPIC_REDIRECT_TO)) {
					topic.setRedirectTo(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_TOPIC_TYPE)) {
					topic.setTopicType(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				}
			}
			return topic;
		} catch (Exception e) {
			logger.severe("Failure while initializing topic for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	private TopicVersion initTopicVersion(File file) {
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
					topicVersion.setTopicVersionId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_TOPIC_VERSION_TOPIC_ID)) {
					topicVersion.setTopicId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_COMMENT)) {
					topicVersion.setEditComment(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_DATE)) {
					topicVersion.setEditDate(Timestamp.valueOf(XMLUtil.getTextContent(rootChild)));
				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_TYPE)) {
					topicVersion.setEditType(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_TOPIC_VERSION_TEXT)) {
					topicVersion.setVersionContent(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_TOPIC_VERSION_AUTHOR)) {
					NodeList authorChildren = rootChild.getChildNodes();
					for (int j=0; j < authorChildren.getLength(); j++) {
						Node authorChild = authorChildren.item(j);
						if (authorChild.getNodeName().equals(XML_TOPIC_VERSION_AUTHOR_ID)) {
							topicVersion.setAuthorId(new Integer(XMLUtil.getTextContent(authorChild)));
						} else if (authorChild.getNodeName().equals(XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS)) {
							topicVersion.setAuthorIpAddress(XMLUtil.getTextContent(authorChild));
						}
					}
				} else if (childName.equals(XML_TOPIC_VERSION_PREVIOUS_TOPIC_VERSION_ID)) {
					topicVersion.setPreviousTopicVersionId(new Integer(XMLUtil.getTextContent(rootChild)));
				}
			}
			return topicVersion;
		} catch (Exception e) {
			logger.severe("Failure while initializing topic version for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	private VirtualWiki initVirtualWiki(File file) {
		if (!file.exists()) return null;
		try {
			VirtualWiki virtualWiki = new VirtualWiki();
			Document document = XMLUtil.parseXML(file, false);
			// get root node
			Node rootNode = document.getElementsByTagName(XML_VIRTUAL_WIKI_ROOT).item(0);
			NodeList rootChildren = rootNode.getChildNodes();
			Node rootChild = null;
			String childName = null;
			for (int i=0; i < rootChildren.getLength(); i++) {
				rootChild = rootChildren.item(i);
				childName = rootChild.getNodeName();
				if (childName.equals(XML_VIRTUAL_WIKI_ID)) {
					virtualWiki.setVirtualWikiId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_VIRTUAL_WIKI_NAME)) {
					virtualWiki.setName(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_VIRTUAL_WIKI_DEFAULT_TOPIC_NAME)) {
					virtualWiki.setDefaultTopicName(XMLUtil.getTextContent(rootChild));
				}
			}
			return virtualWiki;
		} catch (Exception e) {
			logger.severe("Failure while initializing virtual wiki for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiFile initWikiFile(File file) {
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
					wikiFile.setFileName(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_ID)) {
					wikiFile.setFileId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_WIKI_FILE_TOPIC_ID)) {
					wikiFile.setTopicId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_WIKI_FILE_VIRTUAL_WIKI)) {
					wikiFile.setVirtualWiki(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_URL)) {
					wikiFile.setUrl(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_ADMIN_ONLY)) {
					wikiFile.setAdminOnly(new Boolean(XMLUtil.getTextContent(rootChild)).booleanValue());
				} else if (childName.equals(XML_WIKI_FILE_READ_ONLY)) {
					wikiFile.setReadOnly(new Boolean(XMLUtil.getTextContent(rootChild)).booleanValue());
				} else if (childName.equals(XML_WIKI_FILE_DELETE_DATE)) {
					wikiFile.setDeleteDate(Timestamp.valueOf(XMLUtil.getTextContent(rootChild)));
				} else if (childName.equals(XML_WIKI_FILE_MIME_TYPE)) {
					wikiFile.setMimeType(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_SIZE)) {
					wikiFile.setFileSize(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				}
			}
			return wikiFile;
		} catch (Exception e) {
			logger.severe("Failure while initializing wiki file for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiFileVersion initWikiFileVersion(File file) {
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
					wikiFileVersion.setFileVersionId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_FILE_ID)) {
					wikiFileVersion.setFileId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_WIKI_FILE_VERSION_UPLOAD_COMMENT)) {
					wikiFileVersion.setUploadComment(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_VERSION_UPLOAD_DATE)) {
					wikiFileVersion.setUploadDate(Timestamp.valueOf(XMLUtil.getTextContent(rootChild)));
				} else if (childName.equals(XML_WIKI_FILE_VERSION_MIME_TYPE)) {
					wikiFileVersion.setMimeType(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_VERSION_URL)) {
					wikiFileVersion.setUrl(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_FILE_VERSION_AUTHOR)) {
					NodeList authorChildren = rootChild.getChildNodes();
					for (int j=0; j < authorChildren.getLength(); j++) {
						Node authorChild = authorChildren.item(j);
						if (authorChild.getNodeName().equals(XML_WIKI_FILE_VERSION_AUTHOR_ID)) {
							wikiFileVersion.setAuthorId(new Integer(XMLUtil.getTextContent(authorChild)));
						} else if (authorChild.getNodeName().equals(XML_WIKI_FILE_VERSION_AUTHOR_IP_ADDRESS)) {
							wikiFileVersion.setAuthorIpAddress(XMLUtil.getTextContent(authorChild));
						}
					}
				} else if (childName.equals(XML_WIKI_FILE_VERSION_SIZE)) {
					wikiFileVersion.setFileSize(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				}
			}
			return wikiFileVersion;
		} catch (Exception e) {
			logger.severe("Failure while initializing topic version for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	private WikiUser initWikiUser(File file) {
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
					user.setUserId(new Integer(XMLUtil.getTextContent(rootChild)).intValue());
				} else if (childName.equals(XML_WIKI_USER_ADMIN)) {
					user.setAdmin(new Boolean(XMLUtil.getTextContent(rootChild)).booleanValue());
				} else if (childName.equals(XML_WIKI_USER_CREATE_DATE)) {
					user.setCreateDate(Timestamp.valueOf(XMLUtil.getTextContent(rootChild)));
				} else if (childName.equals(XML_WIKI_USER_CREATE_IP_ADDRESS)) {
					user.setCreateIpAddress(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_DISPLAY_NAME)) {
					user.setDisplayName(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_EMAIL)) {
					user.setEmail(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_ENCODED_PASSWORD)) {
					user.setEncodedPassword(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_FIRST_NAME)) {
					user.setFirstName(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_LAST_LOGIN_DATE)) {
					user.setLastLoginDate(Timestamp.valueOf(XMLUtil.getTextContent(rootChild)));
				} else if (childName.equals(XML_WIKI_USER_LAST_LOGIN_IP_ADDRESS)) {
					user.setLastLoginIpAddress(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_LAST_NAME)) {
					user.setLastName(XMLUtil.getTextContent(rootChild));
				} else if (childName.equals(XML_WIKI_USER_LOGIN)) {
					user.setLogin(XMLUtil.getTextContent(rootChild));
				}
			}
			return user;
		} catch (Exception e) {
			logger.severe("Failure while initializing user for file " + file.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
		return lookupTopic(virtualWiki, topicName, false);
	}

	/**
	 *
	 */
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK) throws Exception {
		String filename = topicFilename(topicName);
		File file = getPathFor(virtualWiki, FileHandler.TOPIC_DIR, filename);
		Topic topic = initTopic(file);
		if (topic == null && deleteOK) {
			file = getPathFor(virtualWiki, FileHandler.DELETE_DIR, filename);
			topic = initTopic(file);
		}
		return topic;
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
	public WikiUser lookupWikiUser(String login) throws Exception {
		if (login == null) return null;
		String filename = wikiUserFilename(login);
		File file = getPathFor(null, WIKI_USER_DIR, filename);
		return initWikiUser(file);
	}

	/**
	 *
	 */
	private static int nextFileWrite(int nextId, File file) throws Exception {
		FileUtils.writeStringToFile(file, new Integer(nextId).toString(), "UTF-8");
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
				String integer = FileUtils.readFileToString(topicIdFile, "UTF-8");
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
				String integer = FileUtils.readFileToString(topicVersionIdFile, "UTF-8");
				NEXT_TOPIC_VERSION_ID = new Integer(integer).intValue();
			}
		}
		NEXT_TOPIC_VERSION_ID++;
		return nextFileWrite(NEXT_TOPIC_VERSION_ID, topicVersionIdFile);
	}

	/**
	 *
	 */
	private static int nextVirtualWikiId() throws Exception {
		File virtualWikiIdFile = getPathFor(null, null, NEXT_VIRTUAL_WIKI_ID_FILE);
		if (NEXT_VIRTUAL_WIKI_ID < 1) {
			// read value from file
			if (virtualWikiIdFile.exists()) {
				String integer = FileUtils.readFileToString(virtualWikiIdFile, "UTF-8");
				NEXT_VIRTUAL_WIKI_ID = new Integer(integer).intValue();
			}
		}
		NEXT_VIRTUAL_WIKI_ID++;
		return nextFileWrite(NEXT_VIRTUAL_WIKI_ID, virtualWikiIdFile);
	}

	/**
	 *
	 */
	private static int nextWikiFileId() throws Exception {
		File wikiFileIdFile = getPathFor(null, null, NEXT_WIKI_FILE_ID_FILE);
		if (NEXT_WIKI_FILE_ID < 1) {
			// read value from file
			if (wikiFileIdFile.exists()) {
				String integer = FileUtils.readFileToString(wikiFileIdFile, "UTF-8");
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
				String integer = FileUtils.readFileToString(wikiFileVersionIdFile, "UTF-8");
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
				String integer = FileUtils.readFileToString(userIdFile, "UTF-8");
				NEXT_WIKI_USER_ID = new Integer(integer).intValue();
			}
		}
		NEXT_WIKI_USER_ID++;
		return nextFileWrite(NEXT_WIKI_USER_ID, userIdFile);
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	public void purgeData() throws Exception {
		String rootDir = Environment.getValue(Environment.PROP_BASE_FILE_DIR);
		File rootDirFile = new File(rootDir);
		// BOOM!  Everything gone...
		FileUtils.cleanDirectory(rootDirFile);
	}

	/**
	 *
	 */
	private File[] retrieveTopicFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.TOPIC_DIR);
		File[] files = file.listFiles();
		Arrays.sort(files);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveTopicVersionFiles(String virtualWiki, String topicName, boolean descending) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.TOPIC_VERSION_DIR, topicName);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new WikiDescendingFileComparator();
		if (!descending) comparator = new WikiAscendingFileComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveVirtualWikiFiles() throws Exception {
		File file = FileHandler.getPathFor(null, null, FileHandler.VIRTUAL_WIKI_DIR);
		File[] files = file.listFiles();
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveWikiFileFiles(String virtualWiki) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.WIKI_FILE_DIR);
		File[] files = file.listFiles();
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveWikiFileVersionFiles(String virtualWiki, String topicName, boolean descending) throws Exception {
		File file = FileHandler.getPathFor(virtualWiki, FileHandler.WIKI_FILE_VERSION_DIR, topicName);
		File[] files = file.listFiles();
		if (files == null) return null;
		Comparator comparator = new WikiDescendingFileComparator();
		if (!descending) comparator = new WikiAscendingFileComparator();
		Arrays.sort(files, comparator);
		return files;
	}

	/**
	 *
	 */
	private File[] retrieveWikiUserFiles() throws Exception {
		File file = FileHandler.getPathFor(null, null, FileHandler.WIKI_USER_DIR);
		File[] files = file.listFiles();
		return files;
	}

	/**
	 *
	 */
	private void saveTopic(Topic topic) throws Exception {
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
		content.append(XMLUtil.buildTag(XML_TOPIC_READ_ONLY, topic.getReadOnly()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_DELETE_DATE, topic.getDeleteDate()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_REDIRECT_TO, topic.getRedirectTo(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_TOPIC_TYPE, topic.getTopicType()));
		content.append("\n");
		content.append("</").append(XML_TOPIC_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = topicFilename(topic.getName());
		File file = FileHandler.getPathFor(topic.getVirtualWiki(), FileHandler.TOPIC_DIR, filename);
		if (topic.getDeleteDate() != null) {
			file = FileHandler.getPathFor(topic.getVirtualWiki(), FileHandler.DELETE_DIR, filename);
		}
		FileUtils.writeStringToFile(file, content.toString(), "UTF-8");
	}

	/**
	 *
	 */
	private void saveTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
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
		FileUtils.writeStringToFile(versionFile, content.toString(), "UTF-8");
	}

	/**
	 *
	 */
	private void saveVirtualWiki(VirtualWiki virtualWiki) throws Exception {
		if (virtualWiki.getVirtualWikiId() > NEXT_VIRTUAL_WIKI_ID) {
			NEXT_VIRTUAL_WIKI_ID = virtualWiki.getVirtualWikiId();
			nextFileWrite(NEXT_VIRTUAL_WIKI_ID, getPathFor(null, null, NEXT_VIRTUAL_WIKI_ID_FILE));
		}
		StringBuffer content = new StringBuffer();
		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
		content.append("\n");
		content.append("<").append(XML_VIRTUAL_WIKI_ROOT).append(">");
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_VIRTUAL_WIKI_ID, virtualWiki.getVirtualWikiId()));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_VIRTUAL_WIKI_NAME, virtualWiki.getName(), true));
		content.append("\n");
		content.append(XMLUtil.buildTag(XML_VIRTUAL_WIKI_DEFAULT_TOPIC_NAME, virtualWiki.getDefaultTopicName(), true));
		content.append("\n");
		content.append("</").append(XML_VIRTUAL_WIKI_ROOT).append(">");
		content.append("\n");
		content.append("</mediawiki>");
		String filename = virtualWikiFilename(virtualWiki.getName());
		File file = FileHandler.getPathFor(null, FileHandler.VIRTUAL_WIKI_DIR, filename);
		FileUtils.writeStringToFile(file, content.toString(), "UTF-8");
	}

	/**
	 *
	 */
	private void saveWikiFile(String topicName, WikiFile wikiFile) throws Exception {
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
		content.append(XMLUtil.buildTag(XML_WIKI_FILE_DELETE_DATE, wikiFile.getDeleteDate()));
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
		FileUtils.writeStringToFile(file, content.toString(), "UTF-8");
	}

	/**
	 *
	 */
	private void saveWikiFileVersion(String virtualWiki, String topicName, WikiFileVersion wikiFileVersion) throws Exception {
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
		FileUtils.writeStringToFile(versionFile, content.toString(), "UTF-8");
	}

	/**
	 *
	 */
	private void saveWikiUser(WikiUser user) throws Exception {
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
		FileUtils.writeStringToFile(userFile, content.toString(), "UTF-8");
		File userIdHashFile = getPathFor(null, null, WIKI_USER_ID_HASH_FILE);
		if (WIKI_USER_ID_HASH == null) {
			WIKI_USER_ID_HASH = new Properties();
			if (userIdHashFile.exists()) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(userIdHashFile);
					WIKI_USER_ID_HASH.load(fis);
				} finally {
					if (fis != null) fis.close();
				}
			}
		}
		WIKI_USER_ID_HASH.setProperty(new Integer(user.getUserId()).toString(), user.getLogin());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(userIdHashFile);
			WIKI_USER_ID_HASH.store(fos, null);
		} finally {
			if (fos != null) fos.close();
		}
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
	protected static String virtualWikiFilename(String virtualWikiName) {
		return virtualWikiName + EXT;
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
	 * Return a list, sorted so that the largest values are first.
	 */
	class WikiDescendingFileComparator implements Comparator {

		/**
		 * Returns a positive value if first value should come after the
		 * second value.  Returns a negative value if first value should
		 * come before the second value.  Returns zero if the values
		 * are equal.
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

	/**
	 * Return a list, sorted so that the smallest values are first.
	 */
	class WikiAscendingFileComparator implements Comparator {

		/**
		 * Returns a negative value if first value should come after the
		 * second value.  Returns a positive value if first value should
		 * come before the second value.  Returns zero if the values
		 * are equal.
		 */
		public int compare(Object first, Object second) {
			String one = ((File)first).getName();
			String two = ((File)second).getName();
			int pos = one.lastIndexOf(EXT);
			int arg1 = new Integer(one.substring(0, pos)).intValue();
			pos = two.lastIndexOf(EXT);
			int arg2 = new Integer(two.substring(0, pos)).intValue();
			return arg1 - arg2;
		}
	}
}
