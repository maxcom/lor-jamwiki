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
package org.jamwiki.authentication;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * This class is a hack implemented to work around the fact that the default
 * Acegi classes can only redirect to a single, hard-coded URL.  Due to the
 * fact that JAMWiki may have multiple virtual wikis this class overrides some
 * of the default Acegi behavior to allow additional flexibility.  Hopefully
 * future versions of Acegi will add additional flexibility and this class
 * can be removed.
 */
public class JAMWikiAuthenticationProcessingFilter extends AuthenticationProcessingFilter {

	/** Standard logger. */
	private static WikiLogger logger = WikiLogger.getLogger(JAMWikiAuthenticationProcessingFilter.class.getName());

	/**
	 * FIXME - override the parent method to determine if processing should
	 * occur.  Needed due to the fact that different virtual wikis may be
	 * used.
	 */
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI();
		int pathParamIndex = uri.indexOf(';');
		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}
		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
		return uri.endsWith(request.getContextPath() + "/" + virtualWiki + this.getFilterProcessesUrl());
	}

	/**
	 * Allow subclasses to modify the redirection message.
	 *
	 * @param request the request
	 * @param response the response
	 * @param url the URL to redirect to
	 * @throws IOException in the event of any failure
	 */
	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			String virtualWiki = Utilities.getVirtualWikiFromURI(request);
			url = request.getContextPath() + "/" + virtualWiki + url;
		}
		response.sendRedirect(response.encodeRedirectURL(url));
	}
}
