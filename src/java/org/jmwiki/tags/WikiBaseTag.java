/**
 *
 */
package org.jmwiki.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jmwiki.WikiBase;

/**
 *
 */
public class WikiBaseTag extends TagSupport {

	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			this.pageContext.setAttribute(var, WikiBase.getInstance());
		} catch (Exception e) {
			throw new JspException("Getting WikiBase", e);
		}
		return SKIP_BODY;
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
