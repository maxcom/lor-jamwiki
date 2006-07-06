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

import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiUser;
import org.jamwiki.persistency.file.FileHandler;
import org.jamwiki.servlets.JAMWikiServlet;
import org.springframework.util.StringUtils;

/**
 *
 */
public class Utilities {

	private static final Logger logger = Logger.getLogger(Utilities.class);
	private static final int STATE_NO_ENTITY = 0;
	private static final int STATE_AMPERSAND = 1;
	private static final int STATE_AMPERSAND_HASH = 2;

	/**
	 *
	 */
	public static String buildInternalLink(String context, String virtualWiki, String page) {
		return buildInternalLink(context, virtualWiki, page, null);
	}

	/**
	 *
	 */
	public static String buildInternalLink(String context, String virtualWiki, String page, String section) {
		String url = context;
		// context never ends with a "/" per servlet specification
		url += "/";
		// get the virtual wiki, which should have been set by the parent servlet
		url += Utilities.encodeURL(virtualWiki);
		url += "/";
		url += Utilities.encodeURL(page);
		if (section != null && section.length() > 0) {
			if (section.startsWith("#")) {
				section = section.substring(1);
			}
			url += "#" + Utilities.encodeURL(section);
		}
		return url;
	}

	/**
	 *
	 */
	public static String buildWikiLink(String context, String virtualWiki, String topic) throws Exception {
		if (!StringUtils.hasText(topic)) {
			return null;
		}
		// search for hash mark
		String section = "";
		int pos = topic.indexOf('#');
		if (pos > 0) {
			section = topic.substring(pos+1).trim();
			topic = topic.substring(0, pos).trim();
		}
		String url = Utilities.buildInternalLink(context, virtualWiki, topic, section);
		if (!WikiBase.exists(virtualWiki, topic)) {
			url = Utilities.buildInternalLink(context, virtualWiki, "Special:Edit");
			url += "?topic=" + Utilities.encodeURL(topic);
		}
		return url;
	}

