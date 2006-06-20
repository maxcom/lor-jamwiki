/**
 *
 */
package org.jmwiki.tags;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;

/**
 *
 */
public class WikiVersionTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(WikiVersionTag.class);

	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			if (var == null) {
				this.pageContext.getOut().print(WikiBase.WIKI_VERSION);
			} else {
				pageContext.setAttribute(var, WikiBase.WIKI_VERSION);
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
}
