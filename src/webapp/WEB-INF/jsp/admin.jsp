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
        org.jamwiki.Environment,
        org.jamwiki.WikiBase
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<script type="text/javascript">
function onPersistenceType() {
	var disabled = true;
	if (document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").options[document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").selectedIndex].value == "<%= WikiBase.PERSISTENCE_EXTERNAL %>") {
		disabled = false;
	}
	document.getElementById("<%= Environment.PROP_DB_DRIVER %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_DB_TYPE %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_DB_URL %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_DB_USERNAME %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_DB_PASSWORD %>").disabled = disabled;
}
function onUploadType() {
	var whitelistDisabled = true;
	var blacklistDisabled = true;
	if (document.getElementById("<%= Environment.PROP_FILE_BLACKLIST_TYPE %>").options[document.getElementById("<%= Environment.PROP_FILE_BLACKLIST_TYPE %>").selectedIndex].value == "<%= WikiBase.UPLOAD_BLACKLIST %>") {
		blacklistDisabled = false;
	} else if (document.getElementById("<%= Environment.PROP_FILE_BLACKLIST_TYPE %>").options[document.getElementById("<%= Environment.PROP_FILE_BLACKLIST_TYPE %>").selectedIndex].value == "<%= WikiBase.UPLOAD_WHITELIST %>") {
		whitelistDisabled = false;
	}
	document.getElementById("<%= Environment.PROP_FILE_BLACKLIST %>").disabled = blacklistDisabled;
	document.getElementById("<%= Environment.PROP_FILE_WHITELIST %>").disabled = whitelistDisabled;
}
function onLdap() {
	var disabled = true;
	if (document.getElementById("<%= Environment.PROP_BASE_USER_HANDLER %>").options[document.getElementById("<%= Environment.PROP_BASE_USER_HANDLER %>").selectedIndex].value == "<%= WikiBase.USER_HANDLER_LDAP %>") {
		disabled = false;
	}
	document.getElementById("<%= Environment.PROP_LDAP_FACTORY_CLASS %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_URL %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_CONTEXT %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_FIELD_EMAIL %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_FIELD_FIRST_NAME %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_FIELD_LAST_NAME %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_FIELD_USERID %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_LOGIN %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_PASSWORD %>").disabled = disabled;
	document.getElementById("<%= Environment.PROP_LDAP_SECURITY_AUTHENTICATION %>").disabled = disabled;
}
function onRSS() {
	var disabled = true;
	if (document.getElementById("<%= Environment.PROP_RSS_ALLOWED %>").checked) {
		disabled = false;
	}
	document.getElementById("<%= Environment.PROP_RSS_TITLE %>").disabled = disabled;
}
</script>

<form name="form1" method="post" action="<jamwiki:link value="Special:Admin" />">

<c:if test="${!empty message}">
<div class="message red"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></div>
</c:if>
<c:if test="${!empty errors}">
<div class="message red"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></div>
</c:if>

<div class="submenu">
<a href="#general"><f:message key="admin.header.general" /></a> | <a href="#parser"><f:message key="admin.header.parser" /></a> | <a href="#database"><f:message key="admin.header.persistence" /></a> | <a href="#upload"><f:message key="admin.header.upload" /></a><br />
<a href="#authentication"><f:message key="admin.header.ldap" /></a> | <a href="#cache"><f:message key="admin.header.cache" /></a> | <a href="#rss"><f:message key="admin.header.rss" /></a> | <a href="#save"><f:message key="admin.action.save" /></a>
</div>

