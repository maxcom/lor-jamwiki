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

<%--
Note: This page handles errors that occur during processing of a JSP page.  Servlet
errors should be caught by the servlet and handled more cleanly.  If this page is
called it means that a catastrophic error has occurred.
--%>

<%@ page import="
        org.jamwiki.WikiLogger
    "
    isErrorPage="true"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<html>
<head>
<title><f:message key="common.sitename" /> - <f:message key="error.title" /></title>
<script language="JavaScript">
function cancel() {
	history.go(-1);
}
</script>
</head>
<body>
<%
WikiLogger logger = WikiLogger.getLogger("org.jamwiki.jsp");
String errorMessage = "";
if (exception != null) {
	logger.severe("Error in JSP page", exception);
	errorMessage = exception.toString();
}
%>

<p><f:message key="error.heading" /></p>
<p><font style="color: red;font-weight:bold"><%= errorMessage %></font></p>
<%
if (exception != null) {
%>
<p><% exception.printStackTrace(); %></p>
<%
}
%>
<form><input type="button" onClick="cancel();" value="<f:message key="common.back" />" /></form>

</body>
</html>
