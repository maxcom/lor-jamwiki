package org.jmwiki.parser;

import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;

/**
 * Experimental class. This class may be used in two ways:
 *
 * The static addTableOfContents(String) method may be called to automatically
 * adds a table of contents on the right side of an article.  This method
 * works with all lexers, because it parses the HTML for headers. However it
 * doesn't care where it is. So if you have a header on the TopArea / LeftMenu /
 * BottomArea, it will also add a TOC there...
 *
 * The static addTableOfContents(TableOfContents, StringBuffer) method
 * may be called to insert a pre-built TableOfContents object into an
 * article.  This method requires that the parser has added all table of
 * contents headings to the object and included a TOC_INSERT_TAG at the point
 * where the table of contents should be inserted.  It is a bit more flexible
 * but requires more preperatory work.
 *
 * @author studer
 */
public class TableOfContents {

	private static final Logger logger = Logger.getLogger(TableOfContents.class);
	public static final int STATUS_TOC_UNINITIALIZED = 0;
	public static final int STATUS_TOC_INITIALIZED = 1;
	public static final int STATUS_NO_TOC = 2;
	public static final String TOC_INSERT_TAG = "__INSERT_TOC__";
	private int currentLevel = 0;
	private int minLevel = 4;
	private Vector entries = new Vector();
	private int status = STATUS_TOC_UNINITIALIZED;

	/**
	 * Adds TOC at the beginning as a table on the right side of the page if the
	 * page has any HTML-headers.
	 *
	 * @param text
	 * @return
	 */
	public static String addTableOfContents(String text) {
		logger.debug("Start TOC generating...");
		Pattern p = Pattern.compile("<[Hh][123][^>]*>(.*)</[Hh][123][^>]*>");
		Matcher m = p.matcher(text);
		StringBuffer result = new StringBuffer();
		StringBuffer toc = new StringBuffer();
		toc.append("<table align=\"right\" class=\"toc\"><tr><td>");
		int position = 0;
		while (m.find()) {
			result.append(text.substring(position, m.start(1)));
			position = m.start(1);
			result.append("<a class=\"tocheader\" name=\"" + position
					+ "\" id=\"" + position + "\"></a>");
			if (m.group().startsWith("<h1") || m.group().startsWith("<H1")) {
				toc.append("<span class=\"tocheader1\">");
			} else if (m.group().startsWith("<h2") || m.group().startsWith("<H2")) {
				toc.append("<span class=\"tocheader2\">");
			} else {
				toc.append("<span class=\"tocheader3\">");
			}
			toc.append("<li><a href=\"#" + position + "\">" + m.group(1)
					+ "</a></li></span>");
			result.append(text.substring(position, m.end(1)));
			position = m.end(1);
			logger.debug("Adding content: " + m.group(1));
		}
		toc.append("</td></tr></table>");
		result.append(text.substring(position));
		if (position > 0) {
			logger.debug("adding TOC at the beginning!");
			toc.append(result);
		} else {
			toc = result;
		}
		return toc.toString();
	}

	/**
	 * Insert an existing TableOfContents object into formatted HTML
	 * output.
	 *
	 * @param toc A pre-built TableOfContents object.
	 * @param contents The Wiki syntax, which should contain TOC_INSERT_TAG at
	 *  the point where the table of contents object is to be inserted.
	 * @return The formatted content containing the table of contents.
	 */
	public static StringBuffer addTableOfContents(TableOfContents toc, StringBuffer contents) {
		int pos = contents.indexOf(TableOfContents.TOC_INSERT_TAG);
		if (pos >= 0) {
			// FIXME - don't hardcode minimum TOC size
			if (toc == null || toc.size() <= 3 || toc.getStatus() == TableOfContents.STATUS_NO_TOC || !Environment.getBooleanValue(Environment.PROP_PARSER_TOC)) {
				// remove the insert tag
				contents.delete(pos, pos + TableOfContents.TOC_INSERT_TAG.length());
			} else {
				// insert the toc
				contents.replace(pos, pos + TableOfContents.TOC_INSERT_TAG.length(), toc.toHTML());
			}
		}
		return contents;
	}

	/**
	 * Add a new table of contents entry.
	 *
	 * @param name The name of the entry, to be used in the anchor tag name.
	 * @param text The text to display for the table of contents entry.
	 * @param level The level of the entry.  If an entry is a sub-heading of
	 *  another entry the value should be 2.  If there is a sub-heading of that
	 *  entry then its value would be 3, and so forth.
	 */
	public void addEntry(String name, String text, int level) {
		if (this.status != STATUS_NO_TOC) this.status = STATUS_TOC_INITIALIZED;
		TableOfContentsEntry entry = new TableOfContentsEntry(name, text, level);
		entries.add(entry);
		if (level < minLevel) minLevel = level;
	}

	/**
	 * Internal method to close any list tags prior to adding the next entry.
	 */
	private void closeList(int level, StringBuffer text) {
		while (level < currentLevel) {
			// close lists to current level
			text.append("</li></ol>");
			currentLevel--;
		}
	}

	/**
	 * Return the current table of contents status, such as "no table of contents
	 * allowed" or "uninitialized".
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Internal method to open any list tags prior to adding the next entry.
	 */
	private void openList(int level, StringBuffer text) {
		if (level == currentLevel) {
			// same level as previous item, close previous and open new
			text.append("</li><li>");
			return;
		}
		while (level > currentLevel) {
			// open lists to current level
			text.append("<ol><li>");
			currentLevel++;
		}
	}

	/**
	 * Set the current table of contents status, such as "no table of contents
	 * allowed" or "uninitialized".
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Return the number of entries in this TOC object.
	 */
	public int size() {
		return this.entries.size();
	}

	/**
	 * Return an HTML representation of this table of contents object.
	 */
	public String toHTML() {
		Enumeration e = entries.elements();
		StringBuffer text = new StringBuffer();
		text.append("<table class=\"toc\"><tr><td>");
		TableOfContentsEntry entry = null;
		int adjustedLevel = 0;
		while (e.hasMoreElements()) {
			entry = (TableOfContentsEntry)e.nextElement();
			// adjusted level determines how far to indent the list
			adjustedLevel = ((entry.level - minLevel) + 1);
			closeList(adjustedLevel, text);
			openList(adjustedLevel, text);
			text.append("<a href=\"#").append(entry.name).append("\">").append(entry.text).append("</a>");
		}
		closeList(0, text);
		text.append("</td></tr></table>");
		return text.toString();
	}

	/**
	 * Inner class holds TOC entries until they can be processed for display.
	 */
	class TableOfContentsEntry {

		int level;
		String name;
		String text;

		/**
		 *
		 */
		TableOfContentsEntry(String name, String text, int level) {
			this.name = name;
			this.text = text;
			this.level = level;
		}
	}
}
