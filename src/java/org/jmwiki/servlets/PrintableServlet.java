package org.jmwiki.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.PrintableEntry;
import org.jmwiki.WikiBase;
import org.jmwiki.PseudoTopicHandler;
import org.jmwiki.utils.Utilities;
import org.jmwiki.utils.JSPUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class PrintableServlet extends JMController implements Controller {

	private static Logger logger = Logger.getLogger(PrintableServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("printable");
		JMController.buildLayout(request, next);
		print(request, next);
		return next;
	}

	/**
	 *
	 */
	private void print(HttpServletRequest request, ModelAndView next) throws Exception {
		String topic = JMController.getTopicFromRequest(request);
		String virtualWiki = JMController.getVirtualWikiFromURI(request);
		next.addObject("topic", topic);
		next.addObject("title", topic);
		String strDepth = request.getParameter("depth");
		if (request.getParameter("hideform") != null) {
			next.addObject("hideform", "true");
		}
		int depth = 0;
		try {
			depth = Integer.parseInt(strDepth);
		} catch (NumberFormatException e1) {
			depth = 0;
		}
		next.addObject("depth", String.valueOf(depth));
		String contextPath = request.getContextPath();
		Environment.setValue(Environment.PROP_TOPIC_BASE_CONTEXT, contextPath);
		ArrayList result = new ArrayList();
		Vector alreadyVisited = new Vector();
		try {
			result.addAll(parsePage(request, virtualWiki, topic, depth, alreadyVisited));
		} catch (Exception e) {
			logger.error("Failure while creating printable page", e);
			throw new Exception("Failure while creating printable page " + e.getMessage());
		}
		// now go through all pages and replace
		// all href=/ with href=# for the
		// pages in the alreadyVisited vector
		for (Iterator iter = result.iterator(); iter.hasNext();) {
			PrintableEntry element = (PrintableEntry) iter.next();
			for (Iterator visitedIterator = alreadyVisited.iterator(); visitedIterator.hasNext();) {
				String visitedTopic = (String) visitedIterator.next();
				element.setContent(Utilities.replaceString(element.getContent(),
					"href=\"" + visitedTopic, "href=\"#" + visitedTopic)
				);
			}
		}
		// put the result in the request
		next.addObject("contentList", result);
		next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_PRINT);
	}

	/**
	 * Parse page and supages
	 * @param virtualWiki The virutal wiki to use
	 * @param topic The topic to start with
	 * @param depth The depth to go into
	 * @return Collection of pages
	 */
	private Collection parsePage(HttpServletRequest request, String virtualWiki, String topic, int depth, Vector alreadyVisited)
		throws Exception {
		WikiBase base = WikiBase.getInstance();
		String onepage = base.readCooked(request.getContextPath(), virtualWiki, topic);
		Collection result = new ArrayList();
		if (onepage != null) {
			PrintableEntry entry = new PrintableEntry();
			entry.setTopic(topic);
			entry.setContent(onepage);
			result.add(entry);
			alreadyVisited.add(topic);
			if (depth > 0) {
				String searchfor = "href=\"";
				int iPos = onepage.indexOf(searchfor);
				int iEndPos = onepage.indexOf(JMController.getMessage("topic.ismentionedon", request.getLocale()));
				if (iEndPos == -1) iEndPos = Integer.MAX_VALUE;
				while (iPos > -1 && iPos < iEndPos) {
					String link = onepage.substring(iPos + searchfor.length(),
					onepage.indexOf('"', iPos + searchfor.length()));
					if (link.indexOf('&') > -1) {
						link = link.substring(0, link.indexOf('&'));
					}
					link = JSPUtils.decodeURL(link);
					if (link.length() > 3 &&
						!link.startsWith("topic=") &&
						!link.startsWith("action=") &&
						!alreadyVisited.contains(link) &&
						!PseudoTopicHandler.getInstance().isPseudoTopic(link)) {
						result.addAll(parsePage(request, virtualWiki, link, (depth - 1), alreadyVisited));
					}
					iPos = onepage.indexOf(searchfor, iPos + 10);
				}
			}
		}
		return result;
	}
}
