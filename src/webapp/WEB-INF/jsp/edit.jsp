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
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<c:if test="${pageInfo.actionEditResolve}">
<p><f:message key="edit.exception.conflict" /></p>
</c:if>

<c:if test="${!empty topicVersionId}"><p><f:message key="edit.warning.oldversion" /></p></c:if>

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
<c:if test="${pageInfo.actionEditPreview}">
<blockquote><hr /><font color="red"><f:message key="edit.warning.preview" /></font><hr /></blockquote>
</c:if>
<%
}
%>

<%@ include file="category-include.jsp" %>

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
<c:if test="${pageInfo.actionEditPreview}">
<%@ include file="view-topic-include.jsp" %>
</c:if>
<%
}
%>

<form name="form" method="post" name="editform" action="<jamwiki:link value="Special:Edit" />">
<p>
<input type="hidden" name="topic" value="<c:out value="${pageInfo.topicName}"/>" />
<input type="hidden" name="lastTopicVersionId" value="<c:out value="${lastTopicVersionId}"/>" />
<input type="hidden" name="section" value="<c:out value="${section}"/>" />
<input type="hidden" name="topicVersionId" value="<c:out value="${topicVersionId}"/>" />
<%--
FIXME - restore the Javascript edit buttons
<script type="text/javascript" src="../js/edit.js" language="JavaScript1.3"></script>
--%>
<p>
<textarea name="contents" rows="25" cols="80" style="width:100%"><c:out value="${contents}" escapeXml="true" /></textarea>
</p>
<p><label for="editComment"><f:message key="edit.caption.comment" /></label>: <input type="text" name="editComment" value="<c:out value="${editComment}" />" size="60" id="editComment" /></p>
<p>
<input type="submit" name="save" value="<f:message key="common.save"/>"/>

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
<input type="submit" name="preview" value="<f:message key="edit.action.preview"/>"/>
<%
}
%>

&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" name="minorEdit"<c:if test="${minorEdit}"> checked</c:if> id="minorEdit" />
<label for="minorEdit"><f:message key="edit.isMinorEdit"/></label>
</p>

<c:if test="${pageInfo.actionEditResolve}">
<%@ include file="diff-include.jsp" %>
<p>
<textarea name="contentsResolve" rows="25" cols="80" style="width:100%"><c:out value="${contentsResolve}" escapeXml="true" /></textarea>
</p>
</c:if>

</form>
