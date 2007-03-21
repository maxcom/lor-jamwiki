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
	<td class="menu-tab-nonselected"><jamwiki:link value="${article}"><f:message key="tab.common.article" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="${comments}"><f:message key="tab.common.comments" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${pageInfo.editable}">
	<td class="menu-tab-nonselected"><jamwiki:link value="${edit}"><f:message key="tab.common.edit" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:History"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="tab.common.history" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${pageInfo.moveable}">
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Move"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="tab.common.move" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<c:if test="${!empty user}">
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Watchlist"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><c:if test="${pageInfo.watched}"><f:message key="tab.common.unwatch" /></c:if><c:if test="${!pageInfo.watched}"><f:message key="tab.common.watch" /></c:if></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:LinkTo"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="tab.common.links" /></jamwiki:link></td>
	<c:if test="${adminUser}">
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Manage"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="tab.common.manage" /></jamwiki:link></td>
	</c:if>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Print" target="${pageInfo.printTarget}"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><f:message key="tab.common.print" /></jamwiki:link></td>
</c:if>
<c:if test="${pageInfo.special}">
	<td class="menu-tab-nonselected"><jamwiki:link value="${pageInfo.topicName}"><f:message key="tab.common.special" /></jamwiki:link></td>
</c:if>
<c:if test="${pageInfo.admin}">
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Admin"><f:message key="tab.admin.configuration" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Maintenance"><f:message key="tab.admin.maintenance" /></jamwiki:link></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><jamwiki:link value="Special:Translation"><f:message key="tab.admin.translations" /></jamwiki:link></td>
</c:if>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>