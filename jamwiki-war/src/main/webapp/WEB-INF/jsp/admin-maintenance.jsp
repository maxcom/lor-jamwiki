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

<%@ include file="shared-db-javascript.jsp" %>

<div id="maintenance" class="admin">

<%-- sub-menu tabs --%>
<ul class="tab-menu" id="tab_submenu">
<li><a href="#system"><fmt:message key="admin.maintenance.title.system" /></a></li>
<li><a href="#data"><fmt:message key="admin.maintenance.title.data" /></a></li>
<li><a href="#password"><fmt:message key="admin.title.password" /></a></li>
<li><a href="#adduser"><fmt:message key="admin.title.adduser" /></a></li>
<li><a href="#migrate"><fmt:message key="admin.title.migratedatabase" /></a></li>
</ul>
<div class="submenu-tab-content">

<%-- System Tools --%>
<div id="system" class="submenu-tab-item">

<%-- Refresh Search Index --%>
<c:if test="${!empty message && function == 'search'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'search'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.refresh" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#system" method="post">
<div class="row">
	<label><fmt:message key="admin.title.refresh" /></label>
	<span><input type="submit" name="submit" value="<fmt:message key="admin.action.refresh" />" /></span>
	<div class="formhelp"><fmt:message key="admin.help.rebuildsearch" /></div>
</div>
<input type="hidden" name="function" value="search" />
</form>
</fieldset>

<%-- Cache --%>
<c:if test="${!empty message && function == 'cache'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'cache'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.cache" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#system" method="post">
<div class="row">
	<label><fmt:message key="admin.cache.caption" /></label>
	<span><input type="submit" value="<fmt:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><fmt:message key="admin.help.clearcache" /></div>
</div>
<input type="hidden" name="function" value="cache" />
</form>
</fieldset>

<jamwiki:enabled property="PROP_TOPIC_SPAM_FILTER">

<%-- Spam Filter --%>
<c:if test="${!empty message && function == 'spam'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'spam'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.spamfilter" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#system" method="post">
<div class="row">
	<label><fmt:message key="admin.caption.spamfilter" /></label>
	<span><input type="submit" value="<fmt:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><fmt:message key="admin.help.reloadspamfilter" /></div>
</div>
<input type="hidden" name="function" value="spam" />
</form>
</fieldset>

</jamwiki:enabled>

</div>

<%-- Data Tools --%>
<div id="data" class="submenu-tab-item">

<%-- Recent Changes --%>
<c:if test="${!empty message && function == 'recentchanges'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'recentchanges'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.recentchanges" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#data" method="post">
<div class="row">
	<label><fmt:message key="admin.caption.recentchanges" /></label>
	<span><input type="submit" value="<fmt:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><fmt:message key="admin.help.reloadrecentchanges" /></div>
</div>
<input type="hidden" name="function" value="recentchanges" />
</form>
</fieldset>

<%-- Log Items --%>
<c:if test="${!empty message && function == 'logitems'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'logitems'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.logitems" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#data" method="post">
<div class="row">
	<label><fmt:message key="admin.caption.logitems" /></label>
	<span><input type="submit" value="<fmt:message key="admin.caption.reset" />" /></span>
	<div class="formhelp"><fmt:message key="admin.help.reloadlogitems" /></div>
</div>
<input type="hidden" name="function" value="logitems" />
</form>
</fieldset>

<%-- Namespaces --%>
<c:if test="${!empty message && function == 'namespaces'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'namespaces'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.maintenance.title.namespaces" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#data" method="post">
<div class="row">
	<label><fmt:message key="admin.maintenance.caption.namespaces" /></label>
	<span><input type="submit" value="<fmt:message key="common.update" />" /></span>
	<div class="formhelp"><fmt:message key="admin.maintenance.help.namespaces" /></div>
</div>
<input type="hidden" name="function" value="namespaces" />
</form>
</fieldset>

