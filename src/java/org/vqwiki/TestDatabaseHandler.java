/*
Very Quick Wiki - WikiWikiWeb clone
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
package org.vqwiki;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.vqwiki.persistency.db.DatabaseHandler;

public class TestDatabaseHandler extends TestCase {

	protected static Logger logger = Logger.getLogger(TestDatabaseHandler.class);
	final static String MYTOPIC1 = "MyTopic";
	final static String CONTENTS1 = "This is SomeContents";

	/**
	 *
	 */
	public TestDatabaseHandler(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void setUp() throws Exception {
		DatabaseHandler handler = new DatabaseHandler();
		handler.executeSQL("DROP TABLE Topic");
		handler.executeSQL("DROP TABLE TopicLock");
		handler.executeSQL("DROP TABLE TopicChange");
		handler.executeSQL("DROP TABLE TopicVersion");
	}

	/**
	 *
	 */
	public void testStartup() throws Exception {
		DatabaseHandler handler = new DatabaseHandler();
	}

	/**
	 *
	 */
	public void testTopic() throws Exception {
		DatabaseHandler handler = new DatabaseHandler();
		assertTrue("doesn't exist", !handler.exists("", MYTOPIC1));
		handler.write("", CONTENTS1, true, MYTOPIC1);
		assertTrue("does exist", handler.exists("", MYTOPIC1));
		assertEquals("contents", CONTENTS1, handler.read("", MYTOPIC1));
	}

	/**
	 *
	 */
	public void testLocking() throws Exception {
		DatabaseHandler handler = new DatabaseHandler();
		handler.write("", CONTENTS1, true, MYTOPIC1);
		assertTrue("lock", handler.lockTopic("", MYTOPIC1, "x"));
		handler.unlockTopic("", MYTOPIC1);
		assertTrue("lock", handler.lockTopic("", MYTOPIC1, "x"));
		assertTrue("can't lock", !handler.lockTopic("", MYTOPIC1, "x"));
	}
}
