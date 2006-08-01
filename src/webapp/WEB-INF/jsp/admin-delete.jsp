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
        org.jamwiki.servlets.JAMWikiServlet
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=UTF-8"
%>

<%@ include file="page-init.jsp" %>

<form name="adminDelete" method="get" action="<jamwiki:link value="Special:Delete" />">
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_TOPIC %>" value="<c:out value="${topic}" />" />
<table style="border:2px solid #333333;padding=1em;">
<c:if test="${!empty errorMessage}"><tr><td colspan="2" align="center"><div style="color:red;size=110%;"><f:message key="${errorMessage.key}"><f:param value="${errorMessage.params[0]}" /><f:param value="${errorMessage.params[1]}" /></f:message></div></td></tr></c:if>
<c:if test="${!empty message}"><tr><td colspan="2" align="center"><div style="color:green;size=110%;"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div></td></tr></c:if>
<tr><td><f:message key="delete.reason" />: </td><td><input type="text" name="deleteComment" value="" /></td></tr>
<tr><td colspan="2"><input type="submit" name="delete" value="<f:message key="common.delete" />" /></td></tr>
</table>
</form>

<c:if test="${!empty messages}">
<br />
<table>
<c:forEach items="${messages}" var="message">
<tr><td><c:out value="${message}" /></td></tr>
</c:forEach>
</table>
</c:if>
