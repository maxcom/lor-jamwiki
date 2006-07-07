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

<br />

<ul>

<c:forEach items="${contributions}" var="contribution">
<li>
	<%-- FIXME: hard coding --%>
	(<a href="<jamwiki:link value="Special:Diff" />?topic=<jamwiki:encode value="${contribution.topicName}" />&version2=<c:out value="${contribution.previousTopicVersionId}" />&version1=<c:out value="${contribution.topicVersionId}" />">diff</a>)
	&#160;
	(<a href="<jamwiki:link value="Special:History" />?topic=<jamwiki:encode value="${contribution.topicName}" />&type=all">history</a>)
	&#160;
	<%-- FIXME: do not hardcode date pattern --%>
	<f:formatDate value="${contribution.editDate}" type="both" pattern="dd-MMM-yyyy HH:mm" />
	&#160;
	<a href='<jamwiki:link value="${contribution.topicName}"/>'><c:out value="${contribution.topicName}"/></a>
	&#160;
	<c:if test="${!empty contribution.editComment}">&#160;(<i><c:out value="${contribution.editComment}" /></i>)</c:if>
</c:forEach>
</ul>
