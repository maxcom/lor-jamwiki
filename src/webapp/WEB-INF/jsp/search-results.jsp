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
    pageEncoding="UTF-8"
%>

<%@ include file="page-init.jsp" %>

<br />
<c:forEach items="${results}" var="result">
<p><div class="searchresult"><jamwiki:link value="${result.topic}?highlight=${result.foundWord}" text="${result.topic}" /></div>
<c:if test="${!empty result.textBefore || !empty result.textAfter || !empty result.foundWord}">
  <br /><c:out value="${result.textBefore}" />
  <jamwiki:link value="${result.topic}?highlight=${result.foundWord}" style="highlight"><c:out value="${result.foundWord}" /></jamwiki:link>
  <c:out value="${result.textAfter}" />
  </c:if>
</p>
</c:forEach>
<c:if test="${empty results}">
<p><f:message key="searchresult.notfound"><f:param value="${searchField}" /></f:message></p>
</c:if>
<br /><br /><br />
<font size="-1"><i><f:message key="search.poweredby" /></i></font>
<a href="http://jakarta.apache.org/lucene"><img src="../images/lucene_green_100.gif" alt="Lucene" border="0" /></a>
