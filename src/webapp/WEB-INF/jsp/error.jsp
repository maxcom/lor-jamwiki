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
        org.apache.log4j.Logger
    "
    isErrorPage="true"
    contentType="text/html; charset=UTF-8"
%>

<html>
<head>
<title>JAMWiki System Error</title>
<script language="JavaScript">
function cancel() {
	history.go(-1);
}
</script>
</head>
<body>
<%
// FIXME - hard coding
Logger logger = Logger.getLogger("org.jamwiki.jsp");
String errorMessage = "No message available";
if (exception != null) {
	logger.error("Error in JSP page", exception);
	errorMessage = exception.toString();
}
%>

<%-- FIXME - hard coding --%>
<p>A system error has occurred.  The error message is:</p>
<p><font style="color: red;font-weight:bold"><%= errorMessage %></font></p>
<%
if (exception != null) {
%>
<p><% exception.printStackTrace(); %></p>
<%
}
%>
<form><input type="button" onClick="cancel();" value="Back" /></form>

</body>
</html>
