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
 * Utility tag for creating HTML radio boxes.
 */
public class RadioTag extends TagSupport {

	private static WikiLogger logger = WikiLogger.getLogger(RadioTag.class.getName());
	private String checked = null;
	private String id = null;
	private String name = null;
	private String onchange = null;
	private String onclick = null;
	private String style = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String output = "";
		String tagChecked = null;
		String tagId = null;
		String tagName = null;
		String tagStyle = null;
		String tagValue = null;
		// Resin throws ClassCastException with evaluateString for values like "1", so use tmp variable
		Object tmp = null;
		try {
			output += "<input type=\"radio\"";
			tmp = ExpressionEvaluationUtils.evaluate("value", this.value, pageContext);
			if (tmp != null) tagValue = tmp.toString();
			output += " value=\"" + tagValue + "\"";
			tmp = ExpressionEvaluationUtils.evaluate("name", this.name, pageContext);
			if (tmp != null) tagName = tmp.toString();
			output += " name=\"" + tagName + "\"";
			if (StringUtils.hasText(this.id)) {
				tmp = ExpressionEvaluationUtils.evaluate("id", this.id, pageContext);
				if (tmp != null) tagId = tmp.toString();
				output += " id=\"" + tagId + "\"";
			}
			if (StringUtils.hasText(this.style)) {
				tmp = ExpressionEvaluationUtils.evaluate("style", this.style, pageContext);
				if (tmp != null) tagStyle = tmp.toString();
				output += " style=\"" + tagStyle + "\"";
			}
			if (StringUtils.hasText(this.onchange)) {
				output += " onchange=\"" + this.onchange + "\"";
			}
			if (StringUtils.hasText(this.onclick)) {
				output += " onclick=\"" + this.onclick + "\"";
			}
			if (StringUtils.hasText(this.checked)) {
				tmp = ExpressionEvaluationUtils.evaluate("checked", this.checked, pageContext);
				if (tmp != null) tagChecked = tmp.toString();
				if (tagChecked.equals(tagValue)) {
					output += " checked=\"checked\"";
				}
			}
			output += " />";
			this.pageContext.getOut().print(output);
		} catch (Exception e) {
			logger.severe("Failure in radio tag for " + this.id + " / " + this.name + " / " + this.style + " / " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getChecked() {
		return this.checked;
	}

	/**
	 *
	 */
	public void setChecked(String checked) {
		this.checked = checked;
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
	public String getOnchange() {
		return this.onchange;
	}

	/**
	 *
	 */
	public void setOnchange(String onchange) {
		this.onchange = onchange;
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
