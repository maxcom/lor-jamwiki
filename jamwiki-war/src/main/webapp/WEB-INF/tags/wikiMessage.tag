<%@ tag body-content="empty" description="Render a WikiMessage object" %>

<%@ taglib prefix="jamwiki" uri="http://jamwiki.org/taglib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="ApplicationResources" />

<%@ attribute name="message" required="true" rtexprvalue="true" type="org.jamwiki.WikiMessage" description="The WikiMessage object to render" %>

<fmt:message key="${message.key}">
	<%-- message formatting uses an embedded c:if instead of a c:forEach in order to work on Resin (tested with version 3.2.1) --%>
	<c:if test="${message.paramsLength >= 1}"><fmt:param>${message.params[0]}</fmt:param></c:if>
	<c:if test="${message.paramsLength >= 2}"><fmt:param>${message.params[1]}</fmt:param></c:if>
	<c:if test="${message.paramsLength >= 3}"><fmt:param>${message.params[2]}</fmt:param></c:if>
	<c:if test="${message.paramsLength >= 4}"><fmt:param>${message.params[3]}</fmt:param></c:if>
</fmt:message>
