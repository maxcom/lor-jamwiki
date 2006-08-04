<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<%@ page import="
        org.jamwiki.Environment,
        org.jamwiki.servlets.JAMWikiServlet
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<title><f:message key="common.sitename" /> - <f:message key="${pageTitle.key}"><f:param value="${pageTitle.params[0]}" /></f:message></title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<%
if (Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) != null && Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC).length() > 0) {
%>
	<link rel="start" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" />
	<link rel="home" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" />
<%
}
%>
	<link href="jamwiki.css" type="text/css" rel="stylesheet" />
	<link rel="search" title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Special:Search" />
	<link rel="index" title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Special:AllTopics" />
</head>
<body style="background:none">

<%@ include file="view-topic-include.jsp" %>

</body>
</html>