<!-- BEGIN GENERAL SETTINGS -->
<a name="general"></a>
<fieldset>
<legend><f:message key="admin.header.general" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_DEFAULT_TOPIC %>"><f:message key="admin.caption.defaulttopic" /></label></td>
	<c:set var="PROP_BASE_DEFAULT_TOPIC"><%= Environment.PROP_BASE_DEFAULT_TOPIC %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_BASE_DEFAULT_TOPIC}" value="${props[PROP_BASE_DEFAULT_TOPIC]}" size="30" id="${PROP_BASE_DEFAULT_TOPIC}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_LOGO_IMAGE %>"><f:message key="admin.caption.logoimage" /></label></td>
	<c:set var="PROP_BASE_LOGO_IMAGE"><%= Environment.PROP_BASE_LOGO_IMAGE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_BASE_LOGO_IMAGE}" value="${props[PROP_BASE_LOGO_IMAGE]}" size="30" id="${PROP_BASE_LOGO_IMAGE}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_TOPIC_NON_ADMIN_TOPIC_MOVE %>"><f:message key="admin.caption.nonadminmove" /></label></td>
	<c:set var="PROP_TOPIC_NON_ADMIN_TOPIC_MOVE"><%= Environment.PROP_TOPIC_NON_ADMIN_TOPIC_MOVE %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_TOPIC_NON_ADMIN_TOPIC_MOVE}" value="true" checked="${props[PROP_TOPIC_NON_ADMIN_TOPIC_MOVE]}" id="${PROP_TOPIC_NON_ADMIN_TOPIC_MOVE}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_TOPIC_FORCE_USERNAME %>"><f:message key="admin.caption.forceusername" /></label></td>
	<c:set var="PROP_TOPIC_FORCE_USERNAME"><%= Environment.PROP_TOPIC_FORCE_USERNAME %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_TOPIC_FORCE_USERNAME}" value="true" checked="${props[PROP_TOPIC_FORCE_USERNAME]}" id="${PROP_TOPIC_FORCE_USERNAME}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.forceusernamehelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_IMAGE_RESIZE_INCREMENT %>"><f:message key="admin.caption.imageresize" /></label></td>
	<c:set var="PROP_IMAGE_RESIZE_INCREMENT"><%= Environment.PROP_IMAGE_RESIZE_INCREMENT %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_IMAGE_RESIZE_INCREMENT}" size="5" maxlength="4" value="${props[PROP_IMAGE_RESIZE_INCREMENT]}" id="${PROP_IMAGE_RESIZE_INCREMENT}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_RECENT_CHANGES_NUM %>"><f:message key="admin.caption.recentchangesdefault" /></label></td>
	<c:set var="PROP_RECENT_CHANGES_NUM"><%= Environment.PROP_RECENT_CHANGES_NUM %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_RECENT_CHANGES_NUM}" size="5" maxlength="4" value="${props[PROP_RECENT_CHANGES_NUM]}" id="${PROP_RECENT_CHANGES_NUM}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_TOPIC_SPAM_FILTER %>"><f:message key="admin.caption.usespamfilter" /></label></td>
	<c:set var="PROP_TOPIC_SPAM_FILTER"><%= Environment.PROP_TOPIC_SPAM_FILTER %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_TOPIC_SPAM_FILTER}" value="true" checked="${props[PROP_TOPIC_SPAM_FILTER]}" id="${PROP_TOPIC_SPAM_FILTER}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_TOPIC_USE_PREVIEW %>"><f:message key="admin.caption.usepreview" /></label></td>
	<c:set var="PROP_TOPIC_USE_PREVIEW"><%= Environment.PROP_TOPIC_USE_PREVIEW %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_TOPIC_USE_PREVIEW}" value="true" checked="${props[PROP_TOPIC_USE_PREVIEW]}" id="${PROP_TOPIC_USE_PREVIEW}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_TOPIC_WYSIWYG %>"><f:message key="admin.caption.wysiwyg" /></label></td>
	<c:set var="PROP_TOPIC_WYSIWYG"><%= Environment.PROP_TOPIC_WYSIWYG %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_TOPIC_WYSIWYG}" value="true" checked="${props[PROP_TOPIC_WYSIWYG]}" id="${PROP_TOPIC_WYSIWYG}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PRINT_NEW_WINDOW %>"><f:message key="admin.caption.printnewwindow" /></label></td>
	<c:set var="PROP_PRINT_NEW_WINDOW"><%= Environment.PROP_PRINT_NEW_WINDOW %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_PRINT_NEW_WINDOW}" value="true" checked="${props[PROP_PRINT_NEW_WINDOW]}" id="${PROP_PRINT_NEW_WINDOW}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_EXTERNAL_LINK_NEW_WINDOW %>"><f:message key="admin.caption.externallinknewwindow" /></label></td>
	<c:set var="PROP_EXTERNAL_LINK_NEW_WINDOW"><%= Environment.PROP_EXTERNAL_LINK_NEW_WINDOW %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_EXTERNAL_LINK_NEW_WINDOW}" value="true" checked="${props[PROP_EXTERNAL_LINK_NEW_WINDOW]}" id="${PROP_EXTERNAL_LINK_NEW_WINDOW}" /></td>
</tr>
<tr>
	<td class="formcaption" valign="top"><label for="<%= Environment.PROP_BASE_META_DESCRIPTION %>"><f:message key="admin.caption.metadescription" /></label></td>
	<c:set var="PROP_BASE_META_DESCRIPTION"><%= Environment.PROP_BASE_META_DESCRIPTION %></c:set>
	<td class="formelement"><textarea cols="30" rows="3" name="<%= Environment.PROP_BASE_META_DESCRIPTION %>" id="<%= Environment.PROP_BASE_META_DESCRIPTION %>"><c:out value="${props[PROP_BASE_META_DESCRIPTION]}" /></textarea></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.metadescriptionhelp" /></td></tr>
