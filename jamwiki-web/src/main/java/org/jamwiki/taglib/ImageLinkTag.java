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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jamwiki.DataAccessException;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.WikiUtil;

/**
 * JSP tag used to build an HTML image link for a specified topic that
 * corresponds to an image that has been uploaded to the Wiki.
 */
public class ImageLinkTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageLinkTag.class.getName());
	private String maxDimension = null;
	private String style = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		int linkDimension = -1;
		if (this.maxDimension != null) {
			linkDimension = Integer.valueOf(this.maxDimension);
		}
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String virtualWiki = retrieveVirtualWiki(request);
		String html = null;
		try {
			try {
				html = LinkUtil.buildImageLinkHtml(request.getContextPath(), virtualWiki, this.value, false, false, null, null, linkDimension, true, this.style, true);
			} catch (DataAccessException e) {
				logger.severe("Failure while building url " + html + " with value " + this.value, e);
				throw new JspException(e);
			}
			if (html != null) {
				this.pageContext.getOut().print(html);
			}
		} catch (IOException e) {
			logger.severe("Failure while building url " + html + " with value " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private static String retrieveVirtualWiki(HttpServletRequest request) throws JspException {
		String virtualWiki = WikiUtil.getVirtualWikiFromRequest(request);
		if (virtualWiki == null) {
			logger.severe("No virtual wiki found for context path: " + request.getContextPath());
			throw new JspException("No virtual wiki value found");
		}
		return virtualWiki;
	}

	/**
	 *
	 */
	public String getMaxDimension() {
		return this.maxDimension;
	}

	/**
	 *
	 */
	public void setMaxDimension(String maxDimension) {
		this.maxDimension = maxDimension;
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
