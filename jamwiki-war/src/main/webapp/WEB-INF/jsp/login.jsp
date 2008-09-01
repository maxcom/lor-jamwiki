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

<jamwiki:authmsg css="message red" />

<fieldset>
<legend><f:message key="${pageInfo.pageTitle.key}" /></legend>

<form method="post" action="<c:url value="${springSecurityLoginUrl}" />">
<input type="hidden" name="<c:out value="${springSecurityTargetUrlField}" />" value="<c:out value="${springSecurityTargetUrl}"/>" />
<div class="formentry">
	<span class="formcaption-small"><label for="loginUsername"><f:message key="login.username"/></label></span>
	<span class="formelement"><input type="text" name="<c:out value="${springSecurityUsernameField}" />" value="<c:out value="${param.username}" />" id="loginUsername" /></span>
</div>
<div class="formentry">
	<span class="formcaption-small"><label for="loginPassword"><f:message key="login.password"/></label></span>
	<span class="formelement"><input type="password" name="<c:out value="${springSecurityPasswordField}" />" id="loginPassword" /></span>
</div>
<div class="formentry">
	<span class="formcaption-small">&#160;</span>
	<span class="formelement"><input type="checkbox" value="true" name="<c:out value="${springSecurityRememberMeField}" />" id="loginRemember" />&#160;<label for="loginRemember"><f:message key="login.rememberme" /></label></span>
</div>
<div class="formentry">
	<span class="formcaption-small">&#160;</span>
	<span class="formelement"><input type="submit" name="function" value="<f:message key="common.login" />" /></span>
</div>
</form>

</fieldset>
