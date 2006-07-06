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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jamwiki.*;
import org.jamwiki.model.Topic;
import org.jamwiki.users.Usergroup;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This servlet generates a RSS Stream for the default wiki.
 * You can add a parameter "virutal-wiki", which then generates
 * an RSS Stream on a particular virtual wiki.<p>
 *
 * For more details on RSS see:
 * http://www.xml.com/pub/a/2002/12/18/dive-into-xml.html
 * <p>
 *
 * The code of the RSS Generator is taken from
 * JSPWiki. Author: Janne Jalkanen
 * JSPWiki is licenced under GPL.
 * For more information on JSPWiki see:
 * http://www.ecyrd.com/~jalkanen/JSPWiki/
 * <P>
 *  We use the 1.0 spec, including the wiki-specific extensions.  Wiki extensions
 *  have been defined in <A HREF="http://usemod.com/cgi-bin/mb.pl?ModWiki">UseMod:ModWiki</A>.
 */
public class RSSServlet extends HttpServlet implements Controller {

	/** Logging */
	private static final Logger logger = Logger.getLogger(RSSServlet.class);

	/**
	 *
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView next = new ModelAndView("wiki");
		rss(request, response, next);
		return null;
	}

	/**
	 * Handle post request.
	 * Generate a RSS feed and send it back as XML.
	 *
	 * @param request  The current http request
	 * @param response What the servlet will send back as response
	 */
	private void rss(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
		String topicName = JAMWikiServlet.getTopicFromRequest(request);
		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
		try {
			// get the latest pages
			int howManyDatesToGoBack = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
			if (howManyDatesToGoBack == 0) howManyDatesToGoBack = 5;
//			ChangeLog cl = WikiBase.getChangeLogInstance();
			Collection changed = new ArrayList();
//			if (cl != null) {
//				Calendar historycal = Calendar.getInstance();
//				for (int i = 0; i < howManyDatesToGoBack; i++) {
//					try {
//						Collection col = cl.getChanges(virtualWiki, historycal.getTime());
//						if (col != null) {
//							changed.addAll(col);
//						}
//					} catch (Exception e) {
//						logger.fatal("Cannot get changes", e);
//					}
//					historycal.add(Calendar.DATE, -1);
//				}
//			}
			String wikiServerHostname = Environment.getValue(Environment.PROP_BASE_SERVER_HOSTNAME);
			String baseURL = Utilities.createRootPath(request, virtualWiki, wikiServerHostname);
			// generate rss
			// --------- BEGIN CODE BY Janne Jalken ---------------
			StringBuffer result = new StringBuffer();
			SimpleDateFormat iso8601fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//
			//  Preamble
			//
			result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			result.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
				"   xmlns=\"http://purl.org/rss/1.0/\"\n" +
				"   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
				"   xmlns:wiki=\"http://purl.org/rss/1.0/modules/wiki/\">\n");
			//
			//  Channel.
			//
			result.append(" <channel rdf:about=\"" + baseURL + "\">\n");
			result.append("  <title><![CDATA[Wiki running on " + request.getServerName() + "]]></title>\n");
			// FIXME: This might fail in case the base url is not defined.
			result.append("  <link><![CDATA[").append(baseURL).append("]]></link>\n");
			result.append("  <description><![CDATA[");
			result.append("Wiki running on " + request.getServerName());
			result.append("]]></description>\n");
			//
			//  Now, list items.
			//
			//  We need two lists, which is why we gotta make a separate list if
			//  we want to do just a single pass.
			StringBuffer itemBuffer = new StringBuffer();
			result.append("  <items>\n   <rdf:Seq>\n");
			Usergroup usergroup = WikiBase.getUsergroupInstance();
			int items = 0;
			for (Iterator i = changed.iterator(); i.hasNext() && items < 15; items++) {
//				Change change = (Change) i.next();
//				topicName = change.getTopic();
//				String userid = change.getUser();
				String userid = null;
				String author = null;
				if (userid != null) {
					author = usergroup.getFullnameById(userid);
				}
				java.util.Date lastRevisionDate = WikiBase.getHandler().lastRevisionDate(virtualWiki, topicName);
				String url = baseURL + "Wiki?" + topicName;
				result.append("	<rdf:li rdf:resource=\"" + url + "\" />\n");
				itemBuffer.append(" <item rdf:about=\"" + url + "\">\n");
				itemBuffer.append("  <title><![CDATA[");
				itemBuffer.append(topicName);
				itemBuffer.append("]]></title>\n");
				itemBuffer.append("  <link><![CDATA[");
				itemBuffer.append(url);
				itemBuffer.append("]]></link>\n");
				itemBuffer.append("  <description>");
				if (author == null) author = "An unknown author";
				itemBuffer.append("Last changed by " + author + " on " + WikiBase.getHandler().lastRevisionDate(virtualWiki, topicName));
				itemBuffer.append("<p>\n<![CDATA[");
				String content = WikiBase.readRaw(virtualWiki, topicName);
				if (content.length() > 200) {
					content = content.substring(0, 197) + "...";
				}
				itemBuffer.append(content).append("\n");
				itemBuffer.append("]]></p></description>\n");
				//
				//  Modification date.
				//
				if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
					try {
						if (lastRevisionDate != null) {
							itemBuffer.append("  <dc:date>");
							Calendar cal = Calendar.getInstance();
							cal.setTime(lastRevisionDate);
							cal.add(Calendar.MILLISECOND,
								-(cal.get(Calendar.ZONE_OFFSET) +
								(cal.getTimeZone().inDaylightTime(lastRevisionDate) ? cal.get(Calendar.DST_OFFSET) : 0))
							);
							itemBuffer.append(iso8601fmt.format(cal.getTime()));
							itemBuffer.append("</dc:date>\n");
						}
					} catch (Exception e) {
						logger.warn(e);
					}
				}
				//
				//  Author.
				//
				itemBuffer.append("  <dc:contributor>\n");
				itemBuffer.append("   <rdf:Description");
				if (WikiBase.exists(virtualWiki, author)) {
					itemBuffer.append(" link=\"" + baseURL + "Wiki?" + author + "\"");
				}
				itemBuffer.append(">\n");
				itemBuffer.append("	<rdf:value>" + author + "</rdf:value>\n");
				itemBuffer.append("   </rdf:Description>\n");
				itemBuffer.append("  </dc:contributor>\n");
				//  PageHistory
				itemBuffer.append("  <wiki:history>");
				itemBuffer.append(format(baseURL + "Wiki?topic=" +
					topicName + "&action=" + JAMWikiServlet.ACTION_HISTORY + "&type=all")
				);
				itemBuffer.append("</wiki:history>\n");
				//  Close up.
				itemBuffer.append(" </item>\n");
			}
			result.append("   </rdf:Seq>\n  </items>\n");
			result.append(" </channel>\n");
			result.append(itemBuffer.toString());
			//
			//  In the end, add a search box for JSPWiki
			//
			String searchURL = baseURL + "Special:Search";
			result.append(" <textinput rdf:about=\"" + searchURL + "\">\n");
			result.append("  <title>Search</title>\n");
			result.append("  <description>Search this Wiki</description>\n");
			result.append("  <name>query</name>\n");
			result.append("  <link>" + searchURL + "</link>\n");
			result.append(" </textinput>\n");
			//
			//  Be a fine boy and close things.
			//
			result.append("</rdf:RDF>");
			// --------- END CODE BY Janne Jalken ---------------
			byte[] utf_result = result.toString().getBytes("UTF-8");
			// FIXME - try to use Spring here
			response.setContentType("text/xml; charset=UTF-8");
			response.setHeader("Expires", "0");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Keep-Alive", "timeout=15, max=100");
			response.setHeader("Connection", "Keep-Alive");
			response.setContentLength(utf_result.length);
			OutputStream out = response.getOutputStream();
			out.write(utf_result);
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	/**
	 *  Does the required formatting and entity replacement for XML.
	 * @param s The source String to format
	 * @return The String formatted
	 */
	private String format(String s) {
		s = StringUtils.replace(s, "&", "&amp;");
		s = StringUtils.replace(s, "<", "&lt;");
		s = StringUtils.replace(s, "]]>", "]]&gt;");
		return s;
	}
}
