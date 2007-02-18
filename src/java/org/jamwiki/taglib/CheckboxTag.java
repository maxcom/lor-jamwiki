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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * Utility tag for creating HTML checkboxes.
 */
public class CheckboxTag extends TagSupport {

	private static WikiLogger logger = WikiLogger.getLogger(CheckboxTag.class.getName());
	private String id = null;
	private String name = null;
	private String onclick = null;
	private String style = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String output = "";
		String tagId = null;
		String tagName = null;
		String tagStyle = null;
		boolean tagValue = false;
		try {
			output += "<input type=\"checkbox\" value=\"true\"";
			tagName = ExpressionEvaluationUtils.evaluateString("name", this.name, pageContext);
			output += " name=\"" + tagName + "\"";
			if (StringUtils.hasText(this.id)) {
				tagId = ExpressionEvaluationUtils.evaluateString("id", this.id, pageContext);
				output += " id=\"" + tagId + "\"";
			}
			if (StringUtils.hasText(this.style)) {
				tagStyle = ExpressionEvaluationUtils.evaluateString("style", this.style, pageContext);
				output += " style=\"" + tagStyle + "\"";
			}
			tagValue = ExpressionEvaluationUtils.evaluateBoolean("value", this.value, pageContext);
			if (tagValue) {
				output += " checked=\"checked\"";
			}
			if (StringUtils.hasText(this.onclick)) {
				output += " onclick=\"" + this.onclick + "\"";
			}
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
	public String getOnclick() {
		return this.onclick;
	}

	/**
	 *
	 */
	public void setOnclick(String onclick) {
		this.onclick = onclick;
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
