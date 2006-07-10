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
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;

/**
 *
 */
public class LinkTag extends BodyTagSupport {

	private static Logger logger = Logger.getLogger(LinkTag.class);
	private String value = null;
	private String text = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			try {
				this.value = (String)ExpressionUtil.evalNotNull("link", "value", this.value, Object.class, this, pageContext);
			} catch (JspException e) {
				logger.error("Failure in link tag for " + this.value + " / " + this.text, e);
				throw e;
			}
			buildLinkText();
			HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
			String url = null;
			String virtualWiki = retrieveVirtualWiki(request);
			try {
				// return raw link of the form "/wiki/en/Special:Edit"
				url = Utilities.buildWikiLink(request.getContextPath(), virtualWiki, this.value);
				if (StringUtils.hasText(this.text)) {
					// return formatted link of the form "<a href="/wiki/en/Special:Edit">text</a>"
					String css = "";
					String topic = this.value;
					int pos = topic.indexOf('?');
					if (pos > 0) {
						topic = topic.substring(0, pos).trim();
					}
					pos = topic.indexOf('#');
					if (pos > 0) {
						topic = topic.substring(0, pos).trim();
					}
					if (!WikiBase.exists(virtualWiki, topic)) {
						// FIXME - hard coding
						css = " class=\"edit\"";
					}
					url = "<a href=\"" + url + "\"" + css + ">" + this.text + "</a>";
				}
				this.pageContext.getOut().print(url);
			} catch (Exception e) {
				logger.error("Failure while building url " + url + " with value " + value);
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
	private void buildLinkText() throws JspException {
		String body = null;
		if (this.getBodyContent() != null) {
			body = this.getBodyContent().getString();
		}
		if (StringUtils.hasText(body) && StringUtils.hasText(this.text)) {
			throw new JspException("Attribute 'text' and body content may not both be specified for link tag");
		}
		if (StringUtils.hasText(this.text)) {
			this.text = (String)ExpressionUtil.evalNotNull("link", "text", text, Object.class, this, pageContext);
		}
		if (StringUtils.hasText(body)) {
			this.text = body;
		}
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
    public void release() {
		super.release();
		this.value = null;
		this.text = null;
    }
}
