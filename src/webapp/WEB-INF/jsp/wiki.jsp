<%@ page import="
    org.jamwiki.Environment,
    org.jamwiki.WikiBase,
    org.jamwiki.servlets.JAMWikiServlet,
    org.jamwiki.utils.Utilities
" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jamwiki.tld" prefix="jamwiki" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="f" %>
<jamwiki:setPageEncoding />
<% response.setLocale(request.getLocale()); %>
<%@ include file="top.jsp"%>
<%
// FIXME - this needs to be cleaned up
String action = (String)request.getAttribute(JAMWikiServlet.PARAMETER_ACTION);
if (action == null) {
	action = request.getParameter(JAMWikiServlet.PARAMETER_ACTION);
}
if (action == null) action = "";
if (Utilities.isFirstUse() && !action.equals(JAMWikiServlet.ACTION_SETUP)) {
      // Websphere seems to choke on quotation marks in a jsp:forward, so define a variable
      String firstUseUrl = "/" + WikiBase.DEFAULT_VWIKI + "/Special:Setup";
%>
      <jsp:forward page="<%= firstUseUrl %>" />
<%
}
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
if (action.equals(JAMWikiServlet.ACTION_ADMIN)) {
%>
		<%@ include file="admin.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_ADMIN_DELETE)) {
%>
		<%@ include file="adminDelete.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_ADMIN_UPGRADE)) {
%>
		<%@ include file="adminUpgrade.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_ATTACH)) {
%>
		<%@ include file="attach.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_DIFF)) {
%>
		<%@ include file="diff.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_EDIT) || action.equals(JAMWikiServlet.ACTION_PREVIEW)) {
%>
		<%@ include file="edit.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_EDIT_USER)) {
%>
		<%@ include file="register.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_HISTORY)) {
%>
		<%@ include file="history.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_LOCKLIST)) {
%>
		<%@ include file="locklist.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_LOGIN)) {
%>
		<%@ include file="login.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_RECENT_CHANGES)) {
%>
		<%@ include file="recentChanges.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_REGISTER)) {
%>
		<%@ include file="register.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_SEARCH)) {
%>
		<%@ include file="search.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_SEARCH_RESULTS)) {
%>
		<%@ include file="searchResults.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_SETUP)) {
%>
		<%@ include file="setup.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_ALL_TOPICS) || action.equals(JAMWikiServlet.ACTION_TODO_TOPICS) || action.equals(JAMWikiServlet.ACTION_ORPHANED_TOPICS)) {
%>
		<%@ include file="allTopics.jsp" %>
<%
} else if (action.equals(JAMWikiServlet.ACTION_VIRTUAL_WIKI_LIST)) {
%>
		<%@ include file="virtualwikilist.jsp" %>
<%
} else {
%>
		<div id="content-article"><c:out value="${contents}" escapeXml="false" /></div>
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
		<font size="-3"><a href="http://jamwiki.org/">JAMWiki</a> Version <jamwiki:wiki-version/> |
		<a href="<jamwiki:link value="Special:Admin" />"><f:message key="admin.title" /></a>
		</font>
	</td>
</tr>
</table>
<%@ include file="close-document.jsp"%>
