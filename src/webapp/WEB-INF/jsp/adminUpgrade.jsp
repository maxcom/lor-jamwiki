<script type="text/javascript" language="JavaScript">
<!--
function confirmSubmit() {
	return confirm("Are you sure?");
}
// -->
</script>
<form name="adminUpgrade" method="get" action="<jamwiki:link value="Special:Upgrade" />">
<table style="border:2px solid #333333;padding=1em;">
<%-- FIXME: hard coding --%>
<c:if test="${!empty errorMessage}"><tr><td colspan="2" align="center"><div style="color:red;size=110%;"><c:out value="${errorMessage}" /></div></td></tr></c:if>
<c:if test="${!empty message}"><tr><td colspan="2" align="center"><div style="color:green;size=110%;"><c:out value="${message}" /></div></td></tr></c:if>
<tr><td>Create new database tables:</td><td><input type="submit" name="function" value="Create" /></td></tr>
<tr><td>Purge new database tables:</td><td><input type="submit" name="function" value="Purge" onclick="return confirmSubmit();" /></td></tr>
<tr><td>Convert database content to files:</td><td><input type="submit" name="function" value="Convert to File" /></td></tr>
<tr><td>Convert file content to database:</td><td><input type="submit" name="function" value="Convert to Database" /></td></tr>
<tr><td>Load recent changes:</td><td><input type="submit" name="function" value="Load Recent Changes" /></td></tr>
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