	/**
	 * Returns true if the given collection of strings contains the given string where the case
	 * of either is ignored
	 * @param collection collection of {@link String}s
	 * @param string string to find
	 * @return true if the string is in the collection with no regard to case
	 */
	public static boolean containsStringIgnoreCase(Collection collection, String string) {
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			String s = (String) iterator.next();
			if (s.equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 */
	public static String convertToHTML(char character) {
		switch (character) {
			case ('<'):
				return "&lt";
			case ('>'):
				return "&gt";
			case ('&'):
				return "&amp";
		}
		return String.valueOf(character);
	}

	/**
	 *
	 */
	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Create the root path for a specific WIKI without the server name.
	 * This is useful for local redirection or local URL's (relative URL's to the server).
	 * @param request The HttpServletRequest
	 * @param virtualWiki The name of the current virtual Wiki
	 * @return the root path for this viki
	 */
	public static String createLocalRootPath(HttpServletRequest request, String virtualWiki) {
		String contextPath = "";
		contextPath += request.getContextPath();
		if (virtualWiki == null || virtualWiki.length() < 1) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		return contextPath + "/" + virtualWiki + "/";
	}

	/**
	 * Create the root path for a specific WIKI with a specific server
	 * @param request The HttpServletRequest
	 * @param virtualWiki The name of the current virtual Wiki
	 * @param server the specific server given for the path.
	 *			   If it is set to "null" or an empty string, it will take
	 *			   the servername from the given request.
	 * @return the root path for this viki
	 */
	public static String createRootPath(HttpServletRequest request, String virtualWiki, String server) {
		String contextPath = "";
		if (server == null || server.trim().equals("")) {
			contextPath = "http://" + request.getServerName();
		} else {
			contextPath = "http://" + server;
		}
		if (request.getServerPort() != 80) {
			contextPath += ":" + request.getServerPort();
		}
		contextPath += request.getContextPath();
		if (virtualWiki == null || virtualWiki.length() < 1) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		return contextPath + "/" + virtualWiki + "/";
	}

	/**
	 *
	 */
	public static Cookie createUsernameCookie(String username) {
		Cookie c = new Cookie("username", username);
		c.setMaxAge(Environment.getIntValue(Environment.PROP_BASE_COOKIE_EXPIRE));
		return c;
	}

	/**
	 *
	 */
	public static WikiUser currentUser(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return (WikiUser)session.getAttribute(JAMWikiServlet.PARAMETER_USER);
	}

	/**
	 * Converts back file name encoded by encodeSafeFileName().
	 */
	public static String decodeSafeFileName(String name) {
		// URL decode the rest of the name
		try {
			name = URLDecoder.decode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Failure while decoding " + name + " with charset UTF-8", e);
		}
		// replace spaces with underscores
		name = StringUtils.replace(name, " ", "_");
		return name;
	}

	/**
	 *
	 */
	public static String decodeURL(String url) {
		String charSet = Environment.getValue(Environment.PROP_FILE_ENCODING);
		if (charSet == null) charSet = "UTF-8";
		return Utilities.decodeURL(url, charSet);
	}

	/**
	 *
	 */
	public static String decodeURL(String url,String charSet) {
		try {
			url = URLDecoder.decode(url, charSet);
		} catch (UnsupportedEncodingException e) {
			logger.error("Failure while decoding url " + url + " with charset " + charSet, e);
		}
		// convert underscores to spaces
		url = StringUtils.replace(url, "_", " ");
		return url;
	}

	/**
	 *
	 */
	private static String dir() {
		return Environment.getValue(Environment.PROP_BASE_FILE_DIR) + System.getProperty("file.separator");
	}

	/**
	 * Converts arbitrary string into string usable as file name.
	 */
	public static String encodeSafeFileName(String name) {
		// replace spaces with underscores
		name = StringUtils.replace(name, " ", "_");
		// URL encode the rest of the name
		try {
			name = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Failure while encoding " + name + " with charset UTF-8", e);
		}
		return name;
	}

	/**
	 * This caused problems - encoding without a charset is not well-defined
	 * behaviour, so we'll look for a default encoding. (coljac)
	 */
	public static String encodeURL(String url) {
		String charSet = Environment.getValue(Environment.PROP_FILE_ENCODING);
		if (charSet == null) charSet = "UTF-8";
		return Utilities.encodeURL(url, charSet);
	}

	/**
	 *
	 */
	public static String encodeURL(String url,String charSet) {
		// convert spaces to underscores
		url = StringUtils.replace(url, " ", "_");
		try {
			url = URLEncoder.encode(url, charSet);
		} catch (UnsupportedEncodingException e) {
			logger.error("Failure while encoding url " + url + " with charset " + charSet, e);
		}
		// FIXME - un-encode colons.  handle this better.
		url = StringUtils.replace(url, "%3A", ":");
		return url;
	}

	/**
	 * Returns any trailing . , ; : characters on the given string
	 * @param text
	 * @return empty string if none are found
	 */
	public static String extractTrailingPunctuation(String text) {
		StringBuffer buffer = new StringBuffer();
		for (int i = text.length() - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (c == '.' || c == ';' || c == ',' || c == ':') {
				buffer.append(c);
			} else {
				break;
			}
		}
		if (buffer.length() == 0) return "";
		buffer = buffer.reverse();
		return buffer.toString();
	}

	/**
	 * Localised
	 */
	public static String formatDate(Date date) {
		return DateFormat.getDateInstance().format(date);
	}

	/**
	 *
	 */
	public static String formatDateTime(Date date) {
		return DateFormat.getDateTimeInstance().format(date);
	}

	/**
	 * Get messages for the given locale
	 * @param locale locale
	 * @return
	 */
	public static String getMessage(String key, Locale locale) {
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
		return messages.getString(key);
	}

	/**
	 * Provide the capability to highlight terms within a block of HTML.
	 */
	// FIXME - this code is really ugly and confusing...
	public static String highlightHTML(String contents, String highlightparam) {
		if (!StringUtils.hasText(highlightparam)) return contents;
		String highlighttext = "<b style=\"color:black;background-color:#ffff66\">###</b>";
		contents = markToReplaceOutsideHTML(contents, highlightparam);
		for (int i = 0; i < highlightparam.length(); i++) {
			String myhighlightparam = highlightparam.substring(0, i) + highlightparam.substring(i, i + 1).toUpperCase();
			if ((i + 1) < highlightparam.length()) {
				myhighlightparam += highlightparam.substring(i + 1);
			}
			String highlight = highlighttext;
			highlight = StringUtils.replace(highlight, "###", myhighlightparam);
			contents = StringUtils.replace(contents, '\u0000' + myhighlightparam, highlight);
			myhighlightparam = highlightparam.substring(0, i) + highlightparam.substring(i, i + 1).toLowerCase();
			if ((i + 1) < highlightparam.length()) {
				myhighlightparam += highlightparam.substring(i + 1);
			}
			highlight = highlighttext;
			highlight = StringUtils.replace(highlight, "###", myhighlightparam);
			contents = StringUtils.replace(contents, '\u0000' + myhighlightparam, highlight);
		}
		return contents;
	}

	/**
	 *
	 */
	public static boolean isAdmin(HttpServletRequest request) {
		WikiUser user = currentUser(request);
		return (user != null && user.getAdmin());
	}

	/**
	 *
	 */
	public static boolean isFirstUse() {
		return !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED);
	}

