package org.jamwiki.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;
import org.acegisecurity.AuthenticationCredentialsNotFoundException;
import org.apache.commons.io.FileUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.db.DatabaseConnection;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.servlets.ServletUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

public class WikiUtil {
	private static final WikiLogger logger = WikiLogger.getLogger(WikiUtil.class.getName());

	/**
	 * Retrieve a topic name from the servlet request.  This method will
	 * retrieve a request parameter matching the PARAMETER_TOPIC value,
	 * and will decode it appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded topic name retrieved from the request.
	 */
	public static String getTopicFromRequest(HttpServletRequest request) throws Exception {
		String topic = null;
		if (request.getMethod().equalsIgnoreCase("GET")) {
			// parameters passed via the URL and URL encoded, so request.getParameter may
			// not interpret non-ASCII characters properly.  This code attempts to work
			// around that issue by manually decoding.  yes, this is ugly and it would be
			// great if someone could eventually make it unnecessary.
			String query = request.getQueryString();
			if (!StringUtils.hasText(query)) {
				return null;
			}
			String prefix = ServletUtil.PARAMETER_TOPIC + "=";
			int pos = query.indexOf(prefix);
			if (pos != -1 && (pos + prefix.length()) < query.length()) {
				topic = query.substring(pos + prefix.length());
				if (topic.indexOf("&") != -1) {
					topic = topic.substring(0, topic.indexOf("&"));
				}
			}
			return Utilities.decodeFromURL(topic);
		}
		topic = request.getParameter(ServletUtil.PARAMETER_TOPIC);
		if (topic == null) {
			topic = (String)request.getAttribute(ServletUtil.PARAMETER_TOPIC);
		}
		if (topic == null) {
			return null;
		}
		return Utilities.decodeFromRequest(topic);
	}

	/**
	 * Retrieve a topic name from the request URI.  This method will retrieve
	 * the portion of the URI that follows the virtual wiki and decode it
	 * appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded topic name retrieved from the URI.
	 */
	public static String getTopicFromURI(HttpServletRequest request) {
		// skip one directory, which is the virutal wiki
		String topic = retrieveDirectoriesFromURI(request, 1);
		if (topic == null) {
			logger.warning("No topic in URL: " + request.getRequestURI());
			return null;
		}
		int pos = topic.indexOf('?');
		if (pos != -1) {
			// strip everything after and including '?'
			if (pos == 0) {
				logger.warning("No topic in URL: " + request.getRequestURI());
				return null;
			}
			topic = topic.substring(0, topic.indexOf('?'));
		}
		pos = topic.indexOf('#');
		if (pos != -1) {
			// strip everything after and including '#'
			if (pos == 0) {
				logger.warning("No topic in URL: " + request.getRequestURI());
				return null;
			}
			topic = topic.substring(0, topic.indexOf('#'));
		}
		topic = Utilities.decodeFromURL(topic);
		return topic;
	}

	/**
	 * Create a pagination object based on parameters found in the current
	 * request.
	 *
	 * @param request The servlet request object.
	 * @param next A ModelAndView object corresponding to the page being
	 *  constructed.
	 * @return A Pagination object constructed from parameters found in the
	 *  request object.
	 */
	public static Pagination buildPagination(HttpServletRequest request, ModelAndView next) {
		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_NUM);
		if (request.getParameter("num") != null) {
			try {
				num = new Integer(request.getParameter("num")).intValue();
			} catch (Exception e) {
				// invalid number
			}
		}
		int offset = 0;
		if (request.getParameter("offset") != null) {
			try {
				offset = new Integer(request.getParameter("offset")).intValue();
			} catch (Exception e) {
				// invalid number
			}
		}
		if (next != null) {
			next.addObject("num", new Integer(num));
			next.addObject("offset", new Integer(offset));
		}
		return new Pagination(num, offset);
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
		ClassLoader loader = ClassUtils.getDefaultClassLoader();
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
	 * Retrieve a virtual wiki name from the request URI.  This method will
	 * retrieve the portion of the URI that immediately follows the servlet
	 * context and decode it appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded virtual wiki name retrieved from the URI.
	 */
	public static String getVirtualWikiFromURI(HttpServletRequest request) {
		String uri = retrieveDirectoriesFromURI(request, 0);
		if (uri == null) {
			logger.warning("No virtual wiki found in URL: " + request.getRequestURI());
			return null;
		}
		int slashIndex = uri.indexOf('/');
		if (slashIndex == -1) {
			logger.warning("No virtual wiki found in URL: " + request.getRequestURI());
			return null;
		}
		String virtualWiki = uri.substring(0, slashIndex);
		return Utilities.decodeFromURL(virtualWiki);
	}

