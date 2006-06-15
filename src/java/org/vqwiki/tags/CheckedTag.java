package org.vqwiki.tags;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * @author garethc
 * Date: Feb 19, 2003
 */
public class CheckedTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(CheckedTag.class);

	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		evaluateExpressions();
		JspWriter out = pageContext.getOut();
		try {
			if ("true".equals(var)) {
				out.print("checked='true'");
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

	/**
	 *
	 */
	private void evaluateExpressions() throws JspException {
		var = (String) ExpressionUtil.evalNotNull(
			"edit-grid-row", "var", var, String.class, this, pageContext
		);
	}
}
