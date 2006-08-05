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
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.servlets.JAMWikiServlet;
import org.jamwiki.utils.LinkUtil;
import org.springframework.util.StringUtils;

/**
 *
 */
public class LinkTag extends BodyTagSupport {

	private static Logger logger = Logger.getLogger(LinkTag.class);
	private String style = null;
	private String text = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String tagValue = null;
		try {
			tagValue = (String)ExpressionUtil.evalNotNull("link", "value", this.value, Object.class, this, pageContext);
		} catch (JspException e) {
			logger.error("Failure in link tag for " + this.value + " / " + this.text, e);
			throw e;
		}
		String tagText = buildLinkText();
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String url = null;
		String virtualWiki = retrieveVirtualWiki(request);
		try {
			if (StringUtils.hasText(tagText)) {
				// return formatted link of the form "<a href="/wiki/en/Special:Edit">text</a>"
				url = LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki, tagValue, tagText, this.style);
			} else {
				// return raw link of the form "/wiki/en/Special:Edit"
				url = LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWiki, tagValue);
			}
			this.pageContext.getOut().print(url);
		} catch (Exception e) {
			logger.error("Failure while building url " + url + " with value " + this.value + " and text " + this.text, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private String buildLinkText() throws JspException {
		String body = null;
		String tagText = null;
		if (this.getBodyContent() != null) {
			body = this.getBodyContent().getString();
		}
		if (StringUtils.hasText(body) && StringUtils.hasText(this.text)) {
			throw new JspException("Attribute 'text' and body content may not both be specified for link tag");
		}
		if (StringUtils.hasText(this.text)) {
			tagText = (String)ExpressionUtil.evalNotNull("link", "text", this.text, Object.class, this, pageContext);
		} else if (StringUtils.hasText(body)) {
			tagText = body;
		}
		return tagText;
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
	public String getStyle() {
		return this.style;
	}

	/**
	 *
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 *
	 */
	public String getText() {
		return this.text;
	}

	/**
	 *
	 */
	public void setText(String text) {
		this.text = text;
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
