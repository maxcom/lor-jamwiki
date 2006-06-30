<form name="adminDelete" method="get" action="<jamwiki:link value="Special:Delete" />">
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_TOPIC %>" value="<c:out value="${topic}" />" />
<table style="border:2px solid #333333;padding=1em;">
<c:if test="${!empty errorMessage}"><tr><td colspan="2" align="center"><div style="color:red;size=110%;"><c:out value="${errorMessage}" /></div></td></tr></c:if>
<c:if test="${!empty message}"><tr><td colspan="2" align="center"><div style="color:green;size=110%;"><c:out value="${message}" /></div></td></tr></c:if>
<%-- FIXME: hard coding --%>
<tr><td>Reason for deletion: </td><td><input type="text" name="deleteComment" value="" /></td></tr>
<tr><td colspan="2"><input type="submit" name="function" value="Delete" /></td></tr>
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
