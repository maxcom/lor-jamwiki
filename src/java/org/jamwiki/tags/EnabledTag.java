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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.jamwiki.Environment;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class EnabledTag extends BodyTagSupport {

	private static WikiLogger logger = WikiLogger.getLogger(EnabledTag.class.getName());
	private String property = null;

	/**
	 *
	 */
	public int doStartTag() throws JspException {
		try {
			String propertyName = (String)Environment.class.getField(this.property).get(null);
			logger.info("RYAN: propertyName is " + propertyName + " / value is " + Environment.getBooleanValue(propertyName));
			if (Environment.getBooleanValue(propertyName)) {
				return EVAL_BODY_INCLUDE;
			} else {
				return SKIP_BODY;
			}
		} catch (Exception e) {
			logger.severe("Failure in enabled tag for " + this.property, e);
			throw new JspException(e);
		}
	}

	/**
	 *
	 */
	public String getProperty() {
		return this.property;
	}

	/**
	 *
	 */
	public void setProperty(String property) {
		this.property = property;
	}
}
