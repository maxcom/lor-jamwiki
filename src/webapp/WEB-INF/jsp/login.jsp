<form method="post" action="<jamwiki:link value="Special:Login" />">
<input type="hidden" name="redirect" value="<c:out value="${redirect}"/>" />
<table>
<c:if test="${loginFailure}">
<tr><td colspan="2" class="red"><f:message key="error.login" /></td></tr>
</c:if>
<tr>
	<td><f:message key="login.username"/></td>
	<td><input type="text" name="username" value='<c:out value="${param.username}"/>'/></td>
</tr>
<tr>
	<td><f:message key="login.password"/></td>
	<td><input type="password" name="password"/></td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td><input type="submit" name="function" value='<f:message key="login.submit"/>'/></td>
</tr>
</table>
</form>
