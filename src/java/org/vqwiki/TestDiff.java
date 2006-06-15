package org.vqwiki;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.vqwiki.utils.DiffUtil;

/**
 Very Quick Wiki - WikiWikiWeb clone
 Copyright (C) 2001-2002 Gareth Cronin

 This program is free software; you can redistribute it and/or modify
 it under the terms of the latest version of the GNU Lesser General
 Public License as published by the Free Software Foundation;

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program (gpl.txt); if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

public class TestDiff extends TestCase {

	protected static Logger logger = Logger.getLogger(TestDiff.class);

	/**
	 *
	 */
	public TestDiff(String name) {
		super(name);
	}

	/**
	 *
	 */
	public void testStringDiff() throws Exception {
		String string1 = "This is a string";
		String string2 = "This is a string\nwith a bit extra";
		logger.debug(DiffUtil.diff(string1, string2, false));
	}
}
