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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
