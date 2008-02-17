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
public class ParseTemplateTest extends TestCase {

	/**
	 *
	 */
	public ParseTemplateTest(String name) {
		super(name);
	}

	/**
	 * Verify that templates containing <noinclude> tags behave as expected.
	 */
	public void testNoincludeParse() throws Exception {
		String topicName = "Noinclude";
		String parserResult = TestParser.parserResult(topicName);
		String expectedResult = TestParser.expectedResult(topicName);
		assertEquals(parserResult, expectedResult);
	}
}