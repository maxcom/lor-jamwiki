<c:forEach items="${historyThisPage}" var="history">
<a href="<jmwiki:link var="${history} " />"><c:out value="${history}"/></a> &gt;&gt;
</c:forEach>