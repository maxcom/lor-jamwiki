<%@ tag body-content="empty" description="Render a WikiMessage object" %>

<%@ taglib prefix="jamwiki" uri="http://jamwiki.org/taglib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="ApplicationResources" />

<%@ attribute name="message" required="true" rtexprvalue="true" type="org.jamwiki.WikiMessage" description="The WikiMessage object to render" %>

<fmt:message key="${message.key}">
	<%-- message formatting uses an embedded c:if instead of a c:forEach in order to work on Resin (tested with version 3.2.1) --%>
	<c:if test="${message.paramsLength >= 1}">
		<c:choose>
			<c:when test="${message.params[0].wikiLink}"><fmt:param><jamwiki:link value="${message.params[0].param}" text="${message.params[0].paramText}" /></fmt:param></c:when>
			<c:otherwise><fmt:param>${message.params[0]}</fmt:param></c:otherwise>
		</c:choose>
	</c:if>
	<c:if test="${message.paramsLength >= 2}">
		<c:choose>
			<c:when test="${message.params[1].wikiLink}"><fmt:param><jamwiki:link value="${message.params[1].param}" text="${message.params[1].paramText}" /></fmt:param></c:when>
			<c:otherwise><fmt:param>${message.params[1]}</fmt:param></c:otherwise>
		</c:choose>
	</c:if>
	<c:if test="${message.paramsLength >= 3}">
		<c:choose>
			<c:when test="${message.params[2].wikiLink}"><fmt:param><jamwiki:link value="${message.params[2].param}" text="${message.params[2].paramText}" /></fmt:param></c:when>
			<c:otherwise><fmt:param>${message.params[2]}</fmt:param></c:otherwise>
		</c:choose>
	</c:if>
	<c:if test="${message.paramsLength >= 4}">
		<c:choose>
			<c:when test="${message.params[3].wikiLink}"><fmt:param><jamwiki:link value="${message.params[3].param}" text="${message.params[3].paramText}" /></fmt:param></c:when>
			<c:otherwise><fmt:param>${message.params[3]}</fmt:param></c:otherwise>
		</c:choose>
	</c:if>
</fmt:message>
