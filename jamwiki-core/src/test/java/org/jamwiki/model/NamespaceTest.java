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
package org.jamwiki.model;

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
	public void testFindMainNamespace() {
		Namespace result = Namespace.findMainNamespace(null);
		assertNull("result", result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace1() {
		Namespace result = Namespace.findMainNamespace(Namespace.MAIN);
		assertEquals("result", Namespace.MAIN, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace2() {
		Namespace result = Namespace.findMainNamespace(Namespace.USER);
		assertEquals("result", Namespace.USER, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace3() {
		Namespace result = Namespace.findMainNamespace(Namespace.USER_COMMENTS);
		assertEquals("result", Namespace.USER, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindMainNamespace4() {
		Namespace result = Namespace.findMainNamespace(Namespace.SPECIAL);
		assertEquals("result", Namespace.SPECIAL, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace1() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace(Namespace.MAIN);
		assertEquals("result", Namespace.COMMENTS, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace2() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace(Namespace.USER);
		assertEquals("result", Namespace.USER_COMMENTS, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace3() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace(Namespace.USER_COMMENTS);
		assertEquals("result", Namespace.USER_COMMENTS, result);
	}

	/**
	 *
	 */
	@Test
	public void testFindCommentsNamespace4() throws DataAccessException {
		Namespace result = Namespace.findCommentsNamespace(Namespace.SPECIAL);
		assertNull("result", result);
	}
}
