package org.jmwiki.tags;

import org.apache.log4j.Logger;
import org.jmwiki.utils.Utilities;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * @author garethc
 * Date: 3/03/2003
 */
public class ResourceTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(ResourceTag.class);

	private String key;
	private String var;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String value = Utilities.resource(key, pageContext.getRequest().getLocale());
		if (var == null) {
			JspWriter writer = pageContext.getOut();
			try {
				writer.print(value);
			} catch (IOException e) {
				logger.warn(e);
			}
		} else {
			pageContext.setAttribute(var, value);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getKey() {
		return key;
	}

	/**
	 *
	 */
	public void setKey(String key) {
		this.key = key;
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
