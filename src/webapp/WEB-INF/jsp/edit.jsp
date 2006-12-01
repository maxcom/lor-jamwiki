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
        org.jamwiki.Environment
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

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_WYSIWYG)) {
%>
<script type="text/javascript" src="../js/edit.js" language="JavaScript1.3"></script>

<script type='text/javascript'>
/*<![CDATA[*/
document.writeln("<div id='toolbar'>");
addButton('../images/button_bold.png','<f:message key="edit.button.bold"/>','\'\'\'','\'\'\'','<f:message key="edit.button.bold.text"/>');
addButton('../images/button_italic.png','<f:message key="edit.button.italic"/>','\'\'','\'\'','<f:message key="edit.button.italic.text"/>');
addButton('../images/button_underline.png','<f:message key="edit.button.underline"/>','<u>','</u>','<f:message key="edit.button.underline.text"/>');
addButton('../images/button_link.png','<f:message key="edit.button.internal.link"/>','[[',']]','<f:message key="edit.button.internal.link.text"/>');
addButton('../images/button_extlink.png','<f:message key="edit.button.external.link"/>','[',']','<f:message key="edit.button.external.link.text"/>');
addButton('../images/button_headline.png','<f:message key="edit.button.head2"/>','\n== ',' ==\n','<f:message key="edit.button.head2.text"/>');
addButton('../images/button_image.png','<f:message key="edit.button.image"/>','[[Image:',']]','<f:message key="edit.button.image.text"/>');
addButton('../images/button_nowiki.png','<f:message key="edit.button.nowiki"/>','<nowiki>','</nowiki>','<f:message key="edit.button.nowiki.text"/>');
addButton('../images/button_sig.png','<f:message key="edit.button.signature"/>','--~~~~','','');
addButton('../images/button_hr.png','<f:message key="edit.button.line"/>','\n----\n','','');
document.writeln("</div>");
/*]]>*/ 
</script>
<%
}
%>

<p>
<textarea name="contents" rows="25" cols="80" style="width:100%" accesskey=","><c:out value="${contents}" escapeXml="true" /></textarea>
</p>
<p><label for="editComment"><f:message key="edit.caption.comment" /></label>: <input type="text" name="editComment" value="<c:out value="${editComment}" />" size="60" id="editComment" /></p>
<p>
<input type="submit" name="save" value="<f:message key="common.save"/>"  accesskey="s"/>

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW)) {
%>
<input type="submit" name="preview" value="<f:message key="edit.action.preview"/>" accesskey="p"/>
<%
}
%>

&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" name="minorEdit"<c:if test="${minorEdit}"> checked</c:if> id="minorEdit" accesskey="i" />
<label for="minorEdit"><f:message key="edit.caption.minor" /></label>
<c:if test="${!empty user}">
&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" name="watchTopic"<c:if test="${watchTopic}"> checked</c:if> id="watchTopic" accesskey="w" />
<label for="watchTopic"><f:message key="edit.caption.watch" /></label>
</c:if>
</p>

<c:if test="${pageInfo.actionEditResolve}">
<%@ include file="diff-include.jsp" %>
<p>
<textarea name="contentsResolve" rows="25" cols="80" style="width:100%"><c:out value="${contentsResolve}" escapeXml="true" /></textarea>
</p>
</c:if>

</form>
