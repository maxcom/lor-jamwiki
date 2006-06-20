package org.jmwiki.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jmwiki.utils.JSPUtils;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;

/**
 *
 */
public class DecodeTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(DecodeTag.class);

	private String var;
	private String value;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			value = (String)ExpressionUtil.evalNotNull("decode", "value", value, Object.class, this, pageContext);
			try {
				value = JSPUtils.decodeURL(value);
				if (var == null) {
					this.pageContext.getOut().print(value);
				} else {
					this.pageContext.setAttribute(var, value);
				}
			} catch (Exception e) {
				logger.error("Failure while decoding value " + value);
				throw new JspException(e);
			}
			return EVAL_PAGE;
		} finally {
			// FIXME - var & value not getting reset, so explicitly call release
			release();
		}
	}

	/**
	 *
	 */
	public String getValue() {
		return this.value;
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
    public void release() {
		super.release();
		this.var = null;
		this.value = null;
    }
}
