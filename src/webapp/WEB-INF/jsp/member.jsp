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
      <%@ include file="generalmenu.jsp"%>
      <div class="contents">
      <c:choose>
        <c:when test="${empty user}">
          <f:message key="member.setusername.text1"/><a href='Wiki?SetUsername'><f:message key="member.setusername.text2"/></a><f:message key="member.setusername.text3"/>
        </c:when>
        <c:otherwise>
          <c:choose>
            <c:when test="${type=='newMember'}">
              <form action="Wiki" method="POST">
              <f:message key="member.newmember.text1">
                <f:param value="${user}"/>
              </f:message><br/>
              <input type="hidden" name="action" value="<%= WikiServlet.ACTION_MEMBER %>"/>
<%
if (Environment.getBooleanValue(Environment.PROP_USERGROUP_TYPE)) {
%>
                  <input type="text" name="email" size="30" />
<%
} else {
%>
	               <tt><c:out value="${knownEmail}"/></tt><br/><input type="hidden" name="email" value="<c:out value="${knownEmail}"/>" />
<%
}
%>
              <input type="submit" value="<f:message key="member.registeraction"/>" />
              </form>
            </c:when>
            <c:when test="${type=='pendingMember'}">
              <f:message key="member.pending.text1"/>
              <form action="Wiki" method="POST">
              <input type="hidden" name="action" value="<%= WikiServlet.ACTION_MEMBER %>"/>
              <input type="text" name="email" size="30" />
              <input type="submit" value="<f:message key="member.registeraction"/>" />
              </form>
            </c:when>
            <c:when test="${type=='membershipRequested'}">
              <f:message key="member.requested"/>
            </c:when>
            <c:when test="${type=='confirmation'}">
              <c:choose>
                <c:when test="${valid}">
                  <f:message key="member.confirm"/>
                </c:when>
                <c:otherwise>
                  <f:message key="member.error.text1"/><a href="Wiki?action=<%= WikiServlet.ACTION_MEMBER %>"><f:message key="member.error.text2"/></a><f:message key="member.error.text3"/>
                </c:otherwise>
              </c:choose>
            </c:when>
          </c:choose>
        </c:otherwise>
      </c:choose>
      </div>
      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>


<%@ include file="close-document.jsp"%>

