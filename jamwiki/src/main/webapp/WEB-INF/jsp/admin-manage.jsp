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

<c:if test="${!empty message}">
<div class="message green"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div>
</c:if>

<table border="0" class="contents">
<c:if test="${deleted}">
<form name="undelete" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= ServletUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
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
<input type="hidden" name="<%= ServletUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<tr><td colspan="2"><h4><f:message key="manage.caption.delete"><f:param value="${pageInfo.topicName}" /></f:message></h4></td></tr>
<tr><td colspan="2"><div class="message"><f:message key="manage.delete.warning" /></div></td></tr>
</table>

<div class="formentry">
	<div class="formcaption" nowrap><label for="deleteComment"><f:message key="manage.delete.reason" /></label>: </div>
	<div class="formelement" width="90%"><input type="text" name="deleteComment" value="" id="deleteComment" size="60" /></div>
</div>
<c:if test="${!empty manageCommentsPage}">
<div class="formentry">
	<div class="formcaption"><label for="manageCommentsPage"><f:message key="manage.delete.commentspage" /></label>:</div>
	<div class="formelement"><input type="checkbox" name="manageCommentsPage" value="<c:out value="${manageCommentsPage}" />" id="manageCommentsPage" /></div>
</div>
</c:if>
<div class="formentry">
	<div class="formcaption">&#160;</div>
	<div class="formelement"><input type="submit" name="delete" value="<f:message key="common.delete" />" /></div>
</div>
</form>
<h4><f:message key="manage.caption.permissions" /></h4>
<form name="permissions" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= ServletUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<div class="formentry">
	<div class="formcaption" nowrap><label for="readOnly"><f:message key="manage.caption.readonly" /></label>: </div>
	<div class="formelement"><input type="checkbox" name="readOnly" value="true"<c:if test="${readOnly}"> checked</c:if> id="readOnly" /></div>
</div>
<div class="formentry">
	<div class="formcaption" nowrap><label for="adminOnly"><f:message key="manage.caption.adminonly" /></label>: </div>
	<div class="formelement"><input type="checkbox" name="adminOnly" value="true"<c:if test="${adminOnly}"> checked</c:if> id="adminOnly" /></div>
</div>
<div class="formentry">
	<div class="formcaption">&#160;</div>
	<div class="formelement"><input type="submit" name="permissions" value="<f:message key="common.update" />" /></div>
</div>
</form>
</c:if>
