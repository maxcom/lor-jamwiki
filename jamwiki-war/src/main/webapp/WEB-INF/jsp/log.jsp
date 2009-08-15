<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<%@ page
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="log">

<form name="logForm" method="get" action="<jamwiki:link value="Special:Log" />">
<fieldset>
<legend><fmt:message key="log.caption.logs" /></legend>
<select name="logType" onchange="document.logForm.submit()">
	<c:forEach items="${logTypes}" var="logType">
		<option value="${logType.key}" <c:if test="${logTypeSelected == logType.key}">selected="selected"</c:if>><fmt:message key="${logType.value}" /></option>
	</c:forEach>
</select>
</fieldset>
</form>

<div class="message">
<fmt:message key="common.caption.view" />: <jamwiki:pagination total="${numLogs}" rootUrl="Special:Log" />
</div>

<c:set var="previousDate"><fmt:formatDate value="${logItems[0].logDate}" type="both" pattern="dd MMMM yyyy" /></c:set>
<h4><c:out value="${previousDate}" /></h4>
<ul>
<c:forEach items="${logItems}" var="logItem">
<c:set var="currentDate"><fmt:formatDate value="${logItem.logDate}" type="both" pattern="dd MMMM yyyy" /></c:set>
<c:if test="${currentDate != previousDate}">
</ul>
<h4><c:out value="${currentDate}" /></h4>
<ul>
</c:if>
<li>
	<fmt:formatDate value="${logItem.logDate}" type="both" pattern="HH:mm" />
	&#160;
	<%-- FIXME - clean up --%>
	<c:choose>
		<c:when test="${logItem.delete}">(<jamwiki:link value="Special:Log"><jamwiki:linkParam key="logType" value="${logItem.logType}" /><fmt:message key="log.caption.log.deletion" /></jamwiki:link>)</c:when> 
		<c:when test="${logItem.import}">(<jamwiki:link value="Special:Log"><jamwiki:linkParam key="logType" value="${logItem.logType}" /><fmt:message key="log.caption.log.import" /></jamwiki:link>)</c:when> 
		<c:when test="${logItem.move}">(<jamwiki:link value="Special:Log"><jamwiki:linkParam key="logType" value="${logItem.logType}" /><fmt:message key="log.caption.log.move" /></jamwiki:link>)</c:when> 
		<c:when test="${logItem.upload}">(<jamwiki:link value="Special:Log"><jamwiki:linkParam key="logType" value="${logItem.logType}" /><fmt:message key="log.caption.log.upload" /></jamwiki:link>)</c:when> 
		<c:when test="${logItem.user}">(<jamwiki:link value="Special:Log"><jamwiki:linkParam key="logType" value="${logItem.logType}" /><fmt:message key="log.caption.log.user" /></jamwiki:link>)</c:when> 
	</c:choose>
	&#160;.&#160;.&#160;
	<jamwiki:link value="User:${logItem.userDisplayName}" text="${logItem.userDisplayName}" />
	(<jamwiki:link value="User comments:${logItem.userDisplayName}"><fmt:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${logItem.userDisplayName}" /><fmt:message key="recentchanges.caption.contributions" /></jamwiki:link>)
	&#160;
	<%-- FIXME - clean up --%>
	<c:choose>
		<c:when test="${logItem.delete}"><fmt:message key="log.message.deletion"><fmt:param><jamwiki:link value="${logItem.logParams[0]}" text="${logItem.logParams[0]}" /></fmt:param></fmt:message></c:when> 
		<c:when test="${logItem.import}"><fmt:message key="log.message.import"><fmt:param><jamwiki:link value="${logItem.logParams[0]}" text="${logItem.logParams[0]}" /></fmt:param></fmt:message></c:when> 
		<c:when test="${logItem.move}"><fmt:message key="log.message.move"><fmt:param><jamwiki:link value="${logItem.logParams[0]}" text="${logItem.logParams[0]}" /></fmt:param><fmt:param><jamwiki:link value="${logItem.logParams[1]}" text="${logItem.logParams[1]}" /></fmt:param></fmt:message></c:when>
		<c:when test="${logItem.upload}"><fmt:message key="log.message.upload"><fmt:param><jamwiki:link value="${logItem.logParams[0]}" text="${logItem.logParams[0]}" /></fmt:param></fmt:message></c:when> 
		<c:when test="${logItem.user}"><fmt:message key="log.message.user" /></c:when> 
	</c:choose>
	<c:if test="${!empty logItem.logComment}">&#160;<span class="edit-comment">(<c:out value="${logItem.logComment}" />)</span></c:if>
</li>
<c:set var="previousDate" value="${currentDate}" />
</c:forEach>
</ul>

</div>
