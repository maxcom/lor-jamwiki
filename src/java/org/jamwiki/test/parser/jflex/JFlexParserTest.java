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
	protected String parse(String raw) {
		// FIXME - hard coding
		String topicName = "DUMMY";
		ParserInput parserInput = new ParserInput();
		parserInput.setContext("/wiki");
		parserInput.setLocale(new Locale("en-US"));
		parserInput.setWikiUser(null);
		parserInput.setTopicName(topicName);
		parserInput.setUserIpAddress("0.0.0.0");
		parserInput.setVirtualWiki("en");
		parserInput.setAllowSectionEdit(true);
		JFlexParser parser = new JFlexParser(parserInput);
		String output = null;
		try {
			ParserOutput parserOutput = parser.parseHTML(raw);
			output = parserOutput.getContent();
		} catch (Exception e) {}
		return output;
	}

	/**
	 *
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
}