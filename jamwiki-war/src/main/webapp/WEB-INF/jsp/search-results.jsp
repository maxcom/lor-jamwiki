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

<c:if test="${!empty notopic}">
<div class="message"><fmt:message key="topic.notcreated"><fmt:param value="${notopic}" /><fmt:param><jamwiki:link value="${notopic}" text="${notopic}" /></fmt:param></fmt:message></div>
</c:if>
<c:forEach items="${results}" var="result">
<div class="searchresult"><jamwiki:link value="${result.topic}" text="${result.topic}" /></div>
<div class="searchsummary"><c:out value="${result.summary}" escapeXml="false" /></div>
</c:forEach>
<c:if test="${empty results}">
<div class="message"><fmt:message key="searchresult.notfound"><fmt:param><c:out value="${searchField}" escapeXml="true"/></fmt:param></fmt:message></div>
<div class="message"><fmt:message key="topic.notcreated"><fmt:param><c:out value="${searchField}" escapeXml="true" /></fmt:param><fmt:param><jamwiki:link value="${searchField}" text="${searchField}" /></fmt:param></fmt:message></div>
</c:if>
<br /><br /><br />
<font size="-1"><i><fmt:message key="search.poweredby" /></i></font>
<a href="http://lucene.apache.org/java/"><img src="../images/lucene_green_100.gif" alt="Lucene" border="0" /></a>
