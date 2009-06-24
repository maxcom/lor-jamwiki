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

<c:if test="${!empty categoryName}">
	<c:if test="${numSubCategories > 0}">
<h3><fmt:message key="topic.category.subcategories"><fmt:param value="${categoryName}" /></fmt:message></h3>
<div class="message"><fmt:message key="topic.category.numsubcategories"><fmt:param value="${numSubCategories}" /><fmt:param value="${categoryName}" /></fmt:message></div>

<table width="100%"><tr><td>
<ul>
		<c:set var="columnCount" value="1" />
		<c:forEach items="${subCategories}" var="subCategory" varStatus="status">
<li><jamwiki:link value="${subCategory.key}" text="${subCategory.value}" /></li>
			<%-- FIXME - do not hard code min num topics and num columns --%>
			<c:if test="${(numSubCategories > 9) && (columnCount < 3) && ((status.count * 3) >= (numSubCategories * columnCount))}">
				<c:set var="columnCount" value="${columnCount + 1}" />
</ul></td><td><ul>
			</c:if>
		</c:forEach>
</ul>

</td></tr></table>
	</c:if>

	<c:if test="${numCategoryImages > 0}">
<h3><fmt:message key="topic.category.images"><fmt:param value="${categoryName}" /></fmt:message></h3>
<div class="message"><fmt:message key="topic.category.numimages"><fmt:param value="${numCategoryImages}" /><fmt:param value="${categoryName}" /></fmt:message></div>

<table class="gallery" cellpadding="0" cellspacing="0"><tr>
		<%-- FIXME - number of columns and max image size are hard-coded --%>
		<c:forEach items="${categoryImages}" var="categoryImage" varStatus="status">
<td><a href="<jamwiki:link value="${categoryImage.childTopicName}" />"><jamwiki:image value="${categoryImage.childTopicName}" maxDimension="120" style="gallery" /></a></td>
			<c:if test="${(status.count % 4) == 0}">
</tr><tr>
			</c:if>
		</c:forEach>
		<c:forEach begin="1" end="${4 - (numCategoryImages % 4)}">
<td>&#160;</td>
		</c:forEach>
</tr></table>
	</c:if>

<h3><fmt:message key="topic.category.topics"><fmt:param value="${categoryName}" /></fmt:message></h3>
<div class="message"><fmt:message key="topic.category.numtopics"><fmt:param value="${numCategoryTopics}" /><fmt:param value="${categoryName}" /></fmt:message></div>
	<c:if test="${numCategoryTopics > 0}">
<table width="100%"><tr><td>
<ul>
		<c:set var="columnCount" value="1" />
		<c:forEach items="${categoryTopics}" var="subtopic" varStatus="status">
<li><jamwiki:link value="${subtopic.childTopicName}" text="${subtopic.childTopicName}" /></li>
			<%-- FIXME - do not hard code min num topics and num columns --%>
			<c:if test="${(numCategoryTopics > 9) && (columnCount < 3) && ((status.count * 3) >= (numCategoryTopics * columnCount))}">
				<c:set var="columnCount" value="${columnCount + 1}" />
</ul></td><td><ul>
			</c:if>
		</c:forEach>
</ul>
</td></tr></table>
	</c:if>
</c:if>