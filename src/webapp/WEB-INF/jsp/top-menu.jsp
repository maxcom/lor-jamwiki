 <%--
Java MediaWiki - WikiWikiWeb clone
Copyright (C) 2001-2003 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<div id="menu-tab">
<jmwiki:encode var="encodedTopic" value="${topic}" />
<table class="menu-tab-table">
<tr>
	<td class="menu-tab-space">&#160;</td>
<%
// FIXME - this needs to be cleaned up
boolean special = false;
try {
	if (request.getAttribute(WikiServlet.PARAMETER_SPECIAL) != null) {
		special = ((Boolean)request.getAttribute(WikiServlet.PARAMETER_SPECIAL)).booleanValue();
	}
	if (request.getParameter(WikiServlet.PARAMETER_SPECIAL) != null) {
		special = (new Boolean(request.getParameter(WikiServlet.PARAMETER_SPECIAL))).booleanValue();
	}
} catch (Exception e) {}
if (!special) {
%>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link var="${topic}" />"><f:message key="menu.article" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link var="Comments:${topic}" />"><f:message key="menu.comments" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Edit" />?topic=<jmwiki:encode value="${topic}" />"><f:message key="menu.editpage" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:History" />?topic=<jmwiki:encode value="${topic}" />&type=all"><f:message key="menu.history" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Attach" />?topic=<jmwiki:encode value="${topic}" />"><f:message key="menu.attach" /></a></td>
	</c:if>
	<%-- FIXME: admin only --%>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Delete" />?topic=<jmwiki:encode value="${topic}" />"><f:message key="menu.delete" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Print" />?topic=<jmwiki:encode value="${topic}" />" target="_blank"><f:message key="menu.printablepage" /></a></td>
<%
} else {
%>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link var="${topic}" />"><f:message key="menu.special" /></a></td>
<%
}
%>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>