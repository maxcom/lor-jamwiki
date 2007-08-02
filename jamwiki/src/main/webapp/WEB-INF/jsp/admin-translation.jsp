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

<div class="message"><f:message key="translation.message.instructions" /></div>

<fieldset>
<legend><f:message key="translation.title" /></legend>

<table>
<tr>
<td>
<form name="adminTranslation" method="get" action="<jamwiki:link value="Special:Translation" />">
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
<tr class="<jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="translation" />">
	<td><label for="translations[<c:out value="${translation.key}" />]"><c:out value="${translation.key}" /></label></td>
	<td><textarea name="translations[<c:out value="${translation.key}" />]" style="overflow:auto;width:30em;height:4em;" id="translations[<c:out value="${translation.key}" />]"><c:out value="${translation.value}" /></textarea></td>
</tr>
</c:forEach>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="common.save" />" /></td></tr>
</table>
</form>

</fieldset>
