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

<form method="post" action="<jamwiki:link value="Special:Login" />">
<input type="hidden" name="redirect" value="<c:out value="${redirect}"/>" />
<table>
<c:if test="${!empty errorMessage}">
<tr><td colspan="2" class="red"><f:message key="${errorMessage.key}"><f:param value="${errorMessage.params[0]}" /></f:message></td></tr>
</c:if>
<tr>
	<td><label for="loginUsername"><f:message key="login.username"/></label></td>
	<td><input type="text" name="username" value="<c:out value="${param.username}" />" id="loginUsername" /></td>
</tr>
<tr>
	<td><label for="loginPassword"><f:message key="login.password"/></label></td>
	<td><input type="password" name="password" id="loginPassword" /></td>
</tr>
<tr>
	<td>&#160;</td>
	<td><input type="checkbox" value="true" name="remember" id="loginRemember" />&#160;<label for="loginRemember"><f:message key="login.rememberme" /></label></td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td><input type="submit" name="function" value='<f:message key="common.login" />'/></td>
</tr>
</table>
</form>
