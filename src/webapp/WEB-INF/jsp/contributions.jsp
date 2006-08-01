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
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>

<%@ include file="page-init.jsp" %>

<div id="change">

<form name="num-changes" method="get" action="<jamwiki:link value="Special:Contributions" />">
<input type="hidden" name="contributor" value="<c:out value="${contributor}" />" />

<br />

<%-- FIXME: use JSP tag --%>
<%
int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
if (request.getParameter("num") != null) {
	// FIXME - breaks if non-integer
	num = new Integer(request.getParameter("num")).intValue();
}
%>
<select name="num">
<option value="10"<%= (num == 10) ? " selected=\"selected\"" : "" %>>10</option>
<option value="25"<%= (num == 25) ? " selected=\"selected\"" : "" %>>25</option>
<option value="50"<%= (num == 50) ? " selected=\"selected\"" : "" %>>50</option>
<option value="100"<%= (num == 100) ? " selected=\"selected\"" : "" %>>100</option>
<option value="250"<%= (num == 250) ? " selected=\"selected\"" : "" %>>250</option>
<option value="500"<%= (num == 500) ? " selected=\"selected\"" : "" %>>500</option>
</select>
&#160;
<input type="submit" value="<f:message key="common.change" />" />

<br /><br />

<ul>

<c:forEach items="${contributions}" var="change">
<li>
	(<a href="<jamwiki:link value="Special:Diff" />?topic=<jamwiki:encode value="${change.topicName}" />&version2=<c:out value="${change.previousTopicVersionId}" />&version1=<c:out value="${change.topicVersionId}" />"><f:message key="common.caption.diff" /></a>)
	&#160;
	(<a href="<jamwiki:link value="Special:History" />?topic=<jamwiki:encode value="${change.topicName}" />&type=all"><f:message key="common.caption.history" /></a>)
	&#160;
	<%-- FIXME: do not hardcode date pattern --%>
	<f:formatDate value="${change.editDate}" type="both" pattern="dd-MMM-yyyy HH:mm" />
	&#160;
	<a href='<jamwiki:link value="${change.topicName}"/>'><c:out value="${change.topicName}"/></a>
	<%-- FIXME: need a better way to denote minor edits --%>
	<c:if test="${change.minor}">&#160;<b>m</b></c:if>
	<c:if test="${!empty change.editComment}">&#160;(<i><c:out value="${change.editComment}" /></i>)</c:if>
</c:forEach>
</ul>
</form>

</div>