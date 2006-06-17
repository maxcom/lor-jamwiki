package org.jmwiki.servlets;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jmwiki.SearchEngine;
import org.jmwiki.SearchResultEntry;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.JSPUtils;

public class SearchServlet extends JMWikiServlet {

	private static final Logger logger = Logger.getLogger(SearchServlet.class);

	/**
	 *
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", request.getLocale());
		MessageFormat formatter = new MessageFormat("");
		formatter.setLocale(request.getLocale());
		try {
			String searchField = request.getParameter("text");
			formatter.applyPattern(messages.getString("searchresult.title"));
			request.setAttribute("title", formatter.format(new Object[]{searchField}));
			// It's best to get the vwiki from the request, if it's there.
			String virtualWiki = (String) request.getAttribute("virtualWiki");
			// forward back to the search page if the request is blank or null
			if (searchField == null || "".equals(searchField)) {
				redirect("Wiki?WikiSearch", response);
				return;
			}
			// grab search engine instance and find
			boolean fuzzy = false;
			if (request.getParameter("fuzzy") != null) fuzzy = true;
			SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
			Collection results = sedb.findMultiple(virtualWiki, searchField, fuzzy);
			StringBuffer contents = new StringBuffer();
			if (results != null && results.size() > 0) {
				Iterator it = results.iterator();
				while (it.hasNext()) {
					SearchResultEntry result = (SearchResultEntry) it.next();
					contents.append("<p>");
					contents.append("<div class=\"searchresult\">");
					contents.append("<a href=\"");
					contents.append(JSPUtils.createLocalRootPath(request, virtualWiki));
					contents.append("Wiki?");
					contents.append(result.getTopic());
					if (result.getFoundWord().length() > 0) {
						contents.append("&highlight=");
						contents.append(JSPUtils.encodeURL(result.getFoundWord()));
					}
					contents.append("\">" + result.getTopic() + "</a>");
					contents.append("</div>");
					if (result.getTextBefore().length() > 0 || result.getTextAfter().length() > 0
						|| result.getFoundWord().length() > 0) {
						contents.append("<br>");
						contents.append(result.getTextBefore());
						contents.append("<a style=\"background:yellow\" href=\"");
						contents.append(JSPUtils.createLocalRootPath(request, virtualWiki));
						contents.append("Wiki?");
						contents.append(result.getTopic());
						contents.append("&highlight=");
						contents.append(JSPUtils.encodeURL(result.getFoundWord()));
						contents.append("\">");
						contents.append(result.getFoundWord());
						contents.append("</a> ");
						contents.append(result.getTextAfter());
					}
					contents.append("</p>");
				}
			} else {
				contents.append("<p>");
				formatter = new MessageFormat("");
				formatter.setLocale(request.getLocale());
				formatter.applyPattern(messages.getString("searchresult.notfound"));
				contents.append(formatter.format(new Object[]{searchField}));
				contents.append("</p>");
			}
			request.setAttribute("results", contents.toString());
			request.setAttribute("titlelink", "Wiki?WikiSearch");
			request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_SEARCH_RESULTS);
			request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
			dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
		} catch (Exception err) {
			logger.error(err);
			err.printStackTrace();
			throw new WikiServletException(err.toString());
		}
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
