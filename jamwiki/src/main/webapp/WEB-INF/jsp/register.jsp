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
<div class="message red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></div>
</c:if>
<c:if test="${newuser.userId < 1}">
<div class="formentry">
	<div class="formcaption"><label for="registerLogin"><f:message key="common.login" /></label>:</div>
	<div class="formelement"><input type="text" name="login" value="<c:out value="${newuser.username}" />" id="registerLogin" /></div>
</div>
</c:if>
<c:if test="${newuser.userId > 0}">
<div class="formentry">
	<input type="hidden" name="login" value="<c:out value="${newuser.username}" />" />
	<div class="formcaption"><f:message key="common.login" />:</div>
	<div class="formelement"><c:out value="${newuser.username}" /></div>
</div>
<div class="formentry">
	<div class="formcaption"><label for="registerOldPassword"><f:message key="register.caption.oldpassword" /></label>:</div>
	<div class="formelement"><input type="password" name="oldPassword" value="<c:out value="${oldPassword}" />" id="registerOldPassword" /></div>
</div>
</c:if>
<c:if test="${newuserinfo.writeable || newuser.userId < 1}">
<div class="formentry">
	<div class="formcaption"><label for="registerNewPassword"><f:message key="register.caption.newpassword" /></label>:</div>
	<div class="formelement"><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" /></div>
</div>
</c:if>
<c:if test="${newuserinfo.writeable}">
<div class="formentry">
	<div class="formcaption"><label for="registerConfirmPassword"><f:message key="register.caption.confirmpassword" /></label>:</div>
	<div class="formelement"><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" /></div>
</div>
<div class="formentry">
	<div class="formcaption"><label for="registerFirstName"><f:message key="register.caption.firstname" /></label>:</div>
	<div class="formelement"><input type="text" name="firstName" value="<c:out value="${newuserinfo.firstName}" />" id="registerFirstName" /></div>
	<div class="formhelp"><f:message key="register.help.firstname" /></div>
</div>
<div class="formentry">
	<div class="formcaption"><label for="registerLastName"><f:message key="register.caption.lastname" /></label>:</div>
	<div class="formelement"><input type="text" name="lastName" value="<c:out value="${newuserinfo.lastName}" />" id="registerLastName" /></div>
	<div class="formhelp"><f:message key="register.help.lastname" /></div>
</div>
</c:if>
<c:if test="${!newuserinfo.writeable && newuser.userId > 0}">
<div class="formentry">
	<div class="formcaption"><f:message key="register.caption.firstname" />:</div>
	<div class="formelement"><c:out value="${newuserinfo.firstName}" /></div>
</div>
<div class="formentry">
	<div class="formcaption"><f:message key="register.caption.lastname" />:</div>
	<div class="formelement"><c:out value="${newuserinfo.lastName}" /></div>
</div>
</c:if>
<div class="formentry">
	<div class="formcaption"><label for="registerDisplayName"><f:message key="register.caption.displayname" /></label>:</div>
	<div class="formelement"><input type="text" name="displayName" value="<c:out value="${newuser.displayName}" />" id="registerDisplayName" /></div>
	<div class="formhelp"><f:message key="register.help.displayname" /></div>
</div>
<c:if test="${newuserinfo.writeable}">
<div class="formentry">
	<div class="formcaption"><label for="registerEmail"><f:message key="register.caption.email" /></label>:</div>
	<div class="formelement"><input type="text" name="email" value="<c:out value="${newuserinfo.email}" />" id="registerEmail" /></div>
	<div class="formhelp"><f:message key="register.help.email" /></div>
</div>
</c:if>
<c:if test="${!newuserinfo.writeable && newuser.userId > 0}">
<div class="formentry">
	<div class="formcaption"><f:message key="register.caption.email" />:</div>
	<div class="formelement"><c:out value="${newuserinfo.email}" /></div>
</div>
</c:if>
<div class="formentry">
	<div class="formcaption"><f:message key="register.caption.locale" />:</div>
	<div class="formelement">
		<select name="defaultLocale" id="registerDefaultLocale">
		<c:forEach items="${locales}" var="defaultLocale">
		<option value="<c:out value="${defaultLocale.value}" />"<c:if test="${newuser.defaultLocale == defaultLocale.value}"> selected</c:if>><c:out value="${defaultLocale.key}" /></option>
		</c:forEach>
		</select>
	</div>
	<div class="formhelp"><f:message key="register.help.locale" /></div>
</div>
<div align="center"><input type="submit" name="function" value="<f:message key="common.save" />"></div>
</form>

</fieldset>