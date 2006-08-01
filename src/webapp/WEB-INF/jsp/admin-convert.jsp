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
    contentType="text/html; charset=UTF-8"
%>

<%@ include file="page-init.jsp" %>

<script type="text/javascript" language="JavaScript">
<!--
function confirmSubmit() {
	return confirm("<f:message key="common.confirm" />");
}
// -->
</script>

<div align="center" width="90%" style="border:1px dashed red;padding:3px;margin:10px 75px 5px 75px;"><font color="red"><f:message key="convert.warning.beta" /></font></div>

<form name="adminUpgrade" method="get" action="<jamwiki:link value="Special:Convert" />">

<table border="0" class="contents">
<c:if test="${!empty errorMessage}"><tr><td colspan="2" align="center"><div style="color:red;size=110%;"><c:out value="${errorMessage}" /></div></td></tr></c:if>
<c:if test="${!empty message}"><tr><td colspan="2" align="center"><div style="color:green;size=110%;"><c:out value="${message}" /></div></td></tr></c:if>
<tr>
	<td class="normal"><f:message key="convert.tofile" /></td>
	<td class="normal"><input type="submit" name="tofile" onclick="return confirmSubmit()" /></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="normal"><f:message key="convert.todatabase" /></td>
	<td class="normal"><input type="submit" name="todatabase" onclick="return confirmSubmit()" /></td>
</tr>
</table>
</form>

<c:if test="${!empty messages}">
<br />
<table>
<c:forEach items="${messages}" var="message">
<tr><td><c:out value="${message}" /></td></tr>
</c:forEach>
</table>
</c:if>
