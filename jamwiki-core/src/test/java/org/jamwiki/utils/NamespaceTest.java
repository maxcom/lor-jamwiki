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
package org.jamwiki.utils;

import org.jamwiki.DataAccessException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class NamespaceTest {

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace() throws DataAccessException {
		Namespace result = Namespace.findMainNamespace("Test");
		assertNull("result", result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace1() throws DataAccessException {
		Namespace result = Namespace.findMainNamespace("");
		assertEquals("result", Namespace.MAIN, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace2() throws DataAccessException {
		Namespace result = Namespace.findMainNamespace("User");
		assertEquals("result", Namespace.USER, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace3() throws DataAccessException {
		Namespace result = Namespace.findMainNamespace("User comments");
		assertEquals("result", Namespace.USER, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace4() throws DataAccessException {
		Namespace result = Namespace.findMainNamespace("Special");
		assertEquals("result", Namespace.SPECIAL, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace() throws DataAccessException {
		Namespace result = Namespace.findMainNamespace("Test");
		assertNull("result", result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace1() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace("");
		assertEquals("result", Namespace.COMMENTS, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace2() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace("User");
		assertEquals("result", Namespace.USER_COMMENTS, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace3() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace("User comments");
		assertEquals("result", Namespace.USER_COMMENTS, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace4() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace("Special");
		assertNull("result", result);
	}
}
