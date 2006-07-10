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
<form method="post" action="<jamwiki:link value="Special:Login" />">
<input type="hidden" name="redirect" value="<c:out value="${redirect}"/>" />
<table>
<c:if test="${!empty errorMessage}">
<tr><td colspan="2" class="red"><c:out value="${errorMessage}" /></td></tr>
</c:if>
<tr>
	<td><f:message key="login.username"/></td>
	<td><input type="text" name="username" value='<c:out value="${param.username}"/>'/></td>
</tr>
<tr>
	<td><f:message key="login.password"/></td>
	<td><input type="password" name="password"/></td>
</tr>
<tr>
	<td>&#160;</td>
	<td><input type="checkbox" value="true" name="remember" />&#160;Remember Me</td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td><input type="submit" name="function" value='<f:message key="login.submit"/>'/></td>
</tr>
</table>
</form>
