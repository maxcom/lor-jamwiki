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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Utility tag for creating HTML text inputs.
 */
public class TextTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(TextTag.class.getName());
	private String id = null;
	private String maxlength = null;
	private String name = null;
	private String size = null;
	private String style = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String output = "";
		String tagId = null;
		int tagMaxlength = 0;
		String tagName = null;
		int tagSize = 0;
		String tagStyle = null;
		String tagValue = "";
		// Resin throws ClassCastException with evaluateString for values like "1", so use tmp variable
		Object tmp = null;
		try {
			output += "<input type=\"text\"";
			tmp = ExpressionEvaluationUtils.evaluate("name", this.name, pageContext);
			if (tmp != null) {
				tagName = tmp.toString();
			}
			output += " name=\"" + StringEscapeUtils.unescapeHtml(tagName) + "\"";
			if (!StringUtils.isBlank(this.id)) {
				tmp = ExpressionEvaluationUtils.evaluate("id", this.id, pageContext);
				if (tmp != null) {
					tagId = tmp.toString();
				}
				output += " id=\"" + tagId + "\"";
			}
			if (!StringUtils.isBlank(this.maxlength)) {
				tagMaxlength = ExpressionEvaluationUtils.evaluateInteger("maxlength", this.maxlength, pageContext);
				output += " maxlength=\"" + tagMaxlength + "\"";
			}
			if (!StringUtils.isBlank(this.size)) {
				tagSize = ExpressionEvaluationUtils.evaluateInteger("size", this.size, pageContext);
				output += " size=\"" + tagSize + "\"";
			}
			if (!StringUtils.isBlank(this.style)) {
				tmp = ExpressionEvaluationUtils.evaluate("style", this.style, pageContext);
				if (tmp != null) {
					tagStyle = tmp.toString();
				}
				output += " style=\"" + tagStyle + "\"";
			}
			if (!StringUtils.isBlank(this.value)) {
				tmp = ExpressionEvaluationUtils.evaluate("value", this.value, pageContext);
				if (tmp != null) {
					tagValue = tmp.toString();
				}
			}
			output += " value=\"" + tagValue + "\"";
			output += " />";
			this.pageContext.getOut().print(output);
		} catch (Exception e) {
			logger.severe("Failure in checkbox tag for " + this.id + " / " + this.name + " / " + this.style + " / " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getId() {
		return this.id;
	}

	/**
	 *
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 *
	 */
	public String getMaxlength() {
		return this.maxlength;
	}

	/**
	 *
	 */
	public void setMaxlength(String maxlength) {
		this.maxlength = maxlength;
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public String getSize() {
		return this.size;
	}

	/**
	 *
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 *
	 */
	public String getStyle() {
		return this.style;
	}

	/**
	 *
	 */
	public void setStyle(String style) {
		this.style = style;
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
}
