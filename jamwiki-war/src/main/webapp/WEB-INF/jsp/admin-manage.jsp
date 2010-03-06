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
        org.jamwiki.utils.WikiUtil
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="manage">

<c:if test="${!empty message}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>

<c:if test="${deleted}">
<a name="undelete"></a>
<form name="undelete" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= WikiUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<fieldset>
<legend><fmt:message key="manage.caption.undelete"><fmt:param value="${pageInfo.topicName}" /></fmt:message></legend>
<div class="row">
	<label for="undeleteComment"><fmt:message key="manage.undelete.reason" /></label>
	<span><input type="text" name="undeleteComment" value="" id="undeleteComment" size="60" /></span>
</div>
<c:if test="${!empty manageCommentsPage}">
<div class="row">
	<label for="manageCommentsPage"><fmt:message key="manage.undelete.commentspage" /></label>
	<span><input type="checkbox" name="manageCommentsPage" value="<c:out value="${manageCommentsPage}" />" id="manageCommentsPage" /></span>
</div>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" name="undelete" value="<fmt:message key="common.undelete" />" /></span>
</div>
</fieldset>
</form>
</c:if>
<c:if test="${!deleted}">
<a name="delete"></a>
<form name="delete" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= WikiUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<fieldset>
<legend><fmt:message key="manage.caption.delete"><fmt:param value="${pageInfo.topicName}" /></fmt:message></legend>
<div class="message"><fmt:message key="manage.delete.warning" /></div>
<div class="row">
	<label for="deleteComment"><fmt:message key="manage.delete.reason" /></label>
	<span><input type="text" name="deleteComment" value="" id="deleteComment" size="60" /></span>
</div>
<c:if test="${!empty manageCommentsPage}">
<div class="row">
	<label for="manageCommentsPage"><fmt:message key="manage.delete.commentspage" /></label>
	<span><input type="checkbox" name="manageCommentsPage" value="<c:out value="${manageCommentsPage}" />" id="manageCommentsPage" /></span>
</div>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" name="delete" value="<fmt:message key="common.delete" />" /></span>
</div>
</fieldset>
</form>
<a name="permissions"></a>
<form name="permissions" method="get" action="<jamwiki:link value="Special:Manage" />">
<input type="hidden" name="<%= WikiUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<fieldset>
<legend><fmt:message key="manage.caption.permissions" /></legend>
<div class="row">
	<label for="readOnly"><fmt:message key="manage.caption.readonly" /></label>
	<span><input type="checkbox" name="readOnly" value="true"<c:if test="${readOnly}"> checked</c:if> id="readOnly" /></span>
</div>
<div class="row">
	<label for="adminOnly"><fmt:message key="manage.caption.adminonly" /></label>
	<span><input type="checkbox" name="adminOnly" value="true"<c:if test="${adminOnly}"> checked</c:if> id="adminOnly" /></span>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="permissions" value="<fmt:message key="common.update" />" /></span>
</div>
</fieldset>
</form>
</c:if>

</div>
