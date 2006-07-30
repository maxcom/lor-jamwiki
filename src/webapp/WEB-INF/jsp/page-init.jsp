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
        org.jamwiki.servlets.JAMWikiServlet
    "
%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jamwiki.tld" prefix="jamwiki" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="f" %>

<%
// no-cache headers
response.setHeader("Cache-Control", "max-age=0, no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
response.setHeader("Pragma", "no-cache");
%>
<f:setBundle basename="ApplicationResources" />
<%
String action = (String)request.getAttribute(JAMWikiServlet.PARAMETER_ACTION);
if (action == null) {
	action = request.getParameter(JAMWikiServlet.PARAMETER_ACTION);
}
if (action == null) action = "";
%>
