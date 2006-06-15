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

<div class="menu">
  <c:if test="${!readOnly}">
        <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${topic}"/>&action=<%= WikiServlet.ACTION_EDIT %>'><f:message key="menu.editpage"/></a> |
  </c:if>
        <a href='<c:out value="${pathRoot}"/>Wiki?RecentChanges'><f:message key="generalmenu.recentchanges"/></a> |
        <a href='<c:out value="${pathRoot}"/>Wiki?<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>'><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a> |
        <a href='<c:out value="${pathRoot}"/>Wiki?WikiSearch'><f:message key="generalmenu.search"/></a>
  <c:if test="${!readOnly}">
       | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${topic}"/>&action=<%= WikiServlet.ACTION_ATTACH %>'><f:message key="menu.attach"/></a>
  </c:if>
      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${topic}"/>&action=<%= WikiServlet.ACTION_PRINT %>' target="_blank"><f:message key="menu.printablepage"/></a>
  <c:if test="${!empty lastRevisionDate}">
      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${topic}"/>&action=<%= WikiServlet.ACTION_DIFF %>'>
        <f:formatDate value="${lastRevisionDate}" type="BOTH" dateStyle="SHORT" timeStyle="DEFAULT" var="formattedDate"/>
          <f:message key="menu.lastedit">
            <f:param value="${formattedDate}"/>
          </f:message>
          <c:if test="${!empty lastAuthor}">
            <f:message key="menu.author">
              <f:param value="${lastAuthor}"/>
            </f:message>
          </c:if>
        </a>
      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${topic}"/>&action=<%= WikiServlet.ACTION_HISTORY %>&type=all'><f:message key="menu.history"/></a>
  </c:if>

</div>

