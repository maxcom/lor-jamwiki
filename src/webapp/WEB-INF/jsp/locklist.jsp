<c:choose>
	<c:when test="${empty locks}">
<f:message key="locklist.nolocks"/>
	</c:when>
	<c:otherwise>
<table>
<tr><th><f:message key="common.topic"/></th><th><f:message key="locklist.lockedat"/></th></tr>
		<c:forEach items="${locks}" var="lock">
<tr>
	<td class="recent"><a href="<jamwiki:link value="${lock.name}" />"><c:out value="${lock.name}"/></a></td>
	<td class="recent"><f:formatDate value="${lock.lockedDate}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" /></td>
	<td><a href="<jamwiki:link value="Special:Unlock" />?topic=<jamwiki:encode value="${lock.name}" />"><f:message key="locklist.unlock"/></a></td>
</tr>
		</c:forEach>
</table>
	</c:otherwise>
</c:choose>
