<br/>
<div class="contents">
<vqwiki:current-user var="user"/>
<vqwiki:notification var="notification" userVar="user" topicVar="topic"/>
<c:choose>
  <c:when test="${notification}">
    <c:url value="Wiki" var="notifyurl">
      <c:param name="action" value="${env.actionNotify}"/>
      <c:param name="notify_action" value="notify_off"/>
      <c:param name="topic" value="${topic}"/>
    </c:url>
    <f:message key="notify.not.1"/><a href='<c:out value="${notifyurl}"/>'><f:message key="notify.not.2"/></a> <f:message key="notify.not.3"/>
  </c:when>
  <c:otherwise>
    <c:url value="Wiki" var="notifyurl">
      <c:param name="action" value="${env.actionNotify}"/>
      <c:param name="notify_action" value="notify_on"/>
      <c:param name="topic" value="${topic}"/>
    </c:url>
    <f:message key="notify.do.1"/><a href='<c:out value="${notifyurl}"/>'><f:message key="notify.do.2"/></a> <f:message key="notify.do.3"/>
  </c:otherwise>
</c:choose>
</div>