	/**
	 * Retrieve a virtual wiki name from the servlet request.  This method
	 * will retrieve a request parameter matching the PARAMETER_VIRTUAL_WIKI
	 * value, and will decode it appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded virtual wiki name retrieved from the request.
	 */
	public static String getVirtualWikiFromRequest(HttpServletRequest request) {
		String virtualWiki = request.getParameter(ServletUtil.PARAMETER_VIRTUAL_WIKI);
		if (virtualWiki == null) {
			virtualWiki = (String)request.getAttribute(ServletUtil.PARAMETER_VIRTUAL_WIKI);
		}
		if (virtualWiki == null) {
			return null;
		}
		return Utilities.decodeFromRequest(virtualWiki);
	}

	/**
	 * Retrieve the current logged-in user's watchlist from the session.  If
	 * there is no watchlist return an empty watchlist.
	 *
	 * @param request The servlet request object.
	 * @param virtualWiki The virtual wiki for the watchlist being parsed.
	 * @return The current logged-in user's watchlist, or an empty watchlist
	 *  if there is no watchlist in the session.
	 */
	public static Watchlist currentWatchlist(HttpServletRequest request, String virtualWiki) throws Exception {
		// get watchlist stored in session
		Watchlist watchlist = (Watchlist)request.getSession().getAttribute(ServletUtil.PARAMETER_WATCHLIST);
		if (watchlist != null) {
			return watchlist;
		}
		// no watchlist in session, retrieve from database
		watchlist = new Watchlist();
		WikiUser user = Utilities.currentUser();
		if (!user.hasRole(Role.ROLE_USER)) {
			return watchlist;
		}
		watchlist = WikiBase.getDataHandler().getWatchlist(virtualWiki, user.getUserId());
		request.getSession().setAttribute(ServletUtil.PARAMETER_WATCHLIST, watchlist);
		return watchlist;
	}

