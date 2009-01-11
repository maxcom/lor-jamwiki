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

<div class="submenu">
<a href="#assign"><f:message key="roles.header.group" /></a> | <a href="#assign"><f:message key="roles.header.user" /></a> | <a href="#create"><f:message key="roles.header.modify" /></a>
</div>

<div class="message"><f:message key="roles.caption.instructions" /></div>

<c:if test="${!empty message}">
<div class="message green"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div>
</c:if>
<c:if test="${!empty errors}">
<div class="message red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></div>
</c:if>

<!-- Assign Group Roles -->
<form action="<jamwiki:link value="Special:Roles" />" method="post">
<input type="hidden" name="function" value="assignRole" />
<a name="group"></a>
<fieldset>
<legend><f:message key="roles.header.group" /></legend>
<table border="0" class="contents" width="99%">
<tr class="darkbg">
	<th><f:message key="roles.caption.groupname" /></th>
	<th colspan="3"><f:message key="roles.caption.roles" /></th>
</tr>
<c:forEach items="${roleMapGroups}" var="roleMap">
	<c:if test="${!empty roleMap.groupId}">
<tr class="<jamwiki:alternate value1="lightbg" value2="mediumbg" attributeName="userList" />">
	<td>
		<input type="hidden" name="candidateGroup" value="<c:out value="${roleMap.groupId}" />" />
		<c:out value="${roleMap.groupName}" />
	</td>
		<c:forEach items="${roles}" var="role" varStatus="status">
			<c:if test="${((3 * status.index) % roleCount) < 3}"><td></c:if>
			<jamwiki:checkbox name="groupRole" value="${roleMap.userGroup}|${role.authority}" checked="${roleMap.roleNamesMap[role.authority]}" />&#160;<c:out value="${role.authority}" /><br />
			<c:if test="${((3 * status.count) % roleCount) < 3}"></td></c:if>
		</c:forEach>
</tr>
	</c:if>
</c:forEach>
<tr><td colspan="4">&nbsp;</td></tr>
<tr><td colspan="4" class="formhelp"><f:message key="roles.help.grouproles" /></td></tr>
</table>
<div align="center" style="padding:10px"><input type="submit" name="Submit" value="<f:message key="common.save" />" /></div>
</fieldset>
</form>

<!-- Assign User Roles -->
<a name="user"></a>
<fieldset>
<legend><f:message key="roles.header.user" /></legend>
<form action="<jamwiki:link value="Special:Roles" />" method="post" name="searchRoleForm">
<input type="hidden" name="function" value="searchRole" />
<div class="formentry">
	<span class="formcaption"><f:message key="roles.caption.searchlogin" />:</span>
	<span class="formelement"><input type="text" name="searchLogin" value="<c:out value="${searchLogin}" />" size="30" /></span>
</div>
<div class="formentry">
	<span class="formcaption"><f:message key="roles.caption.searchrole" />:</span>
	<span class="formelement">
		<select name="searchRole" id="searchRole" onchange="document.searchRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${roles}" var="role"><option value="<c:out value="${role.authority}" />" <c:if test="${role.authority == searchRole}">selected="selected"</c:if>><c:out value="${role.authority}" /></option></c:forEach>
		</select>
	</span>
</div>
<div align="center" style="padding:10px"><input type="submit" name="search" value="<f:message key="search.search" />" /></div>
<table border="0" class="contents">
<tr><td colspan="3" class="formhelp"><f:message key="roles.help.userroles" /></td></tr>
<tr><td colspan="3">&nbsp;</td></tr>
</table>
</form>
<c:if test="${!empty roleMapUsers}">
<form action="<jamwiki:link value="Special:Roles" />" method="post">
<input type="hidden" name="function" value="assignRole" />
<table border="0" class="contents" width="99%">
<tr class="darkbg">
	<th><f:message key="roles.caption.userlogin" /></th>
	<th colspan="3"><f:message key="roles.caption.roles" /></th>
</tr>
<c:forEach items="${roleMapUsers}" var="roleMap">
	<c:if test="${!empty roleMap.userId}">
<tr class="<jamwiki:alternate value1="lightbg" value2="mediumbg" attributeName="userList" />">
	<td>
		<input type="hidden" name="candidateUser" value="<c:out value="${roleMap.userId}" />" />
		<input type="hidden" name="candidateUsername" value="<c:out value="${roleMap.userLogin}" />" />
		<c:out value="${roleMap.userLogin}" />
	</td>
		<c:forEach items="${roles}" var="role" varStatus="status">
			<c:if test="${((3 * status.index) % roleCount) < 3}"><td></c:if>
			<jamwiki:checkbox name="userRole" value="${roleMap.userGroup}|${role.authority}" checked="${roleMap.roleNamesMap[role.authority]}" />&#160;<c:out value="${role.authority}" /><br />
			<c:if test="${((3 * status.count) % roleCount) < 3}"></td></c:if>
		</c:forEach>
</tr>
	</c:if>
</c:forEach>
</table>
<div align="center" style="padding:10px"><input type="submit" name="Submit" value="<f:message key="common.save" />" /></div>
</form>
</c:if>
</fieldset>

<!-- Create/Update Roles -->
<form action="<jamwiki:link value="Special:Roles" />" name="modifyRoleForm" method="post">
<input type="hidden" name="function" value="modifyRole" />
<a name="create"></a>
<fieldset>
<legend><f:message key="roles.header.modify" /></legend>
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="create" />">
	<span class="formcaption"><f:message key="roles.caption.selectrole" />:</span>
	<span class="formelement">
		<select name="updateRole" id="updateRole" onchange="document.modifyRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${roles}" var="role"><option value="<c:out value="${role.authority}" />" <c:if test="${role.authority == roleName}">selected="selected"</c:if>><c:out value="${role.authority}" /></option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><f:message key="roles.help.selectrole" /></div>
</div>
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="create" />">
	<span class="formcaption"><label for="roleName"><f:message key="roles.caption.rolename" /></label></span>
	<span class="formelement"><input type="text" name="roleName" id="roleName" value="<c:out value="${roleName}" />" size="30" <c:if test="${!empty roleName}">disabled="disabled"</c:if> /></span>
	<div class="formhelp"><f:message key="roles.help.rolename" /></div>
</div>
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="create" />">
	<span class="formcaption"><label for="roleDescription"><f:message key="roles.caption.roledescription" /></label></span>
	<span class="formelement"><textarea class="medium" name="roleDescription" id="roleDescription"><c:out value="${roleDescription}" /></textarea></span>
	<div class="formhelp"><f:message key="roles.help.roledescription" /></div>
</div>
<div align="center" style="padding:10px"><input type="submit" name="Submit" value="<f:message key="common.save" />" /></div>
</fieldset>
</form>
