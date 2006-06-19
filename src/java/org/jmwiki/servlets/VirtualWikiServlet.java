package org.jmwiki.servlets;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;
import org.jmwiki.servlets.WikiServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class VirtualWikiServlet extends JMController implements Controller {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(TopicController.class.getName());

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JMController.buildLayout(request, next);
		list(request, next);
		return next;
	}

	/**
	 *
	 */
	private void list(HttpServletRequest request, ModelAndView next) throws Exception {
		Collection virtualWikiList = WikiBase.getInstance().getVirtualWikiList();
		next.addObject("wikis", virtualWikiList);
		// FIXME - hard coding
		next.addObject("title", "Special:VirtualWikiList");
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_VIRTUAL_WIKI_LIST);
		next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
	}
}