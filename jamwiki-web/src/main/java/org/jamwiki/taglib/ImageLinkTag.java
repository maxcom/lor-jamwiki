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
import org.jamwiki.utils.ImageMetadata;
import org.jamwiki.utils.ImageUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * JSP tag used to build an HTML image link for a specified topic that
 * corresponds to an image that has been uploaded to the Wiki.
 */
public class ImageLinkTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageLinkTag.class.getName());
	private String allowEnlarge = null;
	private String maxHeight = null;
	private String maxWidth = null;
	private String style = null;
	private String value = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		int linkHeight = (this.maxHeight != null) ? Integer.valueOf(this.maxHeight) : -1;
		int linkWidth = (this.maxWidth != null) ? Integer.valueOf(this.maxWidth) : -1;
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String virtualWiki = retrieveVirtualWiki(request);
		String html = null;
		ImageMetadata imageMetadata = new ImageMetadata();
		imageMetadata.setMaxHeight(linkHeight);
		imageMetadata.setMaxWidth(linkWidth);
		// set the link field empty to prevent the image from being clickable
		imageMetadata.setLink("");
		if (this.allowEnlarge != null) {
			imageMetadata.setAllowEnlarge(Boolean.valueOf(this.allowEnlarge));
		}
		try {
			try {
				html = ImageUtil.buildImageLinkHtml(request.getContextPath(), virtualWiki, this.value, imageMetadata, this.style, true);
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
	public String getAllowEnlarge() {
		return this.allowEnlarge;
	}

	/**
	 *
	 */
	public void setAllowEnlarge(String allowEnlarge) {
		this.allowEnlarge = allowEnlarge;
	}

	/**
	 *
	 */
	public String getMaxHeight() {
		return this.maxHeight;
	}

	/**
	 *
	 */
	public void setMaxHeight(String maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 *
	 */
	public String getMaxWidth() {
		return this.maxWidth;
	}

	/**
	 *
	 */
	public void setMaxWidth(String maxWidth) {
		this.maxWidth = maxWidth;
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
