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
      <%
      // Websphere seems to choke on quotation marks in a jsp:forward, so define a variable
      String firstUseUrl = "Wiki?action=" + WikiServlet.ACTION_FIRST_USE;
      %>
      <f:message key="firstuse.title" var="res"/>
      <c:set var="title" scope="request" value="${res}"/>
      <jsp:forward page="<%= firstUseUrl %>" />
    </c:if>
    <title><c:out value="${title}"/></title>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
<c:if test="${!empty lastAuthor}">	<meta name="author" content="<c:out value="${lastAuthor}"/>" >
</c:if><c:if test="${!empty lastRevisionDate}">	<meta name="version" content="<c:out value="${lastRevisionDate}"/>" />
</c:if>	
<c:choose><c:when test="${!empty env.defaultTopic}">	<link rel="start" title="<c:out value="${env.defaultTopic}"/>" href="<c:out value="${pathRoot}"/>Wiki?<c:out value="${env.defaultTopic}"/>">
	<link rel="home" title="<c:out value="${env.defaultTopic}"/>" href="<c:out value="${pathRoot}"/>Wiki?<c:out value="${env.defaultTopic}"/>">
</c:when><c:otherwise>	<link rel="start"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/>Wiki?<f:message key="specialpages.startingpoints"/>">
	<link rel="home"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/>Wiki?<f:message key="specialpages.startingpoints"/>">
</c:otherwise></c:choose>
	<link rel="search"  title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Wiki?WikiSearch">
	<link rel="index"  title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Wiki?AllWikiTopics">
	<link rel="alternate" type="application/rss+xml" title="RSS Feed" href="<c:out value="${pathRoot}"/>Wiki?RSS">
    <style>
    <!--

<c:out value="${StyleSheet}" escapeXml="false"/>

    -->
    </style>
  </head>
<body>