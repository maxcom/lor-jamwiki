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

import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;
import org.springframework.util.StringUtils;

/**
 * Utility class for creating either a text of HTML representation of the difference
 * between two files.
 */
public class DiffUtil {

	protected static Logger logger = Logger.getLogger(DiffUtil.class);
	/** The number of lines of unchanged text to display before and after each diff. */
	// FIXME - make this a property value
	private static final int DIFF_UNCHANGED_LINE_DISPLAY = 2;

	/**
	 * Returned an HTML formatted table that displays a diff of two Strings.
	 *
	 * FIXME: return objects and parse to HTML from a JSP tag, not a class file.
	 *
	 * @param newVersion The String that is to be compared to, ie the later version of a topic.
	 * @param oldVersion The String that is to be considered as having changed, ie the earlier
	 *  version of a topic.
	 * @param htmlFormat Set to true if the diff should be returned in HTML format.  Returns
	 *  text otherwise.
	 * @return Returns an HTML-formatted table that displays the diff of the Strings.
	 */
	public static String diff(String newVersion, String oldVersion, boolean htmlFormat) {
		if (oldVersion == null) oldVersion = "";
		if (newVersion == null) newVersion = "";
		// FIXME: don't hard code
		if (!htmlFormat && newVersion.equals(oldVersion)) return "Files are the same";
		DiffUtil diffUtil = new DiffUtil();
		return diffUtil.process(newVersion, oldVersion, htmlFormat);
	}

	/**
	 *
	 */
	private String process(String newVersion, String oldVersion, boolean htmlFormat) {
		logger.debug("Diffing: " + oldVersion + " against: " + newVersion);
		DiffHelper diffHelper = new DiffHelper(oldVersion, newVersion, htmlFormat);
		return diffHelper.diff();
	}

	/**
	 * Split up a large String into an array of Strings made up of each line (indicated
	 * by a newline) of the original String.
	 */
	private static String[] buildArray(String original) {
		if (original == null) return null;
		StringTokenizer tokens = new StringTokenizer(original, "\n");
		int size = tokens.countTokens();
		String[] array = new String[size];
		int count = 0;
		while (tokens.hasMoreTokens()) {
			array[count] = tokens.nextToken();
			count++;
		}
		return array;
	}

	/**
	 *
	 */
	public static String convertToHTML(String input) {
		StringBuffer output = new StringBuffer(input);
		int pos = -1;
		// FIXME - need a general String.replace() method
		// for obvious reasons, ampersands must be escaped first
		while ((pos = output.indexOf("&", (pos+1))) != -1) {
			output.replace(pos, pos+1, "&amp;");
		}
		while ((pos = output.indexOf("<", (pos+1))) != -1) {
			output.replace(pos, pos+1, "&lt;");
		}
		while ((pos = output.indexOf(">", (pos+1))) != -1) {
			output.replace(pos, pos+1, "&gt;");
		}
		return output.toString();
	}

	/**
	 *
	 */
	class DiffHelper {

		String[] oldArray = null;
		String[] newArray = null;
		StringBuffer output = new StringBuffer();
		int oldCurrentLine = 0;
		int newCurrentLine = 0;
		int delStart, delEnd, addStart, addEnd, replacements;
		boolean lineNumberDisplayed = false;
		boolean htmlFormat = true;

		/**
		 *
		 */
		DiffHelper(String oldVersion, String newVersion, boolean htmlFormat) {
			this.oldArray = buildArray(oldVersion);
			this.newArray = buildArray(newVersion);
			this.htmlFormat = htmlFormat;
		}

		/**
		 * Generate an HTML row indicating the diff of two lines of the versioned
		 * files.
		 *
		 * @param oldChange A boolean flag indicating whether the old line has been deleted
		 *  or changed.
		 * @param oldLine A line from the file that has changed.
		 * @param newChange A boolean flag indicating whether the new line has been added.
		 * @param newLine A line from the later version of the file.
		 */
		private String buildRow(boolean oldChange, String oldLine, boolean newChange, String newLine) {
			StringBuffer output = new StringBuffer();
			// escape HTML if needed
			if (this.htmlFormat) {
				if (!StringUtils.hasText(oldLine)) {
					oldLine += "&#160;";
				} else {
					oldLine = convertToHTML(oldLine);
				}
				if (!StringUtils.hasText(newLine)) {
					newLine += "&#160;";
				} else {
					newLine = convertToHTML(newLine);
				}
			}
			// build table row
			if (this.htmlFormat) output.append("<tr>");
			if (this.htmlFormat) {
				if (oldChange) {
					output.append("<td class=\"diff-indicator\">-</td>");
					output.append("<td class=\"diff-delete\">" + oldLine + "</td>");
				} else {
					output.append("<td class=\"diff-no-indicator\">&#160;</td>");
					output.append("<td class=\"diff-unchanged\">" + oldLine + "</td>");
				}
				if (newChange) {
					output.append("<td class=\"diff-indicator\">+</td>");
					output.append("<td class=\"diff-add\">" + newLine + "</td>");
				} else {
					output.append("<td class=\"diff-no-indicator\">&#160;</td>");
					output.append("<td class=\"diff-unchanged\">" + newLine + "</td>");
				}
			} else {
				if (oldChange) {
					output.append("- >").append(oldLine).append("\n");
				} else {
					output.append("  >").append(oldLine).append("\n");
				}
				if (newChange) {
					output.append("+ <").append(newLine).append("\n");
				} else {
					output.append("  <").append(newLine).append("\n");
				}
			}
			if (this.htmlFormat) output.append("</tr>");
			return output.toString();
		}

