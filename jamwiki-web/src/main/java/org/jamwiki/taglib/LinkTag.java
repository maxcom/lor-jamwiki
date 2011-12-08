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
package org.jamwiki.taglib;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * JSP tag that creates an HTML link to a Wiki topic, generating the servlet
 * context and virtual wiki in the link and also properly encoding the topic
 * for use in the URL.
 */
public class LinkTag extends BodyTagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(LinkTag.class.getName());
	private String escape = null;
	private String style = null;
	private String target = null;
	private String text = null;
	private String value = null;
	private String virtualWiki = null;
	private String queryParams = "";

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String tagTarget = null;
		if (!StringUtils.isBlank(this.target)) {
			tagTarget = this.target;
		}
                if (this.value.startsWith("Global:")) {
                  try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("<a href=\"");
                    builder.append(this.value.substring(7)); // crop "Global:"
                    builder.append("\">");
                    builder.append(buildLinkText());
                    builder.append("</a>");
                    this.pageContext.getOut().print(builder.toString());
                  } catch (IOException e) {
                    logger.error("Failure while output " + this.value);
                    throw new JspException(e);
                  }
                  return EVAL_PAGE;
                }
		String tagText = buildLinkText();
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String url = null;
		String tagVirtualWiki = (StringUtils.isBlank(this.virtualWiki)) ? WikiUtil.getVirtualWikiFromRequest(request) : this.virtualWiki;
		if (StringUtils.isBlank(tagVirtualWiki)) {
			tagVirtualWiki = VirtualWiki.defaultVirtualWiki().getName();
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(tagVirtualWiki, this.value);
		if (!StringUtils.isBlank(this.queryParams)) {
			wikiLink.setQuery(this.queryParams);
		}
		try {
			if (!StringUtils.isBlank(tagText)) {
				boolean tagEscape = (!StringUtils.equalsIgnoreCase(this.escape, "false"));
				// return formatted link of the form "<a href="/wiki/en/Special:Edit">text</a>"
				url = LinkUtil.buildInternalLinkHtml(request.getContextPath(), tagVirtualWiki, wikiLink, tagText, this.style, tagTarget, tagEscape);
			} else {
				// return raw link of the form "/wiki/en/Special:Edit"
				url = LinkUtil.buildTopicUrl(request.getContextPath(), tagVirtualWiki, wikiLink);
			}
			this.pageContext.getOut().print(url);
		} catch (DataAccessException e) {
			logger.error("Failure while building url " + url + " with value " + this.value + " and text " + this.text, e);
			throw new JspException(e);
		} catch (IOException e) {
			logger.error("Failure while building url " + url + " with value " + this.value + " and text " + this.text, e);
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
		if (StringUtils.isBlank(key)) {
			throw new JspException("linkParam key value cannot be empty");
		}
		this.queryParams = LinkUtil.appendQueryParam(this.queryParams, key, value);
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
		if (!StringUtils.isBlank(body) && !StringUtils.isBlank(this.text)) {
			throw new JspException("Attribute 'text' and body content may not both be specified for link tag");
		}
		if (!StringUtils.isBlank(this.text)) {
			tagText = this.text;
		} else if (!StringUtils.isBlank(body)) {
			tagText = body;
		}
		return tagText;
	}

	/**
	 * Parameter indicating whether to escape HTML for the tag text.  Defaults to
	 * true unless this value is explicitly set to false.
	 */
	public String getEscape() {
		return this.escape;
	}

	/**
	 *
	 */
	public void setEscape(String escape) {
		this.escape = escape;
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
	public String getTarget() {
		return this.target;
	}

	/**
	 *
	 */
	public void setTarget(String target) {
		this.target = target;
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

	/**
	 *
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}
}
