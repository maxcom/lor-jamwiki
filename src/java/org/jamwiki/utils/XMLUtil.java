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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 */
public class XMLUtil {

	/** Logger */
	public static final Logger logger = Logger.getLogger(XMLUtil.class);

	/**
	 *
	 */
	public static String buildTag(String tagName, String tagValue, boolean escape) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<").append(tagName).append(">");
		if (escape) {
			tagValue = escapeXML(tagValue);
		}
		buffer.append(tagValue);
		buffer.append("</").append(tagName).append(">");
		return buffer.toString();
	}

	/**
	 *
	 */
	public static String buildTag(String tagName, int tagValue) {
		return XMLUtil.buildTag(tagName, new Integer(tagValue).toString(), false);
	}

	/**
	 *
	 */
	public static String buildTag(String tagName, boolean tagValue) {
		return XMLUtil.buildTag(tagName, new Boolean(tagValue).toString(), false);
	}

	/**
	 *
	 */
	public static String buildTag(String tagName, Timestamp tagValue) {
		return XMLUtil.buildTag(tagName, tagValue.toString(), false);
	}

	/**
	 *
	 */
	private static String escapeXML(String text) {
		StringBuffer buffer = new StringBuffer(text);
		buffer = Utilities.replaceString(buffer, "&", "&amp;");
		buffer = Utilities.replaceString(buffer, "<", "&lt;");
		buffer = Utilities.replaceString(buffer, ">", "&gt;");
		buffer = Utilities.replaceString(buffer, "\"", "&quot;");
		buffer = Utilities.replaceString(buffer, "'", "&apos;");
		return buffer.toString();
	}

	/**
	 *
	 */
	public static Document parseXML(File file, boolean validating) throws Exception {
		if (!file.exists()) {
			throw new Exception("File " + file.getAbsolutePath() + " does not exist");
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			InputSource source = new InputSource(stream);
			try {
				return XMLUtil.parseXML(source, validating);
			} catch (SAXException e) {
				// invalid XML
				logger.error("The file " + file.getAbsolutePath() + " contains invalid XML", e);
				throw new Exception("The file " + file.getAbsolutePath() + " contains invalid XML: " + e.getMessage());
			}
		} finally {
			if (stream != null) stream.close();
		}
	}

	/**
	 *
	 */
	public static Document parseXML(InputSource source, boolean validating) throws Exception {
		// Create a builder factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validating);
		// Create the builder and parse the file
		Document doc = factory.newDocumentBuilder().parse(source);
		return doc;
	}
}
