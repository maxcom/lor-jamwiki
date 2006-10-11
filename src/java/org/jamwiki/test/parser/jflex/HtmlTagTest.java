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
package org.jamwiki.test.parser.jflex;

import java.util.Locale;

/**
 *
 */
public class HtmlTagTest extends JFlexParserTest {

	/**
	 *
	 */
	public HtmlTagTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testAttribute() {
		String input = "";
		String output = "";
		input = "<font style=\"test\">text</font>";
		output = "<p><font style=\"test\">text</font>\n</p>";
		assertEquals(output, this.parse(input));
		input = "<font style=\"test\" color=\"red\">text</font>";
		output = "<p><font style=\"test\" color=\"red\">text</font>\n</p>";
		assertEquals(output, this.parse(input));
		input = "< font   style=\"test\" color=\"red\"  >text<  /  font  >";
		output = "<p><font style=\"test\" color=\"red\">text</font>\n</p>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testNoAttribute() {
		String input = "";
		String output = "";
		input = "<u>text</u>";
		output = "<p><u>text</u>\n</p>";
		assertEquals(output, this.parse(input));
		input = "< u >text< / u >";
		output = "<p><u>text</u>\n</p>";
		assertEquals(output, this.parse(input));
		input = "<strike>text</strike>";
		output = "<p><strike>text</strike>\n</p>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testXSS() {
		String input = "";
		String output = "";
		input = "<u CLASS=x onmouseover=\"alert('Ownage');\" >text</u>";
		// FIXME - output should be what is below
		// output = "<p><u class=\"x\">text</u>\n</p>";
		output = "<p><u >text</u>\n</p>";
		assertEquals(output, this.parse(input));
		input = "<DIV STYLE=\"background-image: url(javascript:(1) && ('XSS9'))\">x</div>";
		output = "<div >x</div>\n";
		assertEquals(output, this.parse(input));
	}
}