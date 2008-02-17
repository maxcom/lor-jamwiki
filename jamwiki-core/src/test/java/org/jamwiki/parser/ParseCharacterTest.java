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
public class ParseCharacterTest extends TestParser {

	/**
	 *
	 */
	public ParseCharacterTest(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testCharacterNonEntities() throws Exception {
		executeParserTest("CharacterNonEntities1");
	}

	/**
	 *
	 */
	public void testCharacterEntities() throws Exception {
		executeParserTest("CharacterEntities1");
		executeParserTest("CharacterEntities2");
		executeParserTest("CharacterEntities3");
	}

	/**
	 *
	 */
	public void testCharacterInvalidEntities() throws Exception {
		executeParserTest("CharacterInvalidEntities1");
	}
}