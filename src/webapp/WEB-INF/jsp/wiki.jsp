<%--
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
		<form method="POST" action="Wiki">
		<input type="hidden" name="<%= WikiServlet.PARAMETER_ACTION %>" value="<%= WikiServlet.ACTION_MENU_JUMP %>"/>
		<input name="text" size="20" value="" />
		<br />
		<input type="submit" name="jumpto" value='<f:message key="generalmenu.jumpto"/>'/>
		<input type="submit" name="search" value='<f:message key="generalmenu.search"/>'/>
		</form>
		</div>
	</td>
	<td class="main-content">
		<jmwiki:encode var="encodedTitle" value='"${title}"'/>
		<%@ include file="top-menu.jsp"%>
		<div id="contents" >
		<%@ include file="navbar-virtual-wiki.jsp"%>
		<%@ include file="navbar-history-list.jsp"%>
		<div id="contents-header"><c:out value="${title}"/></div>
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
<tr>
	<td colspan="2" class="footer">
<%
if (Utilities.emailAvailable()) {
%>
		<%@ include file="member-contents.jsp"%>
<%
}
%>
		<hr/>
	</td>
</tr>
</table>
<%@ include file="close-document.jsp"%>