		/**
		 *
		 */
		private boolean canDisplay(int changeStart, int changeEnd, int currentLine) {
			// only display if current line is plus or minus a specified number of lines
			// from the change area
			int earliest = (this.htmlFormat) ? (changeStart - DIFF_UNCHANGED_LINE_DISPLAY) : changeStart;
			int latest = (this.htmlFormat) ? (changeEnd + DIFF_UNCHANGED_LINE_DISPLAY) : changeEnd;
			if (currentLine >= earliest && currentLine <= latest) return true;
			return false;
		}

		/**
		 *
		 */
		String diff() {
			Diff diffObject = new Diff(this.oldArray, this.newArray);
			List diffs = diffObject.diff();
			Difference diff;
			if (this.htmlFormat) this.output.append("<table class=\"diff\">");
			for (int i=0; i < diffs.size(); i++) {
				diff = (Difference)diffs.get(i);
				this.lineNumberDisplayed = false;
				this.delStart = diff.getDeletedStart();
				this.delEnd = diff.getDeletedEnd();
				this.addStart = diff.getAddedStart();
				this.addEnd = diff.getAddedEnd();
				// add lines up to first change point
				displayUnchanged(this.delStart, this.addStart);
				// add changed lines
				displayChanged();
			}
			// if lines at the end of the original Strings haven't changed display them
			displayUnchanged(oldArray.length, newArray.length);
			if (this.htmlFormat) output.append("</table>");
			return output.toString();
		}

		/**
		 *
		 */
		private void displayUnchanged(int delMax, int addMax) {
			replacements = ((delMax - this.oldCurrentLine) > (addMax - this.newCurrentLine)) ? (delMax - this.oldCurrentLine) : (addMax - this.newCurrentLine);
			String oldLine, newLine;
			for (int j=0; j < replacements; j++) {
				oldLine = "";
				if (this.oldCurrentLine < this.oldArray.length && this.oldCurrentLine < delMax) {
					oldLine = this.oldArray[this.oldCurrentLine];
					this.oldCurrentLine++;
				}
				newLine = "";
				if (this.newCurrentLine < this.newArray.length && this.newCurrentLine < addMax) {
					newLine = this.newArray[this.newCurrentLine];
					this.newCurrentLine++;
				}
				// only display if within specified number of lines of a change.  subtract
				// one from current line since that value was incremented above
				if (canDisplay(delMax, this.delEnd, (this.oldCurrentLine - 1)) || canDisplay(addMax, this.addEnd, (this.newCurrentLine - 1))) {
					displayLineNumber();
					this.output.append(buildRow(false, oldLine, false, newLine));
				}
			}
		}

		/**
		 *
		 */
		private void displayChanged() {
			replacements = ((this.delEnd - this.delStart) > (this.addEnd - this.addStart)) ? (this.delEnd - this.delStart) : (this.addEnd - this.addStart);
			String oldLine, newLine;
			boolean oldChange, newChange;
			for (int j=0; j <= replacements; j++) {
				oldLine = "";
				oldChange = false;
				if (this.oldCurrentLine < this.oldArray.length && this.delStart <= this.delEnd) {
					oldLine = this.oldArray[this.oldCurrentLine];
					oldChange = true;
					this.oldCurrentLine++;
					this.delStart++;
				}
				newLine = "";
				newChange = false;
				if (this.newCurrentLine < this.newArray.length && this.addStart <= this.addEnd) {
					newLine = this.newArray[this.newCurrentLine];
					newChange = true;
					this.newCurrentLine++;
					this.addStart++;
				}
				displayLineNumber();
				output.append(buildRow(oldChange, oldLine, newChange, newLine));
			}
		}

		/**
		 *
		 */
		private void displayLineNumber() {
			if (this.lineNumberDisplayed) return;
			int lineNumber = oldCurrentLine;
			this.lineNumberDisplayed = true;
			if (this.htmlFormat) {
				output.append("<tr><td colspan=\"4\" class=\"diff-line\">Line " + lineNumber + ":</td></tr>");
			} else {
				output.append("Line " + lineNumber + ":\n");
			}
		}
	}
}
