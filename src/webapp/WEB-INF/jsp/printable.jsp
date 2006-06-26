<html>
<head>

<%@ page import="
	org.jamwiki.servlets.WikiServlet
" errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/classes/jamwiki.tld" prefix="jamwiki" %>
<%@ taglib uri="/WEB-INF/classes/fmt.tld" prefix="f" %>
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
  <form action="Wiki" method="GET">
  <input type="hidden" name="topic" value="<c:out value="${topic}"/>">
  <input type="hidden" name="action" value="<%= WikiServlet.ACTION_PRINT %>">
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

