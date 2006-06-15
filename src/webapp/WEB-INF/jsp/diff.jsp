
<link rel="stylesheet" href="vqwiki.css" type="text/css">
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

      <p><a href='<vqwiki:path-root/>/Wiki?<c:out value="${topic}"/>'><f:message key="diff.text1"/> <c:out value="${topic}"/><f:message key="diff.text2"/></a></p>
      <%@ include file="top-menu.jsp"%>
      <div class="contents">
        <div class="contents" >
          <span class="pageHeader">
              <c:out value="${title}"/>
          </span><p/>

          <c:if test="${!badinput}">
            <c:out value="${diff}" escapeXml="false"/>
          </c:if>
          <c:if test="${badinput=='true'}">
            <f:message key="diff.badinput"/>
          </c:if>
          </div>
        </div>      
      <%@ include file="bottom-menu.jsp"%>      
    </td>
  </tr>
</table>


<%@ include file="close-document.jsp"%>
