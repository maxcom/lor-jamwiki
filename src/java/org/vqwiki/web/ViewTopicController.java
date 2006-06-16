/**
 * Copyright 2006 - Martijn van der Kleijn.
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
 *
 */
package org.vqwiki.web;

import java.util.Map;
import java.util.HashMap;
import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.*;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import org.vqwiki.WikiBase;
import org.vqwiki.Topic;

/**
 * The <code>ViewTopicController</code> servlet is the servlet which allows users to view topics and
 * makes sure that responses are dispatched to the appropriate views.
 *
 * This controller uses methods from the WikiBase class to parse the URI and determine which virtual wiki
 * and which topic are requested. The URI should be formed as below:
 *
 * www.somesite.com/<context-root>/<virtualwiki>/<action>/<topic>.html
 */
public class ViewTopicController implements Controller {

	/** Logger for this class and subclasses. */
	private static Logger logger = Logger.getLogger(ViewTopicController.class.getName());

	/**
	 * This method handles the request after its parent class receives control. It gets the topic's name and the
	 * virtual wiki name from the uri, loads the topic and returns a view to the end user.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public final ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		//This method should parse uri, loadTopic, return view
		WikiBase wikibase = WikiBase.getInstance();
		String topicname = wikibase.getTopicFromURI(request.getRequestURI());

		Topic topic = new Topic(topicname);
		topic.loadTopic(wikibase.getVirtualWikiFromURI(request.getRequestURI(), request.getContextPath()));

		Map topicModel = new HashMap();
		topicModel.put("topicname", topic.getName());

		// convert the rawcontent to html content
		String contents = WikiBase.getInstance().cook(new BufferedReader(new StringReader(topic.getRenderedContent())), wikibase.getVirtualWikiFromURI(request.getRequestURI(), request.getContextPath()), false);

		topicModel.put("topiccontent", contents);

		logger.debug("Handling request for view of topic: " + topic);
		return new ModelAndView("viewTopic", "model", topicModel);
	}
}