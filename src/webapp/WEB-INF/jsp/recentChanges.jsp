<form name="num-changes" method="get" action="<jamwiki:link value="Special:RecentChanges" />">
<%-- FIXME: hard coding --%>
<input type="hidden" name="<%= JAMWikiServlet.PARAMETER_ACTION %>" value="<%= JAMWikiServlet.ACTION_RECENT_CHANGES %>" />
<table width="100%">
<tr>
	<td colspan="5">
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
		</select>
		&#160;
		<input type="Submit" value="Change" />
	</td>
</tr>
<tr>
	<th colspan="2">&#160;</th>
	<th><f:message key="common.topic"/></th>
	<th><f:message key="common.date"/></th>
	<th><f:message key="common.user"/></th>
</tr>
<c:forEach items="${changes}" var="change">
<tr>
	<%-- FIXME: hard coding --%>
	<td>(<a href="<jamwiki:link value="Special:Diff" />?topic=<jamwiki:encode value="${change.topicName}" />&version2=<c:out value="${change.previousTopicVersionId}" />&version1=<c:out value="${change.topicVersionId}" />">diff</a>)</td>
	<td>(<a href="<jamwiki:link value="Special:History" />?topic=<jamwiki:encode value="${change.topicName}" />&type=all">history</a>)</td>
	<td class="recent">
		<a href='<jamwiki:encode value="${change.topicName}"/>'><c:out value="${change.topicName}"/></a>
	</td>
	<td class="recent">
		<f:formatDate value="${change.editDate}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" />
	</td>
	<td class="recent"><c:out value="${change.authorName}" /><c:if test="${!empty change.editComment}">&#160;(<c:out value="${change.editComment}" />)</c:if></td>
</tr>
</c:forEach>
</table>
</form>
