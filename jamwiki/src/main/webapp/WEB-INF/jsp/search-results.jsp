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
<div class="message"><f:message key="topic.notcreated"><f:param value="${notopic}" /><f:param><jamwiki:link value="${notopic}" text="${notopic}" /></f:param></f:message></div>
</c:if>
<c:forEach items="${results}" var="result">
<div class="searchresult"><jamwiki:link value="${result.topic}" text="${result.topic}" /></div>
<div class="searchsummary"><c:out value="${result.summary}" escapeXml="false" /></div>
</c:forEach>
<c:if test="${empty results}">
<div class="message"><f:message key="searchresult.notfound"><f:param value="${searchField}" /></f:message></div>
</c:if>
<br /><br /><br />
<font size="-1"><i><f:message key="search.poweredby" /></i></font>
<a href="http://lucene.apache.org/java/"><img src="../images/lucene_green_100.gif" alt="Lucene" border="0" /></a>
