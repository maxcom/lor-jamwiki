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

<%-- FIXME: hard coding --%>
<div align="center" width="90%" style="border:1px dashed red;padding:3px;margin:10px 75px 5px 75px;"><font color="red">This tool is <b>BETA</b>.  Some users have reported that it corrupts message data while others have had no problems.  Testing is encouraged, and reports for both working and non-working configurations can be reported at <a href="http://jamwiki.org/wiki/en/Known_Issues#International_Character_Sets">jamwiki.org</a>.</font></div>
<p><f:message key="translation.message.instructions" /></p>
<table>
<tr>
<td>
<form name="adminTranslation" method="get" action="<jamwiki:link value="Special:Translation" />">
<%-- FIXME : hard coding --%>
<select name="language" onchange="this.form.submit()">
<option value=""></option>
<c:forEach items="${codes}" var="code">
<option value="<c:out value="${code}" />"><c:out value="${code}" /></option>
</c:forEach>
</select>
</form>
</td>
<td>&#160;&#160;&#160;&#160;&#160;</td>
<td>
<form name="adminTranslation" method="get" action="<jamwiki:link value="Special:Translation" />">
<input type="text" name="language" size="5" value="<c:out value="${language}" />" />&#160;&#160;<input type="submit" name="submit" value="<f:message key="common.change" />" />
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
	<td><textarea name="translations[<c:out value="${translation.key}" />]" style="overflow:auto;width:30em;height:4em;"><c:out value="${translation.value}" /></textarea></td>
</tr>
</c:forEach>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="common.save" />" /></td></tr>
</table>
</form>
