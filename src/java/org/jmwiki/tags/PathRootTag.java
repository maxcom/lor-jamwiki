package org.jmwiki.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * @author garethc
 * Date: Jan 7, 2003
 */
public class PathRootTag extends TagSupport {

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(((HttpServletRequest) this.pageContext.getRequest()).getContextPath());
		buffer.append("/");
		buffer.append(this.pageContext.findAttribute("virtual-wiki"));
		try {
			this.pageContext.getOut().print(buffer.toString());
		} catch (IOException e) {
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}
}
