
<ul>
<c:forEach items="${wikis}" var="wiki">
<li><a href="../<c:out value="${wiki}"/>/<%= Utilities.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>"><c:out value="${wiki}"/></a></li>
</c:forEach>
</ul>
