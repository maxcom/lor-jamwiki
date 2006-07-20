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
        org.jamwiki.Environment,
        org.jamwiki.WikiBase,
        org.jamwiki.servlets.JAMWikiServlet,
        org.jamwiki.utils.Utilities
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=UTF-8"
%>

<%@ include file="page-init.jsp" %>

<%
// FIXME - cleanup
if (Utilities.isFirstUse() && !action.equals(JAMWikiServlet.ACTION_SETUP)) {
      // Websphere seems to choke on quotation marks in a jsp:forward, so define a variable
      String firstUseUrl = "/" + WikiBase.DEFAULT_VWIKI + "/Special:Setup";
%>
      <jsp:forward page="<%= firstUseUrl %>" />
<%
}
%>

<%@ include file="top.jsp" %>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
	<td class="navigation">
		<div id="logo">
		<c:out value="${topArea}" escapeXml="false"/>
		</div>
		<br />
		<c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
		<div id="nav-menu">
		<c:out value="${leftMenu}" escapeXml="false" />
		</div>
		</c:if>
		<div id="nav-search">
		<form method="POST" action="<jamwiki:link value="Special:Search" />">
		<input type="text" name="text" size="20" value="" />
		<br />
		<input type="submit" name="search" value='<f:message key="generalmenu.search"/>'/>
		<input type="submit" name="jumpto" value='<f:message key="generalmenu.jumpto"/>'/>
		</form>
		</div>
	</td>
	<td class="main-content">
		<div id="user-menu"><%@ include file="user-menu.jsp"%></div>
		<%@ include file="top-menu.jsp"%>
		<div id="contents" >
		<%@ include file="navbar-virtual-wiki.jsp"%>
		<%@ include file="navbar-history-list.jsp"%>
		<div id="contents-header"><c:out value="${title}"/></div>
<%
if (action.equals(JAMWikiServlet.ACTION_ADMIN)) {
%>
		<jsp:include page="admin.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_ADMIN_DELETE)) {
%>
		<jsp:include page="admin-delete.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_ADMIN_CONVERT)) {
%>
		<jsp:include page="admin-convert.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_CONTRIBUTIONS)) {
%>
		<jsp:include page="contributions.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_DIFF)) {
%>
		<jsp:include page="diff.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_EDIT) || action.equals(JAMWikiServlet.ACTION_PREVIEW) || action.equals(JAMWikiServlet.ACTION_EDIT_RESOLVE)) {
%>
		<jsp:include page="edit.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_EDIT_USER)) {
%>
		<jsp:include page="register.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_ERROR)) {
%>
		<jsp:include page="error-display.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_HISTORY)) {
%>
		<jsp:include page="history.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_LOGIN)) {
%>
		<jsp:include page="login.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_RECENT_CHANGES)) {
%>
		<jsp:include page="recent-changes.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_REGISTER)) {
%>
		<jsp:include page="register.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_SEARCH)) {
%>
		<jsp:include page="search.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_SEARCH_RESULTS)) {
%>
		<jsp:include page="search-results.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_SETUP)) {
%>
		<jsp:include page="setup.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_ALL_TOPICS) || action.equals(JAMWikiServlet.ACTION_TODO_TOPICS) || action.equals(JAMWikiServlet.ACTION_ORPHANED_TOPICS)) {
%>
		<jsp:include page="all-topics.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_UPLOAD)) {
%>
		<jsp:include page="upload.jsp" flush="true" />
<%
} else if (action.equals(JAMWikiServlet.ACTION_VIRTUAL_WIKI_LIST)) {
%>
		<jsp:include page="virtualwikilist.jsp" flush="true" />
<%
} else {
%>
		<%@ include file="view-topic-include.jsp" %>
<%
}
%>
		</div>
	</td>
</tr>
<tr>
	<td colspan="2" class="footer">
		<hr width="99%" />
		<c:out value="${bottomArea}" escapeXml="false" />
		<br/>
		<font size="-3"><a href="http://jamwiki.org/">JAMWiki</a> Version <jamwiki:wiki-version/></font>
	</td>
</tr>
</table>
<%@ include file="close-document.jsp"%>
