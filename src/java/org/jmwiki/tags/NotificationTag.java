package org.jmwiki.tags;

import org.jmwiki.Notify;
import org.jmwiki.WikiBase;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * @author garethc
 * Date: Jan 7, 2003
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