	/**
	 * When editing a section of a topic, this method provides a way of splicing
	 * an edited section back into the raw topic content.
	 *
	 * @param parserDocument A ParserDocument object containing parser
	 *  metadata output.
	 * @param request The servlet request object.
	 * @param virtualWiki The virtual wiki for the topic being parsed.
	 * @param topicName The name of the topic being parsed.
	 * @param targetSection The section to be sliced and returned.
	 * @param replacementText The edited content that is to be spliced back into
	 *  the raw topic.
	 * @return The raw topic content including the new replacement text.
	 * @throws Exception Thrown if a parser error occurs.
	 */
	public static String parseSplice(ParserDocument parserDocument, HttpServletRequest request, String virtualWiki, String topicName, int targetSection, String replacementText) throws Exception {
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null || topic.getTopicContent() == null) {
			return null;
		}
		ParserInput parserInput = new ParserInput();
		parserInput.setContext(request.getContextPath());
		parserInput.setLocale(request.getLocale());
		parserInput.setTopicName(topicName);
		parserInput.setVirtualWiki(virtualWiki);
		AbstractParser parser = Utilities.parserInstance(parserInput);
		return parser.parseSplice(parserDocument, topic.getTopicContent(), targetSection, replacementText);
	}

	public static String parseSlice(HttpServletRequest request,
			String virtualWiki, String topicName, int section) throws Exception {
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null || topic.getTopicContent() == null) {
			return null;
		}
		ParserInput parserInput = new ParserInput();
		parserInput.setContext(request.getContextPath());
		parserInput.setLocale(request.getLocale());
		parserInput.setTopicName(topicName);
		parserInput.setVirtualWiki(virtualWiki);
		AbstractParser parser = Utilities.parserInstance(parserInput);
		ParserDocument parserDocument = new ParserDocument();
		return parser.parseSlice(parserDocument, topic.getTopicContent(), section);
	}

	/**
	 * Utility method for retrieving values from the URI.  This method
	 * will attempt to properly convert the URI encoding, and then offers a way
	 * to return directories after the initial context directory.  For example,
	 * if the URI is "/context/first/second/third" and this method is called
	 * with a skipCount of 1, the return value is "second/third".
	 *
	 * @param request The servlet request object.
	 * @param skipCount The number of directories to skip.
	 * @return A UTF-8 encoded portion of the URL that skips the web application
	 *  context and skipCount directories, or <code>null</code> if the number of
	 *  directories is less than skipCount.
	 */
	private static String retrieveDirectoriesFromURI(HttpServletRequest request, int skipCount) {
		String uri = request.getRequestURI().trim();
		// FIXME - needs testing on other platforms
		uri = Utilities.convertEncoding(uri, "ISO-8859-1", "UTF-8");
		String contextPath = request.getContextPath().trim();
		if (!StringUtils.hasText(uri) || contextPath == null) {
			return null;
		}
		uri = uri.substring(contextPath.length() + 1);
		int i = 0;
		while (i < skipCount) {
			int slashIndex = uri.indexOf('/');
			if (slashIndex == -1) {
				return null;
			}
			uri = uri.substring(slashIndex + 1);
			i++;
		}
		return uri;
	}

	/**
	 * Users can specify a default locale in their preferences, so determine
	 * if the current user is logged-in and has chosen a locale.  If not, use
	 * the default locale from the request object.
	 *
	 * @param request The request object for the HTTP request.
	 * @return Either the user's default locale (for logged-in users) or the
	 *  locale specified in the request if no default locale is available.
	 */
	public static Locale retrieveUserLocale(HttpServletRequest request) {
		WikiUser user = null;
		try {
			user = Utilities.currentUser();
			if (user.getDefaultLocale() != null) {
				return Utilities.buildLocale(user.getDefaultLocale());
			}
		} catch (AuthenticationCredentialsNotFoundException e) {
			// ignore
		}
		return request.getLocale();
	}

	/**
	 * Validate that vital system properties, such as database connection settings,
	 * have been specified properly.
	 *
	 * @param props The property object to validate against.
	 * @return A Vector of WikiMessage objects containing any errors encountered,
	 *  or an empty Vector if no errors are encountered.
	 */
	public static Vector validateSystemSettings(Properties props) {
		Vector errors = new Vector();
		// test directory permissions & existence
		WikiMessage baseDirError = WikiUtil.validateDirectory(props.getProperty(Environment.PROP_BASE_FILE_DIR));
		if (baseDirError != null) {
			errors.add(baseDirError);
		}
		WikiMessage fullDirError = WikiUtil.validateDirectory(props.getProperty(Environment.PROP_FILE_DIR_FULL_PATH));
		if (fullDirError != null) {
			errors.add(fullDirError);
		}
		String classesDir = null;
		try {
			classesDir = WikiUtil.getClassLoaderRoot().getPath();
			WikiMessage classesDirError = WikiUtil.validateDirectory(classesDir);
			if (classesDirError != null) {
				errors.add(classesDirError);
			}
		} catch (Exception e) {
			errors.add(new WikiMessage("error.directorywrite", classesDir, e.getMessage()));
		}
		// test database
		String driver = props.getProperty(Environment.PROP_DB_DRIVER);
		String url = props.getProperty(Environment.PROP_DB_URL);
		String userName = props.getProperty(Environment.PROP_DB_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
		try {
			DatabaseConnection.testDatabase(driver, url, userName, password, false);
		} catch (Exception e) {
			logger.severe("Invalid database settings", e);
			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
		}
		// verify valid parser class
		boolean validParser = true;
		String parserClass = props.getProperty(Environment.PROP_PARSER_CLASS);
		String abstractParserClass = "org.jamwiki.parser.AbstractParser";
		if (parserClass == null || parserClass.equals(abstractParserClass)) {
			validParser = false;
		}
		try {
			Class parent = ClassUtils.forName(parserClass);
			Class child = ClassUtils.forName(abstractParserClass);
			if (!child.isAssignableFrom(parent)) {
				validParser = false;
			}
		} catch (Exception e) {
			validParser = false;
		}
		if (!validParser) {
			errors.add(new WikiMessage("error.parserclass", parserClass));
		}
		return errors;
	}

	/**
	 * Verify that a directory exists and is writable.
	 *
	 * @param name The full name (including the path) for the directory being tested.
	 * @return A WikiMessage object containing any error encountered, otherwise
	 *  <code>null</code>.
	 */
	public static WikiMessage validateDirectory(String name) {
		File directory = new File(name);
		if (!directory.exists() || !directory.isDirectory()) {
			return new WikiMessage("error.directoryinvalid", name);
		}
		String filename = "jamwiki-test-" + System.currentTimeMillis() + ".txt";
		File file = new File(name, filename);
		String text = "Testing";
		String read = null;
		try {
			// attempt to write a temp file to the directory
			FileUtils.writeStringToFile(file, text, "UTF-8");
		} catch (Exception e) {
			return new WikiMessage("error.directorywrite", name, e.getMessage());
		}
		try {
			// verify that the file was correctly written
			read = FileUtils.readFileToString(file, "UTF-8");
			if (read == null || !text.equals(read)) {
				throw new IOException();
			}
		} catch (Exception e) {
			return new WikiMessage("error.directoryread", name, e.getMessage());
		}
		try {
			// attempt to delete the file
			FileUtils.forceDelete(file);
		} catch (Exception e) {
			return new WikiMessage("error.directorydelete", name, e.getMessage());
		}
		return null;
	}

	/**
	 * Utility method for determining if the parameters of a Role are valid
	 * or not.
	 *
	 * @param role The Role to validate.
	 * @throws WikiException Thrown if the role is invalid.
	 */
	public static void validateRole(Role role) throws WikiException {
		Matcher m = Utilities.INVALID_ROLE_NAME_PATTERN.matcher(role.getAuthority());
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("roles.error.name", role.getAuthority()));
		}
		if (StringUtils.hasText(role.getDescription()) && role.getDescription().length() > 200) {
			throw new WikiException(new WikiMessage("roles.error.description"));
		}
		// FIXME - throw a user-friendly error if the role name is already in use
	}

	/**
	 * Utility method for determining if a topic name is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.
	 *
	 * @param name The topic name to validate.
	 * @throws WikiException Thrown if the user name is invalid.
	 */
	public static void validateTopicName(String name) throws WikiException {
		if (!StringUtils.hasText(name)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		if (PseudoTopicHandler.isPseudoTopic(name)) {
			throw new WikiException(new WikiMessage("common.exception.pseudotopic", name));
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(name);
		String namespace = wikiLink.getNamespace();
		if (namespace != null && namespace.toLowerCase().trim().equals(NamespaceHandler.NAMESPACE_SPECIAL.toLowerCase())) {
			throw new WikiException(new WikiMessage("common.exception.name", name));
		}
		Matcher m = Utilities.INVALID_TOPIC_NAME_PATTERN.matcher(name);
		if (m.find()) {
			throw new WikiException(new WikiMessage("common.exception.name", name));
		}
	}

	/**
	 * Utility method for determining if a username is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.
	 *
	 * @param name The username to validate.
	 * @throws WikiException Thrown if the user name is invalid.
	 */
	public static void validateUserName(String name) throws WikiException {
		if (!StringUtils.hasText(name)) {
			throw new WikiException(new WikiMessage("error.loginempty"));
		}
		Matcher m = Utilities.VALID_USER_LOGIN_PATTERN.matcher(name);
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("common.exception.name", name));
		}
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
		File file = WikiUtil.getClassLoaderFile("ApplicationResources.properties");
		if (!file.exists()) {
			throw new Exception("Unable to find class loader root");
		}
		return file.getParentFile();
	}
}
