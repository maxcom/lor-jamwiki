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
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>

<%@ include file="page-init.jsp" %>

<c:choose>
	<c:when test="${empty locks}">
<f:message key="locklist.nolocks"/>
	</c:when>
	<c:otherwise>
<table>
<tr><th><f:message key="common.topic"/></th><th><f:message key="locklist.lockedat"/></th></tr>
		<c:forEach items="${locks}" var="lock">
<tr>
	<td class="recent"><jamwiki:link value="${lock.name}"><c:out value="${lock.name}"/></jamwiki:link></td>
	<td class="recent"><f:formatDate value="${lock.lockedDate}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" /></td>
	<td><a href="<jamwiki:link value="Special:Unlock" />?topic=<jamwiki:encode value="${lock.name}" />"><f:message key="locklist.unlock"/></a></td>
</tr>
		</c:forEach>
</table>
	</c:otherwise>
</c:choose>
