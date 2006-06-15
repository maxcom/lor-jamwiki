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
<%@ page import="java.util.*,org.vqwiki.*"%>
<%
  String virtualWiki = (String)request.getAttribute("virtual-wiki");
  Collection all = null;
  String title = "";
  WikiBase wb = WikiBase.getInstance();
  if( request.getParameter("orphaned") == null && request.getAttribute("orphaned") == null &&
      request.getParameter("todo") == null && request.getAttribute("todo") == null
  ){
    all = wb.getSearchEngineInstance().getAllTopicNames(virtualWiki);
    title = "AllWikiTopics";
  }
  else if( request.getParameter("orphaned") != null || request.getAttribute("orphaned") != null ) {
    all = wb.getOrphanedTopics(virtualWiki);
    title = "OrphanedWikiTopics";
  }
  else if( request.getParameter("todo") != null || request.getAttribute("todo") != null ) {
    all = wb.getToDoWikiTopics(virtualWiki);
    title = "ToDoWikiTopics";
  }
%>
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
      <div class="contents" >
        <div class="contents" >
          <span class="pageHeader">
              <c:out value="${title}"/>
          </span><p/>
          <table>
            <%
              if( all.isEmpty() ){%>
              <p class="red"><f:message key="alltopics.notopics"/></p>
            <%}
              else{
            %>
              <tr><td><f:message key="alltopics.topics"><f:param><%=all.size()%></f:param></f:message></td></tr>
            <%
                Iterator it = all.iterator();
                while (it.hasNext()) {
                  String topicName = (String) it.next();%>
              <tr><td class="recent"><a href="Wiki?<%=topicName%>"><%=topicName%></a></td></tr>
              <%
                }
              }
            %></table>
        </div>
      </div>
      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>


<%@include file="close-document.jsp"%>