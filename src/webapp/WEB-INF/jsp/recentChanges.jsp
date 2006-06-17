<form name="num-changes" method="get" action="Wiki?RecentChanges">
<%-- FIXME: hard coding --%>
<input type="hidden" name="<%= WikiServlet.PARAMETER_ACTION %>" value="<%= WikiServlet.ACTION_RECENT_CHANGES %>" />
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
	<jmwiki:encode value="${change.topic}" var="encodedTopic"/>
	<%-- FIXME: hard coding --%>
	<td>(<a href="Wiki?action=<%= WikiServlet.ACTION_DIFF %>&topic=<c:out value="${encodedTopic}" />">diff</a>)</td>
	<td>(<a href="Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&topic=<c:out value="${encodedTopic}" />&type=all">history</a>)</td>
	<td class="recent">
		<a href='<c:out value="Wiki?${encodedTopic}"/>'><c:out value="${change.topic}"/></a>
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
