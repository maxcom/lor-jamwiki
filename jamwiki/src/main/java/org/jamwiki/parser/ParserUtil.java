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

import java.lang.reflect.Constructor;
import javax.servlet.http.HttpServletRequest;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.ClassUtils;

/**
 * This class provides utility methods for use with the parser functions.
 */
public class ParserUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(ParserUtil.class.getName());

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
		return ParserUtil.parseMetadata(parserInput, content);
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
		AbstractParser parser = ParserUtil.parserInstance(parserInput);
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
		AbstractParser parser = ParserUtil.parserInstance(parserInput);
		return parser.parseSplice(parserDocument, topic.getTopicContent(), targetSection, replacementText);
	}
}
