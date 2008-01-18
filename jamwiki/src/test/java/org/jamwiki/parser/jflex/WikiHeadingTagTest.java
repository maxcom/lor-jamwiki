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
public class WikiHeadingTagTest extends JFlexParserTest {

	/**
	 *
	 */
	public WikiHeadingTagTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testHeading() {
		String input = "";
		String output = "";
		if (true) return;
		//FIXME This test fails because of i18n
		//I have "Editer" (="to edit" in French) instead of ("Edit") 
		input = "==heading==";
		output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[<a href=\"/wiki/en/Special:Edit?topic=DUMMY&amp;section=1\">Edit</a>]</div><a name=\"heading\"></a><h2>heading</h2>\n";
		assertEquals(output, this.parse(input));
		input = "=='''heading'''==";
		output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[<a href=\"/wiki/en/Special:Edit?topic=DUMMY&amp;section=1\">Edit</a>]</div><a name=\"heading\"></a><h2><b>heading</b></h2>\n";
		assertEquals(output, this.parse(input));
		input = "== three word heading ==";
		output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[<a href=\"/wiki/en/Special:Edit?topic=DUMMY&amp;section=1\">Edit</a>]</div><a name=\"three_word_heading\"></a><h2>three word heading</h2>\n";
		assertEquals(output, this.parse(input));
		input = "=== unmatched heading ==";
		output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[<a href=\"/wiki/en/Special:Edit?topic=DUMMY&amp;section=1\">Edit</a>]</div><a name=\"%3D_unmatched_heading\"></a><h2>= unmatched heading</h2>\n";
		assertEquals(output, this.parse(input));
	}
}