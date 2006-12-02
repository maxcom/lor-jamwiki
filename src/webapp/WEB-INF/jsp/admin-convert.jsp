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

<script type="text/javascript" language="JavaScript">
<!--
function confirmSubmit() {
	return confirm("<f:message key="common.confirm" />");
}
// -->
</script>

<div style="margin:10px 30px 10px 30px;padding:10px;color:red;text-align:center;border:1px dashed red;"><f:message key="common.warning.experimental" /></div>

<form name="adminUpgrade" method="get" action="<jamwiki:link value="Special:Convert" />">

<c:if test="${!empty errorMessage}"><p class="red"><f:message key="${errorMessage.key}" /></p></c:if>
<c:if test="${!empty message}"><p align="center" style="color:green;size=110%;"><f:message key="${message.key}" /></p></c:if>

<table border="0" class="contents">
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><f:message key="convert.todatabase" /></td>
	<td class="formelement"><input type="submit" name="todatabase" onclick="return confirmSubmit()" /></td>
</tr>
</table>
</form>

<c:if test="${!empty messages}">
<ul>
<c:forEach items="${messages}" var="message">
<li><c:out value="${message}" /></li>
</c:forEach>
</ul>
</c:if>
