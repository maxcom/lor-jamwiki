
<table>
<c:forEach items="${wikis}" var="wiki">
  <tr><td class="recent"><a href="../<c:out value="${wiki}"/>/<%= Utilities.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>"><c:out value="${wiki}"/></a>
  </td></tr>
</c:forEach>
</table>
