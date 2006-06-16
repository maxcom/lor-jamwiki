package org.jmwiki.tags;

import org.jmwiki.Environment;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

/**
 * By Colin Jacobs, coljac@coljac.net
 * Date: Aug 23, 2005
 * (c) 2005
 */
public class PageEncodingTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(CurrentUserTag.class);

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String enc = Environment.getValue(Environment.PROP_BASE_FORCE_ENCODING);
		if (enc != null) {
			try {
				pageContext.getRequest().setCharacterEncoding(enc);
				pageContext.getResponse().setContentType("text/html;charset=" + enc);
			} catch (UnsupportedEncodingException e) {
				logger.warn("Unsupported encoding: " + enc);
				// Do nothing
			}
		}
		return EVAL_PAGE;
	}
}
