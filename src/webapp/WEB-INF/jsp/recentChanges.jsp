<form name="num-changes" method="get" action="<jmwiki:link value="Special:RecentChanges" />">
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
	<%-- FIXME: hard coding --%>
	<td>(<a href="<jmwiki:link value="Special:Diff" />?action=<%= WikiServlet.ACTION_DIFF %>&topic=<jmwiki:encode value="${change.topic}" />">diff</a>)</td>
	<td>(<a href="<jmwiki:link value="Special:History" />?action=<%= WikiServlet.ACTION_HISTORY %>&topic=<jmwiki:encode value="${change.topic}" />&type=all">history</a>)</td>
	<td class="recent">
		<a href='<jmwiki:encode value="${change.topic}"/>'><c:out value="${change.topic}"/></a>
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
