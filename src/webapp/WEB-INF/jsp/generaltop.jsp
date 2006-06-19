<%@ page import="
    org.jmwiki.Environment,
    org.jmwiki.WikiBase,
    org.jmwiki.servlets.WikiServlet,
    org.jmwiki.utils.Encryption,
    org.jmwiki.utils.Utilities
" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib uri="/WEB-INF/classes/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/classes/jmwiki.tld" prefix="jmwiki" %>
<%@ taglib uri="/WEB-INF/classes/fmt.tld" prefix="f" %>
<jmwiki:setPageEncoding />
<html>
<head>
  <f:setBundle basename="ApplicationResources"/>
<%
if (Utilities.isFirstUse()) {
%>
     <f:message key="firstuse.title" var="res"/>
     <c:set var="title" scope="request" value="${res}"/>
     <%
     // websphere doesn't like quotes within jsp:forward, so define a variable
     String firstUseUrl = "Wiki?action=" + WikiServlet.ACTION_FIRST_USE;
     %>
     <jsp:forward page="<%= firstUseUrl %>" /> 
<%
}
%>

  <link rel="stylesheet" href='<c:out value="${pageContext.request.contextPath}"/>/jmwiki.css' type="text/css" />
  <title><c:out value="${title}"/></title>
  <META HTTP-EQUIV="Content-Type" CONTENT="text/html">
</head>
<body>

  <table>
    <tr>
      <td rowspan="2">
        <a class="logo" href='<c:out value="${pageContext.request.contextPath}"/>/jsp/Wiki?<f:message key="specialpages.startingpoints"/>'>
        <%-- FIXME - this used to switch based on context --%>
        <img class="logo" src="images/logo.jpg" alt="<f:message key="specialpages.startingpoints"/>"/>
        </a>
      </td>
      <td width=10>&nbsp;</td>
      <jmwiki:wikibase var="wb"/>
      <c:if test="${wb.virtualWikiCount > 1}">
        <td>
          <b>Wiki :
          <a class="subHeader" href='<jmwiki:link value="StartingPoints" />'>
          <c:out value="${virtualWiki}"/></a></b> :
        </td>
      </c:if>
    </tr>
    <tr>
      <td width=10>&nbsp;</td>
      <td>
        <span class="pageHeader">
          <a href='<c:out value="${titlelink}"/>'>
            <c:out value="${title}"/>
          </a>
        </span>
      </td>
    </tr>
  </table>
