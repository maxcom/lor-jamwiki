<div id="menu-tab">
<table class="menu-tab-table">
<tr>
	<td class="menu-tab-space">&#160;</td>
<%
// FIXME - this needs to be cleaned up
boolean special = false;
try {
	if (request.getAttribute(WikiServlet.PARAMETER_SPECIAL) != null) {
		special = ((Boolean)request.getAttribute(WikiServlet.PARAMETER_SPECIAL)).booleanValue();
	}
	if (request.getParameter(WikiServlet.PARAMETER_SPECIAL) != null) {
		special = (new Boolean(request.getParameter(WikiServlet.PARAMETER_SPECIAL))).booleanValue();
	}
} catch (Exception e) {}
if (!special) {
%>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="${topic}" />"><f:message key="menu.article" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Comments:${topic}" />"><f:message key="menu.comments" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Edit" />?topic=<jmwiki:encode value="${topic}" />"><f:message key="menu.editpage" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:History" />?topic=<jmwiki:encode value="${topic}" />&type=all"><f:message key="menu.history" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Upload" />?topic=<jmwiki:encode value="${topic}" />"><f:message key="menu.attach" /></a></td>
	</c:if>
	<%-- FIXME: admin only --%>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Delete" />?topic=<jmwiki:encode value="${topic}" />"><f:message key="menu.delete" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="Special:Print" />?topic=<jmwiki:encode value="${topic}" />" target="_blank"><f:message key="menu.printablepage" /></a></td>
<%
} else {
%>
	<td class="menu-tab-nonselected"><a href="<jmwiki:link value="${topic}" />"><f:message key="menu.special" /></a></td>
<%
}
%>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>