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
<%@ page import="org.jmwiki.Environment,
                 org.jmwiki.WikiBase"%>
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
      <div class="contents">
        <form name="searchForm" method="post" action="Wiki">
          <f:message key="search.for"/><input type="text" name="text">  <input type="submit" name="Submit" value="<f:message key="search.search"/>">
        <p>&nbsp;</p>
        <f:message key="search.hints"/>

          <input type="hidden" name="action" value="<%= WikiServlet.ACTION_SEARCH %>"/>
        </form>
        <p>&nbsp;</p>
        <font size="-1"><i>search powered by</i></font> <a href="http://jakarta.apache.org/lucene"><img src="../images/lucene_green_100.gif" alt="Lucene" border="0" /></a>
      </div>
          <script language="JavaScript">document.searchForm.text.focus();</script>
      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>


<%@ include file="close-document.jsp"%>

