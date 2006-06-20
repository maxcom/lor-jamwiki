<form name="form" method="post" action="<jmwiki:link value="Special:Edit" />">
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
&nbsp;&nbsp;&nbsp;
<input type="checkbox" name="minorEdit"/>
<f:message key="edit.isMinorEdit"/>
</p>
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
