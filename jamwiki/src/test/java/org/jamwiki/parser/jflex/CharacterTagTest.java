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
public class CharacterTagTest extends JFlexParserTest {

	/**
	 *
	 */
	public CharacterTagTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testEntity() {
		String input = "";
		String output = "";
		input = "&";
		output = "<p>&amp;\n</p>";
		assertEquals(output, this.parse(input));
		input = "& \"";
		output = "<p>&amp; &quot;\n</p>";
		assertEquals(output, this.parse(input));
		input = "&#39;";
		output = "<p>&#39;\n</p>";
		assertEquals(output, this.parse(input));
		input = "&lt;";
		output = "<p>&lt;\n</p>";
		assertEquals(output, this.parse(input));
		input = "&lt; > &gt;";
		output = "<p>&lt; &gt; &gt;\n</p>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testInvalidEntity() {
		String input = "";
		String output = "";
		input = "&bogus;";
		output = "<p>&amp;bogus;\n</p>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testNonEntity() {
		String input = "";
		String output = "";
		input = "a";
		output = "<p>a\n</p>";
		assertEquals(output, this.parse(input));
		input = "a b c 1 2 3";
		output = "<p>a b c 1 2 3\n</p>";
		assertEquals(output, this.parse(input));
	}
}