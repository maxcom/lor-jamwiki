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
package org.jamwiki.migrate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jamwiki.TestFileUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class MigrationUtilTest {

	private static final String FILE_TEST_TWO_TOPICS_WITH_HISTORY = "mediawiki-export-two-topics-with-history.xml";
	private static final String FILE_ONE_TOPIC_WITH_UNSORTED_HISTORY = "mediawiki-export-one-topic-with-unsorted-history.xml";
	private static final String TEST_FILES_DIR = "data/files/";
	private static final String TOPIC_NAME1 = "Test Page 1";
	private static final String TOPIC_NAME2 = "Template comments:Test Template";
	private static final String TOPIC_NAME3 = "Test Page 2";
	private static final String VIRTUAL_WIKI_EN = "en";

	/**
	 *
	 */
	@Test
	public void testImportFromFileWithTwoTopics() throws Throwable {
		File file = TestFileUtil.retrieveFile(TEST_FILES_DIR, FILE_TEST_TWO_TOPICS_WITH_HISTORY);
		Locale locale = new Locale("en", "US");
		String virtualWiki = VIRTUAL_WIKI_EN;
		String authorDisplay = "127.0.0.1";
		WikiUser user = null;
		List<WikiMessage> errors = new ArrayList<WikiMessage>();
		List<String> results = MigrationUtil.importFromFile(file, virtualWiki, user, authorDisplay, locale, errors);
		// validate that the first topic parsed
		assertTrue("Parsed topic '" + TOPIC_NAME1 + "'", results.contains(TOPIC_NAME1));
		Topic topic1 = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME1, false, null);
		// validate that the parsed topic correctly set topic values
		assertEquals("Topic name '" + TOPIC_NAME1 + "' set correctly", TOPIC_NAME1, topic1.getName());
		assertTrue("Topic content set correctly", topic1.getTopicContent().indexOf("Link to user page: [[User:Test User]]") != -1);
		// validate that namespaces were converted from Mediawiki to JAMWiki correctly
		assertTrue("Topic content namespaces updated correctly", topic1.getTopicContent().indexOf("Link to user talk page: [[User comments: Test User]]") != -1);
		// validate that the second topic parsed
		assertTrue("Parsed topic '" + TOPIC_NAME2 + "'", results.contains(TOPIC_NAME2));
		Topic topic2 = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME2, false, null);
		// validate that the parsed topic correctly set topic values
		assertEquals("Topic name '" + TOPIC_NAME2 + "' set correctly", TOPIC_NAME2, topic2.getName());
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileWithUnsortedHistory() throws Throwable {
		File file = TestFileUtil.retrieveFile(TEST_FILES_DIR, FILE_ONE_TOPIC_WITH_UNSORTED_HISTORY);
		Locale locale = new Locale("en", "US");
		String virtualWiki = VIRTUAL_WIKI_EN;
		String authorDisplay = "127.0.0.1";
		WikiUser user = null;
		List<WikiMessage> errors = new ArrayList<WikiMessage>();
		List<String> results = MigrationUtil.importFromFile(file, virtualWiki, user, authorDisplay, locale, errors);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME3, false, null);
		// validate that the current topic content is correct
		assertEquals("Topic content set correctly", "Newest Revision", topic.getTopicContent());
	}
}
