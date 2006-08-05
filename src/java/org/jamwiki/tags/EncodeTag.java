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
package org.jamwiki.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jamwiki.utils.Utilities;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;

/**
 *
 */
public class EncodeTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(EncodeTag.class);

	private String var = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String encodedValue = null;
		try {
			encodedValue = (String)ExpressionUtil.evalNotNull("encode", "value", this.value, Object.class, this, pageContext);
		} catch (JspException e) {
			logger.error("Failure in link tag for " + this.value + " / " + this.var, e);
			throw e;
		}
		try {
			encodedValue = Utilities.encodeURL(encodedValue);
			if (this.var == null) {
				this.pageContext.getOut().print(encodedValue);
			} else {
				this.pageContext.setAttribute(this.var, encodedValue);
			}
		} catch (Exception e) {
			logger.error("Failure while encoding value " + encodedValue);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 *
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 *
	 */
	public String getVar() {
		return this.var;
	}

	/**
	 *
	 */
	public void setVar(String var) {
		this.var = var;
	}
}
