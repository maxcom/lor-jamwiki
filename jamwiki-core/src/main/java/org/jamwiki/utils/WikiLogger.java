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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a wrapper around the http://www.slf4j.org/ logging
 * classes.  This framework consolidates logging from multiple sources from
 * the various libraries used in JAMWiki, each of which may be pre-configured
 * to use a different logging mechanism.
 */
public class WikiLogger {

	private final Logger logger;

	/** Log configuration property file. */
	public final static String LOG_PROPERTIES_FILENAME = "logging.properties";
	public final static String DEFAULT_LOG_FILENAME = "jamwiki.log";

	/**
	 *
	 */
	private WikiLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 *
	 */
	public static String getDefaultLogFile() {
		// by default the log configuration uses a relative path which initializes in the "user.dir" directory
		return System.getProperty("user.dir") + System.getProperty("file.separator") + DEFAULT_LOG_FILENAME;
	}

	/**
	 *
	 */
	public static String getLogConfigFile() {
		return System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "classes" + System.getProperty("file.separator") + LOG_PROPERTIES_FILENAME;
	}

	/**
	 * Retrieve a named <code>WikiLogger</code> object.
	 *
	 * @param name The name of the log object to retrieve or create.
	 * @return A logger instance for the given name.
	 */
	public static WikiLogger getLogger(String name) {
		Logger logger = LoggerFactory.getLogger(name);
		return new WikiLogger(logger);
	}

	/**
	 * Log a message at the {@link java.util.logging.Level#CONFIG} level,
	 * provided that the current log level is {@link java.util.logging.Level#CONFIG}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void config(String msg) {
		this.logger.info(msg);
	}

	/**
	 * Log a message and an exception at the {@link java.util.logging.Level#CONFIG}
	 * level, provided that the current log level is {@link java.util.logging.Level#CONFIG}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void config(String msg, Throwable thrown) {
		this.logger.info(msg, thrown);
	}

	/**
	 * Log a message at the {@link java.util.logging.Level#FINE} level,
	 * provided that the current log level is {@link java.util.logging.Level#FINE}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void fine(String msg) {
		this.logger.debug(msg);
	}

	/**
	 * Log a message and an exception at the {@link java.util.logging.Level#FINE}
	 * level, provided that the current log level is {@link java.util.logging.Level#FINE}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void fine(String msg, Throwable thrown) {
		this.logger.debug(msg, thrown);
	}

	/**
	 * Log a message at the {@link java.util.logging.Level#FINER} level,
	 * provided that the current log level is {@link java.util.logging.Level#FINER}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void finer(String msg) {
		this.logger.trace(msg);
	}

	/**
	 * Log a message and an exception at the {@link java.util.logging.Level#FINER}
	 * level, provided that the current log level is {@link java.util.logging.Level#FINER}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void finer(String msg, Throwable thrown) {
		this.logger.trace(msg, thrown);
	}

	/**
	 * Log a message at the {@link java.util.logging.Level#INFO} level,
	 * provided that the current log level is {@link java.util.logging.Level#INFO}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void info(String msg) {
		this.logger.info(msg);
	}

	/**
	 * Log a message and an exception at the {@link java.util.logging.Level#INFO}
	 * level, provided that the current log level is {@link java.util.logging.Level#INFO}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void info(String msg, Throwable thrown) {
		this.logger.info(msg, thrown);
	}

	/**
	 * Return <code>true</code> if a log message of level CONFIG can be logged.
	 */
	public boolean isConfigEnabled() {
		return this.logger.isInfoEnabled();
	}

	/**
	 * Return <code>true</code> if a log message of level FINE can be logged.
	 */
	public boolean isFineEnabled() {
		return this.logger.isDebugEnabled();
	}

	/**
	 * Return <code>true</code> if a log message of level FINER can be logged.
	 */
	public boolean isFinerEnabled() {
		return this.logger.isTraceEnabled();
	}

	/**
	 * Return <code>true</code> if a log message of level INFO can be logged.
	 */
	public boolean isInfoEnabled() {
		return this.logger.isInfoEnabled();
	}

	/**
	 * Log a message at the {@link java.util.logging.Level#SEVERE} level,
	 * provided that the current log level is {@link java.util.logging.Level#SEVERE}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void severe(String msg) {
		this.logger.error(msg);
	}

	/**
	 * Log a message and an exception at the {@link java.util.logging.Level#SEVERE}
	 * level, provided that the current log level is {@link java.util.logging.Level#SEVERE}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void severe(String msg, Throwable thrown) {
		this.logger.error(msg, thrown);
	}

	/**
	 * Log a message at the {@link java.util.logging.Level#WARNING} level,
	 * provided that the current log level is {@link java.util.logging.Level#WARNING}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void warning(String msg) {
		this.logger.warn(msg);
	}

	/**
	 * Log a message and an exception at the {@link java.util.logging.Level#WARNING}
	 * level, provided that the current log level is {@link java.util.logging.Level#WARNING}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void warning(String msg, Throwable thrown) {
		this.logger.warn(msg, thrown);
	}
}