<%-- Topic Links --%>
<c:if test="${!empty message && function == 'links'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'links'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.maintenance.title.links" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#data" method="post">
<div class="row">
	<label><fmt:message key="admin.maintenance.caption.links" /></label>
	<span><input type="submit" value="<fmt:message key="common.update" />" /></span>
	<div class="formhelp"><fmt:message key="admin.maintenance.help.links" /></div>
</div>
<input type="hidden" name="function" value="links" />
</form>
</fieldset>

</div>

<%-- Password Reset --%>
<div id="password" class="submenu-tab-item">
<c:if test="${!empty message && function == 'password'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'password'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.password" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#password" method="post">
<div class="rowhelp"><fmt:message key="admin.password.help.reset" /></div>
<div class="row">
	<label for="passwordLogin"><fmt:message key="admin.password.caption.login" /></label>
	<span><input type="text" name="passwordLogin" id="passwordLogin" value="<c:out value="${passwordLogin}" />" size="30" /></span>
</div>
<div class="row">
	<label for="passwordPassword"><fmt:message key="admin.password.caption.password" /></label>
	<span><input type="password" name="passwordPassword" id="passwordPassword" value="<c:out value="${passwordPassword}" />" size="30" /></span>
</div>
<div class="row">
	<label for="passwordPasswordConfirm"><fmt:message key="admin.password.caption.passwordconfirm" /></label>
	<span><input type="password" name="passwordPasswordConfirm" id="passwordPasswordConfirm" value="<c:out value="${passwordPasswordConfirm}" />" size="30" /></span>
</div>
<div class="row">
	<label><fmt:message key="admin.password.caption.reset" /></label>
	<span><input type="submit" value="<fmt:message key="admin.caption.reset" />" /></span>
</div>
<input type="hidden" name="function" value="password" />
</form>
</fieldset>
</div>

<%-- Add User  --%>
<div id="adduser" class="submenu-tab-item">
<c:if test="${!empty message && function == 'adduser'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'adduser'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.adduser" /></legend>
<form action="<jamwiki:link value="Special:Maintenance" />#adduser" method="post">
<div class="rowhelp"><fmt:message key="admin.help.adduser" /></div>
<div class="row">
	<label for="adduserLogin"><fmt:message key="admin.adduser.caption.login" /></label>
	<span><input type="text" name="adduserLogin" id="adduserLogin" value="<c:out value="${adduserLogin}" />" size="30" /></span>
</div>
<div class="row">
	<label for="adduserPassword"><fmt:message key="admin.adduser.caption.password" /></label>
	<span><input type="password" name="adduserPassword" id="adduserPassword" value="<c:out value="${adduserPassword}" />" size="30" /></span>
</div>
<div class="row">
	<label for="adduserPasswordConfirm"><fmt:message key="admin.adduser.caption.passwordconfirm" /></label>
	<span><input type="password" name="adduserPasswordConfirm" id="adduserPasswordConfirm" value="<c:out value="${adduserPasswordConfirm}" />" size="30" /></span>
</div>
<div class="row">
	<label for="adduserEmail"><fmt:message key="admin.adduser.caption.email" /></label>
	<span><input type="text" name="adduserEmail" id="adduserEmail" value="<c:out value="${adduserEmail}" />" size="50" /></span>
</div>
<div class="row">
	<label for="adduserdisplayName"><fmt:message key="admin.adduser.caption.displayname" /></label>
	<span><input type="text" name="adduserdisplayName" id="adduserdisplayName" value="<c:out value="${adduserdisplayName}" />" size="50" /></span>
</div>
<div class="row">
	<label><fmt:message key="admin.adduser.caption.adduser" /></label>
	<span><input type="submit" value="<fmt:message key="admin.caption.adduser" />" /></span>
</div>
<input type="hidden" name="function" value="adduser" />
</form>
</fieldset>
</div>

