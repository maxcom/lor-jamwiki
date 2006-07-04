<p><f:message key="createuser.info.text1"/></p>
<form name="form1" method="post" action="<jamwiki:link value="Special:Register" />">
<input type="hidden" name="saved" value="true">
<c:if test="${!empty param.topic}">
<input type="hidden" name="topic" value='<c:out value="${param.topic}"/>'/>
</c:if>
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
