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
<c:if test="${!special}">
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?<c:out value="${encodedTopic}" />"><f:message key="menu.article" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?Comments:<c:out value="${encodedTopic}" />"><f:message key="menu.comments" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_EDIT %>"><f:message key="menu.editpage" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_HISTORY %>&type=all"><f:message key="menu.history" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_ATTACH %>"><f:message key="menu.attach" /></a></td>
	</c:if>
	<%-- FIXME: admin only --%>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_DELETE %>"><f:message key="menu.delete" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<c:out value="${pathRoot}" />Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_PRINT %>" target="_blank"><f:message key="menu.printablepage" /></a></td>
</c:if>
<c:if test="${special}">
	<td class="menu-tab-nonselected"><f:message key="menu.special" /></td>
</c:if>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>