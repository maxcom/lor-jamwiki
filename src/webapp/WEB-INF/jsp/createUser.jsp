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
<%@ page import="org.vqwiki.*"%>
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
      <%@ include file="generalmenu.jsp"%>
      <div class="contents">
      <c:choose>
        <c:when test="${param.saved==true}">
          <f:message key="createuser.ok.text1"><f:param value="${param.username}"/></f:message>
        </c:when>
        <c:otherwise>
          <p>
            <f:message key="createuser.info.text1"/>
          </p>
          <form name="form1" method="post" action="Wiki?action=<%= WikiServlet.ACTION_SAVE_USER %>">
            <f:message key="createuser.form.name"/>
<%
if (Environment.getIntValue(Environment.PROP_USERGROUP_TYPE) == 0) {
%>
	            <input type="text" name="username">
<%
} else {
%>
	               <select name="username">
					 <c:forEach items="${userList}" var="listItem"><option value="<c:out value="${listItem.key}"/>"><c:out value="${listItem.label}"/></option>
					 </c:forEach>
	 			  </select>
<%
}
%>
            <input type="submit" name="Submit" value="<f:message key="createuser.form.save"/>">
            <input type="hidden" name="saved" value="true">
            <c:if test="${!empty param.topic}">
              <input type="hidden" name="topic" value='<c:out value="${param.topic}"/>'/>
            </c:if>
          </form>
        </c:otherwise>
      </c:choose>
      </div>
      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>

<%@ include file="close-document.jsp"%>