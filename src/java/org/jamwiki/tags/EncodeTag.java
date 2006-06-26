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

	private String var;
	private String value;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			value = (String)ExpressionUtil.evalNotNull("encode", "value", value, Object.class, this, pageContext);
			try {
				value = Utilities.encodeURL(value);
				if (var == null) {
					this.pageContext.getOut().print(value);
				} else {
					this.pageContext.setAttribute(var, value);
				}
			} catch (Exception e) {
				logger.error("Failure while encoding value " + value);
				throw new JspException(e);
			}
			return EVAL_PAGE;
		} finally {
			// FIXME - var & value not getting reset, so explicitly call release
			release();
		}
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
		return var;
	}

	/**
	 *
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 *
	 */
    public void release() {
		super.release();
		this.var = null;
		this.value = null;
    }
}
