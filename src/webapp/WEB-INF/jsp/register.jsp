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

<p><f:message key="register.form.info" /></p>
<form name="form1" method="post" action="<jamwiki:link value="Special:Account" />">
<input type="hidden" name="userId" value="<c:out value="${newuser.userId}" />" />
<table>
<c:if test="${!empty errors}">
<tr><td colspan="2" align="center">
	<p class="red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></p>
</td></tr>
</c:if>
<%-- FIXME - handle LDAP --%>
<tr>
	<td><label for="registerLogin"><f:message key="common.login" /></label>:</td>
	<td><input type="text" name="login" value="<c:out value="${newuser.login}" />" id="registerLogin" /></td>
</tr>
<c:if test="${newuser.userId > 0}">
<tr>
	<td><label for="registerOldPassword"><f:message key="register.caption.oldpassword" /></label>:</td>
	<td><input type="password" name="oldPassword" value="<c:out value="${oldPassword}" />" id="registerOldPassword" /></td>
</tr>
</c:if>
<tr>
	<td><label for="registerNewPassword"><f:message key="register.caption.newpassword" /></label>:</td>
	<td><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" /></td>
</tr>
<tr>
	<td><label for="registerConfirmPassword"><f:message key="register.caption.confirmpassword" /></label>:</td>
	<td><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" /></td>
</tr>
<tr>
	<td><label for="registerDisplayName"><f:message key="register.caption.displayname" /></label>:</td>
	<td><input type="text" name="displayName" value="<c:out value="${newuser.displayName}" />" id="registerDisplayName" /></td>
</tr>
<tr>
	<td><label for="registerEmail"><f:message key="register.caption.email" /></label>:</td>
	<td><input type="text" name="email" value="<c:out value="${newuser.email}" />" id="registerEmail" /></td>
</tr>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="common.save" />"></td></tr>
</table>
</form>
