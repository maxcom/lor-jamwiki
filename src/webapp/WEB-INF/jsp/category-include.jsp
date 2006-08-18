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

<table><tr><td>
<ul>
		<c:forEach items="${subcategories}" var="subcategory">
<li><jamwiki:link value="${subcategory}" text="${subcategory}" /></li>
		</c:forEach>
</ul>

</td></tr></table>
	</c:if>

<h3><f:message key="topic.category.topics"><f:param value="${categoryName}" /></f:message></h3>
	<c:if test="${!empty subtopics}">
<table><tr><td>
<ul>
		<c:forEach items="${subtopics}" var="subtopic">
<li><jamwiki:link value="${subtopic}" text="${subtopic}" /></li>
		</c:forEach>
</ul>
</td></tr></table>
	</c:if>
	<c:if test="${empty subtopics}">
<p><f:message key="topic.category.notopics"><f:param value="${categoryName}" /></f:message></p>
	</c:if>
</c:if>