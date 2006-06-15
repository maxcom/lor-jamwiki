package org.vqwiki.tags;

import org.apache.log4j.Logger;
import org.vqwiki.WikiBase;
import org.vqwiki.WikiMembers;
import org.vqwiki.utils.Utilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author garethc
 * Date: Jan 7, 2003
 */
public class MemberTag extends TagSupport {

    private static final Logger logger = Logger.getLogger(MemberTag.class);

    private String var;

    /**
     *
     */
    public int doEndTag() throws JspException {
        try {
            String user = Utilities.getUserFromRequest((HttpServletRequest) this.pageContext.getRequest());
            String virtualWiki = (String) pageContext.findAttribute("virtualWiki");
            WikiMembers members = WikiBase.getInstance().getWikiMembersInstance(virtualWiki);
            pageContext.setAttribute(var, members.findMemberByName(user));
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
