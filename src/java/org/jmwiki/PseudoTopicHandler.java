/**
 *
 */
package org.jmwiki;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 * Class for controlling "pseudotopics". A pseudotopic is a topic name that maps to a redirect URL rather
 * than a real Wiki topic. Examples are RecentChanges and SetUsername. The mappings of topic names
 * to redirect URLs are persisted in WEB-INF/classes/pseudotopics.properties
 * <p/>
 * Pseudotopics can also have permanent parameters associated with them by making appropriate entries in
 * pseudotopics.properties. E.g. the entries for ToDoWikiTopics looks like this:
 * <pre>
 * ToDoWikiTopics=/WEB-INF/jsp/allTopics.jsp
 * ToDoWikiTopics.param.0=todo=true
 * </pre>
 * this means that when ToDoWikiTopics is redirected to the allTopics.jsp a parameter named "todo" with
 * the value "true" is also passed. In this way more than one pseudotopic can be mapped to a single
 * redirect URL.
 * <p/>
 * Subject to LGPL
 */
public class PseudoTopicHandler {

	/** Logger */
	private static final Logger logger = Logger.getLogger(PseudoTopicHandler.class);
	/** Singleton instance */
	private static PseudoTopicHandler instance;
	/** Properties bundle to store mappings */
	private Properties mapping;
	/** Name of resource to access the persisted bundle */
	private static final String RESOURCE_NAME = "/pseudotopics.properties";

	// initialize the singleton instance
	static {
		instance = new PseudoTopicHandler();
	}

	/**
	 * Get instance
	 *
	 * @return singleton instance
	 */
	public synchronized static PseudoTopicHandler getInstance() {
		return instance;
	}

	/**
	 * Hide constructor
	 */
	private PseudoTopicHandler() {
		this.mapping = Environment.loadProperties(RESOURCE_NAME);
	}

	/**
	 * Add a mapping and persist it to the properties file. Used by the plugin manager for actions that
	 * need a pseudotopic.
	 *
	 * @param pseudoTopicName topic name
	 * @param redirectUrl	 url to redirect to
	 */
	public void addMapping(String pseudoTopicName, String redirectUrl) throws IOException {
		logger.debug("adding mapping: " + pseudoTopicName + "->'" + redirectUrl + "'");
		this.mapping.setProperty(pseudoTopicName, redirectUrl);
		Environment.saveProperties(RESOURCE_NAME, this.mapping, "pseudotopics");
	}

	/**
	 * Return a redirect URL for the given topic
	 *
	 * @param pseudotopicName topic
	 * @return redirect URL or null if no mapping exists
	 */
	public String getRedirectURL(String pseudotopicName) {
		String redirectURL = this.mapping.getProperty(pseudotopicName);
		String msg = ((redirectURL == null) ?
			"no pseudotopic redirect for " + pseudotopicName :
			"pseudo topic found for " + pseudotopicName + ": " + redirectURL
		);
		logger.debug(msg);
		return redirectURL;
	}

	/**
	 * Return true if there is a mapping for the given topic
	 *
	 * @param pseudotopicName topic
	 * @return true if mapping exists
	 */
	public boolean isPseudoTopic(String pseudotopicName) {
		return getRedirectURL(pseudotopicName) != null;
	}

	/**
	 * Add parameters defined in the mapping to the servlet request
	 *
	 * @param pseudotopicName topic name
	 * @param request		 incoming request
	 */
	public void setAttributes(String pseudotopicName, HttpServletRequest request) {
		for (int i = 0; ; i++) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(pseudotopicName);
			buffer.append(".param.");
			buffer.append(i);
			String pair = this.mapping.getProperty(buffer.toString());
			if (pair == null) {
				break;
			}
			logger.debug("setting attribute on " + pseudotopicName + ": " + pair);
			request.setAttribute(
				pair.substring(0, pair.indexOf('=')),
				pair.substring(pair.indexOf("=") - 1)
			);
		}
		if ("SetUsername".equals(pseudotopicName)) {
			try {
				request.setAttribute("userList", WikiBase.getInstance().getUsergroupInstance().getListOfAllUsers());
			} catch (Exception e) { }
		}
	}
}
