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

<c:if test="${empty notopic}">
	<c:if test="${!empty topicObject}">
		<div id="content-article">
		<c:if test="${topicImage}"><jamwiki:image value="${topicObject.name}" /></c:if>
		<c:if test="${topicFile}"><div id="topic-file-download"><fmt:message key="topic.file.download" />:&#160;<a href="<c:out value="${fileVersions[0].url}" />"><c:out value="${topicObject.name}" /></a></div></c:if>
		<c:out value="${topicObject.topicContent}" escapeXml="false" />
		</div>
		<div class="clear"></div>
		<c:if test="${!empty fileVersions}">
			<h2><fmt:message key="topic.filehistory" /></h2>
			<ul>
				<c:forEach items="${fileVersions}" var="fileVersion">
				<li>
				<a href="<c:out value="${fileVersion.url}" />"><fmt:formatDate value="${fileVersion.uploadDate}" type="both" pattern="dd-MMM-yyyy HH:mm" /></a>
				&#160;(<fmt:message key="topic.filesize.bytes"><fmt:param value="${fileVersion.fileSize}" /></fmt:message>)
				&#160;.&#160;.&#160;
				<jamwiki:link value="${pageInfo.namespaces['User']}:${fileVersion.authorDisplay}" text="${fileVersion.authorDisplay}" />
				(<jamwiki:link value="${pageInfo.namespaces['User comments']}:${fileVersion.authorDisplay}"><fmt:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${fileVersion.authorDisplay}" /><fmt:message key="recentchanges.caption.contributions" /></jamwiki:link>)
				<c:if test="${!empty fileVersion.uploadComment}">&#160;(<i><c:out value="${fileVersion.uploadComment}" /></i>)</c:if>
				</li>
				</c:forEach>
			</ul>
		</c:if>
	</c:if>
	<%@ include file="category-include.jsp" %>
	<c:if test="${!empty categories}">
		<div id="category-index"><jamwiki:link value="Special:Categories"><fmt:message key="topic.categories" /></jamwiki:link>:
		<c:forEach items="${categories}" var="category" varStatus="status">
			<c:if test="${!status.first}">&#160;|&#160;</c:if><jamwiki:link value="${category.key}" text="${category.value}" />
		</c:forEach>
		</div>
		<div class="clear"></div>
	</c:if>
</c:if>
<c:if test="${!empty notopic}">
<div class="message"><fmt:message key="${notopic.key}"><fmt:param value="${notopic.params[0]}" /><fmt:param><jamwiki:link value="${notopic.params[0]}" text="${notopic.params[0]}" /></fmt:param></fmt:message></div>
</c:if>
