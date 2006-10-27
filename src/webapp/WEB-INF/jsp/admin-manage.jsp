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
<p align="center" style="color:green;font-size:110%;"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></p>
</c:if>

<table border="0" class="contents">
<c:if test="${deleted}">
<form name="undelete" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<tr><td colspan="2"><h4><f:message key="manage.caption.undelete"><f:param value="${pageInfo.topicName}" /></f:message></h4></td></tr>
<tr>
	<td class="formcaption" nowrap><label for="undeleteComment"><f:message key="manage.undelete.reason" /></label>: </td>
	<td class="formelement" width="90%"><input type="text" name="undeleteComment" value="" id="undeleteComment" size="60" /></td>
</tr>
<c:if test="${!empty manageCommentsPage}">
<tr>
	<td class="formcaption" colspan="2"><label for="manageCommentsPage"><f:message key="manage.undelete.commentspage" /></label>:&#160;<input type="checkbox" name="manageCommentsPage" value="<c:out value="${manageCommentsPage}" />" id="manageCommentsPage" /></td>
</tr>
</c:if>
<tr><td>&#160;</td><td align="left"><input type="submit" name="undelete" value="<f:message key="common.undelete" />" /></td></tr>
</form>
</c:if>
<c:if test="${!deleted}">
<form name="delete" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<tr><td colspan="2"><h4><f:message key="manage.caption.delete"><f:param value="${pageInfo.topicName}" /></f:message></h4></td></tr>
<tr><td colspan="2"><p><f:message key="manage.delete.warning" /></p></td></tr>
<tr>
	<td class="formcaption" nowrap><label for="deleteComment"><f:message key="manage.delete.reason" /></label>: </td>
	<td class="formelement" width="90%"><input type="text" name="deleteComment" value="" id="deleteComment" size="60" /></td>
</tr>
<c:if test="${!empty manageCommentsPage}">
<tr>
	<td class="formcaption" colspan="2"><label for="manageCommentsPage"><f:message key="manage.delete.commentspage" /></label>:&#160;<input type="checkbox" name="manageCommentsPage" value="<c:out value="${manageCommentsPage}" />" id="manageCommentsPage" /></td>
</tr>
</c:if>
<tr><td>&#160;</td><td align="left"><input type="submit" name="delete" value="<f:message key="common.delete" />" /></td></tr>
</form>
<tr><td colspan="2"><h4><f:message key="manage.caption.permissions" /></h4></td></tr>
<form name="permissions" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<tr>
	<td class="formcaption" nowrap><label for="readOnly"><f:message key="manage.caption.readonly" /></label>: </td>
	<td class="formelement"><input type="checkbox" name="readOnly" value="true"<c:if test="${readOnly}"> checked</c:if> id="readOnly" /></td>
</tr>
<tr>
	<td class="formcaption" nowrap><label for="adminOnly"><f:message key="manage.caption.adminonly" /></label>: </td>
	<td class="formelement"><input type="checkbox" name="adminOnly" value="true"<c:if test="${adminOnly}"> checked</c:if> id="adminOnly" /></td>
</tr>
<tr><td>&#160;</td><td align="left"><input type="submit" name="permissions" value="<f:message key="common.update" />" /></td></tr>
</form>
</c:if>
</table>
