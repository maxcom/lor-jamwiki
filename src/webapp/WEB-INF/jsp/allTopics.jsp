<table>
<c:choose>
	<c:when test="${empty all}">
<tr><td><p class="red"><f:message key="alltopics.notopics"/></p></td></tr>
	</c:when>
	<c:otherwise>
<tr><td><f:message key="alltopics.topics"><f:param><c:out value="${topicCount}" /></f:param></f:message></td></tr>
		<c:forEach items="${all}" var="topicName">
<tr><td class="recent"><a href="<jmwiki:link var="${topicName}" />"><c:out value="${topicName}" /></a></td></tr>
		</c:forEach>
	</c:otherwise>
</c:choose>
</table>
