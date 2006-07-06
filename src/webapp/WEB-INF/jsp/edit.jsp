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

<form name="form" method="post" action="<jamwiki:link value="Special:Edit" />">
<p>
<input type="hidden" name="topic" value='<c:out value="${topic}"/>'/>
<script type="text/javascript" src="../js/edit.js" language="JavaScript1.3"></script>
<p>
<textarea name="contents" rows="25" cols="80" style="width:100%"><c:out value="${contents}" escapeXml="false"/></textarea>
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
