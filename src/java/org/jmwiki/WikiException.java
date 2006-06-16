/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jmwiki;

import org.apache.log4j.Logger;

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
