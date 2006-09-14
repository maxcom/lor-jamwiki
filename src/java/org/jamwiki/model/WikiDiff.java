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

import org.jamwiki.WikiLogger;

/**
 *
 */
public class WikiDiff {

	private static WikiLogger logger = WikiLogger.getLogger(WikiDiff.class.getName());
	private boolean change = false;
	private String newLine = null;
	private String oldLine = null;
	private int lineNumber = -1;

	/**
	 *
	 */
	public WikiDiff() {
	}

	/**
	 *
	 */
	public WikiDiff(String oldLine, String newLine, int lineNumber, boolean change) {
		this.oldLine = oldLine;
		this.newLine = newLine;
		this.lineNumber = lineNumber;
		this.change = change;
	}

	/**
	 *
	 */
	public boolean getChange() {
		return this.change;
	}

	/**
	 *
	 */
	public void setChange(boolean change) {
		this.change = change;
	}

	/**
	 *
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 *
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 *
	 */
	public String getNewLine() {
		return this.newLine;
	}

	/**
	 *
	 */
	public void setNewLine(String newLine) {
		this.newLine = newLine;
	}

	/**
	 *
	 */
	public String getOldLine() {
		return this.oldLine;
	}

	/**
	 *
	 */
	public void setOldLine(String oldLine) {
		this.oldLine = oldLine;
	}
}