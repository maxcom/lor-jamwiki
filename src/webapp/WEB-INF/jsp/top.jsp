<%@ page import="
    org.jamwiki.Environment,
    org.jamwiki.WikiBase,
    org.jamwiki.servlets.JAMController,
    org.jamwiki.utils.Utilities
" %>
<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jamwiki.tld" prefix="jamwiki" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="f" %>
<jamwiki:setPageEncoding />
<html>
  <head>
    <f:setBundle basename="ApplicationResources"/>
<%
if (Utilities.isFirstUse()) {
%>
      <%
      // Websphere seems to choke on quotation marks in a jsp:forward, so define a variable
      String firstUseUrl = "/en/Wiki?action=" + JAMController.ACTION_FIRST_USE;
      %>
      <jsp:forward page="<%= firstUseUrl %>" />
<%
}
%>
    <title><c:out value="${title}"/></title>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
<%
if (Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) != null && Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC).length() > 0) {
%>
<link rel="start" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>">
<link rel="home" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>">
<%
} else {
%>
<link rel="start"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/><f:message key="specialpages.startingpoints"/>">
<link rel="home"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/><f:message key="specialpages.startingpoints"/>">
<%
}
%>
	<link rel="search"  title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Special:Search">
	<link rel="index"  title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Special:AllTopics">
	<link rel="alternate" type="application/rss+xml" title="RSS Feed" href="<c:out value="${pathRoot}"/>Special:RSS">
    <style>
    <!--

<c:out value="${StyleSheet}" escapeXml="false"/>

    -->
    </style>
  </head>
<body>