/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.servlets.JAMWikiServlet;
import org.jamwiki.utils.LinkUtil;
import org.springframework.util.StringUtils;

/**
 *
 */
public class ImageLinkTag extends TagSupport {

	private static Logger logger = Logger.getLogger(ImageLinkTag.class);
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String linkValue = null;
		try {
			linkValue = (String)ExpressionUtil.evalNotNull("link", "value", this.value, Object.class, this, pageContext);
		} catch (JspException e) {
			logger.error("Image link tag evaluated empty for value " + this.value, e);
			throw e;
		}
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String virtualWiki = retrieveVirtualWiki(request);
		String html = null;
		try {
			html = LinkUtil.buildImageLinkHtml(request.getContextPath(), virtualWiki, linkValue);
			if (html != null) {
				this.pageContext.getOut().print(html);
			}
		} catch (Exception e) {
			logger.error("Failure while building url " + html + " with value " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private static String retrieveVirtualWiki(HttpServletRequest request) throws JspException {
		String virtualWiki = request.getParameter(JAMWikiServlet.PARAMETER_VIRTUAL_WIKI);
		if (virtualWiki == null) {
			virtualWiki = (String)request.getAttribute(JAMWikiServlet.PARAMETER_VIRTUAL_WIKI);
		}
		if (virtualWiki == null) {
			logger.error("No virtual wiki found for context path: " + request.getContextPath());
			throw new JspException("No virtual wiki value found");
		}
		return virtualWiki;
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
}
