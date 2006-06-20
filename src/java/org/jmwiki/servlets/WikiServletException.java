/**
 *
 */
package org.jmwiki.servlets;

import javax.servlet.ServletException;
import org.apache.log4j.Logger;

/**
 *
 */
public class WikiServletException extends ServletException {

	protected static Logger logger = Logger.getLogger(WikiServletException.class);
	public final static int UNKNOWN = -1;
	public final static int TOPIC_LOCKED = 0;
	public final static int LOCK_TIMEOUT = 1;
	public final static int READ_ONLY = 2;
	protected int type;

	/**
	 *
	 */
	public WikiServletException(String s) {
		super(s);
		this.type = -1;
	}

	/**
	 *
	 */
	public WikiServletException(int type) {
		this.type = type;
	}

	/**
	 *
	 */
	public int getType() {
		return type;
	}

	/**
	 *
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 *
	 */
	public String toString() {
		return super.toString() + "\nType: " + this.type + ", " + this.getMessage();
	}
}
