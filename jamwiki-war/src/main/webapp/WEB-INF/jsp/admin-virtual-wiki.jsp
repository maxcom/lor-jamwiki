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

<c:if test="${!empty message && function == 'search'}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>
<c:if test="${!empty errors && function == 'search'}">
<div class="message red"><c:forEach items="${errors}" var="message"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message><br /></c:forEach></div>
</c:if>

<!-- sub-menu tabs -->
<ul class="tab-menu" id="tab_submenu">
<li><a href="#vwiki"><fmt:message key="admin.vwiki.title.virtualwiki" /></a></li>
<li><a href="#addnamespace"><fmt:message key="admin.vwiki.title.namespace.add" /></a></li>
<li><a href="#namespaces"><fmt:message key="admin.vwiki.title.namespace.translations" /></a></li>
<li><a href="#interwiki"><fmt:message key="admin.vwiki.title.interwiki" /></a></li>
</ul>
<div class="submenu-tab-content">

<!-- Add/Update Virtual Wiki -->
<div id="vwiki" class="submenu-tab-item">
<a name="vwiki"></a>
<c:if test="${!empty message && function == 'virtualwiki'}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>
<c:if test="${!empty errors && function == 'virtualwiki'}">
<div class="message red"><c:forEach items="${errors}" var="message"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message><br /></c:forEach></div>
</c:if>
<!-- Select Virtual Wiki -->
<form action="<jamwiki:link value="Special:VirtualWiki" />#vwiki" method="get" name="selectvwiki">
<input type="hidden" name="function" value="search" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.select" /></legend>
<div class="row">
	<label for="name"><fmt:message key="common.name" /></label>
	<span>
		<select name="selected" onchange="document.selectvwiki.submit()">
		<option value=""></option>
		<c:forEach items="${wikis}" var="wiki"><option value="${wiki.name}" <c:if test="${!empty selected && wiki.name == selected.name}">selected="selected"</c:if>>${wiki.name}</option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.search" /></div>
</div>
</fieldset>
</form>
<form action="<jamwiki:link value="Special:VirtualWiki" />#vwiki" method="post">
<fieldset>
<legend><fmt:message key="admin.vwiki.title.virtualwiki" /></legend>
<input type="hidden" name="function" value="virtualwiki" />
<c:if test="${!empty selected && selected.virtualWikiId != -1}">
	<input type="hidden" name="virtualWikiId" value="${selected.virtualWikiId}" />
	<input type="hidden" name="name" value="${selected.name}" />
</c:if>
<div class="row">
	<label for="name"><fmt:message key="common.name" /></label>
	<span>
		<c:choose>
			<c:when test="${!empty selected && selected.virtualWikiId != -1}">${selected.name}</c:when>
			<c:otherwise><input type="text" name="name" id="name" size="30" value="<c:if test="${!empty selected}">${selected.name}</c:if>" /></c:otherwise>
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
	<div class="formhelp"><fmt:message key="admin.vwiki.help.defaulttopic" /></div>
</div>
<div class="row">
	<label for="defaultVirtualWiki"><fmt:message key="admin.vwiki.caption.makedefault" /></label>
	<span><jamwiki:checkbox name="defaultVirtualWiki" value="true" checked="${!empty selected && selected.defaultVirtualWiki}" id="defaultVirtualWiki" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.makedefault" /></div>
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
</div>

<!-- Add Namesapce -->
<div id="addnamespace" class="submenu-tab-item">
<a name="addnamespace"></a>
<c:if test="${!empty message && function == 'addnamespace'}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>
<c:if test="${!empty errors && function == 'addnamespace'}">
<div class="message red"><c:forEach items="${errors}" var="message"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message><br /></c:forEach></div>
</c:if>
<form action="<jamwiki:link value="Special:VirtualWiki" />#addnamespace" method="post">
<fieldset>
<legend><fmt:message key="admin.vwiki.title.namespace.add" /></legend>
<input type="hidden" name="function" value="addnamespace" />
<c:if test="${!empty selected}">
	<input type="hidden" name="selected" value="${selected.name}" />
</c:if>
<div class="rowhelp">
	<fmt:message key="admin.vwiki.help.namespace.add" />
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.namespace.main" /></label>
	<span>
		<input type="text" name="mainNamespace" size="30" value="${mainNamespace}" />
	</span>
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.namespace.comments" /></label>
	<span>
		<input type="text" name="commentsNamespace" size="30" value="${commentsNamespace}" />
	</span>
</div>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.add" />" /></span>
</div>
</fieldset>
</form>
</div>

