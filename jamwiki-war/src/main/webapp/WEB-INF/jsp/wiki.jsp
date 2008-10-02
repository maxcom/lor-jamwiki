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
<%@ page errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<%@ include file="top.jsp" %>

<div id="wiki-page">
<div id="wiki-navigation">
	<div id="logo">
	<%-- FIXME - need image width and height --%>
	<a class="logo" href="<jamwiki:link value="${defaultTopic}" />"><img border="0" src="<c:url value="/images/${logo}" />" alt="" /></a>
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
</div>
<div id="wiki-content">
	<%@ include file="user-menu.jsp"%>
	<%@ include file="top-menu.jsp"%>
	<div id="contents" >
	<div id="contents-header"><f:message key="${pageInfo.pageTitle.key}"><f:param value="${pageInfo.pageTitle.params[0]}" /></f:message></div>
	<c:if test="${!empty pageInfo.redirectUrl}">
	<div id="contents-subheader"><f:message key="topic.redirect.from"><f:param><a href="<c:out value="${pageInfo.redirectUrl}" />"><c:out value="${pageInfo.redirectName}" /></a></f:param></f:message></div>
	</c:if>
<c:set var="contentJsp" scope="page" value="${pageInfo.contentJsp}" />
<%
// Servlet 2.3 doesn't understand the EL language, so a scriptlet has to be used...
String contentJsp = (String)pageContext.getAttribute("contentJsp");
%>
	<jsp:include page="<%= contentJsp %>" flush="true" />
	<br />
	</div>
</div>
<div id="wiki-footer">
	<hr width="99%" />
	<c:out value="${bottomArea}" escapeXml="false" />
	<br/>
	<font size="-3"><a href="http://jamwiki.org/">JAMWiki</a> <f:message key="footer.message.version" /> <jamwiki:wiki-version/></font>
</div>
</div>

<%@ include file="close-document.jsp"%>
