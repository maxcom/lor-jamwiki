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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.jflex.JFlexParser;

/**
 *
 */
public class JFlexParserTest extends TestCase {

	/**
	 *
	 */
	public JFlexParserTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 *
	 */
	private String parse(String raw) {
		ParserMode mode = new ParserMode(ParserMode.MODE_NORMAL);
		// FIXME - hard coding
		String topicName = "DUMMY";
		ParserInput parserInput = new ParserInput();
		parserInput.setContext("/wiki/");
		parserInput.setLocale(new Locale("en-US"));
		parserInput.setWikiUser(null);
		parserInput.setTopicName(topicName);
		parserInput.setUserIpAddress("0.0.0.0");
		parserInput.setVirtualWiki("en");
		parserInput.setAllowSectionEdit(true);
		JFlexParser parser = new JFlexParser(parserInput);
		String output = null;
		try {
			ParserOutput parserOutput = parser.parseHTML(raw, "DUMMY", mode);
			output = parserOutput.getContent();
		} catch (Exception e) {}
		return output;
	}

	/**
	 *
	 */
	public static Test suite() {
		return new TestSuite(JFlexParserTest.class);
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
		output = "<dl><dt>1\n<dl><dd>2.1\n</dt><dt>2.2\n</dt></dl></dd></dl><dl><dt>2\n</dt></dl>";
		assertEquals(output, this.parse(input));
	}

	/**
	 *
	 */
	public void testUnorderedList() {
		String input = "";
		String output = "";
		input = "* first\n*second";
		output = "<ul><li> first\n</li><li>second\n</li></ul>";
		assertEquals(output, this.parse(input));
		input = "*1\n**1.1\n**1.2\n*2";
		output = "<ul><li>1\n<ul><li>1.1\n</li><li>1.2\n</li></ul></li><li>2\n</li></ul>";
		assertEquals(output, this.parse(input));
	}
}