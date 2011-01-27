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
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<p><fmt:message key="viewsource.caption.overview"><fmt:param value="${pageInfo.topicName}" /></fmt:message></p>
<fieldset>
<legend><fmt:message key="viewsource.caption.legend"><fmt:param value="${pageInfo.topicName}" /></fmt:message></legend>
<form method="post" name="viewSourceForm" id="viewSourceForm" action="<jamwiki:link value="Special:Source" />">
<input type="hidden" name="topic" value="<c:out value="${pageInfo.topicName}" />" />
<p><textarea id="topicContents" name="contents" rows="25" cols="80" accesskey="," readonly="readonly"><c:out value="${contents}" escapeXml="true" /></textarea></p>
</form>
</fieldset>
