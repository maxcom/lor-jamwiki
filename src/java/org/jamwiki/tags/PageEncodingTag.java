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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.tags;

import org.jamwiki.Environment;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

/**
 * By Colin Jacobs, coljac@coljac.net
 * Date: Aug 23, 2005
 * (c) 2005
 */
public class PageEncodingTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(CurrentUserTag.class);

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String enc = Environment.getValue(Environment.PROP_BASE_FORCE_ENCODING);
		if (enc != null) {
			try {
				pageContext.getRequest().setCharacterEncoding(enc);
				pageContext.getResponse().setContentType("text/html;charset=" + enc);
			} catch (UnsupportedEncodingException e) {
				logger.warn("Unsupported encoding: " + enc);
				// Do nothing
			}
		}
		return EVAL_PAGE;
	}
}
