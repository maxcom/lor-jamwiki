package org.vqwiki.tags;

import org.vqwiki.WikiBase;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * TODO document
 */
public class VirtualWikiListTag extends TagSupport {

    private static Logger logger = Logger.getLogger(VirtualWikiListTag.class);

    private String var;

    /**
     *
     */
    public int doEndTag() throws JspException {
        try {
            Collection virtualWikiList = null;
            virtualWikiList = WikiBase.getInstance().getVirtualWikiList();
            this.pageContext.setAttribute(var, virtualWikiList);
        } catch (Exception e) {
            logger.error("", e);
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
