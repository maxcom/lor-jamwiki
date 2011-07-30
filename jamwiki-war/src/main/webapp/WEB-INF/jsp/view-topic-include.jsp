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

<c:choose>
	<c:when test="${empty notopic}">
		<c:if test="${!empty topicObject}">
			<div id="content-article">
			<c:if test="${!empty sharedImageTopicObject}">
				<div id="shared-image-message"><fmt:message key="topic.sharedImage"><fmt:param><jamwiki:link value="${sharedImageTopicObject.name}" virtualWiki="${sharedImageTopicObject.virtualWiki}" text="${sharedImageTopicObject.name}" style="interwikilink" /></fmt:param></fmt:message></div>
			</c:if>
			<c:if test="${topicImage}"><a href="<c:out value="${fileVersions[0].url}" />" class="wikiimg"><jamwiki:image value="${topicObject.name}" virtualWiki="${topicObject.virtualWiki}" maxWidth="800" maxHeight="600" allowEnlarge="false" /></a></c:if>
			<c:if test="${topicFile}"><div id="topic-file-download"><fmt:message key="topic.file.download" />:&#160;<a href="<c:out value="${fileVersions[0].url}" />"><c:out value="${topicObject.name}" /></a></div></c:if>
			<c:out value="${topicObject.topicContent}" escapeXml="false" />
			</div>
			<div class="clear"></div>
			<c:if test="${!empty fileVersions}">
				<%-- display image file history --%>
				<h2><fmt:message key="topic.filehistory" /></h2>
				<p><fmt:message key="topic.filehistory.click" /></p>
				<ul>
					<c:forEach items="${fileVersions}" var="fileVersion">
					<li>
					<a href="<c:out value="${fileVersion.url}" />"><fmt:formatDate value="${fileVersion.uploadDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" /></a>
					&#160;(<fmt:message key="topic.filesize.bytes"><fmt:param value="${fileVersion.fileSize}" /></fmt:message>)
					&#160;.&#160;.&#160;
					<c:choose>
						<c:when test="${!empty sharedImageTopicObject}">
							<jamwiki:link value="${pageInfo.namespaces[sharedImageTopicObject.virtualWiki]['User']}:${fileVersion.authorDisplay}" virtualWiki="${sharedImageTopicObject.virtualWiki}" text="${fileVersion.authorDisplay}" style="interwikilink" />
						</c:when>
						<c:otherwise>
							<jamwiki:link value="${pageInfo.namespaces[topicObject.virtualWiki]['User']}:${fileVersion.authorDisplay}" virtualWiki="${topicObject.virtualWiki}" text="${fileVersion.authorDisplay}" />
							(<jamwiki:link value="${pageInfo.namespaces[topicObject.virtualWiki]['User comments']}:${fileVersion.authorDisplay}" virtualWiki="${topicObject.virtualWiki}"><fmt:message key="recentchanges.caption.comments" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Contributions" virtualWiki="${topicObject.virtualWiki}"><jamwiki:linkParam key="contributor" value="${fileVersion.authorDisplay}" /><fmt:message key="recentchanges.caption.contributions" /></jamwiki:link>)
						</c:otherwise>
					</c:choose>
					<c:if test="${!empty fileVersion.uploadComment}">&#160;(<i><c:out value="${fileVersion.uploadComment}" /></i>)</c:if>
					</li>
					</c:forEach>
				</ul>
			</c:if>
		</c:if>
	</c:when>
	<c:otherwise>
		<div class="message"><fmt:message key="${notopic.key}"><fmt:param value="${notopic.params[0]}" /><fmt:param><jamwiki:link value="${notopic.params[0]}" text="${notopic.params[0]}" /></fmt:param></fmt:message></div>
	</c:otherwise>
</c:choose>
