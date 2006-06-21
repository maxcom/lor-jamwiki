package org.jmwiki.servlets;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class LockListServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(LockListServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		if (isTopic(request, "Special:Unlock")) {
			unlock(request, next);
		} else {
			lockList(request, next);
		}
		return next;
	}

	/**
	 *
	 */
	private void lockList(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JMController.getTopicFromRequest(request);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		List locks = null;
		try {
			locks = WikiBase.getInstance().getHandler().getLockList(virtualWiki);
		} catch (Exception e) {
			logger.error("Error retrieving lock list", e);
			// FIXME - hard coding
			throw new Exception("Error retrieving lock list " + e.getMessage());
		}
		next.addObject("locks", locks);
		next.addObject(JMController.PARAMETER_TITLE, JMController.getMessage("locklist.title", request.getLocale()));
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOCKLIST);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}

	/**
	 *
	 */
	private void unlock(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JMController.getTopicFromRequest(request);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		if (!Utilities.isAdmin(request)) {
			String redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, "Special:LockList");
			next.addObject("redirect", redirect);
			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
			next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			return;
		}
		try {
			WikiBase.getInstance().unlockTopic(virtualWiki, topic);
		} catch (Exception e) {
			logger.error("Failure while unlocking " + topic, e);
			// FIXME - hard coding
			throw new Exception("Failure while unlocking " + topic + ": " + e.getMessage());
		}
		lockList(request, next);
	}
}
