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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationCredentialsNotFoundException;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.FileUtils;
import org.jamwiki.DataHandler;
import org.jamwiki.Environment;
import org.jamwiki.UserHandler;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
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

/**
 * This class provides a variety of general utility methods for handling
 * wiki-specific functionality such as retrieving topics from the URL.
 */
public class WikiUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUtil.class.getName());

	private static Pattern INVALID_ROLE_NAME_PATTERN = null;
	private static Pattern INVALID_TOPIC_NAME_PATTERN = null;
	private static Pattern VALID_USER_LOGIN_PATTERN = null;

	static {
		try {
			INVALID_ROLE_NAME_PATTERN = Pattern.compile("([A-Za-z0-9_]+)");
			INVALID_TOPIC_NAME_PATTERN = Pattern.compile("([\\n\\r\\\\<>\\[\\]?#]+)");
			VALID_USER_LOGIN_PATTERN = Pattern.compile("([A-Za-z0-9_]+)");
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
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
	 * Retrieve the current <code>WikiUser</code> from Acegi
	 * <code>SecurityContextHolder</code>.  If the current user is not
	 * logged-in then this method will return an empty <code>WikiUser</code>
	 * object.
	 *
	 * @return The current logged-in <code>WikiUser</code>, or an empty
	 *  <code>WikiUser</code> if there is no user currently logged in.
	 *  This method will never return <code>null</code>.
	 * @throws AuthenticationCredentialsNotFoundException If authentication
	 *  credentials are unavailable.
	 */
	public static WikiUser currentUser() throws AuthenticationCredentialsNotFoundException {
		SecurityContext ctx = SecurityContextHolder.getContext();
		Authentication auth = ctx.getAuthentication();
		return WikiUser.initWikiUser(auth);
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
		WikiUser user = WikiUtil.currentUser();
		if (!user.hasRole(Role.ROLE_USER)) {
			return watchlist;
		}
		watchlist = WikiBase.getDataHandler().getWatchlist(virtualWiki, user.getUserId());
		request.getSession().setAttribute(ServletUtil.PARAMETER_WATCHLIST, watchlist);
		return watchlist;
	}

	/**
	 * Utility method to retrieve an instance of the current data handler.
	 *
	 * @return An instance of the current data handler.
	 * @throws Exception Thrown if a data handler instance can not be
	 *  instantiated.
	 */
	public static DataHandler dataHandlerInstance() throws Exception {
		// FIXME - remove this conditional after the ability to upgrade to
		// 0.5.0 is removed.
		if (Environment.getValue(Environment.PROP_DB_TYPE) == null) {
			// this is a problem, but it should never occur
			logger.warning("WikiUtil.dataHandlerInstance called without a valid PROP_DB_TYPE value");
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("ansi")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_ANSI);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("hsql")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_HSQL);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("mssql")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_MSSQL);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("mysql")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_MYSQL);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("oracle")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_ORACLE);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("postgres")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_POSTGRES);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("db2")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_DB2);
			Environment.saveProperties();
		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals("db2/400")) {
			Environment.setValue(Environment.PROP_DB_TYPE, WikiBase.DATA_HANDLER_DB2400);
			Environment.saveProperties();
		}
		String dataHandlerClass = Environment.getValue(Environment.PROP_DB_TYPE);
		logger.fine("Using data handler: " + dataHandlerClass);
		Class clazz = ClassUtils.forName(dataHandlerClass);
		Class[] parameterTypes = new Class[0];
		Constructor constructor = clazz.getConstructor(parameterTypes);
		Object[] initArgs = new Object[0];
		return (DataHandler)constructor.newInstance(initArgs);
	}

	/**
	 * Given an article name, return the appropriate comments topic article name.
	 * For example, if the article name is "Topic" then the return value is
	 * "Comments:Topic".
	 *
	 * @param name The article name from which a comments article name is to
	 *  be constructed.
	 * @return The comments article name for the article name.
	 */
	public static String extractCommentsLink(String name) throws Exception {
		if (!StringUtils.hasText(name)) {
			throw new Exception("Empty topic name " + name);
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(name);
		if (!StringUtils.hasText(wikiLink.getNamespace())) {
			return NamespaceHandler.NAMESPACE_COMMENTS + NamespaceHandler.NAMESPACE_SEPARATOR + name;
		}
		String namespace = wikiLink.getNamespace();
		String commentsNamespace = NamespaceHandler.getCommentsNamespace(namespace);
		return (StringUtils.hasText(commentsNamespace)) ? commentsNamespace + NamespaceHandler.NAMESPACE_SEPARATOR + wikiLink.getArticle() : NamespaceHandler.NAMESPACE_COMMENTS + NamespaceHandler.NAMESPACE_SEPARATOR + wikiLink.getArticle();
	}

	/**
	 * Given an article name, extract an appropriate topic article name.  For
	 * example, if the article name is "Comments:Topic" then the return value
	 * is "Topic".
	 *
	 * @param name The article name from which a topic article name is to be
	 *  constructed.
	 * @return The topic article name for the article name.
	 */
	public static String extractTopicLink(String name) throws Exception {
		if (!StringUtils.hasText(name)) {
			throw new Exception("Empty topic name " + name);
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(name);
		if (!StringUtils.hasText(wikiLink.getNamespace())) {
			return name;
		}
		String namespace = wikiLink.getNamespace();
		String mainNamespace = NamespaceHandler.getMainNamespace(namespace);
		return (StringUtils.hasText(mainNamespace)) ? mainNamespace + NamespaceHandler.NAMESPACE_SEPARATOR + wikiLink.getArticle() : wikiLink.getArticle();
	}

	/**
	 *
	 */
	public static Topic findRedirectedTopic(Topic parent, int attempts) throws Exception {
		int count = attempts;
		if (parent.getTopicType() != Topic.TYPE_REDIRECT || !StringUtils.hasText(parent.getRedirectTo())) {
			logger.severe("getRedirectTarget() called for non-redirect topic " + parent.getName());
			return parent;
		}
		// avoid infinite redirection
		count++;
		if (count > 10) {
			//TODO throw new WikiException(new WikiMessage("topic.redirect.infinite"));
		}
		// get the topic that is being redirected to
		Topic child = WikiBase.getDataHandler().lookupTopic(parent.getVirtualWiki(), parent.getRedirectTo(), false, null);
		if (child == null) {
			// child being redirected to doesn't exist, return parent
			return parent;
		}
		if (!StringUtils.hasText(child.getRedirectTo())) {
			// found a topic that is not a redirect, return
			return child;
		}
		if (WikiBase.getDataHandler().lookupTopic(child.getVirtualWiki(), child.getRedirectTo(), false, null) == null) {
			// child is a redirect, but its target does not exist
			return child;
		}
		// topic is a redirect, keep looking
		return findRedirectedTopic(child, count);
	}

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
	 * Given a topic name, determine if that name corresponds to a comments
	 * page.
	 *
	 * @param topicName The topic name (non-null) to examine to determine if it
	 *  is a comments page or not.
	 * @return <code>true</code> if the page is a comments page, <code>false</code>
	 *  otherwise.
	 */
	public static boolean isCommentsPage(String topicName) {
		WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
		if (!StringUtils.hasText(wikiLink.getNamespace())) {
			return false;
		}
		String namespace = wikiLink.getNamespace();
		if (namespace.equals(NamespaceHandler.NAMESPACE_SPECIAL)) {
			return false;
		}
		String commentNamespace = NamespaceHandler.getCommentsNamespace(namespace);
		return (namespace.equals(commentNamespace));
	}

	/**
	 * Determine if the system properties file exists and has been initialized.
	 * This method is primarily used to determine whether or not to display
	 * the system setup page or not.
	 *
	 * @return <code>true</code> if the properties file has NOT been initialized,
	 *  <code>false</code> otherwise.
	 */
	public static boolean isFirstUse() {
		return !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED);
	}

	/**
	 * Determine if the system code has been upgraded from the configured system
	 * version.  Thus if the system is upgraded, this method returns <code>true</code>
	 *
	 * @return <code>true</code> if the system has been upgraded, <code>false</code>
	 *  otherwise.
	 */
	public static boolean isUpgrade() throws Exception {
		if (WikiUtil.isFirstUse()) {
			return false;
		}
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		WikiVersion currentVersion = new WikiVersion(WikiVersion.CURRENT_WIKI_VERSION);
		return (oldVersion.before(currentVersion));
	}

	/**
	 * Using the system parser, parse system content.
	 *
	 * @param parserInput A ParserInput object that contains parser
	 *  configuration information.
	 * @param parserDocument A ParserDocument object that will hold metadata
	 *  output.  If this parameter is <code>null</code> then metadata generated
	 *  during parsing will not be available to the calling method.
	 * @param content The raw topic content that is to be parsed.
	 * @return The parsed content.
	 * @throws Exception Thrown if there are any parsing errors.
	 */
	public static String parse(ParserInput parserInput, ParserDocument parserDocument, String content) throws Exception {
		if (content == null) {
			return null;
		}
		if (parserDocument == null) {
			parserDocument = new ParserDocument();
		}
		AbstractParser parser = parserInstance(parserInput);
		return parser.parseHTML(parserDocument, content);
	}

	/**
	 * Retrieve a default ParserDocument object for a given topic name.  Note that
	 * the content has almost no parsing performed on it other than to generate
	 * parser output metadata.
	 *
	 * @param content The raw topic content.
	 * @return Returns a minimal ParserDocument object initialized primarily with
	 *  parser metadata such as links.
	 * @throws Exception Thrown if a parser error occurs.
	 */
	public static ParserDocument parserDocument(String content, String virtualWiki, String topicName) throws Exception {
		ParserInput parserInput = new ParserInput();
		parserInput.setVirtualWiki(virtualWiki);
		parserInput.setTopicName(topicName);
		parserInput.setAllowSectionEdit(false);
		return WikiUtil.parseMetadata(parserInput, content);
	}

	/**
	 * This method provides a way to parse content and set all output metadata,
	 * such as link values used by the search engine.
	 *
	 * @param parserInput A ParserInput object that contains parser configuration
	 *  information.
	 * @param content The raw topic content that is to be parsed.
	 * @return Returns a ParserDocument object with minimally parsed topic content
	 *  and other parser output fields set.
	 * @throws Exception Thrown if there are any parsing errors.
	 */
	public static ParserDocument parseMetadata(ParserInput parserInput, String content) throws Exception {
		AbstractParser parser = parserInstance(parserInput);
		ParserDocument parserDocument = new ParserDocument();
		parser.parseMetadata(parserDocument, content);
		return parserDocument;
	}

	/**
	 * Perform a bare minimum of parsing as required prior to saving a topic
	 * to the database.  In general this method will simply parse signature
	 * tags are return.
	 *
	 * @param parserInput A ParserInput object that contains parser configuration
	 *  information.
	 * @param raw The raw Wiki syntax to be converted into HTML.
	 * @return The parsed content.
	 * @throws Exception Thrown if any error occurs during parsing.
	 */
	public static String parseMinimal(ParserInput parserInput, String raw) throws Exception {
		AbstractParser parser = parserInstance(parserInput);
		return parser.parseMinimal(raw);
	}

	/**
	 * Utility method to retrieve an instance of the current system parser.
	 *
	 * @param parserInput A ParserInput object that contains parser configuration
	 *  information.
	 * @return An instance of the system parser.
	 * @throws Exception Thrown if a parser instance can not be instantiated.
	 */
	public static AbstractParser parserInstance(ParserInput parserInput) throws Exception {
		String parserClass = Environment.getValue(Environment.PROP_PARSER_CLASS);
		logger.fine("Using parser: " + parserClass);
		Class clazz = ClassUtils.forName(parserClass);
		Class[] parameterTypes = new Class[1];
		parameterTypes[0] = ClassUtils.forName("org.jamwiki.parser.ParserInput");
		Constructor constructor = clazz.getConstructor(parameterTypes);
		Object[] initArgs = new Object[1];
		initArgs[0] = parserInput;
		return (AbstractParser)constructor.newInstance(initArgs);
	}

	/**
	 * Given a topic name, return the parser-specific syntax to indicate a page
	 * redirect.
	 *
	 * @param topicName The name of the topic that is being redirected to.
	 * @return A string containing the syntax indicating a redirect.
	 * @throws Exception Thrown if a parser instance cannot be instantiated or
	 *  if any other parser error occurs.
	 */
	public static String parserRedirectContent(String topicName) throws Exception {
		AbstractParser parser = parserInstance(null);
		return parser.buildRedirectContent(topicName);
	}

	/**
	 * When editing a section of a topic, this method provides a way of slicing
	 * out a given section of the raw topic content.
	 *
	 * @param request The servlet request object.
	 * @param virtualWiki The virtual wiki for the topic being parsed.
	 * @param topicName The name of the topic being parsed.
	 * @param targetSection The section to be sliced and returned.
	 * @return Returns the raw topic content for the target section.
	 * @throws Exception Thrown if a parser error occurs.
	 */
	public static String parseSlice(HttpServletRequest request, String virtualWiki, String topicName, int section) throws Exception {
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null || topic.getTopicContent() == null) {
			return null;
		}
		ParserInput parserInput = new ParserInput();
		parserInput.setContext(request.getContextPath());
		parserInput.setLocale(request.getLocale());
		parserInput.setTopicName(topicName);
		parserInput.setVirtualWiki(virtualWiki);
		AbstractParser parser = WikiUtil.parserInstance(parserInput);
		ParserDocument parserDocument = new ParserDocument();
		return parser.parseSlice(parserDocument, topic.getTopicContent(), section);
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
		AbstractParser parser = WikiUtil.parserInstance(parserInput);
		return parser.parseSplice(parserDocument, topic.getTopicContent(), targetSection, replacementText);
	}

	/**
	 * Utility method for reading special topic values from files and returning
	 * the file contents.
	 *
	 * @param locale The locale for the user viewing the special page.
	 * @param pageName The name of the special page being retrieved.
	 */
	public static String readSpecialPage(Locale locale, String pageName) throws Exception {
		String contents = null;
		String filename = null;
		String language = null;
		String country = null;
		if (locale != null) {
			language = locale.getLanguage();
			country = locale.getCountry();
		}
		String subdirectory = "";
		if (StringUtils.hasText(language) && StringUtils.hasText(country)) {
			try {
				subdirectory = new File(WikiBase.SPECIAL_PAGE_DIR, language + "_" + country).getPath();
				filename = new File(subdirectory, Utilities.encodeForFilename(pageName) + ".txt").getPath();
				contents = Utilities.readFile(filename);
			} catch (Exception e) {
				logger.info("File " + filename + " does not exist");
			}
		}
		if (contents == null && StringUtils.hasText(language)) {
			try {
				subdirectory = new File(WikiBase.SPECIAL_PAGE_DIR, language).getPath();
				filename = new File(subdirectory, Utilities.encodeForFilename(pageName) + ".txt").getPath();
				contents = Utilities.readFile(filename);
			} catch (Exception e) {
				logger.info("File " + filename + " does not exist");
			}
		}
		if (contents == null) {
			try {
				subdirectory = new File(WikiBase.SPECIAL_PAGE_DIR).getPath();
				filename = new File(subdirectory, Utilities.encodeForFilename(pageName) + ".txt").getPath();
				contents = Utilities.readFile(filename);
			} catch (Exception e) {
				logger.warning("File " + filename + " could not be read", e);
				throw e;
			}
		}
		return contents;
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
	 * If a blacklist or whitelist of allowed file upload types is being used,
	 * retrieve the list from the properties file and return as a List object.
	 * If no such list is being used then return an empty List object.
	 *
	 * @return A list consisting of lowercase versions of all file extensions
	 *  for the whitelist/blacklist.  Entries in the list are of the form
	 *  "txt", not ".txt".
	 */
	public static List retrieveUploadFileList() {
		List list = new Vector();
		int blacklistType = Environment.getIntValue(Environment.PROP_FILE_BLACKLIST_TYPE);
		String listString = "";
		if (blacklistType == WikiBase.UPLOAD_BLACKLIST) {
			listString = Environment.getValue(Environment.PROP_FILE_BLACKLIST);
		} else if (blacklistType == WikiBase.UPLOAD_WHITELIST) {
			listString = Environment.getValue(Environment.PROP_FILE_WHITELIST);
		}
		StringTokenizer tokens = new StringTokenizer(listString, "\n\r ,.");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			if (!StringUtils.hasText(token)) {
				continue;
			}
			list.add(token.toLowerCase());
		}
		return list;
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
			user = WikiUtil.currentUser();
			if (user.getDefaultLocale() != null) {
				return Utilities.buildLocale(user.getDefaultLocale());
			}
		} catch (AuthenticationCredentialsNotFoundException e) {
			// ignore
		}
		return request.getLocale();
	}

	/**
	 * Utility method to retrieve an instance of the current user handler.
	 *
	 * @return An instance of the current user handler.
	 * @throws Exception Thrown if a user handler instance can not be
	 *  instantiated.
	 */
	public static UserHandler userHandlerInstance() throws Exception {
		String userHandlerClass = Environment.getValue(Environment.PROP_BASE_USER_HANDLER);
		logger.fine("Using user handler: " + userHandlerClass);
		Class clazz = ClassUtils.forName(userHandlerClass);
		Class[] parameterTypes = new Class[0];
		Constructor constructor = clazz.getConstructor(parameterTypes);
		Object[] initArgs = new Object[0];
		return (UserHandler)constructor.newInstance(initArgs);
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
		Matcher m = WikiUtil.INVALID_ROLE_NAME_PATTERN.matcher(role.getAuthority());
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("roles.error.name", role.getAuthority()));
		}
		if (StringUtils.hasText(role.getDescription()) && role.getDescription().length() > 200) {
			throw new WikiException(new WikiMessage("roles.error.description"));
		}
		// FIXME - throw a user-friendly error if the role name is already in use
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
			classesDir = Utilities.getClassLoaderRoot().getPath();
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
		Matcher m = WikiUtil.INVALID_TOPIC_NAME_PATTERN.matcher(name);
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
		Matcher m = WikiUtil.VALID_USER_LOGIN_PATTERN.matcher(name);
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("common.exception.name", name));
		}
	}
}
