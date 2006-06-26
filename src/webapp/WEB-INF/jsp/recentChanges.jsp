<form name="num-changes" method="get" action="<jamwiki:link value="Special:RecentChanges" />">
<%-- FIXME: hard coding --%>
<input type="hidden" name="<%= JAMController.PARAMETER_ACTION %>" value="<%= JAMController.ACTION_RECENT_CHANGES %>" />
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
	<th><f:message key="common.user"/></th>
	<th><f:message key="common.date"/></th>
</tr>
<c:forEach items="${changes}" var="change">
<tr>
	<%-- FIXME: hard coding --%>
	<td>(<a href="<jamwiki:link value="Special:Diff" />?action=<%= JAMController.ACTION_DIFF %>&topic=<jamwiki:encode value="${change.topic}" />">diff</a>)</td>
	<td>(<a href="<jamwiki:link value="Special:History" />?action=<%= JAMController.ACTION_HISTORY %>&topic=<jamwiki:encode value="${change.topic}" />&type=all">history</a>)</td>
	<td class="recent">
		<a href='<jamwiki:encode value="${change.topic}"/>'><c:out value="${change.topic}"/></a>
	</td>
	<td class="recent">
		<c:out value="${change.username}"/>
	</td>
	<td class="recent">
		<f:formatDate value="${change.time}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" />
	</td>
</tr>
</c:forEach>
</table>
</form>
