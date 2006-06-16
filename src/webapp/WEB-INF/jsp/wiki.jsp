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
<% response.setLocale(request.getLocale()); %>
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
 <jmwiki:encode var="encodedTitle" value='"${title}"'/>
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
      <td nowrap class="leftMenu" valign="top" width="10%">
        <c:out value="${leftMenu}" escapeXml="false"/>
      </td>
    </c:if>
    <td valign="top">
      <%@ include file="top-menu.jsp"%>
      <div class="contents" >
        <div class="contents" >
<%
if (Environment.getIntValue(Environment.PROP_USERGROUP_TYPE) != 0) {
%>
		    <c:choose>
			  <c:when test="${!empty lastAuthorDetails}">
			    <div class="authordetails">
			      <c:out value="${lastAuthorDetails}" escapeXml="false"/>
			    </div>
			  </c:when>
			  <c:otherwise>
			    <c:if test="${!empty lastAuthor}">
 			      <div class="authordetails">
			        <c:out value="${lastAuthor}"/>
			      </div>
			    </c:if>
			  </c:otherwise>
			</c:choose>
<%
}
%>
          <span class="pageHeader">
            <a href='<jmwiki:path-root/>/Wiki?action=<%= WikiServlet.ACTION_SEARCH %>&text=<c:out value="${encodedTitle}"/>'>
<%
if (Environment.getBooleanValue(Environment.PROP_PARSER_SEPARATE_WIKI_TITLE_WORDS)) {
%>
                  <jmwiki:separate-words value="${title}" />
<%
} else {
%>
                  <c:out value="${title}"/>
<%
}
%>
            </a>
          </span><p/>
          <c:out value="${contents}" escapeXml="false"/>
        </div>
      </div>
      <%@ include file="bottom-menu.jsp"%>
    </td>
  </tr>
</table>
<%
if (Utilities.emailAvailable()) {
%>
  <%@ include file="member-contents.jsp"%>
<%
}
%>
<hr/>
<%@ include file="close-document.jsp"%>