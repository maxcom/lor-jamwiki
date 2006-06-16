<jmwiki:current-user var="user"/>
<c:choose>
  <c:when test="${empty user}">
     <p>
      <f:message key="member.become.text1"/>&nbsp;<a href='Wiki?action=<%= WikiServlet.ACTION_MEMBER %>'><f:message key="member.become.text2"/></a><f:message key="member.become.text3"/>
      </p>
  </c:when>
  <c:otherwise>
    <jmwiki:current-member var="member"/>
    <c:choose>
      <c:when test="${!member.confirmed}">
        <p><f:message key="member.register.text1"/> &nbsp;<a href='Wiki?action=<%= WikiServlet.ACTION_MEMBER %>'><f:message key="member.register.text2"/></a><f:message key="member.register.text3"/>
        </p>
      </c:when>
      <c:otherwise>
            <%@ include file="notify-contents.jsp"%>
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
