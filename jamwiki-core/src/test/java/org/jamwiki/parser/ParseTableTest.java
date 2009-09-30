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

/**
 *
 */
public class ParseTableTest extends TestParser {

	/**
	 *
	 */
	public ParseTableTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testBasicTables1() throws Exception {
		executeParserTest("Table1");
	}

	/**
	 *
	 */
	public void testBasicTables2() throws Exception {
		executeParserTest("Table2");
	}

	/**
	 *
	 */
	public void testBasicTables3() throws Exception {
		executeParserTest("Table3");
	}

	/**
	 *
	 */
	public void testBasicTables4() throws Exception {
		executeParserTest("Table4");
	}

	/**
	 *
	 */
	public void testBasicTables5() throws Exception {
		executeParserTest("Table5");
	}

	/**
	 *
	 */
	public void testBasicTables6() throws Exception {
		executeParserTest("Table6");
	}

	/**
	 *
	 */
	public void testBasicTables7() throws Exception {
		executeParserTest("Table7");
	}

	/**
	 *
	 */
	public void testNestedTables1() throws Exception {
// FIXME
//		executeParserTest("NestedTable1");
	}

	/**
	 *
	 */
	public void testNestedTables2() throws Exception {
// FIXME
//		executeParserTest("NestedTable2");
	}
}