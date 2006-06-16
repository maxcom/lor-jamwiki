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
<%@ include file="generaltop.jsp"%>

<form name="form" method="post" action="Wiki">
  <p>
    <input type="hidden" name="topic" value='<c:out value="${topic}"/>'/>
    <input type="submit" name="action" value='<f:message key="edit.action.save"/>'/>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
    <input type="submit" name="action" value="<f:message key="edit.action.preview"/>"/>
<%
}
%>
    <input type="submit" name="action" value='<f:message key="edit.action.cancel"/>'/>
<%
    if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_TEMPLATES)) {
%>
      &nbsp;<f:message key="edit.appendtemplate"/>
      <select name="templateabove">
        <option><f:message key="edit.notemplate"/></option>
        <c:if test="${!empty templateNames}">
          <c:forEach items="${templateNames}" var="name">
            <option><c:out value="${name}"/></option>
          </c:forEach>
        </c:if>
      </select>
      <input type="submit" name="action" value='<f:message key="edit.action.append"/>'/>
<%
}
%>
    <f:message key="edit.spacetotabs"/>
    <input type="checkbox" name="convertTabs" value="true"<%= (Environment.getBooleanValue(Environment.PROP_TOPIC_CONVERT_TABS)) ? " checked" : "" %> />
  </p>
  <script type="text/javascript" src="../js/edit.js" language="JavaScript1.3"></script>
  <p>
    <textarea name="contents" cols="80" rows="26"><c:out value="${contents}" escapeXml="false"/></textarea>
  </p>
  <p>
    <input type="submit" name="action" value="<f:message key="edit.action.save"/>"/>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
    <input type="submit" name="action" value="<f:message key="edit.action.preview"/>"/>
<%
}
%>
    <input type="submit" name="action" value="<f:message key="edit.action.cancel"/>"/>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_TEMPLATES)) {
%>
      &nbsp;<f:message key="edit.appendtemplate"/>
      <select name="templatebelow">
        <option><f:message key="edit.notemplate"/></option>
        <c:if test="${!empty templateNames}">
          <c:forEach items="${templateNames}" var="name">
            <option><c:out value="${name}"/></option>
          </c:forEach>
        </c:if>
      </select>
      <input type="submit" name="action" value='<f:message key="edit.action.append"/>'/>
<%
}
%>
    &nbsp;&nbsp;&nbsp;
    <input type="checkbox" name="minorEdit"/>
    <f:message key="edit.isMinorEdit"/>
  </p>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_TEMPLATES)) {
%>
    <f:message key="edit.template.save1"/> <input type="text" name="save-template"/> <f:message key="edit.template.save2"/> <input type="submit" value='<f:message key="edit.action.savetemplate"/>' name="action"/>
<%
}
%>
</form>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
<table border="1" rules="group" bgcolor="#FFFFCC" width="100%">
<tr><td>
<h3>Preview</h3>
</td></tr>
<tr><td class="contents">
<c:out value="${preview}" escapeXml="false" />
</td></tr>
</table>
<%
}
%>
<c:set var="quickhelp"><f:message key="edit.quickhelppage"/></c:set>
<c:import url="${quickhelp}"/>
<%@include file="close-document.jsp"%>

