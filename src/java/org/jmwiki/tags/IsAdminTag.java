package org.jmwiki.tags;

import org.jmwiki.utils.Utilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author garethc
 * Date: 7/03/2003
 */
public class IsAdminTag extends TagSupport {

	String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		pageContext.setAttribute(var, new Boolean(Utilities.isAdmin(request)));
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
}
