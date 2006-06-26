<c:forEach items="${historyThisPage}" var="history">
<a href="<jamwiki:link value="${history} " />"><c:out value="${history}"/></a> &gt;&gt;
</c:forEach>