<%--
Very Quick Wiki - WikiWikiWeb clone
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
      <f:message key="virtualwiki.${virtualWiki}.name" var="wikiname"/>
      <vqwiki:wikibase var="wb"/>
      <c:if test="${wb.virtualWikiCount > 1}">
          <c:if test="${env.allowVirtualWikiList && wb.virtualWikiCount > 1}"><a href="Wiki?VirtualWikiList"></c:if>
          <b><f:message key="common.wiki"/><c:if test="${env.allowVirtualWikiList && wb.virtualWikiCount > 1}"></a></c:if> :
          <a href='<vqwiki:path-root/>/Wiki?StartingPoints'>
          <c:out value="${wikiname}"/></a></b> :
      </c:if>