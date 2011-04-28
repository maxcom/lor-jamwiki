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
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.TestFileUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class MigrationUtilTest extends JAMWikiUnitTest {

	private static final String FILE_TEST_TWO_TOPICS_WITH_HISTORY = "mediawiki-export-two-topics-with-history.xml";
	private static final String FILE_ONE_TOPIC_WITH_UNSORTED_HISTORY = "mediawiki-export-one-topic-with-unsorted-history.xml";
	private static final String FILE_TOPIC_NAME_WITH_QUESTION_MARK = "mediawiki-export-topic-name-with-question-mark.xml";
	private static final String FILE_NAMESPACE_TEST = "mediawiki-export-namespace-test.xml";
	private static final String TEST_FILES_DIR = "data/files/";
	private static final String TOPIC_NAME1 = "Test Page 1";
	private static final String TOPIC_NAME2 = "Template comments:Test Template";
	private static final String TOPIC_NAME3 = "Test Page 2";
	private static final String TOPIC_NAME4 = "Who am i";
	private static final String TOPIC_NAME5 = "Namespace Test";
	private static final String VIRTUAL_WIKI_EN = "en";
	@Rule
	public TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

	private static boolean INITIALIZED = false;

	/**
	 *
	 */
	@Before
	public void setup() throws Exception {
		super.setup();
		if (!INITIALIZED) {
			this.setupTopic(null, "Example1");
			this.setupTopic(null, "Example2");
			INITIALIZED = true;
		}
	}

	/**
	 *
	 */
	@Test
	public void testExportNonExistentTopic() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> topicNames = new ArrayList<String>();
		topicNames.add("Bogus Topic Name");
		boolean excludeHistory = false;
		File file = TEMP_FOLDER.newFile("export.xml");
		try {
			MigrationUtil.exportToFile(file, virtualWiki, topicNames, excludeHistory);
		} catch (MigrationException e) {
			if (file.exists()) {
				// should have been deleted
				fail("Partial export file not deleted");
			}
			return;
		}
		fail("Expected MigrationException to be thrown");
	}

	/**
	 *
	 */
	@Test
	public void testTwoTopics() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> topicNames = new ArrayList<String>();
		topicNames.add("Example1");
		topicNames.add("Example2");
		boolean excludeHistory = false;
		File file = TEMP_FOLDER.newFile("export.xml");
		try {
			MigrationUtil.exportToFile(file, virtualWiki, topicNames, excludeHistory);
		} catch (MigrationException e) {
			fail("Failure during export" + e);
		}
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileWithTwoTopics() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_TEST_TWO_TOPICS_WITH_HISTORY);
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
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_ONE_TOPIC_WITH_UNSORTED_HISTORY);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME3, false, null);
		// validate that the current topic content is correct
		assertEquals("Incorrect topic ordering: " + topic.getTopicId() + " / " + topic.getCurrentVersionId(), "Newest Revision", topic.getTopicContent());
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileTopicNameWithQuestionMark() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_TOPIC_NAME_WITH_QUESTION_MARK);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME4, false, null);
		assertNotNull("Topic with question mark in name imported correctly", topic);
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileNamespaceTest() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_NAMESPACE_TEST);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME5, false, null);
		assertNotNull("Namespace test topic imported correctly", topic);
		// verify that Mediawiki namespaces were correctly converted to JAMWiki namespaces
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Talk:Test - [[Comments:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("User:Test - [[User:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("User talk:Test - [[User comments:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Wikipedia:Test - [[Project:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Wikipedia talk:Test - [[Project comments:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("File:Test - [[Image:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("File talk:Test - [[Image comments:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Template:Test - [[Template:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Template talk:Test - [[Template comments:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Category:Test - [[Category:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Category talk:Test - [[Category comments:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Custom:Test - [[Custom:Test]]") != -1));
		assertTrue("Namespace converted", (topic.getTopicContent().indexOf("Custom talk:Test - [[Custom talk:Test]]") != -1));
	}

	/**
	 * Utility method for importing test files.
	 */
	private List<String> importTestFile(String filename) throws Throwable {
		File file = TestFileUtil.retrieveFile(TEST_FILES_DIR, filename);
		Locale locale = new Locale("en", "US");
		String virtualWiki = VIRTUAL_WIKI_EN;
		String authorDisplay = "127.0.0.1";
		WikiUser user = null;
		return MigrationUtil.importFromFile(file, virtualWiki, user, authorDisplay, locale);
	}
}
