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
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>

<%@ include file="page-init.jsp" %>

<p><f:message key="createuser.info.text1"/></p>
<form name="form1" method="post" action="<jamwiki:link value="Special:Account" />">
<input type="hidden" name="userId" value="<c:out value="${user.userId}" />" />
<table>
<c:if test="${!empty errors}">
<tr><td colspan="2" align="center">
	<div style="color:red;size=110%;"><c:forEach items="${errors}" var="message"><c:out value="${message}" /><br /></c:forEach></div>
</td></tr>
</c:if>
<%-- FIXME: hard coding --%>
<%-- FIXME - handle LDAP --%>
<tr>
	<td>Login:</td>
	<td><input type="text" name="login" value="<c:out value="${user.login}" />"></td>
</tr>
<c:if test="${user.userId > 0}">
<tr>
	<td>Old Password:</td>
	<td><input type="password" name="oldPassword" value="<c:out value="${oldPassword}" />"></td>
</tr>
</c:if>
<tr>
	<td>New Password:</td>
	<td><input type="password" name="newPassword" value="<c:out value="${newPassword}" />"></td>
</tr>
<tr>
	<td>Confirm Password:</td>
	<td><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />"></td>
</tr>
<tr>
	<td>Display Name:</td>
	<td><input type="text" name="displayName" value="<c:out value="${user.displayName}" />"></td>
</tr>
<tr>
	<td>Email:</td>
	<td><input type="text" name="email" value="<c:out value="${user.email}" />"></td>
</tr>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="createuser.form.save"/>"></td></tr>
</table>
</form>
