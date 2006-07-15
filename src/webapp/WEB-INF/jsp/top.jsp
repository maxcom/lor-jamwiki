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
<html>
<head>
	<title><c:out value="${title}" /></title>
	<meta http-equiv="Content-Type" content="text/html" charset="UTF-8" />
<%
if (Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) != null && Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC).length() > 0) {
%>
	<link rel="start" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" />
	<link rel="home" title="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" href="<c:out value="${pathRoot}"/><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" />
<%
} else {
%>
	<link rel="start"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/><f:message key="specialpages.startingpoints"/>" />
	<link rel="home"  title="<f:message key="specialpages.startingpoints"/>" href="<c:out value="${pathRoot}"/><f:message key="specialpages.startingpoints"/>" />
<%
}
%>
	<link href="jamwiki.css" type="text/css" rel="stylesheet" />
	<link rel="search" title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Special:Search" />
	<link rel="index" title="<f:message key="generalmenu.search"/>" href="<c:out value="${pathRoot}"/>Special:AllTopics" />
	<link rel="alternate" type="application/rss+xml" title="RSS Feed" href="<c:out value="${pathRoot}"/>Special:RSS" />
</head>
<body>