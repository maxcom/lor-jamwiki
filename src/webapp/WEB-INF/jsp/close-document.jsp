<c:url value="Wiki" var="redirectUrl">
  <c:param name="action" value="${env.actionLogin}"/>
  <c:param name="logout" value="true"/>
  <c:param name="redirect" value="Wiki?${topic}"/>
</c:url>
<br>
<c:if test="${!env.defaultLogoImageName}">
<p>
	<a class="logo" href="http://veryquickwiki.sourceforge.net">
	 <img align="right" class="logo" src="<%=request.getContextPath()%>/images/pblogo.jpg" alt="Very Quick Wiki"/>
	</a>
</c:if>
  <font size="-3">VeryQuickWiki Version <vqwiki:wiki-version/> |
    <a href="Wiki?action=<c:out value="${env.actionAdmin}"/>&username=admin"><f:message key="admin.title"/></a>
  </font>
  <c:if test="${not empty pageContext.request.userPrincipal}">|
    <font size="-3"><a href='<c:out value="${redirectUrl}"/>'><f:message key="general.logout"/></a></font>
  </c:if>
  <br/>
  <c:out value="${bottomArea}" escapeXml="false"/>
  </body>
</html>
