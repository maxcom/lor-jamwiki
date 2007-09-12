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
package org.jamwiki.test.parser;

import junit.framework.TestCase;
import org.jamwiki.parser.TableOfContents;

/**
 *
 */
public class TableOfContentsTest extends TestCase {

	/**
	 *
	 */
	public TableOfContentsTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testDuplicateHeadings1() {
		String input = "";
		input = "==test==\n"
		      + "==test==\n"
		      + "==test==\n"
		      + "==test==";
		String output = "";
		output = "<table class=\"toc\">"
		       + "<tr><td><ol>"
		       + "<li><a href=\"#test\">test</a></li>"
		       + "<li><a href=\"#test_1\">test</a></li>"
		       + "<li><a href=\"#test_2\">test</a></li>"
		       + "<li><a href=\"#test_3\">test</a></li>"
		       + "</ol></td></tr></table>";
		TableOfContents toc = new TableOfContents();
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		assertEquals(output, toc.toHTML());
	}

	/**
	 *
	 */
	public void testDuplicateHeadings2() {
		String input = "";
		input = "==test==\n"
		      + "==test_2==\n"
		      + "==test==\n"
		      + "==test==";
		String output = "";
		output = "<table class=\"toc\">"
		       + "<tr><td><ol>"
		       + "<li><a href=\"#test\">test</a></li>"
		       + "<li><a href=\"#test_2\">test_2</a></li>"
		       + "<li><a href=\"#test_1\">test</a></li>"
		       + "<li><a href=\"#test_3\">test</a></li>"
		       + "</ol></td></tr></table>";
		TableOfContents toc = new TableOfContents();
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		toc.addEntry(toc.checkForUniqueName("test_2"), "test_2", 2);
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		toc.addEntry(toc.checkForUniqueName("test"), "test", 2);
		assertEquals(output, toc.toHTML());
	}

	/**
	 *
	 */
	public void testMultiLevelHeadings1() {
		String input = "";
		input = "==1==\n"
		      + "===1.1===\n"
		      + "==2==\n"
		      + "==3==";
		String output = "";
		output = "<table class=\"toc\">"
		       + "<tr><td><ol>"
		       + "<li><a href=\"#1\">1</a><ol>"
		       +   "<li><a href=\"#1.1\">1.1</a></li>"
		       + "</ol></li>"
		       + "<li><a href=\"#2\">2</a></li>"
		       + "<li><a href=\"#3\">3</a></li>"
		       + "</ol></td></tr></table>";
		TableOfContents toc = new TableOfContents();
		toc.addEntry(toc.checkForUniqueName("1"), "1", 2);
		toc.addEntry(toc.checkForUniqueName("1.1"), "1.1", 3);
		toc.addEntry(toc.checkForUniqueName("2"), "2", 2);
		toc.addEntry(toc.checkForUniqueName("3"), "3", 2);
		assertEquals(output, toc.toHTML());
	}

	/**
	 *
	 */
	public void testMultiLevelHeadings2() {
		// if the first level is not the greatest level it should be reset
		/* FIXME - implement
		String input = "";
		input = "====1.1.1====\n"
		      + "==2==\n"
		      + "==3==\n"
		      + "==4==";
		String output = "";
		output = "<table class=\"toc\">"
		       + "<tr><td><ol>"
		       + "<li><a href=\"#1.1.1\">1.1.1</a></li>"
		       + "<li><a href=\"#2\">2</a></li>"
		       + "<li><a href=\"#3\">3</a></li>"
		       + "<li><a href=\"#4\">4</a></li>"
		       + "</ol></td></tr></table>";
		TableOfContents toc = new TableOfContents();
		toc.addEntry(toc.checkForUniqueName("1.1.1"), "1.1.1", 4);
		toc.addEntry(toc.checkForUniqueName("2"), "2", 2);
		toc.addEntry(toc.checkForUniqueName("3"), "3", 2);
		toc.addEntry(toc.checkForUniqueName("4"), "4", 2);
		assertEquals(output, toc.toHTML());
		*/
	}
}