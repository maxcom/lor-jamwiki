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
package org.jamwiki.parser;

import junit.framework.TestCase;

/**
 *
 */
public class TableOfContentsTest extends TestParser {

	/**
	 *
	 */
	public TableOfContentsTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testDuplicateHeadings1() throws Exception {
		executeParserTest("TableOfContents1");
	}

	/**
	 *
	 */
	public void testDuplicateHeadings2() throws Exception {
		executeParserTest("TableOfContents2");
	}

	/**
	 *
	 */
	public void testMultiLevelHeadings1() throws Exception {
		executeParserTest("TableOfContents3");
	}

	/**
	 *
	 */
	public void testMultiLevelHeadings2() throws Exception {
		executeParserTest("TableOfContents4");
	}
}