<%-- Migrate Database --%>
<div id="migrate" class="submenu-tab-item">
<c:if test="${!empty message && function == 'migrate'}">
<div class="message green"><jamwiki_t:wikiMessage message="${message}" /></div>
</c:if>
<c:if test="${!empty errors && function == 'migrate'}">
<div class="message red"><c:forEach items="${errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<fieldset>
<legend><fmt:message key="admin.title.migratedatabase" /> (<fmt:message key="common.caption.experimental" />)</legend>
<form action="<jamwiki:link value="Special:Maintenance" />#migrate" method="post">
<div class="rowhelp"><fmt:message key="admin.help.migratedatabase" /></div>
<div class="row">
	<label for="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"><fmt:message key="admin.persistence.caption" /></label>
	<span>
		<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" onchange="onPersistenceType()">
		<c:set var="persistenceType"><%= Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE) %></c:set>
		<c:set var="persistenceTypeInternal"><%= WikiBase.PERSISTENCE_INTERNAL %></c:set>
		<c:set var="persistenceTypeExternal"><%= WikiBase.PERSISTENCE_EXTERNAL %></c:set>
		<option value="<%= WikiBase.PERSISTENCE_INTERNAL %>"<c:if test="${persistenceType == persistenceTypeInternal}"> selected</c:if>><fmt:message key="admin.persistencetype.internal"/></option>
		<option value="<%= WikiBase.PERSISTENCE_EXTERNAL %>"<c:if test="${persistenceType == persistenceTypeExternal}"> selected</c:if>><fmt:message key="admin.persistencetype.database"/></option>
		</select>
	</span>
</div>
<div class="row">
	<label for="<%= Environment.PROP_DB_TYPE %>"><fmt:message key="admin.persistence.caption.type" /></label>
	<span>
		<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>" onchange="onDatabaseType()">
		<c:set var="selectedDataHandler"><%= Environment.getValue(Environment.PROP_DB_TYPE) %></c:set>
		<c:forEach items="${dataHandlers}" var="dataHandler">
		<option value="<c:out value="${dataHandler.clazz}" />"<c:if test="${selectedDataHandler == dataHandler.clazz}"> selected</c:if>><c:if test="${!empty dataHandler.key}"><fmt:message key="${dataHandler.key}" /></c:if><c:if test="${empty dataHandler.key}"><c:out value="${dataHandler.name}" /></c:if><c:if test="${dataHandler.experimental}"> (<fmt:message key="common.caption.experimental" />)</c:if></option>
		</c:forEach>
		</select>
	</span>
</div>
<div class="row">
	<label for="<%= Environment.PROP_DB_DRIVER %>"><fmt:message key="admin.persistence.caption.driver" /></label>
	<span><input type="text" name="<%= Environment.PROP_DB_DRIVER %>" id="<%= Environment.PROP_DB_DRIVER %>" value="<%= Environment.getValue(Environment.PROP_DB_DRIVER) %>" size="50" /></span>
</div>
<div class="row">
	<label for="<%= Environment.PROP_DB_URL %>"><fmt:message key="admin.persistence.caption.url" /></label>
	<span><input type="text" name="<%= Environment.PROP_DB_URL %>" id="<%= Environment.PROP_DB_URL %>" value="<%= Environment.getValue(Environment.PROP_DB_URL) %>" size="50" /></span>
</div>
<div class="row">
	<label for="<%= Environment.PROP_DB_USERNAME %>"><fmt:message key="admin.persistence.caption.user" /></label>
	<span><input type="text" name="<%= Environment.PROP_DB_USERNAME %>" id="<%= Environment.PROP_DB_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_DB_USERNAME) %>" size="15" /></span>
</div>
<div class="row">
	<label for="<%= Environment.PROP_DB_PASSWORD %>"><fmt:message key="admin.persistence.caption.pass" /></label>
	<span><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="15" /></span>
</div>
<div class="row">
	<label><fmt:message key="admin.caption.migratedatabase" /></label>
	<span><input type="submit" value="<fmt:message key="common.migrate" />" /></span>
</div>
<input type="hidden" name="function" value="migrate" />
</form>
</fieldset>
</div>

</div>

</div>

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
