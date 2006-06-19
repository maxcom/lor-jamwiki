<%
String virtualWiki = (String)request.getAttribute("virtualWiki");
Collection all = null;
String title = "";
WikiBase wb = WikiBase.getInstance();
if (request.getParameter("orphaned") == null && request.getAttribute("orphaned") == null && request.getParameter("todo") == null && request.getAttribute("todo") == null) {
	all = wb.getSearchEngineInstance().getAllTopicNames(virtualWiki);
	title = "Special:AllTopics";
} else if (request.getParameter("orphaned") != null || request.getAttribute("orphaned") != null) {
	all = wb.getOrphanedTopics(virtualWiki);
	title = "Special:OrphanedTopics";
} else if (request.getParameter("todo") != null || request.getAttribute("todo") != null) {
	all = wb.getToDoWikiTopics(virtualWiki);
	title = "Special:ToDoTopics";
}
%>
<table>
<%
if (all.isEmpty()) {
%>
<tr><td><p class="red"><f:message key="alltopics.notopics"/></p></td></tr>
<%
} else {
%>
<tr><td><f:message key="alltopics.topics"><f:param><%=all.size()%></f:param></f:message></td></tr>
<%
	Iterator it = all.iterator();
	while (it.hasNext()) {
		String topicName = (String) it.next();
%>
<tr><td class="recent"><a href="Wiki?<%=topicName%>"><%=topicName%></a></td></tr>
<%
	}
}
%>
</table>
