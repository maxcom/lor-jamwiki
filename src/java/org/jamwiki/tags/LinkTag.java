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
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.servlets.JAMWikiServlet;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.springframework.util.StringUtils;

/**
 *
 */
public class LinkTag extends BodyTagSupport {

	private static WikiLogger logger = WikiLogger.getLogger(LinkTag.class.getName());
	private String style = null;
	private String text = null;
	private String value = null;
	private String queryParams = "";

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String tagValue = null;
		try {
			tagValue = ExpressionUtil.evalNotNull("link", "value", this.value, Object.class, this, pageContext).toString();
		} catch (JspException e) {
			logger.severe("Failure in link tag for " + this.value + " / " + this.text, e);
			throw e;
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(tagValue);
		String tagText = buildLinkText();
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String url = null;
		String virtualWiki = retrieveVirtualWiki(request);
		if (StringUtils.hasText(this.queryParams)) {
			wikiLink.setQuery(this.queryParams);
		}
		try {
			if (StringUtils.hasText(tagText)) {
				// return formatted link of the form "<a href="/wiki/en/Special:Edit">text</a>"
				url = LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki, wikiLink, tagText, this.style, true);
			} else {
				// return raw link of the form "/wiki/en/Special:Edit"
				url = LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWiki, wikiLink);
			}
			this.pageContext.getOut().print(url);
		} catch (Exception e) {
			logger.severe("Failure while building url " + url + " with value " + this.value + " and text " + this.text, e);
			throw new JspException(e);
		} finally {
			this.queryParams = "";
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	protected void addQueryParam(String key, String value) throws JspException {
		if (!StringUtils.hasText(key)) {
			throw new JspException("linkParam key value cannot be empty");
		}
		if (!StringUtils.hasText(this.queryParams)) {
			this.queryParams = "?";
		} else {
			this.queryParams += "&amp;";
		}
		this.queryParams += Utilities.encodeForURL(key);
		this.queryParams += "=";
		if (StringUtils.hasText(value)) {
			this.queryParams += Utilities.encodeForURL(value);
		}
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
			tagText = ExpressionUtil.evalNotNull("link", "text", this.text, Object.class, this, pageContext).toString();
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
			logger.severe("No virtual wiki found for context path: " + request.getContextPath());
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
