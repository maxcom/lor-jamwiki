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
<%@ page import="org.jamwiki.Environment,org.jamwiki.WikiBase"
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div class="submenu">
<a href="#virtualwiki"><f:message key="admin.title.virtualwiki" /></a> | <a href="#search"><f:message key="admin.title.refresh" /></a> | <a href="#recentchanges"><f:message key="admin.title.recentchanges" /></a><br />
<a href="#cache"><f:message key="admin.title.cache" /></a> <jamwiki:enabled property="PROP_TOPIC_SPAM_FILTER">| <a href="#spam"><f:message key="admin.title.spamfilter" /></a> </jamwiki:enabled><c:if test="${allowExportToCsv}">| <a href="#export"><f:message key="admin.title.exportcsv" /></a></c:if>| <a href="#migrate"><f:message key="admin.title.migratedatabase" /></a>
</div>

<c:if test="${!empty message}">
<div class="message red"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div>
</c:if>
<c:if test="${!empty errors}">
<div class="message red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></div>
</c:if>

<!-- Virtual Wikis -->
<a name="virtualwiki"></a>
<fieldset>
<legend><f:message key="admin.title.virtualwiki" /></legend>
<table border="0" class="contents" width="99%">
<tr class="darkbg">
	<th><f:message key="common.name" /></th>
	<th><f:message key="admin.caption.defaulttopic" /></th>
	<th>&#160;</th>
</tr>
<c:forEach items="${wikis}" var="wiki">
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">
<input type="hidden" name="function" value="addVirtualWiki" />
<input type="hidden" name="virtualWikiId" value="<c:out value="${wiki.virtualWikiId}" />" />
<input type="hidden" name="name" value="<c:out value="${wiki.name}" />" />
<tr class="<jamwiki:alternate value1="lightbg" value2="mediumbg" attributeName="virtualwiki" />">
	<%-- FIXME: need label element --%>
	<td class="formelement" style="width:150px"><c:out value="${wiki.name}" /></td>
	<td class="formelement" style="width:200px"><input type="text" name="defaultTopicName" value="<c:out value="${wiki.defaultTopicName}" />" size="30" /></td>
	<td class="formelement"><input type="submit" value="<f:message key="common.update" />" /></td>
</tr>
</form>
</c:forEach>
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">
<input type="hidden" name="function" value="addVirtualWiki" />
<tr class="<jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="virtualwiki" />">
	<td class="formelement"><input type="text" name="name" /></td>
	<td class="formelement"><input type="text" name="defaultTopicName" value="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" size="30" /></td>
	<td class="formelement"><input type="submit" value="<f:message key="common.add" />" /></td>
</tr>
</form>
<tr><td colspan="3">&nbsp;</td></tr>
</table>
</fieldset>

<!-- Refresh Search Index -->
<a name="search"></a>
<fieldset>
<legend><f:message key="admin.title.refresh" /></legend>
<form name="refreshform" method="post" action="<jamwiki:link value="Special:Maintenance" />">
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="search" />">
	<span class="formcaption"><f:message key="admin.title.refresh" /></span>
	<span class="formelement"><input type="submit" name="submit" value="<f:message key="admin.action.refresh" />" /></span>
	<div class="formhelp"><f:message key="admin.help.rebuildsearch" /></div>
</div>
<input type="hidden" name="function" value="refreshIndex" />
</form>
</fieldset>

<!-- Recent Changes -->
<a name="recentchanges"></a>
<fieldset>
<legend><f:message key="admin.title.recentchanges" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="recentchanges" />">
	<span class="formcaption"><f:message key="admin.caption.recentchanges" /></span>
	<span class="formelement"><input type="submit" value="<f:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><f:message key="admin.help.reloadrecentchanges" /></div>
</div>
<input type="hidden" name="function" value="recentChanges" />
</form>
</fieldset>

<!-- Cache -->
<a name="cache"></a>
<fieldset>
<legend><f:message key="admin.title.cache" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="cache" />">
	<span class="formcaption"><f:message key="admin.cache.caption" /></span>
	<span class="formelement"><input type="submit" value="<f:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><f:message key="admin.help.clearcache" /></div>
</div>
<input type="hidden" name="function" value="cache" />
</form>
</fieldset>

<jamwiki:enabled property="PROP_TOPIC_SPAM_FILTER">

<!-- Spam Filter -->
<a name="spam"></a>
<fieldset>
<legend><f:message key="admin.title.spamfilter" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="spam" />">
	<span class="formcaption"><f:message key="admin.caption.spamfilter" /></span>
	<span class="formelement"><input type="submit" value="<f:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><f:message key="admin.help.reloadspamfilter" /></div>
