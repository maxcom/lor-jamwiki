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
<form name="form1" method="post" action="<jamwiki:link value="Special:Account" />">
<input type="hidden" name="userId" value="<c:out value="${newuser.userId}" />" />
<c:if test="${!empty errors}">
<div class="message red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><f:message key="register.caption.accountdetails" /></legend>

<%-- new user --%>

<c:if test="${newuser.userId < 1}">
<div class="formentry">
	<span class="formcaption"><label for="registerLogin"><f:message key="common.login" /></label>:</span>
	<span class="formelement"><input type="text" name="login" value="<c:out value="${newuser.username}" />" id="registerLogin" /></span>
</div>
<div class="formentry">
	<span class="formcaption"><label for="registerNewPassword"><f:message key="register.caption.newpassword" /></label>:</span>
	<span class="formelement"><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" /></span>
</div>
<div class="formentry">
	<span class="formcaption"><label for="registerConfirmPassword"><f:message key="register.caption.confirmpassword" /></label>:</span>
	<span class="formelement"><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" /></span>
</div>
</c:if>

<%-- existing user --%>

<c:if test="${newuser.userId > 0}">
<div class="formentry">
	<input type="hidden" name="login" value="<c:out value="${newuser.username}" />" />
	<span class="formcaption"><f:message key="common.login" />:</span>
	<span class="formelement"><c:out value="${newuser.username}" /></span>
</div>
</c:if>

<%-- new or existing user --%>

<div class="formentry">
	<span class="formcaption"><label for="registerEmail"><f:message key="register.caption.email" /></label>:</span>
	<span class="formelement"><input type="text" name="email" value="<c:out value="${newuser.email}" />" id="registerEmail" /></span>
	<div class="formhelp"><f:message key="register.help.email" /></div>
</div>
</fieldset>
<fieldset>
<legend><f:message key="register.caption.userpreferences" /></legend>
<div class="formentry">
	<span class="formcaption"><label for="registerDisplayName"><f:message key="register.caption.displayname" /></label>:</span>
	<span class="formelement"><input type="text" name="displayName" value="<c:out value="${newuser.displayName}" />" id="registerDisplayName" /></span>
	<div class="formhelp"><f:message key="register.help.displayname" /></div>
</div>
<div class="formentry">
	<span class="formcaption"><f:message key="register.caption.locale" />:</span>
	<span class="formelement">
		<select name="defaultLocale" id="registerDefaultLocale">
		<c:forEach items="${locales}" var="defaultLocale">
		<option value="<c:out value="${defaultLocale.value}" />"<c:if test="${newuser.defaultLocale == defaultLocale.value}"> selected</c:if>><c:out value="${defaultLocale.key}" /></option>
		</c:forEach>
		</select>
	</span>
	<div class="formhelp"><f:message key="register.help.locale" /></div>
</div>
<div class="formentry">
	<span class="formcaption"><label for="registerEditor"><f:message key="register.caption.editor" /></label>:</span>
	<span class="formelement">
		<select name="editor" id="registerEditor">
		<c:forEach items="${editors}" var="editor">
		<option value="<c:out value="${editor.key}" />"<c:if test="${newuser.editor == editor.key}"> selected</c:if>><c:out value="${editor.value}" /></option>
		</c:forEach>
		</select>
	</span>
	<div class="formhelp"><f:message key="register.help.editor" /></div>
</div>
<%--
<div class="formentry">
	<span class="formcaption"><label for="registerSignature"><f:message key="register.caption.signature" /></label>:</span>
	<span class="formelement"><input type="text" name="signature" value="<c:out value="${newuser.signature}" />" id="registerSignature" /></span>
	<div class="formhelp"><f:message key="register.help.signature" /></div>
</div>
--%>
</fieldset>

<%-- existing user --%>

<c:if test="${newuser.userId > 0}">
<fieldset>
<legend><f:message key="register.caption.changepassword" /></legend>
<div class="formentry">
	<span class="formcaption"><label for="registerOldPassword"><f:message key="register.caption.oldpassword" /></label>:</span>
	<span class="formelement"><input type="password" name="oldPassword" value="<c:out value="${oldPassword}" />" id="registerOldPassword" /></span>
</div>
<div class="formentry">
	<span class="formcaption"><label for="registerNewPassword"><f:message key="register.caption.newpassword" /></label>:</span>
	<span class="formelement"><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" /></span>
</div>
<div class="formentry">
	<span class="formcaption"><label for="registerConfirmPassword"><f:message key="register.caption.confirmpassword" /></label>:</span>
	<span class="formelement"><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" /></span>
</div>
</fieldset>
</c:if>
<div class="formentry">
	<span class="formcaption">&#160;</span>
	<span class="formelement"><input type="submit" name="function" value="<f:message key="common.save" />"></span>
</div>
</form>
