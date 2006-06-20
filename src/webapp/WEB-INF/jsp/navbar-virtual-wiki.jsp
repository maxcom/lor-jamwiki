
<f:message key="virtualwiki.${virtualWiki}.name" var="wikiname"/>
<jmwiki:wikibase var="wb"/>
<c:if test="${wb.virtualWikiCount > 1}">

<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) && WikiBase.getInstance().getVirtualWikiCount() > 1) {
%>
<a href="<jmwiki:link value="Special:VirtualWikiList" />">
<%
}
%>
<b><f:message key="common.wiki"/>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) && WikiBase.getInstance().getVirtualWikiCount() > 1) {
%>
</a>
<%
}
%>
:
<a href="<jmwiki:link value="StartingPoints" />">
<c:out value="${wikiname}"/></a></b> :
</c:if>