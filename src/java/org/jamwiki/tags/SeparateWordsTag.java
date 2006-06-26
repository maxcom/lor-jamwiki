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
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class SeparateWordsTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(SeparateWordsTag.class);

	private String value;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		evaluateExpressions();
		JspWriter out = pageContext.getOut();
		try {
			out.print(Utilities.separateWords(value));
		} catch (IOException e) {
			logger.warn(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getValue() {
		return value;
	}

	/**
	 *
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Evaluates expressions as necessary
	 */
	private void evaluateExpressions() throws JspException {
		value = (String) ExpressionUtil.evalNotNull(
			"out", "value", value, String.class, this, pageContext
		);
	}
}
