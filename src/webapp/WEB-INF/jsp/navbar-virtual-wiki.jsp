
<f:message key="virtualwiki.${virtualWiki}.name" var="wikiname"/>
<%-- FIXME: clean this up --%>
<%
if (WikiBase.getVirtualWikiCount() > 1) {
%>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) && WikiBase.getVirtualWikiCount() > 1) {
%>
<a href="<jamwiki:link value="Special:VirtualWikiList" />">
<%
}
%>
<b><f:message key="common.wiki"/>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) && WikiBase.getVirtualWikiCount() > 1) {
%>
</a>
<%
}
%>
:
<a href="<jamwiki:link value="StartingPoints" />"><c:out value="${virtualWiki}"/></a></b> :
<%
}
%>