<!-- Add/Update Namesapce Translations -->
<div id="namespaces" class="submenu-tab-item">
<a name="namespaces"></a>
<c:if test="${!empty message && function == 'namespaces'}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>
<c:if test="${!empty errors && function == 'namespaces'}">
<div class="message red"><c:forEach items="${errors}" var="message"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message><br /></c:forEach></div>
</c:if>
<!-- Select Virtual Wiki -->
<form action="<jamwiki:link value="Special:VirtualWiki" />#namespaces" method="get" name="selectnamespaces">
<input type="hidden" name="function" value="search" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.select" /></legend>
<div class="row">
	<label for="name"><fmt:message key="common.name" /></label>
	<span>
		<select name="selected" onchange="document.selectnamespaces.submit()">
		<option value=""></option>
		<c:forEach items="${wikis}" var="wiki"><option value="${wiki.name}" <c:if test="${!empty selected && wiki.name == selected.name}">selected="selected"</c:if>>${wiki.name}</option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.search" /></div>
</div>
</fieldset>
</form>
<c:if test="${!empty selected}">
	<form action="<jamwiki:link value="Special:VirtualWiki" />#namespaces" method="post">
	<input type="hidden" name="function" value="namespaces" />
	<input type="hidden" name="selected" value="${selected.name}" />
	<fieldset>
	<legend><fmt:message key="admin.vwiki.title.namespace.translations" /></legend>
	<div class="rowhelp">
		<p><fmt:message key="admin.vwiki.help.namespace.translations" /></p>
		<p><fmt:message key="admin.vwiki.help.namespace.translations.warning" /></p>
	</div>
	<c:forEach items="${namespaces}" var="namespace">
		<%-- suppress display of namespaces that cannot be translated --%>
		<div class="row">
			<label>${namespace.defaultLabel} [${namespace.id}]</label>
			<%-- do not allow translations of the Special: and JAMWiki: namespaces --%>
			<c:choose>
				<c:when test="${namespace.id >= 0 && namespace.id != 8}">
					<input type="hidden" name="namespace_id" value="${namespace.id}" />
					<input type="hidden" name="${namespace.id}_label" value="${namespace.defaultLabel}" />
					<span><input type="text" name="${namespace.id}_vwiki" size="30" value="${namespace.namespaceTranslations[selected.name]}" /></span>
				</c:when>
				<c:otherwise>
					<span><fmt:message key="admin.vwiki.caption.namespace.notallowed" /></span>
				</c:otherwise>
			</c:choose>
		</div>
	</c:forEach>
	<div class="row">
		<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
	</div>
	</fieldset>
	</form>
</c:if>
</div>

<!-- Interwiki Links -->
<div id="interwiki" class="submenu-tab-item">
<a name="interwiki"></a>
<c:if test="${!empty message && (function == 'addInterwiki' || function == 'updateInterwiki')}">
<div class="message green"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message></div>
</c:if>
<c:if test="${!empty errors && (function == 'addInterwiki' || function == 'updateInterwiki')}">
<div class="message red"><c:forEach items="${errors}" var="message"><fmt:message key="${message.key}"><fmt:param value="${message.params[0]}" /></fmt:message><br /></c:forEach></div>
</c:if>
<%-- Add Interwiki --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#interwiki" method="post">
<input type="hidden" name="function" value="addInterwiki" />
<input type="hidden" name="selected" value="${selected.name}" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.interwiki.add" /></legend>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.interwiki.prefix" /></label>
	<span><input type="text" name="interwikiPrefix" size="30" maxlength="30" value="${interwikiPrefix}" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.interwiki.prefix" /></div>
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.interwiki.pattern" /></label>
	<span><input type="text" name="interwikiPattern" size="30" maxlength="200" value="${interwikiPattern}" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.interwiki.pattern" /></div>
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.interwiki.display" /></label>
	<span><input type="text" name="interwikiDisplay" size="30" maxlength="30" value="${interwikiDisplay}" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.interwiki.display" /></div>
</div>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.add" />" /></span>
</div>
</fieldset>
</form>
<c:if test="${!empty interwikis}">
<%-- Update Interwiki --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#interwiki" method="post">
<input type="hidden" name="function" value="updateInterwiki" />
<input type="hidden" name="selected" value="${selected.name}" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.interwiki.update" /></legend>
<c:if test="${!empty selected}">
	<input type="hidden" name="selected" value="${selected.name}" />
</c:if>
<c:forEach items="${interwikis}" var="interwiki" varStatus="status">
<div class="row">
	<input type="hidden" name="interwikiPrefix" value="${interwiki.interwikiPrefix}" />
	<label for="name">${interwiki.interwikiPrefix}</label>
	<span>
		<input type="text" name="pattern-${interwiki.interwikiPrefix}" size="50" maxlength="200" value="${interwiki.interwikiPattern}" />
		<input type="text" name="display-${interwiki.interwikiDisplay}" size="30" maxlength="30" value="${interwiki.interwikiDisplay}" />
		<input type="checkbox" name="delete-${interwiki.interwikiPrefix}" value="true" /> <fmt:message key="admin.vwiki.caption.interwiki.delete" />
	</span>
</div>
</c:forEach>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
</div>
</fieldset>
</form>
</c:if>
</div>

</div>

</div>
