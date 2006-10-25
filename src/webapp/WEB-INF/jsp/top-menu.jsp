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
<c:if test="${!pageInfo.special && !pageInfo.admin}">
	<td class="menu-tab-nonselected"><jamwiki:link value="${article}"><f:message key="menu.article" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="${comments}"><f:message key="menu.comments" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="${edit}"><f:message key="menu.editpage" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:History"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="menu.history" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${pageInfo.canMove}">
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Move"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="menu.move" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Watchlist"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="menu.watch" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:LinkTo"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="menu.links" /></jamwiki:link></td>
	<c:if test="${adminUser}">
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Manage"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="menu.manage" /></jamwiki:link></td>
	</c:if>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Print"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="menu.print" /></jamwiki:link></td>
</c:if>
<c:if test="${pageInfo.special}">
	<td class="menu-tab-nonselected"><jamwiki:link value="${pageInfo.topicName}"><f:message key="menu.special" /></jamwiki:link></td>
</c:if>
<c:if test="${pageInfo.admin}">
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Admin">Special:Admin</jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Translation">Special:Translation</jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Convert">Special:Convert</jamwiki:link></td>
</c:if>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>