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
        org.jamwiki.servlets.ServletUtil
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<c:if test="${!empty errorMessage}"><p class="red"><f:message key="${errorMessage.key}"><f:param value="${errorMessage.params[0]}" /></f:message></p></c:if>

<table border="0" class="contents">
<form name="delete" method="get" action="<jamwiki:link value="Special:Move" />">
<input type="hidden" name="<%= ServletUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<tr><td colspan="2"><p><f:message key="move.overview" /></p></td></tr>
<tr>
	<td class="formcaption" nowrap><label for="moveDestination"><f:message key="move.destination" /></label>: </td>
	<td class="formelement" width="90%"><input type="text" name="moveDestination" value="<c:out value="${moveDestination}" />" id="moveDestination" size="60" /></td>
</tr>
<tr>
	<td class="formcaption" nowrap><label for="moveComment"><f:message key="move.comment" /></label>: </td>
	<td class="formelement" width="90%"><input type="text" name="moveComment" value="<c:out value="${moveComment}" />" id="moveComment" size="60" /></td>
</tr>
<c:if test="${!empty moveCommentsPage}">
<tr>
	<td class="formcaption" colspan="2"><label for="moveCommentsPage"><f:message key="move.commentspage" /></label>:&#160;<input type="checkbox" name="moveCommentsPage" value="<c:out value="${moveCommentsPage}" />" id="moveCommentsPage" /></td>
</tr>
</c:if>
<tr><td>&#160;</td><td align="left"><input type="submit" name="move" value="<f:message key="common.move" />" /></td></tr>
</form>
</table>
