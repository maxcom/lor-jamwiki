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


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;
import org.jamwiki.Notify;
import org.jamwiki.WikiBase;

/**
 *
 */
public class NotificationTag extends TagSupport {

	private static final Logger logger = Logger.getLogger(NotificationTag.class);

	private String var; // set to true if user is subscribed to topic
	private String userVar;
	private String topicVar;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		Notify notifier = null;
		String virtualWiki = (String) this.pageContext.findAttribute("virtualWiki");
		String topic = (String) this.pageContext.findAttribute(topicVar);
		String user = (String) this.pageContext.findAttribute(userVar);
		try {
			notifier = WikiBase.getInstance().getNotifyInstance(virtualWiki, topic);
		} catch (Exception e) {
			throw new JspException(e);
		}
		try {
			pageContext.setAttribute(var, new Boolean(notifier.isMember(user)));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getVar() {
		return var;
	}

	/**
	 *
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 *
	 */
	public String getUserVar() {
		return userVar;
	}

	/**
	 *
	 */
	public void setUserVar(String userVar) {
		this.userVar = userVar;
	}

	/**
	 *
	 */
	public String getTopicVar() {
		return topicVar;
	}

	/**
	 *
	 */
	public void setTopicVar(String topicVar) {
		this.topicVar = topicVar;
	}
}
