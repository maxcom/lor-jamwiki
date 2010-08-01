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

<table id="diff_heading">
<tr>
	<td>
		<c:if test="${!empty version2}">
			<div id="left_revision"><jamwiki:link value="Special:History"><jamwiki:linkParam key="topicVersionId" value="${version2.topicVersionId}" /><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><fmt:message key="diff.revision"><fmt:param><fmt:formatDate value="${version2.editDate}" type="both" pattern="dd MMMM yyyy HH:mm" /></fmt:param></fmt:message></jamwiki:link></div>
			<div id="left_revision_user">
				<jamwiki:link value="${pageInfo.namespaces['User']}:${authorVersion2}" text="${authorVersion2}" />
				(<jamwiki:link value="${pageInfo.namespaces['User comments']}:${authorVersion2}"><fmt:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${authorVersion2}" /><fmt:message key="recentchanges.caption.contributions" /></jamwiki:link>)
			</div>
			<c:if test="${!empty version2.editComment}"><div id="left_revision_comment">(<span class="edit-comment"><jamwiki:editComment comment="${version2.editComment}" topic="${pageInfo.topicName}" /></span>)</div></c:if>
			<c:if test="${!empty version2.previousTopicVersionId}">
				<div id="left_revision_nav"><jamwiki:link value="Special:Diff" escape="false"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><jamwiki:linkParam key="version1" value="${version2.topicVersionId}" /><jamwiki:linkParam key="version2" value="${version2.previousTopicVersionId}" /><fmt:message key="diff.previous" /></jamwiki:link></div>
			</c:if>
		</c:if>
	</td>
	<td>
		<c:if test="${!empty version1}">
			<div id="right_revision"><jamwiki:link value="Special:History"><jamwiki:linkParam key="topicVersionId" value="${version1.topicVersionId}" /><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><fmt:message key="diff.revision"><fmt:param><fmt:formatDate value="${version1.editDate}" type="both" pattern="dd MMMM yyyy HH:mm" /></fmt:param></fmt:message></jamwiki:link></div>
			<div id="right_revision_user">
				<jamwiki:link value="${pageInfo.namespaces['User']}:${authorVersion1}" text="${authorVersion1}" />
				(<jamwiki:link value="${pageInfo.namespaces['User comments']}:${authorVersion1}"><fmt:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${authorVersion1}" /><fmt:message key="recentchanges.caption.contributions" /></jamwiki:link>)
			</div>
			<c:if test="${!empty version1.editComment}"><div id="right_revision_comment">(<span class="edit-comment"><jamwiki:editComment comment="${version1.editComment}" topic="${pageInfo.topicName}" /></span>)</div></c:if>
			<c:if test="${!empty nextTopicVersionId}">
				<div id="right_revision_nav"><jamwiki:link value="Special:Diff" escape="false"><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><jamwiki:linkParam key="version1" value="${nextTopicVersionId}" /><jamwiki:linkParam key="version2" value="${version1.topicVersionId}" /><fmt:message key="diff.next" /></jamwiki:link></div>
			</c:if>
		</c:if>
	</td>
</tr>
</table>

<%@ include file="diff-include.jsp" %>