</table>
</fieldset>
<!-- END GENERAL SETTINGS -->

<!-- BEGIN PARSER -->
<a name="parser"></a>
<fieldset>
<legend><f:message key="admin.header.parser" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_CLASS %>"><f:message key="admin.caption.parser" /></label></td>
	<td class="formelement">
		<select name="<%= Environment.PROP_PARSER_CLASS %>" id="<%= Environment.PROP_PARSER_CLASS %>">
		<c:set var="PROP_PARSER_CLASS"><%= Environment.PROP_PARSER_CLASS %></c:set>
		<c:forEach items="${parsers}" var="parser">
		<option value="<c:out value="${parser.clazz}" />"<c:if test="${props[PROP_PARSER_CLASS] == parser.clazz}"> selected="selected"</c:if>><c:if test="${!empty parser.key}"><f:message key="${parser.key}" /></c:if><c:if test="${empty parser.key}"><c:out value="${parser.name}" /></c:if></option>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_TOC %>"><f:message key="admin.caption.tableofcontents" /></label></td>
	<c:set var="PROP_PARSER_TOC"><%= Environment.PROP_PARSER_TOC %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_PARSER_TOC}" value="true" checked="${props[PROP_PARSER_TOC]}" id="${PROP_PARSER_TOC}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_TOC_DEPTH %>"><f:message key="admin.caption.tableofcontentsdepth" /></label></td>
	<c:set var="PROP_PARSER_TOC_DEPTH"><%= Environment.PROP_PARSER_TOC_DEPTH %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_PARSER_TOC_DEPTH}" value="${props[PROP_PARSER_TOC_DEPTH]}" size="5" maxlength="1" id="${PROP_PARSER_TOC_DEPTH}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.tableofcontentsdepthhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_ALLOW_HTML %>"><f:message key="admin.caption.allowhtml" /></label></td>
	<c:set var="PROP_PARSER_ALLOW_HTML"><%= Environment.PROP_PARSER_ALLOW_HTML %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_PARSER_ALLOW_HTML}" value="true" checked="${props[PROP_PARSER_ALLOW_HTML]}" id="${PROP_PARSER_ALLOW_HTML}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_ALLOW_JAVASCRIPT %>"><f:message key="admin.caption.allowjavascript" /></label></td>
	<c:set var="PROP_PARSER_ALLOW_JAVASCRIPT"><%= Environment.PROP_PARSER_ALLOW_JAVASCRIPT %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_PARSER_ALLOW_JAVASCRIPT}" value="true" checked="${props[PROP_PARSER_ALLOW_JAVASCRIPT]}" id="${PROP_PARSER_ALLOW_JAVASCRIPT}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_ALLOW_TEMPLATES %>"><f:message key="admin.caption.allowtemplates" /></label></td>
	<c:set var="PROP_PARSER_ALLOW_TEMPLATES"><%= Environment.PROP_PARSER_ALLOW_TEMPLATES %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_PARSER_ALLOW_TEMPLATES}" value="true" checked="${props[PROP_PARSER_ALLOW_TEMPLATES]}" id="${PROP_PARSER_ALLOW_TEMPLATES}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_SIGNATURE_USER_PATTERN %>"><f:message key="admin.caption.signatureuser" /></label></td>
	<c:set var="PROP_PARSER_SIGNATURE_USER_PATTERN"><%= Environment.PROP_PARSER_SIGNATURE_USER_PATTERN %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_PARSER_SIGNATURE_USER_PATTERN}" value="${props[PROP_PARSER_SIGNATURE_USER_PATTERN]}" size="50" id="${PROP_PARSER_SIGNATURE_USER_PATTERN}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.signatureuserhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN %>"><f:message key="admin.caption.signaturedate" /></label></td>
	<c:set var="PROP_PARSER_SIGNATURE_DATE_PATTERN"><%= Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_PARSER_SIGNATURE_DATE_PATTERN}" value="${props[PROP_PARSER_SIGNATURE_DATE_PATTERN]}" size="50" id="${PROP_PARSER_SIGNATURE_DATE_PATTERN}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.signaturedatehelp" /></td></tr>
</table>
</fieldset>
<!-- END PARSER -->

<%--
FIXME - Email not supported right now, comment this out

