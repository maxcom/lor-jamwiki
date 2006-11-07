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
        org.jamwiki.utils.Utilities
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<%@ include file="top.jsp" %>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
	<td class="navigation">
		<div id="logo">
		<%-- FIXME - need image width and height --%>
		<a class="logo" href="<jamwiki:link value="${defaultTopic}" />"><img border="0" src="../images/<c:out value="${logo}" />" alt="" /></a>
		</div>
		<br />
		<c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
		<div id="nav-menu">
		<c:out value="${leftMenu}" escapeXml="false" />
		</div>
		</c:if>
		<div id="nav-search">
		<form method="post" action="<jamwiki:link value="Special:Search" />">
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
		<div id="contents-header"><f:message key="${pageInfo.pageTitle.key}"><f:param value="${pageInfo.pageTitle.params[0]}" /></f:message></div>
		<c:if test="${!empty pageInfo.redirectName}">
		<c:set var="redirectUrl"><jamwiki:link value="${pageInfo.redirectName}"><jamwiki:linkParam key="redirect" value="no" /><c:out value="${pageInfo.redirectName}" /></jamwiki:link></c:set>
		<div id="contents-subheader"><f:message key="topic.redirect.from"><f:param value="${redirectUrl}" /></f:message></div>
		</c:if>
<c:choose>
	<c:when test="${pageInfo.actionAdmin}">
		<jsp:include page="admin.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionAdminConvert}">
		<jsp:include page="admin-convert.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionAdminManage}">
		<jsp:include page="admin-manage.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionAdminTranslation}">
		<jsp:include page="admin-translation.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionAllPages}">
		<jsp:include page="all-pages.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionCategories}">
		<jsp:include page="categories.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionContributions}">
		<jsp:include page="contributions.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionDiff}">
		<jsp:include page="diff.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionEdit || pageInfo.actionEditPreview || pageInfo.actionEditResolve}">
		<jsp:include page="edit.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionError}">
		<jsp:include page="error-display.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionHistory}">
		<jsp:include page="history.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionImport}">
		<jsp:include page="import.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionLinkTo}">
		<jsp:include page="linkto.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionLogin}">
		<jsp:include page="login.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionMove}">
		<jsp:include page="move.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionRecentChanges}">
		<jsp:include page="recent-changes.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionRegister}">
		<jsp:include page="register.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionSearch}">
		<jsp:include page="search.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionSearchResults}">
		<jsp:include page="search-results.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionSpecialPages}">
		<jsp:include page="all-special-pages.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionTopicsAdmin}">
		<jsp:include page="topics-admin.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionUpload}">
		<jsp:include page="upload.jsp" flush="true" />
	</c:when>
	<c:when test="${pageInfo.actionWatchlist}">
		<jsp:include page="watchlist.jsp" flush="true" />
	</c:when>
	<c:otherwise>
		<%@ include file="category-include.jsp" %>
		<%@ include file="view-topic-include.jsp" %>
	</c:otherwise>
</c:choose>
		<br />
		</div>
	</td>
</tr>
<tr>
	<td colspan="2" class="footer">
		<hr width="99%" />
		<c:out value="${bottomArea}" escapeXml="false" />
		<br/>
		<font size="-3"><a href="http://jamwiki.org/">JAMWiki</a> <f:message key="footer.message.version" /> <jamwiki:wiki-version/></font>
	</td>
</tr>
</table>
<%@ include file="close-document.jsp"%>
