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

<c:set var="errorMessage" value="${param['errorMessage']}"/>
<c:if test="${!empty errorMessage}"><p class="red"><f:message key='${errorMessage}'>/></f:message></p></c:if>

<form method="post" action='<c:url value="/en/j_acegi_security_check"/>'>
<input type="hidden" name="redirect" value="<c:out value="${redirect}"/>" />
<table>
<tr>
	<td><label for="loginUsername"><f:message key="login.username"/></label></td>
	<td><input type="text" name="j_username" value="<c:out value="${param.username}" />" id="loginUsername" /></td>
</tr>
<tr>
	<td><label for="loginPassword"><f:message key="login.password"/></label></td>
	<td><input type="password" name="j_password" id="loginPassword" /></td>
</tr>
<tr>
	<td>&#160;</td>
	<td><input type="checkbox" value="true" name="_acegi_security_remember_me" id="loginRemember" />&#160;<label for="loginRemember"><f:message key="login.rememberme" /></label></td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td><input type="submit" name="function" value='<f:message key="common.login" />'/></td>
</tr>
</table>
</form>
