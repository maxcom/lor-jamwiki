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
package org.jamwiki.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 */
public class DiffServlet extends JAMController implements Controller {

	private static Logger logger = Logger.getLogger(DiffServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		JAMController.buildLayout(request, next);
		diff(request, next);
		return next;
	}

	/**
	 *
	 */
	protected void diff(HttpServletRequest request, ModelAndView next) throws Exception {
		String virtualWiki = JAMController.getVirtualWikiFromURI(request);
		String topicName = JAMController.getTopicFromRequest(request);
		next.addObject(JAMController.PARAMETER_TITLE, "Diff " + topicName);
		next.addObject(JAMController.PARAMETER_TOPIC, topicName);
		try {
			String diffType = request.getParameter("type");
			if (diffType != null && diffType.equals("arbitrary")) {
				int firstVersion = -1;
				int secondVersion = -1;
				Enumeration e = request.getParameterNames();
				while (e.hasMoreElements()) {
					String name = (String) e.nextElement();
					if (name.startsWith("diff:")) {
						int version = Integer.parseInt(name.substring(name.indexOf(":") + 1));
						if (firstVersion >= 0) {
							secondVersion = version;
						} else {
							firstVersion = version;
						}
					}
				}
				if (firstVersion == -1 || secondVersion == -1) {
					next.addObject("badinput", "true");
				} else {
					String diff = WikiBase.getInstance().getHandler().diff(virtualWiki, topicName, Math.max(firstVersion, secondVersion), Math.min(firstVersion, secondVersion), true);
					next.addObject("diff", diff);
				}
			} else {
				int topicVersionId1 = new Integer(request.getParameter("version1")).intValue();
				int topicVersionId2 = new Integer(request.getParameter("version2")).intValue();
				String diff = WikiBase.getInstance().getHandler().diff(virtualWiki, topicName, topicVersionId1, topicVersionId2, true);
				next.addObject("diff", diff);
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		next.addObject(JAMController.PARAMETER_ACTION, JAMController.ACTION_DIFF);
	}
}
