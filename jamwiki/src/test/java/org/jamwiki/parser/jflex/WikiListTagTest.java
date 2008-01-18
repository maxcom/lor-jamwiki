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
package org.jamwiki.parser.jflex;

/**
 *
 */
public class WikiListTagTest extends JFlexParserTest {

	/**
	 *
	 */
	public WikiListTagTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testDefinitionList() {
		String input = "";
		String output = "";
		input = ";first\n:second";
		output = "<dl><dt>first\n</dt><dd>second\n</dd></dl>";
		assertEquals(output, this.parse(input));
		input = ";1\n::2.1\n;;2.2\n;2";
		output = "<dl><dt>1\n<dl><dd>2.1\n</dd><dt>2.2\n</dt></dl></dt><dt>2\n</dt></dl>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testMixedList() {
		String input = "";
		String output = "";
		input = "#1\n*2\n#3";
		output = "<ol><li>1\n</li></ol><ul><li>2\n</li></ul><ol><li>3\n</li></ol>";
		assertEquals(output, this.parse(input));
		input = "#1\n#*1.1\n#*1.2\n#*:1.2.1\n#*;1.2.2\n#*;*1.2.2.1\n#*;1.2.3\n#2";
		output = "<ol><li>1\n<ul><li>1.1\n</li><li>1.2\n<dl><dd>1.2.1\n</dd><dt>1.2.2\n<ul><li>1.2.2.1\n</li></ul></dt><dt>1.2.3\n</dt></dl></li></ul></li><li>2\n</li></ol>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testOrderedList() {
		String input = "";
		String output = "";
		input = "#1\n#2";
		output = "<ol><li>1\n</li><li>2\n</li></ol>";
		assertEquals(output, this.parse(input));
		input = "#1\n#2\n##2.1\n##2.2\n###2.2.1\n###2.2.2\n#3";
		output = "<ol><li>1\n</li><li>2\n<ol><li>2.1\n</li><li>2.2\n<ol><li>2.2.1\n</li><li>2.2.2\n</li></ol></li></ol></li><li>3\n</li></ol>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testUnorderedList() {
		String input = "";
		String output = "";
		input = "*1\n*2";
		output = "<ul><li>1\n</li><li>2\n</li></ul>";
		assertEquals(output, this.parse(input));
		input = "*1\n**1.1\n**1.2\n***1.2.1\n***1.2.2\n*2";
		output = "<ul><li>1\n<ul><li>1.1\n</li><li>1.2\n<ul><li>1.2.1\n</li><li>1.2.2\n</li></ul></li></ul></li><li>2\n</li></ul>";
		assertEquals(output, this.parse(input));
	}
}