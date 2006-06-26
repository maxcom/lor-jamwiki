<div id="menu-tab">
<table class="menu-tab-table">
<tr>
	<td class="menu-tab-space">&#160;</td>
<%
// FIXME - this needs to be cleaned up
boolean special = false;
try {
	if (request.getAttribute(JAMController.PARAMETER_SPECIAL) != null) {
		special = ((Boolean)request.getAttribute(JAMController.PARAMETER_SPECIAL)).booleanValue();
	}
	if (request.getParameter(JAMController.PARAMETER_SPECIAL) != null) {
		special = (new Boolean(request.getParameter(JAMController.PARAMETER_SPECIAL))).booleanValue();
	}
} catch (Exception e) {}
if (!special) {
%>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="${topic}" />"><f:message key="menu.article" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<%-- FIXME: hard coding --%>
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="Comments:${topic}" />"><f:message key="menu.comments" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="Special:Edit" />?topic=<jamwiki:encode value="${topic}" />"><f:message key="menu.editpage" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	</c:if>
	<c:if test="${!readOnly}">
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="Special:History" />?topic=<jamwiki:encode value="${topic}" />&type=all"><f:message key="menu.history" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="Special:Upload" />?topic=<jamwiki:encode value="${topic}" />"><f:message key="menu.attach" /></a></td>
	</c:if>
	<%-- FIXME: admin only --%>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="Special:Delete" />?topic=<jamwiki:encode value="${topic}" />"><f:message key="menu.delete" /></a></td>
	<td class="menu-tab-space">&#160;</td>
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="Special:Print" />?topic=<jamwiki:encode value="${topic}" />" target="_blank"><f:message key="menu.printablepage" /></a></td>
<%
} else {
%>
	<td class="menu-tab-nonselected"><a href="<jamwiki:link value="${topic}" />"><f:message key="menu.special" /></a></td>
<%
}
%>
	<td class="menu-tab-close">&#160;</td>
</tr>
</table>
</div>