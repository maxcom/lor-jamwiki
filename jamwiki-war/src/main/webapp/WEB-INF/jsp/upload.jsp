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

<c:if test="${!empty uploadSpam}">
<div class="message red"><f:message key="${spam.key}"><f:param value="${spam.params[0]}" /></f:message></div>
</c:if>

<form name="form1" method="post" action="<jamwiki:link value="Special:Upload" />" enctype="multipart/form-data">
<table border="0">
<tr>
	<td><label for="uploadSource"><f:message key="import.caption.source" /></label>:</td>
	<td><input type="file" name="contents" size="50" id="uploadSource" /></td>
</tr>
<tr>
	<td><label for="uploadDescription"><f:message key="upload.caption.filedescription" /></label>:</td>
	<td><textarea name="description" rows="6" cols="80" id="uploadDescription"><c:out value="${contents}" escapeXml="true" /></textarea></td>
</tr>
<tr>
	<td colspan="2" align="center"><input type="submit" name="save" value="<f:message key="common.save" />" /></td>
</tr>
</table>
</form>
