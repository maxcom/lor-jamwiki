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

/**
 * This abstract class implements functionality for both the "radio" tag and
 * "checkbox" tag as they are very similar.
 *
 * @see CheckboxTag
 * @see RadioTag
 */
public abstract class AbstractButtonTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(AbstractButtonTag.class.getName());
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
	public AbstractButtonTag() {
		super();
	}

	/**
	 * Generate the tag HTML output.
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
			output += "<input type=\""+getButtonType()+"\"";
			tmp = ExpressionEvaluationUtils.evaluate("value", this.value, pageContext);
			if (tmp != null) {
				tagValue = tmp.toString();
			}
			output += " value=\"" + tagValue + "\"";
			tmp = ExpressionEvaluationUtils.evaluate("name", this.name, pageContext);
			if (tmp != null) {
				tagName = tmp.toString();
			}
			output += " name=\"" + tagName + "\"";
			if (!StringUtils.isBlank(this.id)) {
				tmp = ExpressionEvaluationUtils.evaluate("id", this.id, pageContext);
				if (tmp != null) {
					tagId = tmp.toString();
				}
				output += " id=\"" + tagId + "\"";
			}
			if (!StringUtils.isBlank(this.style)) {
				tmp = ExpressionEvaluationUtils.evaluate("style", this.style, pageContext);
				if (tmp != null) {
					tagStyle = tmp.toString();
				}
				output += " style=\"" + tagStyle + "\"";
			}
			if (!StringUtils.isBlank(this.onchange)) {
				output += " onchange=\"" + this.onchange + "\"";
			}
			if (!StringUtils.isBlank(this.onclick)) {
				output += " onclick=\"" + this.onclick + "\"";
			}
			if (!StringUtils.isBlank(this.checked)) {
				tmp = ExpressionEvaluationUtils.evaluate("checked", this.checked, pageContext);
				if (tmp != null) {
					tagChecked = tmp.toString();
				}
				if (tagChecked.equals(tagValue)) {
					output += " checked=\"checked\"";
				}
			}
			output += " />";
			this.pageContext.getOut().print(output);
		} catch (Exception e) {
			logger.severe("Failure in "+getButtonType()+" tag for " + this.id + " / " + this.name + " / " + this.style + " / " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 * Return the form tag checked value.
	 */
	public String getChecked() {
		return this.checked;
	}

	/**
	 * Set the form tag checked value.
	 */
	public void setChecked(String checked) {
		this.checked = checked;
	}

	/**
	 * Return the form tag ID value.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Set the form tag ID value.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Return the form tag name value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the form tag name value.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return the form tag onchange value.
	 */
	public String getOnchange() {
		return this.onchange;
	}

	/**
	 * Set the form tag onchange value.
	 */
	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}

	/**
	 * Return the form tag onclick value.
	 */
	public String getOnclick() {
		return this.onclick;
	}

	/**
	 * Set the form tag onclick value.
	 */
	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	/**
	 * Return the form tag CSS style value.
	 */
	public String getStyle() {
		return this.style;
	}

	/**
	 * Set the form tag CSS style value.
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * Set the form tag value (if any).
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Returns the form tag value (if any).
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the type of button, eg. "checkbox" or "radio".
	 *
	 * @return The type of button, eg. "checkbox" or "radio".
	 */
	public abstract String getButtonType() ;
}