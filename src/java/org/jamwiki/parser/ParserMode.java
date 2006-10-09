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
public class ParserMode {

	public static final int MODE_NORMAL = 1;
	/** Preview mode indicates that the topic is being edited but not saved yet. */
	public static final int MODE_PREVIEW = 2;
	/** Save mode indicates that the topic was edited and is being saved. */
	public static final int MODE_SAVE = 4;
	public static final int MODE_SPLICE = 8;
	public static final int MODE_SLICE = 16;
	public static final int MODE_SEARCH = 32;
	public static final int MODE_TEMPLATE = 64;

	private int mode = MODE_NORMAL;

	/**
	 *
	 */
	public ParserMode() {
	}

	/**
	 *
	 */
	public ParserMode(ParserMode parserMode) {
		this.mode = parserMode.mode;
	}

	/**
	 *
	 */
	public ParserMode(int mode) {
		this.mode = mode;
	}

	/**
	 *
	 */
	public void addMode(int mode) {
		if (this.hasMode(mode)) return;
		// add mode
		this.mode |= mode;
	}

	/**
	 *
	 */
	public int getMode() {
		return this.mode;
	}

	/**
	 *
	 */
	public boolean hasMode(int mode) {
		return ((this.mode & mode) == mode);
	}

	/**
	 *
	 */
	public void removeMode(int mode) {
		if (!this.hasMode(mode)) return;
		// remove mode
		this.mode ^= mode;
	}
}
