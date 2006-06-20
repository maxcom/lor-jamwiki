/**
 *
 */
package org.jmwiki;

import org.apache.log4j.Logger;

/**
 *
 */
public class WikiException extends java.lang.Exception {

	protected static Logger logger = Logger.getLogger(WikiException.class);
	public final static int UNKNOWN = -1;
	public final static int TOPIC_LOCKED = 0;
	public final static int LOCK_TIMEOUT = 1;
	public final static int READ_ONLY = 2;
	protected int type;
	protected String message;

	/**
	 *
	 */
	public WikiException(String s) {
		super(s);
		this.type = -1;
	}

	/**
	 *
	 */
	public WikiException(int type) {
		this.type = type;
		switch (type) {
			case TOPIC_LOCKED:
				this.message = "The topic is already locked for editing";
				break;
			case LOCK_TIMEOUT:
				this.message = "Your lock has timed out and is now held by someone else";
				break;
			case READ_ONLY:
				this.message = "The topic is read-only";
				break;
			default:
				this.message = "General error, see logs for details";
		}
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
	public String getMessage() {
		return message;
	}

	/**
	 *
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
