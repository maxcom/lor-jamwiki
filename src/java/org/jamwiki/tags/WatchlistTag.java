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
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.model.Watchlist;
import org.jamwiki.utils.Utilities;

/**
 *
 */
public class WatchlistTag extends BodyTagSupport {

	private static WikiLogger logger = WikiLogger.getLogger(WatchlistTag.class.getName());
	private String topic = null;

	/**
	 *
	 */
	public int doStartTag() throws JspException {
		try {
			String tagValue = evaluateTag();
			logger.info("RYAN: " + this.topic + " / " + tagValue);
			HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
			Watchlist watchlist = Utilities.currentWatchlist(request);
			if (watchlist.containsTopic(tagValue)) {
				this.pageContext.getOut().print("<strong>");
			}
		} catch (Exception e) {
			logger.severe("Failure processing watchlist item " + this.topic, e);
			throw new JspException(e);
		}
		return EVAL_BODY_INCLUDE;
	}

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			String tagValue = evaluateTag();
			logger.info("RYAN: " + this.topic + " / " + tagValue);
			HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
			Watchlist watchlist = Utilities.currentWatchlist(request);
			if (watchlist.containsTopic(tagValue)) {
				this.pageContext.getOut().print("</strong>");
			}
		} catch (Exception e) {
			logger.severe("Failure processing watchlist item " + this.topic, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private String evaluateTag() throws JspException {
		String tagValue = null;
		try {
			tagValue = ExpressionUtil.evalNotNull("watchlist", "topic", this.topic, Object.class, this, pageContext).toString();
		} catch (JspException e) {
			logger.severe("Failure in watchlist tag for " + this.topic, e);
			throw e;
		}
		return tagValue;
	}

	/**
	 *
	 */
	public String getTopic() {
		return this.topic;
	}

	/**
	 *
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}
}
