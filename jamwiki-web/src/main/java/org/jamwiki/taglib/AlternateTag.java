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
package org.jamwiki.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility tag for alternating between two values.  This tag takes as
 * parameters two values, and each time it is invoke the return value
 * alternates between the two supplied values.  It is most useful for
 * functions such as alternating the background color of table rows.
 */
public class AlternateTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(AlternateTag.class.getName());
	private static final String ATTRIBUTE_ROOT_NAME = "org.jamwiki.taglib.AlternateTag";
	private String value1 = null;
	private String value2 = null;
	private String attributeName = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String tagAttributeName = "default";
		if (!StringUtils.isBlank(this.attributeName)) {
			tagAttributeName = this.attributeName;
		}
		tagAttributeName = ATTRIBUTE_ROOT_NAME + "." + tagAttributeName;
		// check the request for a value.
		String previousValue = (String)this.pageContext.getRequest().getAttribute(tagAttributeName);
		String output = "";
		if (previousValue == null || previousValue.equals(this.value2)) {
			output = this.value1;
			this.pageContext.getRequest().setAttribute(tagAttributeName, this.value1);
		} else {
			output = this.value2;
			this.pageContext.getRequest().setAttribute(tagAttributeName, this.value2);
		}
		try {
			this.pageContext.getOut().print(output);
		} catch (IOException e) {
			logger.severe("Failure in alternate tag for " + this.value1 + " / " + this.value2 + " / " + this.attributeName, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 * The attributeName option is used when two or more separate page objects
	 * need to alternate background colors independently.  When a attributeName
	 * is specified all calls using the same attributeName will alternate
	 * values independently of other calls to this tag.
	 */
	public String getAttributeName() {
		return this.attributeName;
	}

	/**
	 *
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 *
	 */
	public String getValue1() {
		return this.value1;
	}

	/**
	 *
	 */
	public void setValue1(String value1) {
		this.value1 = value1;
	}

	/**
	 *
	 */
	public String getValue2() {
		return this.value2;
	}

	/**
	 *
	 */
	public void setValue2(String value2) {
		this.value2 = value2;
	}
}