<!-- BEGIN EMAIL -->
<a name="email"></a>
<fieldset>
<legend><f:message key="admin.caption.smtp" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_EMAIL_SMTP_HOST %>"><f:message key="admin.caption.smtp.host" /></label></td>
	<c:set var="PROP_EMAIL_SMTP_HOST"><%= Environment.PROP_EMAIL_SMTP_HOST %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_EMAIL_SMTP_HOST}" value="${props[PROP_EMAIL_SMTP_HOST]}" size="30" id="${PROP_EMAIL_SMTP_HOST}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_EMAIL_SMTP_USERNAME %>"><f:message key="admin.caption.smtp.user" /></label></td>
	<c:set var="PROP_EMAIL_SMTP_USERNAME"><%= Environment.PROP_EMAIL_SMTP_USERNAME %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_EMAIL_SMTP_USERNAME}" value="${props[PROP_EMAIL_SMTP_USERNAME]}" size="30" id="${PROP_EMAIL_SMTP_USERNAME}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_EMAIL_SMTP_PASSWORD %>"><f:message key="admin.caption.smtp.pass" /></label></td>
	<td class="formelement"><input type="password" name="<%= Environment.PROP_EMAIL_SMTP_PASSWORD %>" value="<c:out value="${smtpPassword}" />" size="30" id="<%= Environment.PROP_EMAIL_SMTP_PASSWORD %>" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_EMAIL_REPLY_ADDRESS %>"><f:message key="admin.caption.reply" /></label></td>
	<c:set var="PROP_EMAIL_REPLY_ADDRESS"><%= Environment.PROP_EMAIL_REPLY_ADDRESS %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_EMAIL_REPLY_ADDRESS}" value="${props[PROP_EMAIL_REPLY_ADDRESS]}" size="50" id="${PROP_EMAIL_REPLY_ADDRESS}" /></td>
</tr>
</table>
</fieldset>
<!-- END EMAIL -->

--%>

