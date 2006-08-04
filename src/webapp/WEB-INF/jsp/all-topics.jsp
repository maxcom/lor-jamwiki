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

<table>
<c:choose>
	<c:when test="${empty all}">
<tr><td><p class="red"><f:message key="alltopics.notopics"/></p></td></tr>
	</c:when>
	<c:otherwise>
<tr><td><f:message key="alltopics.topics"><f:param><c:out value="${topicCount}" /></f:param></f:message></td></tr>
		<c:forEach items="${all}" var="topicName">
<tr><td><jamwiki:link value="${topicName}"><c:out value="${topicName}" /></jamwiki:link></td></tr>
		</c:forEach>
	</c:otherwise>
</c:choose>
</table>
