/**
 * Copyright 2004, Gareth Cronin
 * User: garethc
 * Date: 6/06/2004
 * Time: 09:19:14
 */
package org.vqwiki;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Manager that maintains bindings between logical action names and classes that implement {@link WikiAction}
 * The action manager can delegate requests to appropriate actions.
 */
public class ActionManager {

	/** Logger */
	public static final Logger logger = Logger.getLogger(ActionManager.class);
	/** Mapping of action names to classes */
	private Properties mapping;
	/** Singleton instance */
	private static ActionManager instance;
	/** Name of the resource that is the mapping file to persist to */
	private static final String MAPPING_PROPERTIES_FILE = "/actions.properties";

	// initialize the singleton instance
	static {
		instance = new ActionManager();
	}

	/**
	 * Get singleton instance
	 *
	 * @return singleton instance
	 */
	public static ActionManager getInstance() {
		return instance;
	}

	/**
	 * Hide constructor
	 */
	private ActionManager() {
		this.mapping = Environment.loadProperties(MAPPING_PROPERTIES_FILE);
	}

	/**
	 * Return an instance of the named action
	 *
	 * @param actionName name
	 * @return instance or null if there is no mapping for the named action
	 * @throws ClassNotFoundException if the mapped class can't be found
	 * @throws IllegalAccessException on a security problem
	 * @throws InstantiationException if the mapped class can't be instantiated
	 */
	public WikiAction getActionInstance(String actionName) throws ClassNotFoundException, IllegalAccessException,
		InstantiationException {
		logger.debug("getting action instance: " + actionName);
		String className = this.mapping.getProperty(actionName);
		if (className == null) {
			return null;
		}
		Class clazz = Class.forName(className);
		return (WikiAction) clazz.newInstance();
	}

	/**
	 * Add a mapping
	 *
	 * @param actionName action name
	 * @param className  class implementing {@link WikiAction}
	 * @throws IOException on any error persisting the mapping
	 */
	public void addMapping(String actionName, String className) throws IOException {
		logger.debug("adding action mapping: " + actionName + "->" + className);
		this.mapping.setProperty(actionName, className);
		Environment.saveProperties(MAPPING_PROPERTIES_FILE, this.mapping, "action");
	}

	/**
	 * Return whether a mapping exists for the named action
	 *
	 * @param actionName action name
	 * @return true if it exists
	 */
	public boolean actionExists(String actionName) {
		boolean result = this.mapping.containsKey(actionName);
		logger.debug("action exists: '" + actionName + "' ?" + result);
		return result;
	}
}
