
<table>
<c:forEach items="${wikis}" var="wiki">
  <tr><td class="recent">
  <f:message key="virtualwiki.${wiki}.name" var="wikiname"/>
  <a href="../<c:out value="${wiki}"/>/<%= Utilities.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>"><c:out value="${wikiname}"/></a>
  </td></tr>
</c:forEach>
</table>
