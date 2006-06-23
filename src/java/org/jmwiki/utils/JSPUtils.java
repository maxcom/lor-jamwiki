package org.jmwiki.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.io.UnsupportedEncodingException;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;

/**
 *
 */
public class JSPUtils {

	/** Logger */
	public static final Logger logger = Logger.getLogger(JSPUtils.class);
	protected static DecimalFormat decFormat = new DecimalFormat("0.00");

	/**
	 *
	 */
	public JSPUtils() {
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
	public static String decimalFormat(double number) {
		return decFormat.format(number);
	}

	/**
	 *
	 */
	public static String decodeURL(String url) {
		String charSet = Environment.getValue(Environment.PROP_FILE_ENCODING);
		if (charSet == null) charSet = "UTF-8";
		return JSPUtils.decodeURL(url, charSet);
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
		url = Utilities.replaceString(url, "_", " ");
		return url;
	}

	/**
	 * This caused problems - encoding without a charset is not well-defined
	 * behaviour, so we'll look for a default encoding. (coljac)
	 */
	public static String encodeURL(String url) {
		String charSet = Environment.getValue(Environment.PROP_FILE_ENCODING);
		if (charSet == null) charSet = "UTF-8";
		return JSPUtils.encodeURL(url, charSet);
	}

	/**
	 *
	 */
	public static String encodeURL(String url,String charSet) {
		// convert spaces to underscores
		url = Utilities.replaceString(url, " ", "_");
		try {
			url = URLEncoder.encode(url, charSet);
		} catch (UnsupportedEncodingException e) {
			logger.error("Failure while encoding url " + url + " with charset " + charSet, e);
		}
		// FIXME - un-encode colons.  handle this better.
		url = Utilities.replaceString(url, "%3A", ":");
		return url;
	}
}