	/**
	 * Mark all needles in a haystack, so that they can be replaced later. Take special care on HTML,
	 * so that no needle is replaced inside a HTML tag.
	 *
	 * @param haystack The haystack to go through.
	 * @param needle   The needle to search.
	 * @return The haystack with all needles (outside HTML) marked with the char \u0000
	 */
	private static String markToReplaceOutsideHTML(String haystack, String needle) {
		if (needle.length() == 0) {
			return haystack;
		}
		StringBuffer sb = new StringBuffer();
		boolean inHTMLmode = false;
		int l = haystack.length();
		for (int j = 0; j < l; j++) {
			char c = haystack.charAt(j);
			switch (c) {
				case '<':
					if (((j + 1) < l) && (haystack.charAt(j + 1) != ' ')) {
						inHTMLmode = true;
					}
					break;
				case '>':
					if (inHTMLmode) {
						inHTMLmode = false;
					}
					break;
			}
			if ((c == needle.charAt(0) || Math.abs(c - needle.charAt(0)) == 32) &&
				!inHTMLmode) {
				boolean ok = true;
				if ((j + needle.length()) > l ||
					!haystack.substring(j, j + needle.length()).equalsIgnoreCase(needle)) {
					ok = false;
				}
				if (ok) {
					sb.append('\u0000');
					for (int k = 0; k < needle.length(); k++) {
						sb.append(haystack.charAt(j + k));
					}
					j = j + needle.length() - 1;
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Read a file from the file system
	 * @param file The file to read
	 * @return a Stringbuffer with the content of this file or an empty StringBuffer, if an error has occured
	 */
	public static String readFile(File file) {
		char[] buf = new char[1024];
		StringBuffer content = new StringBuffer((int)file.length());
		try {
			Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),Environment.getValue(Environment.PROP_FILE_ENCODING)));
			int numread = 0;
			while((numread=in.read(buf))!=-1) {
				content.append(buf,0,numread);
			}
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return content.toString();
	}

	/**
	 * Read a file and return its contents as a String.
	 */
	public static String readFile(String filename) throws Exception {
		StringBuffer output = new StringBuffer();
		InputStreamReader reader = null;
		try {
			File file = new File(filename);
			if (file.exists()) {
				// file passed in as full path
				reader = new FileReader(file);
			} else {
				// look for file in resource directories
				Class[] parameterTypes = null;
				Method method = Thread.class.getMethod("getContextClassLoader", parameterTypes);
				Object[] args = null;
				ClassLoader loader = (ClassLoader)method.invoke(Thread.currentThread(), args);
				InputStream stream = loader.getResourceAsStream(filename);
				if (stream == null) {
					throw new FileNotFoundException("File " + filename + " is not available for reading");
				}
				reader = new InputStreamReader(stream);
			}
			char[] buf = new char[4096];
			int c;
			while ((c = reader.read(buf, 0, buf.length)) != -1) {
				output.append(buf, 0, c);
			}
			return output.toString();
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (Exception e) {}
		}
	}

	/**
	 *
	 */
	public static String relativeDirIfNecessary(String path) {
		if (path.length() <= 2) {
			return path;
		}
		if (!path.startsWith("/") && !(Character.isLetter(path.charAt(0)) && path.charAt(1) == ':')) {
			return new File(Utilities.dir(), path).getAbsolutePath();
		}
		return path;
	}

	/**
	 *
	 */
	public static void unzip(File zipFileToOpen, File outputDirectory) {
		Enumeration entries;
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(zipFileToOpen);
			entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory()) {
					logger.debug("Extracting directory: " + entry.getName());
					// This is not robust, just for demonstration purposes.
					File file = new File(outputDirectory, entry.getName());
					file.mkdir();
				}
			}
			entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory()) {
					logger.debug("Extracting file: " + entry.getName());
					File outputFile = new File(outputDirectory, entry.getName());
					copyInputStream(zipFile.getInputStream(entry),
					new BufferedOutputStream(new FileOutputStream(outputFile)));
				}
			}
			zipFile.close();
		} catch (IOException ioe) {
			logger.error("Unzipping error: " + ioe);
			ioe.printStackTrace();
			return;
		}
	}

	/**
	 * The directory to place attachments in. This is either an absolute path if the admin setting for "upload directory"
	 * starts with a "/" or a drive letter, or it is a relative path.
	 * @param virtualWiki
	 * @param name
	 * @return
	 */
	public static File uploadPath(String virtualWiki, String name) {
		String path = Environment.getValue(Environment.PROP_ATTACH_UPLOAD_DIR);
		String dir = Utilities.relativeDirIfNecessary(path);
		if (virtualWiki == null || "".equals(virtualWiki)) {
			virtualWiki = WikiBase.DEFAULT_VWIKI;
		}
		File baseDir = new File(dir, virtualWiki);
		baseDir.mkdirs();
		dir = baseDir.getAbsolutePath();
		File uploadedFile = new File(dir, name);
		return uploadedFile;
	}
}
