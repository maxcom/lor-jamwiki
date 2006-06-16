<br/>
<div class="contents">
<jmwiki:current-user var="user"/>
<jmwiki:notification var="notification" userVar="user" topicVar="topic"/>
<c:choose>
  <c:when test="${notification}">
    <f:message key="notify.not.1"/><a href='Wiki?action=<%= WikiServlet.ACTION_NOTIFY %>&notify_action=notify_off&topic=<c:out value="${topic}" />'><f:message key="notify.not.2"/></a> <f:message key="notify.not.3"/>
  </c:when>
  <c:otherwise>
    <f:message key="notify.do.1"/><a href='Wiki?action=<%= WikiServlet.ACTION_NOTIFY %>&notify_action=notify_on&topic=<c:out value="${topic}" />'><f:message key="notify.do.2"/></a> <f:message key="notify.do.3"/>
  </c:otherwise>
</c:choose>
</div>