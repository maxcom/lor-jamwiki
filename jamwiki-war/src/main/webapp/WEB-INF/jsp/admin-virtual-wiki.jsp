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
<%@ page import="org.jamwiki.Environment"
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="virtualwiki" class="admin">

<c:if test="${!empty message}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>
<c:if test="${!empty errors}">
<div class="message red"><c:forEach items="${errors}" var="message"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message><br /></c:forEach></div>
</c:if>

<form action="<jamwiki:link value="Special:VirtualWiki" />" method="get" name="search">
<input type="hidden" name="function" value="search" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.select" /></legend>
<div class="row lightbg">
	<label for="name"><fmt:message key="common.name" /></label>
	<span>
		<select name="selected" id="searchVirtualWiki" onchange="document.search.submit()">
		<option value=""></option>
		<c:forEach items="${wikis}" var="wiki"><option value="${wiki.name}" <c:if test="${!empty selected && wiki.name == selected.name}">selected="selected"</c:if>>${wiki.name}</option></c:forEach>
		</select>
	</span>
</div>
</fieldset>
</form>
<form action="<jamwiki:link value="Special:VirtualWiki" />" method="post">
<fieldset>
<legend><fmt:message key="admin.title.virtualwiki" /></legend>
<input type="hidden" name="function" value="virtualwiki" />
<c:if test="${!empty selected}">
	<input type="hidden" name="virtualWikiId" value="${selected.virtualWikiId}" />
	<input type="hidden" name="name" value="${selected.name}" />
</c:if>
<div class="row">
	<label for="name"><fmt:message key="common.name" /></label>
	<span>
		<c:choose>
			<c:when test="${!empty selected}">${selected.name}</c:when>
			<c:otherwise><input type="text" name="name" id="name" size="30" /></c:otherwise>
		</c:choose>
	</span>
</div>
<div class="row">
	<label for="defaultTopicName"><fmt:message key="admin.caption.defaulttopic" /></label>
	<span>
		<c:set var="virtualWikiName"><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></c:set>
		<c:if test="${!empty selected}">
			<c:set var="virtualWikiName" value="${selected.defaultTopicName}" />
		</c:if>
		<input type="text" name="defaultTopicName" id="defaultTopicName" value="${virtualWikiName}" size="30" />
	</span>
</div>
<c:set var="buttonLabel"><fmt:message key="common.add" /></c:set>
<c:if test="${!empty selected}">
	<c:set var="buttonLabel"><fmt:message key="common.update" /></c:set>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" value="${buttonLabel}" /></span>
</div>
</fieldset>
</form>
<c:if test="${!empty selected}">
	<form action="<jamwiki:link value="Special:VirtualWiki" />" method="post">
	<input type="hidden" name="function" value="namespaces" />
	<input type="hidden" name="selected" value="${selected.name}" />
	<fieldset>
	<legend><fmt:message key="admin.vwiki.title.namespaces" /></legend>
	<div class="row">
		<table width="99%">
		<tr>
			<th><fmt:message key="admin.vwiki.caption.namespace.id" /></th>
			<th><fmt:message key="admin.vwiki.caption.namespace.default" /></th>
			<th><fmt:message key="admin.vwiki.caption.namespace.virtualwiki" /></th>
		</tr>
		<c:forEach items="${namespaces}" var="namespace">
			<tr>
				<td>
					<input type="hidden" name="namespace_id" value="${namespace.id}" />
					<input type="hidden" name="${namespace.id}_label" value="${namespace.label}" />
					${namespace.id}
				</td>
				<td>
					<input type="hidden" name="${namespace.id}_newlabel" value="${namespace.label}" />
					${namespace.label}
				</td>
				<td><input type="text" name="${namespace.id}_vwiki" size="30" value="${namespace.namespaceTranslations[selected.name]}" /></td>
			</tr>
		</c:forEach>
		</table>
	</div>
	<div class="row">
		<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
	</div>
	</fieldset>
	</form>
</c:if>

</div>
