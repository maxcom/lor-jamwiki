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
<html>
<head>

<%@ page import="
	org.jamwiki.servlets.JAMWikiServlet
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=UTF-8"
%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jamwiki.tld" prefix="jamwiki" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="f" %>
<f:setBundle basename="ApplicationResources"/>
<link rel="stylesheet" href='<c:out value="${pageContext.request.contextPath}"/>/jamwiki.css' type="text/css" />
<style type="text/css">
    @media print { /* if the page is printed, hide the hr line */ 
	  hr { width:0px; color:white; }
      #hideprint {
           display: none;
           visibility: hidden;
      }
	}
</style>
<title><c:out value="${title}"/></title>
</head>
<body>
<c:if test="${hideform != 'true'}">
  <div id="hideprint">
  <form action="Wiki" method="get">
  <input type="hidden" name="topic" value="<c:out value="${topic}"/>">
  <input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_PRINT %>">
  <f:message key="print.depth"/> <input type="text" name="depth" value="<c:out value="${depth}"/>">
  <input type="submit">
  <input type="hidden" name="hideform" value="true"/>
  </form>
  <p>&nbsp;</p>
  </div>
</c:if>
<% boolean looped = false; %>
<c:forEach var="item" items="${contentList}">
<% if (looped) { %>
<p>&nbsp;</p>
<hr>
<p style="page-break-after:always">&nbsp;</p>
<% } looped = true; %>
<a name="<c:out value="${item.topic}"/>"><h1 class="pageHeader"><c:out value="${item.topic}"/></h1></a>
<div class="contents">
  <c:out value="${item.content}" escapeXml="false"/>
</div>
</c:forEach>
<%@ include file="close-document.jsp"%>

