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
<a href="#assign"><f:message key="roles.header.group" /> | <a href="#assign"><f:message key="roles.header.user" /></a> | <a href="#create"><f:message key="roles.header.modify" /></a>
</div>

<c:if test="${!empty message}">
<div class="message red"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div>
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
<table border="0" class="contents" width="100%">
<tr bgcolor="#d8d8e7">
	<th><f:message key="roles.caption.groupname" /></th>
	<th colspan="3"><f:message key="roles.caption.roles" /></th>
</tr>
<c:forEach items="${roleMapGroups}" var="roleMap">
	<c:if test="${!empty roleMap.groupId}">
<tr bgcolor="<jamwiki:alternate value1="#ffffff" value2="#e9e9f8" attributeName="groupList" />">
	<td>
		<input type="hidden" name="candidateGroup" value="<c:out value="${roleMap.groupId}" />" />
		<c:out value="${roleMap.groupName}" />
	</td>
		<c:forEach items="${roles}" var="role" varStatus="status">
			<c:if test="${((3 * status.index) % roleCount) < 3}"><td></c:if>
			<jamwiki:checkbox name="groupRole" value="${roleMap.userGroup}|${role.name}" checked="${roleMap.roleNamesMap[role.name]}" />&#160;<c:out value="${role.name}" /><br />
			<c:if test="${((3 * status.count) % roleCount) < 3}"></td></c:if>
		</c:forEach>
</tr>
	</c:if>
</c:forEach>
<tr><td colspan="4">&nbsp;</td></tr>
<tr><td colspan="4" class="formelement" align="center"><input type="submit" name="Submit" value="<f:message key="common.save" />" /></td></tr>
<tr><td colspan="4">&nbsp;</td></tr>
</table>
</fieldset>
</form>

<!-- Assign User Roles -->
<a name="user"></a>
<fieldset>
<legend><f:message key="roles.header.user" /></legend>
<form action="<jamwiki:link value="Special:Roles" />" method="post" name="searchRoleForm">
<input type="hidden" name="function" value="searchRole" />
<table border="0" class="contents">
<tr>
	<td class="formcaption"><f:message key="roles.caption.searchlogin" />:</td>
	<td class="formelement"><input type="text" name="searchLogin" value="<c:out value="${searchLogin}" />" size="30" /></td>
	<td align="center"><input type="submit" name="search" value="<f:message key="search.search" />" /></td>
</tr>
<tr>
	<td class="formcaption"><f:message key="roles.caption.searchrole" />:</td>
	<td class="formelement" colspan="2">
		<select name="searchRole" id="searchRole" onchange="document.searchRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${roles}" var="role"><option value="<c:out value="${role.name}" />" <c:if test="${role.name == searchRole}">selected="selected"</c:if>><c:out value="${role.name}" /></option></c:forEach>
		</select>
	</td>
</tr>
<tr><td colspan="3">&nbsp;</td></tr>
</table>
</form>
<c:if test="${!empty roleMapUsers}">
<form action="<jamwiki:link value="Special:Roles" />" method="post">
<input type="hidden" name="function" value="assignRole" />
<table border="0" class="contents" width="100%">
<tr bgcolor="#d8d8e7">
	<th><f:message key="roles.caption.userlogin" /></th>
	<th colspan="3"><f:message key="roles.caption.roles" /></th>
</tr>
<c:forEach items="${roleMapUsers}" var="roleMap">
	<c:if test="${!empty roleMap.userId}">
<tr bgcolor="<jamwiki:alternate value1="#ffffff" value2="#e9e9f8" attributeName="userList" />">
	<td>
		<input type="hidden" name="candidateUser" value="<c:out value="${roleMap.userId}" />" />
		<c:out value="${roleMap.userLogin}" />
	</td>
		<c:forEach items="${roles}" var="role" varStatus="status">
			<c:if test="${((3 * status.index) % roleCount) < 3}"><td></c:if>
			<jamwiki:checkbox name="userRole" value="${roleMap.userGroup}|${role.name}" checked="${roleMap.roleNamesMap[role.name]}" />&#160;<c:out value="${role.name}" /><br />
			<c:if test="${((3 * status.count) % roleCount) < 3}"></td></c:if>
		</c:forEach>
</tr>
	</c:if>
</c:forEach>
<tr><td colspan="4">&nbsp;</td></tr>
<tr><td colspan="4" class="formelement" align="center"><input type="submit" name="Submit" value="<f:message key="common.save" />" /></td></tr>
<tr><td colspan="4">&nbsp;</td></tr>
</table>
</form>
</c:if>
</fieldset>

<!-- Create/Update Roles -->
<form action="<jamwiki:link value="Special:Roles" />" name="modifyRoleForm" method="post">
<input type="hidden" name="function" value="modifyRole" />
<a name="create"></a>
<fieldset>
<legend><f:message key="roles.header.modify" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><f:message key="roles.caption.selectrole" />:</td>
	<td class="formelement">
		<select name="updateRole" id="updateRole" onchange="document.modifyRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${roles}" var="role"><option value="<c:out value="${role.name}" />" <c:if test="${role.name == roleName}">selected="selected"</c:if>><c:out value="${role.name}" /></option></c:forEach>
		</select>
	</td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="roles.caption.selectrolehelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="roleName"><f:message key="roles.caption.name" /></label></td>
	<td class="formelement"><input type="text" name="roleName" id="roleName" value="<c:out value="${roleName}" />" size="30" <c:if test="${!empty roleName}">disabled="disabled"</c:if> /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="roles.caption.namehelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="roleDescription"><f:message key="roles.caption.description" /></label></td>
	<td class="formelement"><textarea cols="30" rows="3" name="roleDescription" id="roleDescription"><c:out value="${roleDescription}" /></textarea></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="roles.caption.descriptionhelp" /></td></tr>
</table>
<table border="0" class="contents" width="100%">
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td colspan="2" class="formelement" align="center"><input type="submit" name="Submit" value="<f:message key="common.save" />" /></td></tr>
<tr><td colspan="2">&nbsp;</td></tr>
</table>
</fieldset>
</form>
