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
package org.jamwiki.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 */
public class JAMWikiTestSuite extends TestCase {

	/**
	 *
	 */
	public JAMWikiTestSuite(String name) {
		super(name);
	}

	/**
	 *
	 */
	public static Test suite() {
		TestSuite s = new TestSuite();
		s.addTestSuite(org.jamwiki.test.WikiMessageTest.class);
		s.addTestSuite(org.jamwiki.test.WikiVersionTest.class);
		s.addTestSuite(org.jamwiki.test.utils.DiffUtilTest.class);
		s.addTestSuite(org.jamwiki.test.utils.EncryptionTest.class);
		s.addTestSuite(org.jamwiki.test.utils.ImageUtilTest.class);
		s.addTestSuite(org.jamwiki.test.utils.InterWikiHandlerTest.class);
		s.addTestSuite(org.jamwiki.test.utils.LinkUtilTest.class);
		s.addTestSuite(org.jamwiki.test.utils.NamespaceHandlerTest.class);
		s.addTestSuite(org.jamwiki.test.utils.PaginationTest.class);
		s.addTestSuite(org.jamwiki.test.utils.PseudoTopicHandlerTest.class);
		s.addTestSuite(org.jamwiki.test.utils.SortedPropertiesTest.class);
		s.addTestSuite(org.jamwiki.test.utils.UtilitiesTest.class);
		s.addTestSuite(org.jamwiki.test.utils.WikiCacheTest.class);
		s.addTestSuite(org.jamwiki.test.utils.WikiLinkTest.class);
		s.addTestSuite(org.jamwiki.test.utils.WikiLogFormatterTest.class);
		s.addTestSuite(org.jamwiki.test.utils.WikiLoggerTest.class);
		s.addTestSuite(org.jamwiki.test.utils.XMLTopicFactoryTest.class);
		s.addTestSuite(org.jamwiki.test.utils.XMLUtilTest.class);
		s.addTestSuite(org.jamwiki.test.utils.TiddlyWikiParserTest.class);
		s.addTestSuite(org.jamwiki.test.utils.TiddlyWiki2MediaWikiTranslatorTest.class);
		s.addTestSuite(org.jamwiki.test.parser.TableOfContentsTest.class);
		s.addTestSuite(org.jamwiki.test.parser.jflex.CharacterTagTest.class);
		s.addTestSuite(org.jamwiki.test.parser.jflex.HtmlTagTest.class);
		s.addTestSuite(org.jamwiki.test.parser.jflex.HtmlLinkTagTest.class);
		s.addTestSuite(org.jamwiki.test.parser.jflex.HtmlPreTagTest.class);
		s.addTestSuite(org.jamwiki.test.parser.jflex.WikiHeadingTagTest.class);
		s.addTestSuite(org.jamwiki.test.parser.jflex.WikiListTagTest.class);
		return s;
	}
}
