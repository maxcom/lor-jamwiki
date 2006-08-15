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
<%@ page import="
        org.jamwiki.Environment
    "
%>

<c:if test="${empty notopic}">
	<c:if test="${!empty topicObject}">
<div id="content-article" class="clearblock">
<%-- FIXME - ugly, clean this up --%>
<c:if test="${!empty fileVersions}"><jamwiki:image value="${topicObject.name}" /></c:if>
<c:out value="${topicObject.topicContent}" escapeXml="false" /></div>
		<c:if test="${!empty fileVersions}">
<h2><f:message key="topic.filehistory" /></h2>
<ul>
			<c:forEach items="${fileVersions}" var="fileVersion">
<li>
<%-- FIXME - clean this up, verify path build correctly --%>
<a href="<%= Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) %><c:out value="${fileVersion.url}" />"><f:formatDate value="${fileVersion.uploadDate}" type="both" pattern="dd-MMM-yyyy HH:mm" /></a>
&#160;(<c:out value="${fileVersion.fileSize}" /> bytes)
				<c:if test="${!empty fileVersion.uploadComment}">&#160;(<i><c:out value="${fileVersion.uploadComment}" /></i>)</c:if>
</li>
			</c:forEach>
</ul>
		</c:if>
	</c:if>
</c:if>
<c:if test="${!empty notopic}">
<p><f:message key="${notopic.key}"><f:param value="${notopic.params[0]}" /></f:message></p>
</c:if>
