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

<div class="message"><f:message key="register.form.info" /></div>

<fieldset>
<legend><f:message key="${pageInfo.pageTitle.key}" /></legend>

<form name="form1" method="post" action="<jamwiki:link value="Special:Account" />">
<input type="hidden" name="userId" value="<c:out value="${newuser.userId}" />" />
<c:if test="${!empty errors}">
<p class="red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></p>
</c:if>
<table>
<c:if test="${newuser.userId < 1}">
<tr>
	<td><label for="registerLogin"><f:message key="common.login" /></label>:</td>
	<td><input type="text" name="login" value="<c:out value="${newuser.username}" />" id="registerLogin" /></td>
</tr>
</c:if>
<c:if test="${newuser.userId > 0}">
<tr>
	<input type="hidden" name="login" value="<c:out value="${newuser.username}" />" />
	<td><f:message key="common.login" />:</td>
	<td><c:out value="${newuser.username}" /></td>
</tr>
<tr>
	<td><label for="registerOldPassword"><f:message key="register.caption.oldpassword" /></label>:</td>
	<td><input type="password" name="oldPassword" value="<c:out value="${oldPassword}" />" id="registerOldPassword" /></td>
</tr>
</c:if>
<c:if test="${newuserinfo.writeable || newuser.userId < 1}">
<tr>
	<td><label for="registerNewPassword"><f:message key="register.caption.newpassword" /></label>:</td>
	<td><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" /></td>
</tr>
</c:if>
<c:if test="${newuserinfo.writeable}">
<tr>
	<td><label for="registerConfirmPassword"><f:message key="register.caption.confirmpassword" /></label>:</td>
	<td><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" /></td>
</tr>
<tr>
	<td><label for="registerFirstName"><f:message key="register.caption.firstname" /></label>:</td>
	<td><input type="text" name="firstName" value="<c:out value="${newuserinfo.firstName}" />" id="registerFirstName" /></td>
</tr>
<tr>
	<td><label for="registerLastName"><f:message key="register.caption.lastname" /></label>:</td>
	<td><input type="text" name="lastName" value="<c:out value="${newuserinfo.lastName}" />" id="registerLastName" /></td>
</tr>
</c:if>
<c:if test="${!newuserinfo.writeable && newuser.userId > 0}">
<tr>
	<td><f:message key="register.caption.firstname" />:</td>
	<td><c:out value="${newuserinfo.firstName}" /></td>
</tr>
<tr>
	<td><f:message key="register.caption.lastname" />:</td>
	<td><c:out value="${newuserinfo.lastName}" /></td>
</tr>
</c:if>
<tr>
	<td><label for="registerDisplayName"><f:message key="register.caption.displayname" /></label>:</td>
	<td><input type="text" name="displayName" value="<c:out value="${newuser.displayName}" />" id="registerDisplayName" /></td>
</tr>
<c:if test="${newuserinfo.writeable}">
<tr>
	<td><label for="registerEmail"><f:message key="register.caption.email" /></label>:</td>
	<td><input type="text" name="email" value="<c:out value="${newuserinfo.email}" />" id="registerEmail" /></td>
</tr>
</c:if>
<c:if test="${!newuserinfo.writeable && newuser.userId > 0}">
<tr>
	<td><f:message key="register.caption.email" />:</td>
	<td><c:out value="${newuserinfo.email}" /></td>
</tr>
</c:if>
<tr>
	<td><f:message key="register.caption.locale" />:</td>
	<td>
		<select name="defaultLocale" id="registerDefaultLocale">
		<c:forEach items="${locales}" var="defaultLocale">
		<option value="<c:out value="${defaultLocale.value}" />"<c:if test="${newuser.defaultLocale == defaultLocale.value}"> selected</c:if>><c:out value="${defaultLocale.key}" /></option>
		</c:forEach>
		</select>
	</td>
</tr>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="common.save" />"></td></tr>
</table>
</form>

</fieldset>