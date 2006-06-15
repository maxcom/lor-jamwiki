<%@ page import="
    org.vqwiki.Environment,
    org.vqwiki.WikiBase,
    org.vqwiki.servlets.WikiServlet,
    org.vqwiki.utils.JSPUtils,
    org.vqwiki.utils.Utilities
" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib uri="/WEB-INF/classes/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/classes/vqwiki.tld" prefix="vqwiki" %>
<%@ taglib uri="/WEB-INF/classes/fmt.tld" prefix="f" %>
<vqwiki:setPageEncoding />
<html>
  <head>
    <f:setBundle basename="ApplicationResources"/>
<%
if (Utilities.isFirstUse()) {
%>
      <%
      // Websphere seems to choke on quotation marks in a jsp:forward, so define a variable
      String firstUseUrl = "/jsp/Wiki?action=" + WikiServlet.ACTION_FIRST_USE;
      %>
      <f:message key="firstuse.title" var="res"/>
      <c:set var="title" scope="request" value="${res}"/>
      <jsp:forward page="<%= firstUseUrl %>" />
<%
}
%>
    <title><c:out value="${title}"/></title>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
<c:if test="${!empty lastAuthor}">	<meta name="author" content="<c:out value="${lastAuthor}"/>" >
</c:if><c:if test="${!empty lastRevisionDate}">	<meta name="version" content="<c:out value="${lastRevisionDate}"/>" />
</c:if>	
<%
if (Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) != null && Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC).length() > 0) {
%>
<link rel="start" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/>Wiki?<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>">
<link rel="home" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/>Wiki?<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>">
<%
} else {
%>
<link rel="start"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/>Wiki?<f:message key="specialpages.startingpoints"/>">
<link rel="home"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/>Wiki?<f:message key="specialpages.startingpoints"/>">
<%
}
%>
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