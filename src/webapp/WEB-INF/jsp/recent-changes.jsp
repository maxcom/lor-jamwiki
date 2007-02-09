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
<%@ page errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="change">

<div class="message">
<f:message key="common.caption.view" />: <jamwiki:pagination total="${numChanges}" rootUrl="Special:RecentChanges" />
<br /><br />
<%-- FIXME: do not hardcode date patterns --%>
<f:message key="recentchanges.caption.time"><f:param><jsp:useBean id="now" class="java.util.Date" /><f:formatDate value="${now}" type="both" pattern="dd MMMM yyyy HH:mm" /></f:param></f:message> <jamwiki:enabled property="PROP_RSS_ALLOWED">(<jamwiki:link value="Special:RecentChangesFeed"><f:message key="recentchanges.caption.rss" /></jamwiki:link>)</jamwiki:enabled>
</div>

<form name="num-changes" method="get" action="<jamwiki:link value="Special:RecentChanges" />">

<c:set var="previousDate"><f:formatDate value="${changes[0].editDate}" type="both" pattern="dd MMMM yyyy" /></c:set>
<h4><c:out value="${previousDate}" /></h4>
<ul>
<c:forEach items="${changes}" var="change">
<c:set var="currentDate"><f:formatDate value="${change.editDate}" type="both" pattern="dd MMMM yyyy" /></c:set>
<c:if test="${currentDate != previousDate}">
</ul>
<h4><c:out value="${currentDate}" /></h4>
<ul>
</c:if>
<li<c:if test="${change.delete}"> class="deletechange"</c:if><c:if test="${change.minor}"> class="minorchange"</c:if><c:if test="${change.undelete}"> class="undeletechange"</c:if><c:if test="${change.move}"> class="movechange"</c:if><c:if test="${change.normal}"> class="standardchange"</c:if>>
	(<jamwiki:link value="Special:Diff"><jamwiki:linkParam key="topic" value="${change.topicName}" /><jamwiki:linkParam key="version2"><c:out value="${change.previousTopicVersionId}" /></jamwiki:linkParam><jamwiki:linkParam key="version1" value="${change.topicVersionId}" /><f:message key="common.caption.diff" /></jamwiki:link>)
	&#160;
	(<jamwiki:link value="Special:History"><jamwiki:linkParam key="topic" value="${change.topicName}" /><f:message key="common.caption.history" /></jamwiki:link>)
	&#160;
	<f:formatDate value="${change.editDate}" type="both" pattern="HH:mm" />
	&#160;
	<jamwiki:watchlist topic="${change.topicName}">
	<c:if test="${!change.delete}"><jamwiki:link value="${change.topicName}" text="${change.topicName}" /></c:if>
	<c:if test="${change.delete}"><c:out value="${change.topicName}" /></c:if>
	</jamwiki:watchlist>
	&#160;.&#160;.&#160;
	<jamwiki:link value="User:${change.authorName}" text="${change.authorName}" />
	(<jamwiki:link value="User comments:${change.authorName}"><f:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${change.authorName}" /><f:message key="recentchanges.caption.contributions" /></jamwiki:link>)
	<c:if test="${!empty change.changeTypeNotification}">&#160;<b><c:out value="${change.changeTypeNotification}" /></b></c:if>
	<c:if test="${!empty change.editComment}">&#160;(<i><c:out value="${change.editComment}" /></i>)</c:if>
</li>
<c:set var="previousDate" value="${currentDate}" />
</c:forEach>
</ul>
</form>

</div>