<!-- BEGIN DATABASE PERSISTENCE -->
<a name="database"></a>
<fieldset>
<legend><f:message key="admin.header.persistence" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_FILE_DIR %>"><f:message key="admin.caption.filedir" /></label></td>
	<c:set var="PROP_BASE_FILE_DIR"><%= Environment.PROP_BASE_FILE_DIR %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_BASE_FILE_DIR}" value="${props[PROP_BASE_FILE_DIR]}" size="50" id="${PROP_BASE_FILE_DIR}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.filedirhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"><f:message key="admin.caption.persistence" /></label></td>
	<td class="formelement">
		<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" onchange="onPersistenceType()">
		<c:set var="PROP_BASE_PERSISTENCE_TYPE"><%= Environment.PROP_BASE_PERSISTENCE_TYPE %></c:set>
		<c:set var="persistenceTypeInternal"><%= WikiBase.PERSISTENCE_INTERNAL %></c:set>
		<c:set var="persistenceTypeExternal"><%= WikiBase.PERSISTENCE_EXTERNAL %></c:set>
		<option value="<%= WikiBase.PERSISTENCE_INTERNAL %>"<c:if test="${props[PROP_BASE_PERSISTENCE_TYPE] == persistenceTypeInternal}"> selected="selected"</c:if>><f:message key="admin.persistencetype.internal" /></option>
		<option value="<%= WikiBase.PERSISTENCE_EXTERNAL %>"<c:if test="${props[PROP_BASE_PERSISTENCE_TYPE] == persistenceTypeExternal}"> selected="selected"</c:if>><f:message key="admin.persistencetype.database" /></option>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_DRIVER %>"><f:message key="admin.caption.databasedriver" /></label></td>
	<c:set var="PROP_DB_DRIVER"><%= Environment.PROP_DB_DRIVER %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DB_DRIVER}" id="${PROP_DB_DRIVER}" value="${props[PROP_DB_DRIVER]}" size="50" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_TYPE %>"><f:message key="admin.caption.databasetype" /></label></td>
	<td class="formelement">
		<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>">
		<c:set var="PROP_DB_TYPE"><%= Environment.PROP_DB_TYPE %></c:set>
		<c:forEach items="${dataHandlers}" var="dataHandler">
		<option value="<c:out value="${dataHandler.clazz}" />"<c:if test="${props[PROP_DB_TYPE] == dataHandler.clazz}"> selected="selected"</c:if>><c:if test="${!empty dataHandler.key}"><f:message key="${dataHandler.key}" /></c:if><c:if test="${empty dataHandler.key}"><c:out value="${dataHandler.name}" /></c:if><c:if test="${dataHandler.experimental}"> (<f:message key="common.caption.experimental" />)</c:if></option>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_URL %>"><f:message key="admin.caption.databaseurl" /></label></td>
	<c:set var="PROP_DB_URL"><%= Environment.PROP_DB_URL %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DB_URL}" id="${PROP_DB_URL}" value="${props[PROP_DB_URL]}" size="50" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_USERNAME %>"><f:message key="admin.caption.databaseuser" /></label></td>
	<c:set var="PROP_DB_USERNAME"><%= Environment.PROP_DB_USERNAME %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DB_USERNAME}" id="${PROP_DB_USERNAME}" value="${props[PROP_DB_USERNAME]}" size="30" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_PASSWORD %>"><f:message key="admin.caption.databasepass" /></label></td>
	<td class="formelement"><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="30" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_MAX_ACTIVE %>"><f:message key="admin.caption.dbcp.maxactive" /></label></td>
	<c:set var="PROP_DBCP_MAX_ACTIVE"><%= Environment.PROP_DBCP_MAX_ACTIVE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DBCP_MAX_ACTIVE}" id="${PROP_DBCP_MAX_ACTIVE}" value="${props[PROP_DBCP_MAX_ACTIVE]}" size="5" maxlength="3" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_MAX_IDLE %>"><f:message key="admin.caption.dbcp.maxidle" /></label></td>
	<c:set var="PROP_DBCP_MAX_IDLE"><%= Environment.PROP_DBCP_MAX_IDLE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DBCP_MAX_IDLE}" id="${PROP_DBCP_MAX_IDLE}" value="${props[PROP_DBCP_MAX_IDLE]}" size="5" maxlength="3" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_TEST_ON_BORROW %>"><f:message key="admin.caption.dbcp.testonborrow" /></label></td>
	<c:set var="PROP_DBCP_TEST_ON_BORROW"><%= Environment.PROP_DBCP_TEST_ON_BORROW %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_DBCP_TEST_ON_BORROW}" value="true" checked="${props[PROP_DBCP_TEST_ON_BORROW]}" id="${PROP_DBCP_TEST_ON_BORROW}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_TEST_ON_RETURN %>"><f:message key="admin.caption.dbcp.testonreturn" /></label></td>
	<c:set var="PROP_DBCP_TEST_ON_RETURN"><%= Environment.PROP_DBCP_TEST_ON_RETURN %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_DBCP_TEST_ON_RETURN}" value="true" checked="${props[PROP_DBCP_TEST_ON_RETURN]}" id="${PROP_DBCP_TEST_ON_RETURN}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_TEST_WHILE_IDLE %>"><f:message key="admin.caption.dbcp.testwhileidle" /></label></td>
	<c:set var="PROP_DBCP_TEST_WHILE_IDLE"><%= Environment.PROP_DBCP_TEST_WHILE_IDLE %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_DBCP_TEST_WHILE_IDLE}" value="true" checked="${props[PROP_DBCP_TEST_WHILE_IDLE]}" id="${PROP_DBCP_TEST_WHILE_IDLE}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %>"><f:message key="admin.caption.dbcp.minevictableidletime" /></label></td>
	<c:set var="PROP_DBCP_MIN_EVICTABLE_IDLE_TIME"><%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DBCP_MIN_EVICTABLE_IDLE_TIME}" id="${PROP_DBCP_MIN_EVICTABLE_IDLE_TIME}" value="${props[PROP_DBCP_MIN_EVICTABLE_IDLE_TIME]}" size="5" maxlength="4" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %>"><f:message key="admin.caption.dbcp.timebetweenevictionruns" /></label></td>
	<c:set var="PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS"><%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS}" id="${PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS}" value="${props[PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS]}" size="5" maxlength="4" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %>"><f:message key="admin.caption.dbcp.numtestsperevictionrun" /></label></td>
	<c:set var="PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN"><%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN}" id="${PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN}" value="${props[PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN]}" size="5" maxlength="4" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>"><f:message key="admin.caption.dbcp.whenexhaustedaction" /></label></td>
	<td class="formelement">
		<select name="<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>" id="<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>">
		<c:set var="PROP_DBCP_WHEN_EXHAUSTED_ACTION"><%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %></c:set>
		<c:forEach items="${poolExhaustedMap}" var="poolExhausted">
		<option value="<c:out value="${poolExhausted.key}" />"<c:if test="${poolExhausted.key == props[PROP_DBCP_WHEN_EXHAUSTED_ACTION]}"> selected="selected"</c:if>><f:message key="${poolExhausted.value}" /></option>
		</c:forEach>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_REMOVE_ABANDONED %>"><f:message key="admin.caption.dbcp.removeabandoned" /></label></td>
	<c:set var="PROP_DBCP_REMOVE_ABANDONED"><%= Environment.PROP_DBCP_REMOVE_ABANDONED %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_DBCP_REMOVE_ABANDONED}" value="true" checked="${props[PROP_DBCP_REMOVE_ABANDONED]}" id="${PROP_DBCP_REMOVE_ABANDONED}" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %>"><f:message key="admin.caption.dbcp.removeabandonedtimeout" /></label></td>
	<c:set var="PROP_DBCP_REMOVE_ABANDONED_TIMEOUT"><%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_DBCP_REMOVE_ABANDONED_TIMEOUT}" id="${PROP_DBCP_REMOVE_ABANDONED_TIMEOUT}" value="${props[PROP_DBCP_REMOVE_ABANDONED_TIMEOUT]}" size="5" /></td>
