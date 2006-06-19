<%--
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

<table class="menu-user-table">
<tr>
	<%-- FIXME: do not hardcode --%>
	<td class="menu-user"><a href="<c:out value="${pathRoot}"/>Special:SetUsername">Username</a></td>
	<td class="menu-user"><a href="<c:out value="${pathRoot}"/><%= JSPUtils.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>"><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a></td>
	<td class="menu-user"><a href="<c:out value="${pathRoot}"/><%= JSPUtils.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>"><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a></td>
</tr>
</table>
