<%--
Java MediaWiki - WikiWikiWeb clone
Copyright (C) 2001-2003 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<% response.setLocale(request.getLocale()); %>
<%@ include file="top.jsp"%>
<c:out value="${topArea}" escapeXml="false"/>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <td>
      <div class="navbar">
        <%@ include file="navbar-virtual-wiki.jsp"%>
        <%@ include file="navbar-history-list.jsp"%>
        &nbsp; <!-- to render the bar even when empty -->
      </div>
    </td>
  </tr>
</table>
 <jmwiki:encode var="encodedTitle" value='"${title}"'/>
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
      <td nowrap class="leftMenu" valign="top" width="10%">
        <c:out value="${leftMenu}" escapeXml="false"/>
      </td>
    </c:if>
    <td valign="top">
      <%@ include file="top-menu.jsp"%>
        <div class="contents" >
          <span class="pageHeader"><c:out value="${title}"/></span><p/>
<%
// FIXME - this needs to be cleaned up
String action = (String)request.getAttribute("action");
if (action == null) action = "";
if (action.equals(WikiServlet.ACTION_ADMIN)) {
%>
		<%@ include file="admin.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_ATTACH)) {
%>
		<%@ include file="attach.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_DIFF)) {
%>
		<%@ include file="diff.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_EDIT) || action.equals(WikiServlet.ACTION_PREVIEW)) {
%>
		<%@ include file="edit.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_EDIT_USER)) {
%>
		<%@ include file="createUser.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_HISTORY)) {
%>
		<%@ include file="history.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_LOCKLIST)) {
%>
		<%@ include file="locklist.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_LOGIN)) {
%>
		<%@ include file="login.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_RECENT_CHANGES)) {
%>
		<%@ include file="recentChanges.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_SEARCH)) {
%>
		<%@ include file="search.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_SEARCH_RESULTS)) {
%>
		<%@ include file="searchResults.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_TODO_TOPICS) || action.equals(WikiServlet.ACTION_ORPHANED_TOPICS)) {
%>
		<%@ include file="allTopics.jsp" %>
<%
} else {
%>
		<div id="content-article"><c:out value="${contents}" escapeXml="false"/></div>
<%
}
%>
        </div>
    </td>
  </tr>
</table>
<%
if (Utilities.emailAvailable()) {
%>
  <%@ include file="member-contents.jsp"%>
<%
}
%>
<hr/>
<%@ include file="close-document.jsp"%>