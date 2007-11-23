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

<html>
<head>
<style>
body {
	background: #f9f9f9;
	color: black;
	margin: 0;
	padding: 5px;
}
body, input, select {
	font: 95% sans-serif, tahoma;
}
.red {
	font: verdana, helvetica, sans-serif;
	font-size: 110%;
	color: #ff0000;
	text-align: center;
}
.green {
	font: verdana, helvetica, sans-serif;
	font-size: 110%;
	color: #009900;
	text-align: center;
}
</style>
</head>
<body>

<c:if test="${!empty message}">
<p class="green"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></p>
</c:if>

<c:if test="${!empty error}">
<p class="red"><f:message key="${error.key}" /></p>
</c:if>

<c:if test="${empty message && empty failure}">
<form name="adminUpgrade" method="post">
<input type="hidden" name="function" value="upgrade" />
<table style="border:2px solid #333333;padding=1em;">
<tr><td colspan="2"><f:message key="upgrade.caption.detected" /></td></tr>
<tr>
	<td><label for="loginUsername"><f:message key="login.username"/></label></td>
	<td><input type="text" name="username" value="<c:out value="${param.username}" />" id="loginUsername" /></td>
</tr>
<tr>
	<td><label for="loginPassword"><f:message key="login.password"/></label></td>
	<td><input type="password" name="password" id="loginPassword" /></td>
</tr>
<tr><td colspan="2" align="center"><input type="submit" name="button" value="Submit" /></td></tr>
</table>
</form>
</c:if>

<c:if test="${!empty errors}">
<br />
<table>
<tr><td class="red" colspan="2" align="center"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /><f:param value="${message.params[1]}" /></f:message><br /></c:forEach></td></tr>
</table>
</c:if>

<c:if test="${!empty messages}">
<br />
<table>
	<c:forEach items="${messages}" var="message">
<tr><td><c:out value="${message}" /></td></tr>
	</c:forEach>
</table>
</c:if>

</body>
</html>
