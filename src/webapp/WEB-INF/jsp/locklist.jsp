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
<%@ include file="top.jsp"%>
<c:out value="${topArea}" escapeXml="false"/>


<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <td>
      <div class="navbar">
        <%@ include file="navbar-virtual-wiki.jsp"%>
        <%@ include file="navbar-history-list.jsp"%>
        &nbsp; <!-- to render the bar even when empty -->
      </div>
    </td>
  </tr>
</table>
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
      <td nowrap class="leftMenu" valign="top" width="10%">
        <c:out value="${leftMenu}" escapeXml="false"/>
      </td>
    </c:if>
    <td valign="top">
      <%@include file="generalmenu.jsp"%>

        <c:choose>
          <c:when test="${empty locks}">
            <div class="contents">
              <div class="contents" >
                <span class="pageHeader">
                    <c:out value="${title}"/>
                </span><p/>

                <f:message key="locklist.nolocks"/>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="contents">
              <div class="contents" >
                <span class="pageHeader">
                    <c:out value="${title}"/>
                </span><p/>

                <table>
                <tr>
                  <th><f:message key="common.topic"/></th><th><f:message key="locklist.lockedat"/></th>
                </tr>
                <c:forEach items="${locks}" var="lock">
                  <tr>
                    <vqwiki:encode value="${lock.topicName}" var="encodedTopic"/>
                    <td class="recent">
                      <a href='<c:out value="Wiki?${encodedTopic}"/>'>
                        <c:out value="${lock.topicName}"/>
                      </a>
                    </td>
                    </a>
                    <td class="recent">
                    <f:formatDate
                        value="${lock.time}"
                        type="both"
                        dateStyle="MEDIUM"
                        timeStyle="MEDIUM"
                      />
                    </td>
                    <td>
                      <a href='Wiki?topic=<c:out value="${encodedTopic}" />&action=<%= WikiServlet.ACTION_UNLOCK %>'>
                        <f:message key="locklist.unlock"/>

                      </a>
                    </td>
                  </tr>
                </c:forEach>
                </table>
              </div>
            </div>
          </c:otherwise>
        </c:choose>

      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>



<%@include file="close-document.jsp"%>