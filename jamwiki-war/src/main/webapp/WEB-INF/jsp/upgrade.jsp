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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><f:message key="${pageInfo.pageTitle.key}"><f:param value="${pageInfo.pageTitle.params[0]}" /><f:param value="${pageInfo.pageTitle.params[1]}" /></f:message></title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style>
body {
	background: #f9f9f9;
	color: black;
	padding: 5px;
}
body, input, select {
	font: 95% sans-serif, tahoma;
}
#upgrade-container {
	margin: 20px auto;
	width: 800px;
	padding: 10px 5px;
}
#upgrade-table {
	border: 2px solid #333333;
	padding: 10px;
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
<div id="upgrade-container">

<h3><f:message key="${pageInfo.pageTitle.key}"><f:param value="${pageInfo.pageTitle.params[0]}" /><f:param value="${pageInfo.pageTitle.params[1]}" /></f:message></h3>

<c:if test="${!empty message}">
<div class="green"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div>
</c:if>

<c:if test="${!empty error}">
<div class="red"><f:message key="${error.key}" /></div>
</c:if>

<c:if test="${!empty errors}">
<c:forEach items="${errors}" var="message">
<div class="red"><f:message key="${message.key}"><f:param value="${message.params[0]}" /><f:param value="${message.params[1]}" /></f:message></div>
</c:forEach>
</c:if>

<c:if test="${!empty messages}">
<c:forEach items="${messages}" var="message">
<div class="green"><c:out value="${message}" /></div>
</c:forEach>
</c:if>

<c:if test="${empty message && empty failure}">
<form name="adminUpgrade" method="post">
<input type="hidden" name="function" value="upgrade" />
<table id="upgrade-table">
<tr><td colspan="2"><f:message key="upgrade.caption.detected" /></td></tr>
<c:if test="${!empty upgradeDetails}">
	<tr>
		<td colspan="2">
			<ul>
				<c:forEach items="${upgradeDetails}" var="upgradeDetail">
					<li><f:message key="${upgradeDetail.key}" /></li>
				</c:forEach>
			</ul>
		</td>
	</tr>
</c:if>
<tr><td colspan="2"><f:message key="upgrade.caption.login" /></td></tr>
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

</div>
</body>
</html>
