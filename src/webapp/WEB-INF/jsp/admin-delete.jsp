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
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<c:if test="${!empty message}">
<p style="color:green;font-size:130%;"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></p>
</c:if>

<c:if test="${empty message}">

<form name="adminDelete" method="get" action="<jamwiki:link value="Special:Delete" />">
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_TOPIC %>" value="<c:out value="${topic}" />" />
	<c:if test="${!empty errorMessage}">
<div align="center" style="margin:10px 0px 10px 0px;color:red;size=110%;"><f:message key="${errorMessage.key}"><f:param value="${errorMessage.params[0]}" /><f:param value="${errorMessage.params[1]}" /></f:message></div>
	</c:if>

<p><f:message key="delete.message.warning" /></p>

<table border="0" class="contents">
<tr>
	<td class="normal"><label for="deleteComment"><f:message key="delete.reason" /></label>: </td>
	<td class="normal"><input type="text" name="deleteComment" value="" id="deleteComment" size="60" /></td>
</tr>
<tr><td colspan="2" align="center"><input type="submit" name="delete" value="<f:message key="common.delete" />" /></td></tr>
</table>
</form>

</c:if>