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
package org.jmwiki.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;
import org.jmwiki.utils.Utilities;

/**
 *
 */
public class CurrentUserTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(CurrentUserTag.class);

	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			String user = Utilities.getUserFromRequest((HttpServletRequest) this.pageContext.getRequest());
			if (user != null) {
				pageContext.setAttribute(this.var, user);
			}
		} catch (Exception e) {
			logger.warn(e);
		}
		return SKIP_BODY;
	}

	/**
	 *
	 */
	public String getVar() {
		return var;
	}

	/**
	 *
	 */
	public void setVar(String var) {
		this.var = var;
	}
}
