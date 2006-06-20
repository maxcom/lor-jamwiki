/**
 *
 */
package org.jmwiki.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jmwiki.utils.Utilities;

/**
 *
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
