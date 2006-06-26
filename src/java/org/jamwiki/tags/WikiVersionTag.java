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

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;

/**
 *
 */
public class WikiVersionTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(WikiVersionTag.class);

	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			if (var == null) {
				this.pageContext.getOut().print(WikiBase.WIKI_VERSION);
			} else {
				pageContext.setAttribute(var, WikiBase.WIKI_VERSION);
			}
		} catch (IOException e) {
			logger.warn(e);
		}
		return EVAL_PAGE;
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
