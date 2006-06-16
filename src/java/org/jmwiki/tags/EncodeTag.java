package org.jmwiki.tags;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.jmwiki.utils.JSPUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * @author garethc
 * Date: 5/03/2003
 */
public class EncodeTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(EncodeTag.class);

	private String var;
	private String value;
	private String expandedValue;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		evaluateExpressions();
		if (var == null) {
			JspWriter out = pageContext.getOut();
			try {
				out.print(JSPUtils.encodeURL(expandedValue));
			} catch (IOException e) {
				logger.warn(e);
			}
		} else {
			pageContext.setAttribute(var, JSPUtils.encodeURL(expandedValue));
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
	 *
	 */
	private void evaluateExpressions() throws JspException {
		expandedValue = (String) ExpressionUtil.evalNotNull(
			"encode", "value", value, String.class, this, pageContext
		);
	}
}
