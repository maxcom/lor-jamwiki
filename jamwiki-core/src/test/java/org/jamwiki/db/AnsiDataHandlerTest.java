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
package org.jamwiki.db;

import java.io.IOException;
import org.jamwiki.DataAccessException;
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.model.Topic;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for data handler functionality.
 */
public class AnsiDataHandlerTest extends JAMWikiUnitTest {

	private static boolean INITIALIZED = false;

	/**
	 *
	 */
	@Test
	public void testTopicLookup1() throws DataAccessException {
		Topic topic = WikiBase.getDataHandler().lookupTopic("en", WikiBase.SPECIAL_PAGE_STYLESHEET, false);
		assertEquals("Incorrect topic name", topic.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET);
	}

	/**
	 *
	 */
	@Test
	public void testWriteAndTopicLookup1() throws DataAccessException, IOException, WikiException {
		String FILE_NAME = "Help_-_Test";
		String TOPIC_NAME = "Help:Test";
		this.setupTopic(null, FILE_NAME);
		Topic topic = WikiBase.getDataHandler().lookupTopic("en", TOPIC_NAME, false);
		assertEquals("Incorrect topic name (case-sensitive)", topic.getName(), TOPIC_NAME);
		topic = WikiBase.getDataHandler().lookupTopic("en", "HELP:Test", false);
		assertEquals("Incorrect topic name (case-insensitive)", topic.getName(), TOPIC_NAME);
	}
}
