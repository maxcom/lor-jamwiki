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
<div id="menu-tab">
<table class="menu-tab-table" cellspacing="0">
<tr>
	<td class="menu-tab-space">&#160;</td>
	<c:forEach items="${pageInfo.tabMenu}" var="menuItem" varStatus="status">
		<c:set var="menuText" value="${menuItem.value}" />
		<%-- FIXME - the print target check is an ugly hack.  need to find a better way. --%>
		<c:if test="${menuItem.key == 'Special:Print'}"><td class="menu-tab-nonselected"><jamwiki:link value="${menuItem.key}" target="${pageInfo.target}"><f:message key="${menuText.key}"><f:param value="${menuText.params[0]}" /></f:message></jamwiki:link></td></c:if>
		<c:if test="${menuItem.key != 'Special:Print'}"><td class="menu-tab-nonselected"><jamwiki:link value="${menuItem.key}"><f:message key="${menuText.key}"><f:param value="${menuText.params[0]}" /></f:message></jamwiki:link></td></c:if>
		<c:if test="${!status.last}"><td class="menu-tab-space">&#160;</td></c:if>
	</c:forEach>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>