<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<%@ page import="
    org.jamwiki.Environment,
    org.jamwiki.servlets.JAMWikiServlet
" errorPage="/WEB-INF/jsp/error.jsp" %>

<%@ include file="page-init.jsp" %>

<%
if (action.equals(JAMWikiServlet.ACTION_EDIT_RESOLVE)) {
%>

<%-- FIXME - hard coding --%>
<p>WARNING: Someone else has edited this topic.  The current version of the topic is show in the first box below, and your version is shown in the second box below.  Please manually resolve any differences and re-save.</p>

<%
}
%>

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW) && action.equals(JAMWikiServlet.ACTION_PREVIEW)) {
%>

<%-- FIXME - hard coding --%>
<blockquote><hr /><font color="red">Preview: changes have not been saved</font><hr /></blockquote>
<%@ include file="view-topic-include.jsp" %>

<%
}
%>

<form name="form" method="post" action="<jamwiki:link value="Special:Edit" />">
<p>
<input type="hidden" name="topic" value="<c:out value="${topic}"/>" />
<input type="hidden" name="lastTopicVersionId" value="<c:out value="${lastTopicVersionId}"/>" />
<%--
FIXME - restore the Javascript edit buttons
<script type="text/javascript" src="../js/edit.js" language="JavaScript1.3"></script>
--%>
<p>
<textarea name="contents" rows="25" cols="80" style="width:100%"><c:out value="${contents}" escapeXml="true" /></textarea>
</p>
<%-- FIXME - hard coding --%>
<p>Edit Comment: <input type="text" name="editComment" value="<c:out value="${editComment}" />" size="60" /></p>
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
&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" name="minorEdit"<c:if test="${minorEdit}"> checked</c:if> />
<f:message key="edit.isMinorEdit"/>
</p>

<%
if (action.equals(JAMWikiServlet.ACTION_EDIT_RESOLVE)) {
%>

<%@ include file="diff-include.jsp" %>

<p>
<textarea name="contentsResolve" rows="25" cols="80" style="width:100%"><c:out value="${contentsResolve}" escapeXml="true" /></textarea>
</p>

<%
}
%>

</form>
