<c:choose>
	<c:when test="${empty locks}">
<f:message key="locklist.nolocks"/>
	</c:when>
	<c:otherwise>
<table>
<tr><th><f:message key="common.topic"/></th><th><f:message key="locklist.lockedat"/></th></tr>
		<c:forEach items="${locks}" var="lock">
<tr>
			<jmwiki:encode value="${lock.topicName}" var="encodedTopic"/>
	<td class="recent"><a href='<c:out value="Wiki?${encodedTopic}"/>'><c:out value="${lock.topicName}"/></a></td>
	<td class="recent"><f:formatDate value="${lock.time}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" /></td>
	<td><a href='Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_UNLOCK %>'><f:message key="locklist.unlock"/></a></td>
</tr>
		</c:forEach>
</table>
	</c:otherwise>
</c:choose>
