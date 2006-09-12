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

<div id="change">

<form name="num-changes" method="get" action="<jamwiki:link value="Special:RecentChanges" />">

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

<c:forEach items="${changes}" var="change">
<li<c:if test="${change.delete}"> class="deletechange"</c:if><c:if test="${change.minor}"> class="minorchange"</c:if><c:if test="${change.undelete}"> class="undeletechange"</c:if><c:if test="${change.move}"> class="movechange"</c:if><c:if test="${change.normal}"> class="standardchange"</c:if>>
	(<jamwiki:link value="Special:Diff"><jamwiki:linkParam key="topic" value="${change.topicName}" /><jamwiki:linkParam key="version2"><c:out value="${change.previousTopicVersionId}" /></jamwiki:linkParam><jamwiki:linkParam key="version1" value="${change.topicVersionId}" /><f:message key="common.caption.diff" /></jamwiki:link>)
	&#160;
	(<jamwiki:link value="Special:History"><jamwiki:linkParam key="topic" value="${change.topicName}" /><f:message key="common.caption.history" /></jamwiki:link>)
	&#160;
	<%-- FIXME: do not hardcode date pattern --%>
	<f:formatDate value="${change.editDate}" type="both" pattern="dd-MMM-yyyy HH:mm" />
	&#160;
	<c:if test="${!change.delete}"><jamwiki:link value="${change.topicName}" text="${change.topicName}" /></c:if>
	<c:if test="${change.delete}"><c:out value="${change.topicName}" /></c:if>
	&#160;.&#160;.&#160;
	<%-- FIXME: ugly --%>
	<jamwiki:link value="User:${change.authorName}" text="${change.authorName}" />
	(<jamwiki:link value="User comments:${change.authorName}"><f:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${change.authorName}" /><f:message key="recentchanges.caption.contributions" /></jamwiki:link>)
	<c:if test="${!empty change.changeTypeNotification}">&#160;<b><c:out value="${change.changeTypeNotification}" /></b></c:if>
	<c:if test="${!empty change.editComment}">&#160;(<i><c:out value="${change.editComment}" /></i>)</c:if>
</li>
</c:forEach>
</ul>
</form>

</div>