<%@ page import="
    vqwiki.Environment,
    vqwiki.servlets.WikiServlet
" %>
<%@ page errorPage="/jsp/error.jsp" %>
<%@ taglib uri="/WEB-INF/classes/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/classes/vqwiki.tld" prefix="vqwiki" %>
<%@ taglib uri="/WEB-INF/classes/fmt.tld" prefix="f" %>
<vqwiki:setPageEncoding />
<vqwiki:environment var="env"/>
<html>
<head>
  <f:setBundle basename="ApplicationResources"/>
   <c:if test="${env.firstUse}">
     <f:message key="firstuse.title" var="res"/>
     <c:set var="title" scope="request" value="${res}"/>
     <%
     // websphere doesn't like quotes within jsp:forward, so define a variable
     String firstUseUrl = "Wiki?action=" + WikiServlet.ACTION_FIRST_USE;
     %>
     <jsp:forward page="<%= firstUseUrl %>" /> 
   </c:if>

  <link rel="stylesheet" href='<c:out value="${pageContext.request.contextPath}"/>/vqwiki.css' type="text/css" />
  <title><c:out value="${title}"/></title>
  <META HTTP-EQUIV="Content-Type" CONTENT="text/html">
</head>
<body>

  <table>
    <tr>
      <td rowspan="2">
        <a class="logo" href='<c:out value="${pageContext.request.contextPath}"/>/jsp/Wiki?<f:message key="specialpages.startingpoints"/>'>
        <c:choose>
          <c:when test="${env.logoImageAbsoluteUrl}">
             <img class="logo" src='<c:out value="${env.logoImageName}"/>' alt="<f:message key="specialpages.startingpoints"/>"/>
          </c:when>
          <c:otherwise>
             <img class="logo" src='<c:out value="${pageContext.request.contextPath}"/>/<c:out value="${env.logoImageName}"/>' alt="<f:message key="specialpages.startingpoints"/>"/>
          </c:otherwise>
        </c:choose>
        </a>
      </td>
      <td width=10>&nbsp;</td>
      <vqwiki:wikibase var="wb"/>
      <c:if test="${wb.virtualWikiCount > 1}">
        <td>
          <b>Wiki :
          <a class="subHeader" href='<vqwiki:path-root/>/Wiki?StartingPoints'>
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