</div>
<input type="hidden" name="function" value="spamFilter" />
</form>
</fieldset>

</jamwiki:enabled>

<c:if test="${allowExportToCsv}">

<!-- Export to CSV -->
<a name="export"></a>
<fieldset>
<legend><f:message key="admin.title.exportcsv" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">
<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="export" />">
	<span class="formcaption"><f:message key="admin.caption.exportcsv" /></span>
	<span class="formelement"><input type="submit" value="<f:message key="common.export" />" /></span>
	<div class="formhelp"><f:message key="admin.help.exportcsv" /></div>
</div>
<input type="hidden" name="function" value="exportToCsv" />
</form>
</fieldset>

</c:if>

<!-- Migrate Database -->
<a name="migrate"></a>
<fieldset>
<legend><f:message key="admin.title.migratedatabase" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />" method="post">

<div class="formentry <jamwiki:alternate value1="mediumbg" value2="lightbg" attributeName="migrate" />">
	<div class="formhelp"><f:message key="admin.help.migratedatabase" /></div>
<table style="border:none;">
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"><f:message key="admin.persistence.caption" /></label>:</td>
	<td class="formelement">
		<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" onchange="onPersistenceType()">
		<c:set var="persistenceType"><%= Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE) %></c:set>
		<c:set var="persistenceTypeInternal"><%= WikiBase.PERSISTENCE_INTERNAL %></c:set>
		<c:set var="persistenceTypeExternal"><%= WikiBase.PERSISTENCE_EXTERNAL %></c:set>
		<option value="<%= WikiBase.PERSISTENCE_INTERNAL %>"<c:if test="${persistenceType == persistenceTypeInternal}"> selected</c:if>><f:message key="admin.persistencetype.internal"/></option>
		<option value="<%= WikiBase.PERSISTENCE_EXTERNAL %>"<c:if test="${persistenceType == persistenceTypeExternal}"> selected</c:if>><f:message key="admin.persistencetype.database"/></option>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_DRIVER %>"><f:message key="admin.persistence.caption.driver" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_DB_DRIVER %>" id="<%= Environment.PROP_DB_DRIVER %>" value="<%= Environment.getValue(Environment.PROP_DB_DRIVER) %>" size="50"></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_TYPE %>"><f:message key="admin.persistence.caption.type" /></label>:</td>
	<td class="formelement">
		<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>">
		<c:set var="selectedDataHandler"><%= Environment.getValue(Environment.PROP_DB_TYPE) %></c:set>
		<c:forEach items="${dataHandlers}" var="dataHandler">
		<option value="<c:out value="${dataHandler.clazz}" />"<c:if test="${selectedDataHandler == dataHandler.clazz}"> selected</c:if>><c:if test="${!empty dataHandler.key}"><f:message key="${dataHandler.key}" /></c:if><c:if test="${empty dataHandler.key}"><c:out value="${dataHandler.name}" /></c:if><c:if test="${dataHandler.experimental}"> (<f:message key="common.caption.experimental" />)</c:if></option>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_URL %>"><f:message key="admin.persistence.caption.url" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_DB_URL %>" id="<%= Environment.PROP_DB_URL %>" value="<%= Environment.getValue(Environment.PROP_DB_URL) %>" size="50"></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_USERNAME %>"><f:message key="admin.persistence.caption.user" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_DB_USERNAME %>" id="<%= Environment.PROP_DB_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_DB_USERNAME) %>" size="15"></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_PASSWORD %>"><f:message key="admin.persistence.caption.pass" /></label>:</td>
	<td class="formelement"><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="15"></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
 <td><span class="formcaption"><f:message key="admin.caption.migratedatabase" /></span></td>
 <td><span class="formelement"><input type="submit" value="<f:message key="common.migrate" />" /></span></td></tr>
</table>
	
</div>
<input type="hidden" name="function" value="migrateDatabase" />
</form>
</fieldset>

<script type="text/javascript">
function onPersistenceType() {
	if (document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").options[document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").selectedIndex].value == "<%= WikiBase.PERSISTENCE_INTERNAL %>") {
		document.getElementById("<%= Environment.PROP_DB_DRIVER %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_TYPE %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_URL %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_USERNAME %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_PASSWORD %>").disabled=true
	} else {
		document.getElementById("<%= Environment.PROP_DB_DRIVER %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_TYPE %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_URL %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_USERNAME %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_PASSWORD %>").disabled=false
	}
}
</script>
