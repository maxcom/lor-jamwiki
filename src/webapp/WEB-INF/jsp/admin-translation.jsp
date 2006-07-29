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
%>

<%@ include file="page-init.jsp" %>

<table>
<tr>
<td>
<form name="adminTranslation" method="get" action="<jamwiki:link value="Special:Translation" />">
<%-- FIXME : hard coding --%>
<select name="language" onchange="this.form.submit()">
<option value=""></option>
<option value="hu">hu</option>
<option value="zh_CN">zh_CN</option>
<option value="zh_TW">zh_TW</option>
</select>
</form>
</td>
<td>&#160;&#160;&#160;&#160;&#160;</td>
<td>
<form name="adminTranslation" method="get" action="<jamwiki:link value="Special:Translation" />">
<input type="text" name="language" size="5" value="<c:out value="${language}" />" />&#160;&#160;<input type="submit" name="submit" />
</form>
</td>
</tr>
</table>

<form name="adminTranslation" method="post" action="<jamwiki:link value="Special:Translation" />">
<input type="hidden" name="language" value="<c:out value="${language}" />" />
<table>
<c:forEach items="${translations}" var="translation">
<tr>
	<td><c:out value="${translation.key}" /></td>
	<td><textarea name="translations[<c:out value="${translation.key}" />]" style="overflow:auto;width:30em;height:2.8em;"><c:out value="${translation.value}" /></textarea></td>
</tr>
</c:forEach>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="common.save" />" /></td></tr>
</table>
</form>
