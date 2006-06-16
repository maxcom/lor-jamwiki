package org.jmwiki.persistency.file;

import junit.framework.TestCase;

/**
 Java MediaWiki - WikiWikiWeb clone
 Copyright (C) 2001-2002 Gareth Cronin

 This program is free software; you can redistribute it and/or modify
 it under the terms of the latest version of the GNU Lesser General
 Public License as published by the Free Software Foundation;

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program (gpl.txt); if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

public class FileHandlerTest extends TestCase {

	// file/path separator for the platform
	protected static String sep = System.getProperty("file.separator");
	FileHandler handler;

	/**
	 *
	 */
	public FileHandlerTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testLocking() throws Exception {
		String topicName = "NewTopic";
		FileHandler handler = new FileHandler();
		String key1 = "aKey";
		String key2 = "bKey";
		handler.lockTopic("", topicName, key1);
		assertTrue(handler.lockTopic("", topicName, key1));
		assertTrue(!handler.lockTopic("", topicName, key2));
		handler.unlockTopic("", topicName);
		handler.lockTopic("", topicName, key2);
		assertTrue(handler.lockTopic("", topicName, key2));
		assertTrue(!handler.lockTopic("", topicName, key1));
	}
}
