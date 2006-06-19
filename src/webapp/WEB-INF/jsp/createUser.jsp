<c:choose>
	<c:when test="${param.saved==true}">
	<f:message key="createuser.ok.text1"><f:param value="${param.username}"/></f:message>
	</c:when>
	<c:otherwise>
<p><f:message key="createuser.info.text1"/></p>
<form name="form1" method="post" action="<jmwiki:link value="Special:SetUsername" />">
	<f:message key="createuser.form.name"/>
<%
if (Environment.getIntValue(Environment.PROP_USERGROUP_TYPE) == 0) {
%>
<input type="text" name="username">
<%
} else {
%>
<select name="username">
<c:forEach items="${userList}" var="listItem"><option value="<c:out value="${listItem.key}"/>"><c:out value="${listItem.label}"/></option></c:forEach>
</select>
<%
}
%>
<input type="submit" name="Submit" value="<f:message key="createuser.form.save"/>">
<input type="hidden" name="saved" value="true">
<c:if test="${!empty param.topic}">
<input type="hidden" name="topic" value='<c:out value="${param.topic}"/>'/>
</c:if>
</form>
	</c:otherwise>
</c:choose>
