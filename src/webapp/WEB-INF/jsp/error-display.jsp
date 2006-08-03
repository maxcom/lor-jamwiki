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
<%@ page errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>

<%@ include file="page-init.jsp" %>

<%--
Note: This page is not a JSP error page, it merely displays errors that were
caught by a servlet.
--%>

<script language="JavaScript">
function cancel() {
	history.go(-1);
}
</script>

<div class="contents">
	<p><f:message key="error.caption" /></p>
	<p><font style="color: red;font-weight:bold"><f:message key="${errorMessage.key}"><f:param value="${errorMessage.params[0]}" /></f:message></font></p>
	<form><input type="button" onClick="cancel();" value="<f:message key="common.back" />" /></form>
</div>