</tr>
</table>
</fieldset>
<!-- END DATABASE PERSISTENCE -->

<!-- BEGIN FILE UPLOAD -->
<a name="upload"></a>
<fieldset>
<legend><f:message key="admin.header.upload" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_MAX_FILE_SIZE %>"><f:message key="admin.caption.maxfilesize" /></label></td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_FILE_MAX_FILE_SIZE %>" value="<c:out value="${maximumFileSize}" />" size="10" id="<%= Environment.PROP_FILE_MAX_FILE_SIZE %>" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_FULL_PATH %>"><f:message key="admin.caption.uploaddir" /></label></td>
	<c:set var="PROP_FILE_DIR_FULL_PATH"><%= Environment.PROP_FILE_DIR_FULL_PATH %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_FILE_DIR_FULL_PATH}" value="${props[PROP_FILE_DIR_FULL_PATH]}" size="50" id="${PROP_FILE_DIR_FULL_PATH}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.uploaddirhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>"><f:message key="admin.caption.uploaddirrel" /></label></td>
	<c:set var="PROP_FILE_DIR_RELATIVE_PATH"><%= Environment.PROP_FILE_DIR_RELATIVE_PATH %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_FILE_DIR_RELATIVE_PATH}" value="${props[PROP_FILE_DIR_RELATIVE_PATH]}" size="50" id="${PROP_FILE_DIR_RELATIVE_PATH}" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.uploaddirrelhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_BLACKLIST_TYPE %>"><f:message key="admin.caption.upload.blacklisttype" /></label></td>
	<td class="formelement">
		<c:set var="PROP_FILE_BLACKLIST_TYPE"><%= Environment.PROP_FILE_BLACKLIST_TYPE %></c:set>
		<select name="<%= Environment.PROP_FILE_BLACKLIST_TYPE %>" id="<%= Environment.PROP_FILE_BLACKLIST_TYPE %>" onchange="onUploadType()">
		<c:forEach items="${blacklistTypes}" var="blacklistType">
		<option value="<c:out value="${blacklistType.key}" />"<c:if test="${props[PROP_FILE_BLACKLIST_TYPE] == blacklistType.key}"> selected="selected"</c:if>><f:message key="${blacklistType.value}" /></option>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_BLACKLIST %>"><f:message key="admin.caption.upload.blacklist" /></label></td>
	<c:set var="PROP_FILE_BLACKLIST"><%= Environment.PROP_FILE_BLACKLIST %></c:set>
	<td class="formelement"><textarea cols="30" rows="3" name="<%= Environment.PROP_FILE_BLACKLIST %>" id="<%= Environment.PROP_FILE_BLACKLIST %>"><c:out value="${props[PROP_FILE_BLACKLIST]}" /></textarea></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.upload.blacklisthelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_WHITELIST %>"><f:message key="admin.caption.upload.whitelist" /></label></td>
	<c:set var="PROP_FILE_WHITELIST"><%= Environment.PROP_FILE_WHITELIST %></c:set>
	<td class="formelement"><textarea cols="30" rows="3" name="<%= Environment.PROP_FILE_WHITELIST %>" id="<%= Environment.PROP_FILE_WHITELIST %>"><c:out value="${props[PROP_FILE_WHITELIST]}" /></textarea></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.upload.whitelisthelp" /></td></tr>
</table>
</fieldset>
<!-- END FILE UPLOAD -->

