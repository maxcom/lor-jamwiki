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

<form method="post" action="<c:url value="/${virtualWiki}/j_acegi_security_check" />">
<input type="hidden" name="target" value="<c:out value="${target}"/>" />
<div class="formentry">
	<div class="formcaption-small"><label for="loginUsername"><f:message key="login.username"/></label></div>
	<div class="formelement"><input type="text" name="j_username" value="<c:out value="${param.username}" />" id="loginUsername" /></div>
</div>
<div class="formentry">
	<div class="formcaption-small"><label for="loginPassword"><f:message key="login.password"/></label></div>
	<div class="formelement"><input type="password" name="j_password" id="loginPassword" /></div>
</div>
<div class="formentry">
	<div class="formcaption-small">&#160;</div>
	<div class="formelement"><input type="checkbox" value="true" name="_acegi_security_remember_me" id="loginRemember" />&#160;<label for="loginRemember"><f:message key="login.rememberme" /></label></div>
</div>
<div class="formentry">
	<div class="formcaption-small">&#160;</div>
	<div class="formelement"><input type="submit" name="function" value="<f:message key="common.login" />" /></div>
</div>
</form>

</fieldset>
