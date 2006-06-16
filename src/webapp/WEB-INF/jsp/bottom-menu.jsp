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
<div class="menu" align="right">
  <jmwiki:encode var="encodedTopic" value="${topic}"/>
  <c:if test="${!readOnly}">
        <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${encodedTopic}"/>&action=<%= WikiServlet.ACTION_EDIT %>'><f:message key="menu.editpage"/></a>
  </c:if>
      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${encodedTopic}"/>&action=<%= WikiServlet.ACTION_HISTORY %>&type=all'><f:message key="menu.history"/></a>
  <c:if test="${!empty lastRevisionDate}">
      | <a href='<c:out value="${pathRoot}"/>Wiki?topic=<c:out value="${encodedTopic}"/>&action=<%= WikiServlet.ACTION_DIFF %>'>
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
  </c:if>
  &nbsp;
</div>

