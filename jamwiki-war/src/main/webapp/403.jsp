<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<%@ page import="
        org.jamwiki.authentication.JAMWikiAuthenticationConstants,
        org.jamwiki.model.VirtualWiki
    "
    errorPage="error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="defaultTopic"><%= VirtualWiki.defaultVirtualWiki().getRootTopicName() %></c:set>
<c:set var="defaultVirtualWiki"><%= VirtualWiki.defaultVirtualWiki().getName() %></c:set>
<%
String accessDeniedUri = (String)session.getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_REDIRECT_URI);
if (accessDeniedUri != null) {
	session.removeAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_REDIRECT_URI);
%>
	<c:redirect url="<%= accessDeniedUri %>" />
<%
} else {
%>
	<c:redirect url="/${defaultVirtualWiki}/${defaultTopic}" />
<%
}
%>
