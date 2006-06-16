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
<div class="menu">
  <jmwiki:encode var="encodedTopic" value="${topic}"/>
	<table style="width: 100%; border: 0px solid;">
		<tr>
			<td class="menu" align=left>
			  <c:if test="${!readOnly}">
			      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${encodedTopic}"/>&action=<%= WikiServlet.ACTION_EDIT %>'><f:message key="menu.editpage"/></a>
		          | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${encodedTopic}"/>&action=<%= WikiServlet.ACTION_ATTACH %>'><f:message key="menu.attach"/></a>
			  </c:if>
			  <c:if test="${readOnly}">
			      | <span class="menuinactive"><f:message key="menu.editpage"/></span>
		          | <span class="menuinactive"><f:message key="menu.attach"/></span>
			  </c:if>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?RecentChanges'><f:message key="generalmenu.recentchanges"/></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?<%= JSPUtils.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>'><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?WikiSearch'><f:message key="generalmenu.search"/></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${encodedTopic}"/>&action=<%= WikiServlet.ACTION_PRINT %>' target="_blank"><f:message key="menu.printablepage"/></a>
				  |
			</td>
		</tr>
	</table>
</div>