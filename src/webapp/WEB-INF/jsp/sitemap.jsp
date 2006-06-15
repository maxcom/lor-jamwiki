<%@ page import="vqwiki.Environment,
                 vqwiki.WikiBase"%>
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
      <% boolean isIE = ((HttpServletRequest)pageContext.getRequest()).getHeader("USER-AGENT").indexOf("MSIE") != -1;
         int count = 0;
         if (isIE) { %>
      <script language="JavaScript">
      <!--
      var picture_xp='../images/x+.png';
      var picture_xm='../images/x-.png';
      var picture_x='../images/x.png';
      var picture_ep='../images/e+.png';
      var picture_em='../images/e-.png';
      var picture_e='../images/e.png';

      var IE4 = (document.all) ? 1 : 0;
      var ver4 = (IE4) ? 1 : 0;

      function getIndex(el) {
          ind = null;
          for (i=0; i<document.layers.length; i++) {
              whichEl = document.layers[i];
              if (whichEl.id == el) {
                  ind = i;
                  break;
              }
          }
          return ind;
      }

      function arrange() {
          nextY = document.layers[firstInd].pageY + document.layers[firstInd].document.height;
          for (i=firstInd; i<document.layers.length; i++) {
              whichEl = document.layers[i];
              if (whichEl.visibility != "hide") {
                  whichEl.pageY = nextY;
                  nextY += whichEl.document.height;
              }
          }
      }

      function initIt(){
        divColl = document.all.tags("DIV");
        for (i=0; i<divColl.length; i++) {
          whichEl = divColl(i);
          if (whichEl.className == "child") whichEl.style.display = "block";
        }
      }

      function expandIt(el) {
        whichEl = eval(el + "Child");
        whichIm = event.srcElement;
        if (whichEl.style.display == "none") {
          whichEl.style.display = "block";
          whichIm.src = picture_xm;
        }
        else {
          whichEl.style.display = "none";
          whichIm.src = picture_xp;
        }
      }

      function expandItE(el) {
        whichEl = eval(el + "Child");
        whichIm = event.srcElement;
        if (whichEl.style.display == "none") {
          whichEl.style.display = "block";
          whichIm.src = picture_em;
        }
        else {
          whichEl.style.display = "none";
          whichIm.src = picture_ep;
        }
      }

      initIt();

      //<div  id='node_" . _ParentIDF . "_Child'  class='child'>- Bereich, der eingeklappt werden soll...</div>
      --><%

        java.util.Vector childNodes = new java.util.Vector();

      %>
      </SCRIPT>
        <c:forEach items="${virtualwikis.vwiki}" var="vwiki">
        <b><f:message key="sitemap.head"><f:param value="${vwiki.name}"/><f:param value="${vwiki.numpages}"/></f:message></b>
        <c:forEach items="${vwiki.pages}" var="line"><c:set value="${line.group}" var="mygroup" scope="page"/><%

        if (childNodes.size() > 0)
        {
        String myGroup = (String)pageContext.getAttribute("mygroup");
          String lastNode = (String)childNodes.get(childNodes.size() - 1);
          while (myGroup.length() <= (lastNode.length()+1) && childNodes.size() > 0) {
            pageContext.setAttribute("lastNode", lastNode);
          %></div><!-- <c:out value="${lastNode}"/> --><%
            childNodes.remove(childNodes.size() - 1);
            if (childNodes.size() > 0) {
              lastNode = (String)childNodes.get(childNodes.size() - 1);
          }
          }
        }

        %><div id="node_<c:out value="${line.group}"/>_Parent" class="parent"><%--

        begin with all the images

        --%><c:forEach items="${line.levels}" var="lev"><%--
        if we have children --%><c:if test="${line.hasChildren}"><c:choose><%--
        if it is an x --%><c:when test="${lev == 'x'}"><a href="#" onClick="expandIt('node_<c:out value="${line.group}"/>_'); return false;"><img src="../images/x-.png" widht="30" height="30" align="top"  name="imEx" border="0"></a></c:when><%--
        if it is an e --%><c:when test="${lev == 'e'}"><a href="#" onClick="expandItE('node_<c:out value="${line.group}"/>_'); return false;"><img src="../images/e-.png" widht="30" height="30" align="top" border="0" name="imEx"></a></c:when><%--
        otherwise     --%><c:otherwise><img src="../images/<c:out value="${lev}"/>.png" widht="30" height="30" align="top"></c:otherwise></c:choose></c:if><%--
        if there are no children: --%><c:if test="${!line.hasChildren}"><img src="../images/<c:out value="${lev}"/>.png" widht="30" height="30" align="top"></c:if><%--
        --%></c:forEach><a href="Wiki?<c:out value="${line.topic}"/>"><c:out value="${line.topic}"/></a></div>
        <c:if test="${line.hasChildren}"><div  id='node_<c:out value="${line.group}"/>_Child'  class='child'><c:set value="${line.group}" var="mygroup" scope="page"/><%
        childNodes.add((String)pageContext.getAttribute("mygroup"));
        %></c:if><% count++; %>
        </c:forEach>
        </c:forEach><%
        for(int i=childNodes.size() - 1; i >= 0; i--) {
            pageContext.setAttribute("lastNode", (String)childNodes.get(i));
          %></div><!-- <c:out value="${lastNode}"/> --><%
        }

       } else { %>
        <table cellspacing="0" cellpadding="0" border="0">
        <c:forEach items="${virtualwikis.vwiki}" var="vwiki">
        <tr><td><b><f:message key="sitemap.head"><f:param value="${vwiki.name}"/><f:param value="${vwiki.numpages}"/></f:message></b></td></tr>
        <c:forEach items="${vwiki.pages}" var="line">
        <tr><td height="30" valign="top"><c:forEach items="${line.levels}" var="lev"><img src="../images/<c:out value="${lev}"/>.png" widht="30" height="30" align="top"></c:forEach><a href="Wiki?<c:out value="${line.topic}"/>"><c:out value="${line.topic}"/></a></td></tr>
        </c:forEach>
        </c:forEach>
        </table>
      <% } %>
      </div>
      <%@include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>



<%@ include file="close-document.jsp"%>

