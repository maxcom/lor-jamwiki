<%@ page import="org.vqwiki.Environment,
                 org.vqwiki.WikiBase"%>
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
      <div class="contents">
        <div class="contents" >
        <span class="pageHeader">
          <c:out value="${title}"/>
        </span><p/>


        <table cellspacing="0" cellpadding="0" border="0">
        <tr><td width="20"><spacer width="20" height="1"></td>
            <td width="20"><spacer width="20" height="1"></td>
          <td></td>
          <td></td>
        </tr>
        <c:choose>
          <c:when test="${virtualwikis.showwikis}">
        <tr><td colspan="3"><f:message key="statistics.numwikis"/></td>
            <td class="recent"><c:out value="${virtualwikis.numwikis}"/></td></tr>
          </c:when>
        </c:choose>

        <c:forEach items="${virtualwikis.vwiki}" var="vwiki">
        <tr><td colspan="4"><b><f:message key="statistics.vwiki.name"><f:param value="${vwiki.name}"/></f:message></b></td></tr>
        <tr><td width="20"><spacer width="20" height="1"></td><td colspan="2"><f:message key="statistics.vwiki.numpages"/></td>
            <td class="recent"><c:out value="${vwiki.numpages}"/></td></tr>
        <tr><td colspan="4">&nbsp;</td></tr>
        <tr><td width="20"><spacer width="20" height="1"></td>
            <td colspan="3" class="recent"><b><i><f:message key="statistics.vwiki.activity"/></i></b></td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td>
            <td><f:message key="statistics.vwiki.numchanges"/></td>
            <td class="recent"><c:out value="${vwiki.numchanges}"/></td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td>
            <td colspan="2"><f:message key="statistics.vwiki.nummodifications"><f:param value="${vwiki.nummodifications}"/></f:message></td></tr>
        <tr><td colspan="4">&nbsp;</td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td>
            <td><f:message key="statistics.vwiki.numpageslw"/></td>
            <td class="recent"><c:out value="${vwiki.numpageslw}"/></td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td><td><f:message key="statistics.vwiki.numchangeslw"/></td>
            <td class="recent"><c:out value="${vwiki.numchangeslw}"/></td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td><td><f:message key="statistics.vwiki.ratiolw"/></td>
            <td class="recent"><c:out value="${vwiki.ratiolw}"/></td></tr>
        <tr><td colspan="4">&nbsp;</td></tr>

        <c:forEach items="${vwiki.months}" var="month">
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td><td><f:message key="statistics.month.pages"><f:param value="${month.name}"/></f:message></td>
            <td class="recent"><c:out value="${month.pages}"/></td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td><td><f:message key="statistics.month.changes"><f:param value="${month.name}"/></f:message></td>
            <td class="recent"><c:out value="${month.changes}"/></td></tr>
        <tr><td colspan="2" width="40"><spacer width="40" height="1"></td><td><f:message key="statistics.month.ratio"/></td>
            <td class="recent"><c:out value="${month.ratio}"/></td></tr>
        <tr><td colspan="4">&nbsp;</td></tr>
        </c:forEach>

         <tr><td width="20"><spacer width="20" height="1"></td>
             <td colspan="2"><b><i><f:message key="statistics.authors.numauthors"/></i></b></td>
             <td class="recent"><c:out value="${vwiki.numauthors}"/></td></tr>
         <tr><td colspan="2" width="40"><spacer width="40" height="1"></td>
             <td><i><f:message key="statistics.authors.name"/></i></td><td><i><f:message key="statistics.authors.numchanges"/></i></td></tr>
             <c:forEach items="${vwiki.authors}" var="author">
         <tr><td colspan="2" width="40"><spacer width="40" height="1"></td>
             <td class="recent"><c:out value="${author.name}"/></td>
             <td class="recent"><c:out value="${author.changes}"/></td></tr>
            </c:forEach>
        </c:forEach>
        </table>
      </div>
      </div>
      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>


<%@ include file="close-document.jsp"%>

