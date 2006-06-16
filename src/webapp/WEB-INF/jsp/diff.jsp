<c:if test="${!badinput}">
	<c:out value="${diff}" escapeXml="false"/>
</c:if>
<c:if test="${badinput=='true'}">
	<f:message key="diff.badinput"/>
</c:if>
