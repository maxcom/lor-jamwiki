<br>
<%-- FIXME - used to be a test for defaultLogoImageName here --%>
<p>
	<a class="logo" href="http://veryquickwiki.sourceforge.net">
	 <img align="right" class="logo" src="<%=request.getContextPath()%>/images/pblogo.jpg" alt="Very Quick Wiki"/>
	</a>
  <font size="-3">VeryQuickWiki Version <jmwiki:wiki-version/> |
    <a href="admin/admin.html?username=admin"><f:message key="admin.title"/></a>
  </font>
  <c:if test="${not empty pageContext.request.userPrincipal}">|
    <font size="-3"><a href='Wiki?action=<%= WikiServlet.ACTION_LOGIN %>&logout=true&redirect=Wiki%3F<c:out value="${topic}"/>'><f:message key="general.logout"/></a></font>
  </c:if>
  <br/>
  <c:out value="${bottomArea}" escapeXml="false"/>
  </body>
</html>