<!-- BEGIN AUTHENTICATION -->
<a name="authentication"></a>
<fieldset>
<legend><f:message key="admin.header.ldap" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_USER_HANDLER %>"><f:message key="admin.caption.ldap" /></label></td>
	<td class="formelement">
		<select name="<%= Environment.PROP_BASE_USER_HANDLER %>" id="<%= Environment.PROP_BASE_USER_HANDLER %>" onchange="onLdap()">
		<c:set var="PROP_BASE_USER_HANDLER"><%= Environment.PROP_BASE_USER_HANDLER %></c:set>
		<c:forEach items="${userHandlers}" var="userHandler">
		<option value="<c:out value="${userHandler.clazz}" />"<c:if test="${props[PROP_BASE_USER_HANDLER] == userHandler.clazz}"> selected="selected"</c:if>><c:if test="${!empty userHandler.key}"><f:message key="${userHandler.key}" /></c:if><c:if test="${empty userHandler.key}"><c:out value="${userHandler.name}" /></c:if><c:if test="${userHandler.experimental}"> (<f:message key="common.caption.experimental" />)</c:if></option>
		</c:forEach>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_FACTORY_CLASS %>"><f:message key="admin.caption.ldap.factory" /></label></td>
	<c:set var="PROP_LDAP_FACTORY_CLASS"><%= Environment.PROP_LDAP_FACTORY_CLASS %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_FACTORY_CLASS}" id="${PROP_LDAP_FACTORY_CLASS}" value="${props[PROP_LDAP_FACTORY_CLASS]}" size="50" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_URL %>"><f:message key="admin.caption.ldap.url" /></label></td>
	<c:set var="PROP_LDAP_URL"><%= Environment.PROP_LDAP_URL %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_URL}" id="${PROP_LDAP_URL}" value="${props[PROP_LDAP_URL]}" size="50" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_CONTEXT %>"><f:message key="admin.caption.ldap.context" /></label></td>
	<c:set var="PROP_LDAP_CONTEXT"><%= Environment.PROP_LDAP_CONTEXT %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_CONTEXT}" id="${PROP_LDAP_CONTEXT}" value="${props[PROP_LDAP_CONTEXT]}" size="50" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.ldap.contexthelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_SECURITY_AUTHENTICATION %>"><f:message key="admin.caption.ldap.security" /></label></td>
	<c:set var="PROP_LDAP_SECURITY_AUTHENTICATION"><%= Environment.PROP_LDAP_SECURITY_AUTHENTICATION %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_SECURITY_AUTHENTICATION}" id="${PROP_LDAP_SECURITY_AUTHENTICATION}" value="${props[PROP_LDAP_SECURITY_AUTHENTICATION]}" size="20" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.ldap.securityhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_LOGIN %>"><f:message key="admin.caption.ldap.login" /></label></td>
	<c:set var="PROP_LDAP_LOGIN"><%= Environment.PROP_LDAP_LOGIN %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_LOGIN}" id="${PROP_LDAP_LOGIN}" value="${props[PROP_LDAP_LOGIN]}" size="30" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.ldap.loginhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_PASSWORD %>"><f:message key="admin.caption.ldap.password" /></label></td>
	<td class="formelement"><input type="password" name="<%= Environment.PROP_LDAP_PASSWORD %>" id="<%= Environment.PROP_LDAP_PASSWORD %>" value="<c:out value="${ldapPassword}" />" size="30" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.ldap.passwordhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_FIELD_USERID %>"><f:message key="admin.caption.ldap.field.userid" /></label></td>
	<c:set var="PROP_LDAP_FIELD_USERID"><%= Environment.PROP_LDAP_FIELD_USERID %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_FIELD_USERID}" id="${PROP_LDAP_FIELD_USERID}" value="${props[PROP_LDAP_FIELD_USERID]}" size="20" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_FIELD_FIRST_NAME %>"><f:message key="admin.caption.ldap.field.firstname" /></label></td>
	<c:set var="PROP_LDAP_FIELD_FIRST_NAME"><%= Environment.PROP_LDAP_FIELD_FIRST_NAME %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_FIELD_FIRST_NAME}" id="${PROP_LDAP_FIELD_FIRST_NAME}" value="${props[PROP_LDAP_FIELD_FIRST_NAME]}" size="20" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_FIELD_FIRST_NAME %>"><f:message key="admin.caption.ldap.field.lastname" /></label></td>
	<c:set var="PROP_LDAP_FIELD_LAST_NAME"><%= Environment.PROP_LDAP_FIELD_LAST_NAME %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_FIELD_LAST_NAME}" id="${PROP_LDAP_FIELD_LAST_NAME}" value="${props[PROP_LDAP_FIELD_LAST_NAME]}" size="20" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_LDAP_FIELD_EMAIL %>"><f:message key="admin.caption.ldap.field.email" /></label></td>
	<c:set var="PROP_LDAP_FIELD_EMAIL"><%= Environment.PROP_LDAP_FIELD_EMAIL %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_LDAP_FIELD_EMAIL}" id="${PROP_LDAP_FIELD_EMAIL}" value="${props[PROP_LDAP_FIELD_EMAIL]}" size="20" /></td>
