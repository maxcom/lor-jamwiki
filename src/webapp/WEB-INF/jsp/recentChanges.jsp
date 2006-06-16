<table>
<tr>
	<th><f:message key="common.date"/></th>
	<th><f:message key="common.topic"/></th>
	<th><f:message key="common.user"/></th>
</tr>
<c:forEach items="${changes}" var="change">
<tr>
	<td class="recent"><f:formatDate value="${change.time}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" /></td>
	<jmwiki:encode value="${change.topic}" var="encodedTopic"/>
	<td class="recent"><a href='<c:out value="Wiki?${encodedTopic}"/>'><c:out value="${change.topic}"/></a></td>
	<td class="recent"><c:out value="${change.username}"/></td>
</tr>
</c:forEach>
</table>
