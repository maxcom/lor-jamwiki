<% response.setLocale(request.getLocale()); %>
<%@ include file="top.jsp"%>
<%
// FIXME - this needs to be cleaned up
String action = (String)request.getAttribute(WikiServlet.PARAMETER_ACTION);
if (action == null) {
	action = request.getParameter(WikiServlet.PARAMETER_ACTION);
}
if (action == null) action = "";
%>

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
		<input type="submit" name="jumpto" value='<f:message key="generalmenu.jumpto"/>'/>
		<input type="submit" name="search" value='<f:message key="generalmenu.search"/>'/>
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
if (action.equals(WikiServlet.ACTION_ADMIN)) {
%>
		<%@ include file="admin.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_ADMIN_UPGRADE)) {
%>
		<%@ include file="adminUpgrade.jsp" %>
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
} else if (action.equals(WikiServlet.ACTION_FIRST_USE)) {
%>
		<%@ include file="firstrun.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_HISTORY)) {
%>
		<%@ include file="history.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_IMPORT)) {
%>
		<%@ include file="afterImport.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_LOCKLIST)) {
%>
		<%@ include file="locklist.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_LOGIN)) {
%>
		<%@ include file="login.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_MEMBER)) {
%>
		<%@ include file="createUser.jsp" %>
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
} else if (action.equals(WikiServlet.ACTION_ALL_TOPICS) || action.equals(WikiServlet.ACTION_TODO_TOPICS) || action.equals(WikiServlet.ACTION_ORPHANED_TOPICS)) {
%>
		<%@ include file="allTopics.jsp" %>
<%
} else if (action.equals(WikiServlet.ACTION_VIRTUAL_WIKI_LIST)) {
%>
		<%@ include file="virtualwikilist.jsp" %>
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
		<hr width="99%" />
		<c:out value="${bottomArea}" escapeXml="false"/>
		<br/>
		<font size="-3">JAMWiki Version <jamwiki:wiki-version/> |
		<a href="<jamwiki:link value="Special:Admin" />?username=admin"><f:message key="admin.title"/></a>
		</font>
		<c:if test="${not empty pageContext.request.userPrincipal}">|
		<font size="-3"><a href='Wiki?action=<%= WikiServlet.ACTION_LOGIN %>&logout=true&redirect=Wiki%3F<c:out value="${topic}"/>'><f:message key="general.logout"/></a></font>
		</c:if>
	</td>
</tr>
</table>
<%@ include file="close-document.jsp"%>
