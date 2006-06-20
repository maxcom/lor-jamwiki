/**
 *
 */
package org.jmwiki.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;
import org.jmwiki.utils.Utilities;

/**
 *
 */
public class CurrentUserTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(CurrentUserTag.class);

	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			String user = Utilities.getUserFromRequest((HttpServletRequest) this.pageContext.getRequest());
			if (user != null) {
				pageContext.setAttribute(this.var, user);
			}
		} catch (Exception e) {
			logger.warn(e);
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
