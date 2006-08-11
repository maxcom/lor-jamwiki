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
<head></head>
<body>

<script type="text/javascript" language="JavaScript">
<!--
function confirmSubmit() {
	return confirm("<f:message key="common.confirm" />");
}
// -->
</script>

<c:if test="${!empty message}"><p align="center" style="color:green;size=110%;"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></p></c:if>

<c:if test="${empty message}">
<form name="adminUpgrade" method="get">
<input type="hidden" name="function" value="upgrade" />
<table style="border:2px solid #333333;padding=1em;">
<tr><td><f:message key="upgrade.caption.detected" /></td></tr>
<tr><td align="center"><input type="submit" name="button" value="Submit" onclick="return confirmSubmit()" /></td></tr>
</table>
</form>
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
