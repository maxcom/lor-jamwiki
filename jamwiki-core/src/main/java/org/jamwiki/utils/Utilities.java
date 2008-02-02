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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class provides a variety of basic utility methods that are not
 * dependent on any other classes within the org.jamwiki package structure.
 */
public class Utilities {

	private static final WikiLogger logger = WikiLogger.getLogger(Utilities.class.getName());

	private static Pattern VALID_IPV4_PATTERN = null;
	private static Pattern VALID_IPV6_PATTERN = null;
	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

	static {
		try {
			VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
			VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 *
	 */
	private Utilities() {
	}

	/**
	 * Convert a string value from one encoding to another.
	 *
	 * @param text The string that is to be converted.
	 * @param fromEncoding The encoding that the string is currently encoded in.
	 * @param toEncoding The encoding that the string is to be encoded to.
	 * @return The encoded string.
	 */
	public static String convertEncoding(String text, String fromEncoding, String toEncoding) {
		if (StringUtils.isBlank(text)) {
			return text;
		}
		if (StringUtils.isBlank(fromEncoding)) {
			logger.warning("No character encoding specified to convert from, using UTF-8");
			fromEncoding = "UTF-8";
		}
		if (StringUtils.isBlank(toEncoding)) {
			logger.warning("No character encoding specified to convert to, using UTF-8");
			toEncoding = "UTF-8";
		}
		try {
			text = new String(text.getBytes(fromEncoding), toEncoding);
		} catch (Exception e) {
			// bad encoding
			logger.warning("Unable to convert value " + text + " from " + fromEncoding + " to " + toEncoding, e);
		}
		return text;
	}

	/**
	 * Decode a value that has been retrieved from a servlet request.  This
	 * method will replace any underscores with spaces.
	 *
	 * @param url The encoded value that is to be decoded.
	 * @param decodeUnderlines Set to <code>true</code> if underlines should
	 *  be automatically converted to spaces.
	 * @return A decoded value.
	 */
	public static String decodeFromRequest(String url, boolean decodeUnderlines) {
		return (decodeUnderlines) ? StringUtils.replace(url, "_", " ") : url;
	}

	/**
	 * Decode a value that has been retrieved directly from a URL or file
	 * name.  This method will URL decode the value and then replace any
	 * underscores with spaces.  Note that this method SHOULD NOT be called
	 * for values retrieved using request.getParameter(), but only values
	 * taken directly from a URL.
	 *
	 * @param url The encoded value that is to be decoded.
	 * @param decodeUnderlines Set to <code>true</code> if underlines should
	 *  be automatically converted to spaces.
	 * @return A decoded value.
	 */
	public static String decodeFromURL(String url, boolean decodeUnderlines) {
		String result = url;
		try {
			result = URLDecoder.decode(result, "UTF-8");
		} catch (Exception e) {
			logger.info("Failure while decoding url " + url + " with charset UTF-8");
		}
		return Utilities.decodeFromRequest(result, decodeUnderlines);
	}

	/**
	 * Convert a topic name or other value into a value suitable for use as a
	 * file name.  This method replaces spaces with underscores, and then URL
	 * encodes the value.
	 *
	 * @param name The value that is to be encoded for use as a file name.
	 * @return The encoded value.
	 */
	public static String encodeForFilename(String name) {
		// replace spaces with underscores
		String result = StringUtils.replace(name, " ", "_");
		// URL encode the rest of the name
		try {
			result = URLEncoder.encode(result, "UTF-8");
		} catch (Exception e) {
			logger.warning("Failure while encoding " + name + " with charset UTF-8");
		}
		return result;
	}

	/**
	 * Encode a topic name for use in a URL.  This method will replace spaces
	 * with underscores and URL encode the value, but it will not URL encode
	 * colons.
	 *
	 * @param url The topic name to be encoded for use in a URL.
	 * @return The encoded topic name value.
	 */
	public static String encodeForURL(String url) {
		String result = Utilities.encodeForFilename(url);
		// un-encode colons
		result = StringUtils.replace(result, "%3A", ":");
		// un-encode forward slashes
		result = StringUtils.replace(result, "%2F", "/");
		return result;
	}

	/**
	 * Returns any trailing period, comma, semicolon, or colon characters
	 * from the given string.  This method is useful when parsing raw HTML
	 * links, in which case trailing punctuation must be removed.
	 *
	 * @param text The text from which trailing punctuation should be returned.
	 * @return Any trailing punctuation from the given text, or an empty string
	 *  otherwise.
	 */
	public static String extractTrailingPunctuation(String text) {
		StringBuffer buffer = new StringBuffer();
		for (int i = text.length() - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (c == '.' || c == ';' || c == ',' || c == ':' || c == ')' || c == '(' || c == ']' || c == '[') {
				buffer.append(c);
			} else {
				break;
			}
		}
		if (buffer.length() == 0) {
			return "";
		}
		buffer = buffer.reverse();
		return buffer.toString();
	}

	/**
	 * Given a message key and locale return a locale-specific message.
	 *
	 * @param key The message key that corresponds to the formatted message
	 *  being retrieved.
	 * @param locale The locale for the message that is to be retrieved.
	 * @return A formatted message string that is specific to the locale.
	 */
	public static String formatMessage(String key, Locale locale) {
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
		return messages.getString(key);
	}

	/**
	 * Given a message key, locale, and formatting parameters, return a
	 * locale-specific message.
	 *
	 * @param key The message key that corresponds to the formatted message
	 *  being retrieved.
	 * @param locale The locale for the message that is to be retrieved.
	 * @param params An array of formatting parameters to use in the message
	 *  being returned.
	 * @return A formatted message string that is specific to the locale.
	 */
	public static String formatMessage(String key, Locale locale, Object[] params) {
		MessageFormat formatter = new MessageFormat("");
		formatter.setLocale(locale);
		String message = Utilities.formatMessage(key, locale);
		formatter.applyPattern(message);
		return formatter.format(params);
	}

	/**
	 * Return the current ClassLoader.  First try to get the current thread's
	 * ClassLoader, and if that fails return the ClassLoader that loaded this
	 * class instance.
	 *
	 * @return An instance of the current ClassLoader.
	 */
	private static ClassLoader getClassLoader() {
		ClassLoader loader = null;
		try {
			loader = Thread.currentThread().getContextClassLoader();
		} catch (Exception e) {
			logger.fine("Unable to retrieve thread class loader, trying default");
		}
		if (loader == null) {
			loader = Utilities.class.getClassLoader();
		}
		return loader;
	}

	/**
	 * Given a file name for a file that is located somewhere in the application
	 * classpath, return a File object representing the file.
	 *
	 * @param filename The name of the file (relative to the classpath) that is
	 *  to be retrieved.
	 * @return A file object representing the requested filename
	 * @throws Exception Thrown if the classloader can not be found or if
	 *  the file can not be found in the classpath.
	 */
	public static File getClassLoaderFile(String filename) throws Exception {
		// note that this method is used when initializing logging, so it must
		// not attempt to log anything.
		File file = null;
		ClassLoader loader = Utilities.getClassLoader();
		URL url = loader.getResource(filename);
		if (url == null) {
			url = ClassLoader.getSystemResource(filename);
		}
		if (url == null) {
			throw new Exception("Unable to find " + filename);
		}
		file = FileUtils.toFile(url);
		if (file == null || !file.exists()) {
			throw new Exception("Found invalid root class loader for file " + filename);
		}
		return file;
	}

	/**
	 * Attempt to get the class loader root directory.  This method works
	 * by searching for a file that MUST exist in the class loader root
	 * and then returning its parent directory.
	 *
	 * @return Returns a file indicating the directory of the class loader.
	 * @throws Exception Thrown if the class loader can not be found.
	 */
	public static File getClassLoaderRoot() throws Exception {
		// The file hard-coded here MUST be in the class loader directory.
		File file = Utilities.getClassLoaderFile("ApplicationResources.properties");
		if (!file.exists()) {
			throw new Exception("Unable to find class loader root");
		}
		return file.getParentFile();
	}

	/**
	 * Retrieve the webapp root.
	 *
	 * @return The default webapp root directory.
	 */
	// FIXME - there HAS to be a utility method available in Spring or some other
	// common library that offers this functionality.
	public static File getWebappRoot() throws Exception {
		// webapp root is two levels above /WEB-INF/classes/
		return Utilities.getClassLoaderRoot().getParentFile().getParentFile();
	}

	/**
	 * Utility method for determining common elements in two Map objects.
	 */
	public static Map intersect(Map map1, Map map2) {
		if (map1 == null || map2 == null) {
			throw new IllegalArgumentException("Utilities.intersection() requires non-null arguments");
		}
		Map result = new HashMap();
		Iterator keys = map1.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			if (ObjectUtils.equals(map1.get(key), map2.get(key))) {
				result.put(key, map1.get(key));
			}
		}
		return result;
	}

	/**
	 * Determine if the given string is a valid IPv4 or IPv6 address.  This method
	 * uses pattern matching to see if the given string could be a valid IP address.
	 *
	 * @param ipAddress A string that is to be examined to verify whether or not
	 *  it could be a valid IP address.
	 * @return <code>true</code> if the string is a value that is a valid IP address,
	 *  <code>false</code> otherwise.
	 */
	public static boolean isIpAddress(String ipAddress) {
		if (StringUtils.isBlank(ipAddress)) {
			return false;
		}
		Matcher m1 = Utilities.VALID_IPV4_PATTERN.matcher(ipAddress);
		if (m1.matches()) {
			return true;
		}
		Matcher m2 = Utilities.VALID_IPV6_PATTERN.matcher(ipAddress);
		return m2.matches();
	}

	/**
	 * Utility method for reading a file from a classpath directory and returning
	 * its contents as a String.
	 *
	 * @param filename The name of the file to be read, either as an absolute file
	 *  path or relative to the classpath.
	 * @return A string representation of the file contents.
	 * @throws Exception Thrown if the file cannot be found or if an I/O exception
	 *  occurs.
	 */
	public static String readFile(String filename) throws Exception {
		File file = new File(filename);
		if (file.exists()) {
			// file passed in as full path
			return FileUtils.readFileToString(file, "UTF-8");
		}
		// look for file in resource directories
		ClassLoader loader = Utilities.getClassLoader();
		URL url = loader.getResource(filename);
		file = FileUtils.toFile(url);
		if (file == null || !file.exists()) {
			throw new FileNotFoundException("File " + filename + " is not available for reading");
		}
		return FileUtils.readFileToString(file, "UTF-8");
	}
}
