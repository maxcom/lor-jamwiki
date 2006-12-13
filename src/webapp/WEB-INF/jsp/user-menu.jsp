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

<%@ taglib uri="/WEB-INF/authz.tld" prefix="authz" %>

<table class="menu-user-table">
<tr>
	<authz:authorize ifAllGranted="ROLE_ANONYMOUS" ifNotGranted="ROLE_EMBEDDED">
		<td class="menu-user"><jamwiki:link value="Special:Login"><f:message key="common.login" /></jamwiki:link> / <jamwiki:link value="Special:Account"><f:message key="usermenu.register" /></jamwiki:link></td>
	</authz:authorize>
	<authz:authorize ifNotGranted="ROLE_ANONYMOUS">
		<td class="menu-user"><jamwiki:link value="${userpage}"><c:if test="${!empty user.displayName}"><c:out value="${user.displayName}" /></c:if><c:if test="${empty user.displayName}"><c:out value="${user.username}" /></c:if></jamwiki:link></td>
		<td class="menu-user"><jamwiki:link value="${usercomments}"><f:message key="usermenu.usercomments" /></jamwiki:link></td>
		<td class="menu-user"><jamwiki:link value="Special:Watchlist"><f:message key="usermenu.watchlist" /></jamwiki:link></td>
		<authz:authorize ifNotGranted="ROLE_NO_ACCOUNT">
			<td class="menu-user"><jamwiki:link value="Special:Account"><f:message key="usermenu.account" /></jamwiki:link></td>
		</authz:authorize>
		<authz:authorize ifNotGranted="ROLE_EMBEDDED">
			<td class="menu-user"><jamwiki:link value="Special:Logout"><f:message key="common.logout" /></jamwiki:link></td>
		</authz:authorize>
		<authz:authorize ifAllGranted="ROLE_ADMIN">
			<td class="menu-user"><jamwiki:link value="Special:Admin"><f:message key="usermenu.admin" /></jamwiki:link></td>
		</authz:authorize>
	</authz:authorize>
</tr>
</table>
