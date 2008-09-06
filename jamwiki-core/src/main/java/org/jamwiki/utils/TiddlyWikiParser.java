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
package org.jamwiki.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;

/**
 * This class parse a TiddlyWiki file and imports it to JamWiki
 * @author Michael Greifeneder mikegr@gmx.net
 */
public class TiddlyWikiParser {

	private static final Logger logger = Logger.getLogger(TiddlyWikiParser.class.getName());

	private static final String DIV_START = "<div tiddler";
	private static final String DIV_END = "</div>";
	private static final String TIDLLER = "tiddler";
	//private static final String MODIFIER = "modifier";
	private static final String MODIFIED = "modified";
	//private static final String TAGS = "tags";
	private static final SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmm");
	private StringBuffer messages = new StringBuffer();
	private String virtualWiki;
	private WikiUser user;
	private String authorIpAddress;

	/**
	 * Facade for WikiBase. Used for enable unit testing.
	 * @author Michael Greifeneder mikegr@gmx.net
	 */
	public interface WikiBaseFascade {
		public void writeTopic(Topic topic, TopicVersion topicVersion, LinkedHashMap categories, Vector links, boolean userVisible, Object transactionObject) throws Exception;
	}

	/**
	 * Defaul WikiBaseFascade for production.
	 */
	private WikiBaseFascade wikiBase = new WikiBaseFascade() {
		public void writeTopic(Topic topic, TopicVersion topicVersion, LinkedHashMap categories, Vector links, boolean userVisible, Object transactionObject) throws Exception {
			WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null, true, null);
		}
	};

	private TiddlyWiki2MediaWikiTranslator translator = new TiddlyWiki2MediaWikiTranslator();

	/**
	 * Main constructor
	 * @param virtualWiki virtualWiki
	 * @param user user who is currently logged in
	 * @param authorIpAddress IP address of uploading user
	 */
	public TiddlyWikiParser(String virtualWiki, WikiUser user, String authorIpAddress) {
		this.virtualWiki = virtualWiki;
		this.user = user;
		this.authorIpAddress = authorIpAddress;
	}

	/**
	 * Use this contructor for test cases
	 * @param virtualWiki Name of VirtualWiki
	 * @param user User who is logged in.
	 * @param authorIpAddress IP address of uploading user.
	 * @param wikiBase Overwrites default WikiBaseFascade
	 */
	public TiddlyWikiParser(String virtualWiki, WikiUser user, String authorIpAddress, WikiBaseFascade wikiBase) {
		this(virtualWiki, user, authorIpAddress);
		this.wikiBase = wikiBase;
	}

	/** Parses file and returns default topic.
	 * @param file TiddlyWiki file
	 * @return main topic for this TiddlyWiki
	 */
	public String parse(File file) throws Exception {
		Reader r = new FileReader(file);
		BufferedReader br = new BufferedReader(r);
		return parse(br);
	}

	/** Parses TiddlyWiki content and returns default topic.
	 * @param br TiddlyWiki file content
	 * @return main topic for this TiddlyWiki
	 */
	public String parse(BufferedReader br) throws Exception {
		String line = br.readLine();
		boolean inTiddler = false;
		int start = 0;
		int end = 0;
		StringBuffer content = new StringBuffer();
		while (line != null) {
			if (inTiddler) {
				end = line.indexOf(DIV_END);
				if (end != -1) {
					inTiddler = false;
					content.append(line.substring(0, end));
					proecessContent(content.toString());
					content.setLength(0);
					line = line.substring(end);
				} else {
					content.append(line);
					line = br.readLine();
				}
			} else {
				start = line.indexOf(DIV_START);
				if (start != -1 && (line.indexOf("<div tiddler=\"%0\"") == -1)) {
					inTiddler = true;
					logger.fine("Ignoring:\n" + line.substring(0, start));
					line = line.substring(start);
				} else {
					logger.fine("Div tiddler not found in: \n" + line);
					line = br.readLine();
				}
			}
		}
		return "DefaultTiddlers";
	}

	private void proecessContent(String content) throws Exception {
		logger.fine("Content: " + content);
		String name = findName(content, TIDLLER);
		if (name == null|| "%0".equals(user)) {
			return;
		}
		/* no need for user
		String user = findName(content, MODIFIER);
		if (user == null ) {
			messages.append("WARN: ")
			return;
		}
		*/
		Date lastMod = null;
		try {
			lastMod = formater.parse(findName(content, MODIFIED));
		} catch (Exception e) {
			messages.append("WARNING: corrupt line: " + content);
		}
		if (lastMod == null) {
			return;
		}
		/* ignoring tags
		String tags = findName(content, TAGS);
		if (tags == null) {
			return;
		}
		*/
		int idx = content.indexOf(">");
		if (idx == -1) {
			logger.warning("No closing of tag");
			messages.append("WARNING: corrupt line: " + content);
			return;
		}
		String wikicode = content.substring(idx +1);
		wikicode = translator.translate(wikicode);
		messages.append("Adding topic " + name + "\n");
		saveTopic(name, lastMod, wikicode);
		logger.fine("Code:" + wikicode);
	}

	private void saveTopic(String name, Date lastMod, String content) throws Exception {
		Topic topic = new Topic();
		topic.setName(name);
		topic.setVirtualWiki(virtualWiki);
		topic.setTopicContent(content);
		int charactersChanged = StringUtils.length(content);
		TopicVersion topicVersion = new TopicVersion(user, authorIpAddress, "imported", content, charactersChanged);
		topicVersion.setEditDate(new Timestamp(lastMod.getTime()));
		// manage mapping bitween MediaWiki and JAMWiki namespaces
		topic.setTopicType(Topic.TYPE_ARTICLE);
		// Store topic in database
		wikiBase.writeTopic(topic, topicVersion, null, null, true, null);
	}

	private String findName(String content, String name) {
		int startIdx = content.indexOf(name);
		if (startIdx == -1) {
			logger.warning("no tiddler name found");
			return null;
		}
		startIdx = content.indexOf("\"", startIdx);
		int endIdx = content.indexOf("\"", startIdx+1);
		String value = content.substring(startIdx+1, endIdx);
		logger.fine(name + ":" + value);
		return value;
	}

	public String getOutput() {
		return messages.toString();
	}
}