</tr>
<script type="text/javascript">
onPersistenceType()
onUploadType()
onLdap()
</script>
</table>
</fieldset>
<!-- END AUTHENTICATION -->

<!-- BEGIN CACHE -->
<a name="cache"></a>
<fieldset>
<legend><f:message key="admin.header.cache" /></legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_CACHE_TOTAL_SIZE %>"><f:message key="admin.caption.cache.totalsize" /></label></td>
	<c:set var="PROP_CACHE_TOTAL_SIZE"><%= Environment.PROP_CACHE_TOTAL_SIZE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_CACHE_TOTAL_SIZE}" id="${PROP_CACHE_TOTAL_SIZE}" value="${props[PROP_CACHE_TOTAL_SIZE]}" size="10" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.cache.totalsizehelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_CACHE_INDIVIDUAL_SIZE %>"><f:message key="admin.caption.cache.individualsize" /></label></td>
	<c:set var="PROP_CACHE_INDIVIDUAL_SIZE"><%= Environment.PROP_CACHE_INDIVIDUAL_SIZE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_CACHE_INDIVIDUAL_SIZE}" id="${PROP_CACHE_INDIVIDUAL_SIZE}" value="${props[PROP_CACHE_INDIVIDUAL_SIZE]}" size="10" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.cache.individualsizehelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_CACHE_MAX_AGE %>"><f:message key="admin.caption.cache.maxage" /></label></td>
	<c:set var="PROP_CACHE_MAX_AGE"><%= Environment.PROP_CACHE_MAX_AGE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_CACHE_MAX_AGE}" id="${PROP_CACHE_MAX_AGE}" value="${props[PROP_CACHE_MAX_AGE]}" size="10" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_CACHE_MAX_IDLE_AGE %>"><f:message key="admin.caption.cache.idleage" /></label></td>
	<c:set var="PROP_CACHE_MAX_IDLE_AGE"><%= Environment.PROP_CACHE_MAX_IDLE_AGE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_CACHE_MAX_IDLE_AGE}" id="${PROP_CACHE_MAX_IDLE_AGE}" value="${props[PROP_CACHE_MAX_IDLE_AGE]}" size="10" /></td>
</tr>
</table>
</fieldset>
<!-- END CACHE -->

<!-- BEGIN RSS -->
<a name="rss"></a>
<fieldset>
<legend><f:message key="admin.header.rss" /> (<f:message key="common.caption.experimental" />)</legend>
<table border="0" class="contents">
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_RSS_ALLOWED %>"><f:message key="admin.caption.rss.allowed" /></label></td>
	<c:set var="PROP_RSS_ALLOWED"><%= Environment.PROP_RSS_ALLOWED %></c:set>
	<td class="formelement"><jamwiki:checkbox name="${PROP_RSS_ALLOWED}" value="true" checked="${props[PROP_RSS_ALLOWED]}" id="${PROP_RSS_ALLOWED}" onclick="onRSS()" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_RSS_TITLE %>"><f:message key="admin.caption.rss.title" /></label></td>
	<c:set var="PROP_RSS_TITLE"><%= Environment.PROP_RSS_TITLE %></c:set>
	<td class="formelement"><jamwiki:text name="${PROP_RSS_TITLE}" id="${PROP_RSS_TITLE}" value="${props[PROP_RSS_TITLE]}" size="50" /></td>
</tr>
</table>
</fieldset>
<!-- END RSS -->

<a name="save"></a>
<table border="0" class="contents" width="100%">
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td colspan="2" class="formelement" align="center"><input type="submit" name="Submit" value="<f:message key="admin.action.save" />" /></td></tr>
<tr><td colspan="2">&nbsp;</td></tr>
</table>

<input type="hidden" name="function" value="properties" />

<%--
  Include a hidden (display:none) password field to prevent Firefox from trying to change the
  admin password.  There is currently (version 1.5 and before) an issue with Firefox where
  anytime two or more password fields are in a form it assumes the password is being
  changed if the last password is different from the saved password.
--%>

<input type="password" name="fakePassword" value="" style="display:none" />
</form>
