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
	<c:if test="${!empty subcategories}">
<h3><f:message key="topic.category.subcategories"><f:param value="${categoryName}" /></f:message></h3>
<p><f:message key="topic.category.numsubcategories"><f:param value="${numsubcategories}" /><f:param value="${categoryName}" /></f:message></p>

<table width="100%"><tr><td>
<ul>
		<c:set var="columnCount" value="1" />
		<c:set var="currentCount" value="1" />
		<c:forEach items="${subcategories}" var="subcategory">
<li><jamwiki:link value="${subcategory}" text="${subcategory}" /></li>
			<%-- FIXME - do not hard code min num topics and num columns --%>
			<c:if test="${(numsubcategories > 9) && (columnCount < 3) && ((currentCount * 3) >= (numsubcategories * columnCount))}">
				<c:set var="columnCount" value="${columnCount + 1}" />
</ul></td><td><ul>
			</c:if>
			<c:set var="currentCount" value="${currentCount + 1}" />
		</c:forEach>
</ul>

</td></tr></table>
	</c:if>

<h3><f:message key="topic.category.topics"><f:param value="${categoryName}" /></f:message></h3>
<p><f:message key="topic.category.numtopics"><f:param value="${numsubtopics}" /><f:param value="${categoryName}" /></f:message></p>
	<c:if test="${!empty subtopics}">
<table width="100%"><tr><td>
<ul>
		<c:set var="columnCount" value="1" />
		<c:set var="currentCount" value="1" />
		<c:forEach items="${subtopics}" var="subtopic">
<li><jamwiki:link value="${subtopic}" text="${subtopic}" /></li>
			<%-- FIXME - do not hard code min num topics and num columns --%>
			<c:if test="${(numsubtopics > 9) && (columnCount < 3) && ((currentCount * 3) >= (numsubtopics * columnCount))}">
				<c:set var="columnCount" value="${columnCount + 1}" />
</ul></td><td><ul>
			</c:if>
			<c:set var="currentCount" value="${currentCount + 1}" />
		</c:forEach>
</ul>
</td></tr></table>
	</c:if>
</c:if>