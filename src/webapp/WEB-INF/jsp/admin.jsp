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
        org.jamwiki.WikiBase,
        org.jamwiki.servlets.JAMWikiServlet,
        org.jamwiki.users.Usergroup,
        org.apache.commons.pool.impl.GenericObjectPool
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<%
int maximumFileSize = (int)((float)Environment.getIntValue(Environment.PROP_FILE_MAX_FILE_SIZE)/1000);
%>

<script type="text/javascript">
function onPersistenceType() {
	if (document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").options[document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").selectedIndex].value == "<%= WikiBase.FILE %>") {
		document.getElementById("<%= Environment.PROP_DB_DRIVER %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_TYPE %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_URL %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_USERNAME %>").disabled=true
		document.getElementById("<%= Environment.PROP_DB_PASSWORD %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_MAX_ACTIVE %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_MAX_IDLE %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_TEST_ON_BORROW %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_TEST_ON_RETURN %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_TEST_WHILE_IDLE %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_VALIDATION_QUERY %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_REMOVE_ABANDONED %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_LOG_ABANDONED %>").disabled=true
		document.getElementById("<%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %>").disabled=true
	} else {
		document.getElementById("<%= Environment.PROP_DB_DRIVER %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_TYPE %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_URL %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_USERNAME %>").disabled=false
		document.getElementById("<%= Environment.PROP_DB_PASSWORD %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_MAX_ACTIVE %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_MAX_IDLE %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_TEST_ON_BORROW %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_TEST_ON_RETURN %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_TEST_WHILE_IDLE %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_VALIDATION_QUERY %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_REMOVE_ABANDONED %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_LOG_ABANDONED %>").disabled=false
		document.getElementById("<%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %>").disabled=false
	}
}
<%--
FIXME - LDAP not supported at the moment, comment this out
function onUserGroupType() {
	if (document.getElementById("<%= Environment.PROP_USERGROUP_TYPE %>").options[document.getElementById("<%= Environment.PROP_USERGROUP_TYPE %>").selectedIndex].value == "0") {
		document.getElementById("<%= Environment.PROP_USERGROUP_FACTORY %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_URL %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_USERNAME %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_PASSWORD %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_BASIC_SEARCH %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_USERID_FIELD %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_FULLNAME_FIELD %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_MAIL_FIELD %>").disabled=true
		document.getElementById("<%= Environment.PROP_USERGROUP_DETAILVIEW %>").disabled=true
	} else {
		document.getElementById("<%= Environment.PROP_USERGROUP_FACTORY %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_URL %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_USERNAME %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_PASSWORD %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_BASIC_SEARCH %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_USERID_FIELD %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_FULLNAME_FIELD %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_MAIL_FIELD %>").disabled=false
		document.getElementById("<%= Environment.PROP_USERGROUP_DETAILVIEW %>").disabled=false
	}
}
--%>
</script>

<form name="form1" method="post" action="<jamwiki:link value="Special:Admin" />">
<c:if test="${!empty message}"><p class="red"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message></p></c:if>

<p class="subHeader"><f:message key="admin.title.settings" /></p>

<table border="0" class="contents">
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.generalsettingsheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_BASE_DEFAULT_TOPIC %>"><f:message key="admin.caption.defaulttopic" /></labe></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_BASE_DEFAULT_TOPIC %>" value="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" size="30" id="<%= Environment.PROP_BASE_DEFAULT_TOPIC %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_BASE_LOGO_IMAGE %>"><f:message key="admin.caption.logoimage" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_BASE_LOGO_IMAGE %>" value="<%= Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE) %>" size="30" id="<%= Environment.PROP_BASE_LOGO_IMAGE %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_TOPIC_VERSIONING_ON %>"><f:message key="admin.caption.useversioning" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_TOPIC_VERSIONING_ON %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON) ? " checked" : "" %> id="<%= Environment.PROP_TOPIC_VERSIONING_ON %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_TOPIC_FORCE_USERNAME %>"><f:message key="admin.caption.forceusername" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_TOPIC_FORCE_USERNAME %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_TOPIC_FORCE_USERNAME) ? " checked" : "" %> id="<%= Environment.PROP_TOPIC_FORCE_USERNAME %>" /></td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.recentchangesheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_RECENT_CHANGES_DAYS %>"><f:message key="admin.caption.recentchangesdefault" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_RECENT_CHANGES_DAYS %>" size="3" maxlength="3" value="<%= Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS) %>" id="<%= Environment.PROP_RECENT_CHANGES_DAYS %>" /></td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.editorheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_TOPIC_USE_PREVIEW %>"><f:message key="admin.caption.usepreview" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_TOPIC_USE_PREVIEW %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_TOPIC_USE_PREVIEW) ? " checked" : "" %> id="<%= Environment.PROP_TOPIC_USE_PREVIEW %>" /></td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.lexerheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_PARSER_CLASS %>"><f:message key="admin.caption.parser" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_PARSER_CLASS %>" value="<%= Environment.getValue(Environment.PROP_PARSER_CLASS) %>" size="50" id="<%= Environment.PROP_PARSER_CLASS %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_PARSER_TOC %>"><f:message key="admin.caption.tableofcontents" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_PARSER_TOC %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_PARSER_TOC) ? " checked" : "" %> id="<%= Environment.PROP_PARSER_TOC %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_PARSER_ALLOW_HTML %>"><f:message key="admin.caption.allowhtml" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_PARSER_ALLOW_HTML %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML) ? " checked" : "" %> id="<%= Environment.PROP_PARSER_ALLOW_HTML %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_PARSER_ALLOW_JAVASCRIPT %>"><f:message key="admin.caption.allowjavascript" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_PARSER_ALLOW_JAVASCRIPT %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT) ? " checked" : "" %> id="<%= Environment.PROP_PARSER_ALLOW_JAVASCRIPT %>" /></td>
</tr>

<%--
FIXME - Email not supported right now, comment this out

<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.smtp" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_EMAIL_SMTP_HOST %>"><f:message key="admin.caption.smtp.host" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_EMAIL_SMTP_HOST %>" value="<%= Environment.getValue(Environment.PROP_EMAIL_SMTP_HOST) %>" size="30" id="<%= Environment.PROP_EMAIL_SMTP_HOST %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_EMAIL_SMTP_USERNAME %>"><f:message key="admin.caption.smtp.user" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_EMAIL_SMTP_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_EMAIL_SMTP_USERNAME) %>" size="30" id="<%= Environment.PROP_EMAIL_SMTP_USERNAME %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_EMAIL_SMTP_PASSWORD %>"><f:message key="admin.caption.smtp.pass" /></label></td>
	<td class="normal"><input type="password" name="<%= Environment.PROP_EMAIL_SMTP_PASSWORD %>" value="<c:out value="${smtpPassword}" />" size="30" id="<%= Environment.PROP_EMAIL_SMTP_PASSWORD %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_EMAIL_REPLY_ADDRESS %>"><f:message key="admin.caption.reply" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_EMAIL_REPLY_ADDRESS %>" value="<%= Environment.getValue(Environment.PROP_EMAIL_REPLY_ADDRESS) %>" size="50" id="<%= Environment.PROP_EMAIL_REPLY_ADDRESS %>" /></td>
</tr>

--%>

<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.persistenceheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_BASE_FILE_DIR %>"><f:message key="admin.caption.filedir" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_BASE_FILE_DIR %>" value="<%= Environment.getValue(Environment.PROP_BASE_FILE_DIR) %>" size="50" id="<%= Environment.PROP_BASE_FILE_DIR %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"><f:message key="admin.caption.persistence" /></label></td>
	<td class="normal">
		<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" onchange="onPersistenceType()">
		<option value="<%=WikiBase.FILE%>"<%= WikiBase.getPersistenceType() == WikiBase.FILE ? " selected" : "" %>><f:message key="admin.persistencetype.flatfile" /></option>
		<option value="<%=WikiBase.DATABASE%>"<%= WikiBase.getPersistenceType() == WikiBase.DATABASE ? " selected" : "" %>><f:message key="admin.persistencetype.database" /></option>
		</select>
	</td>
</tr>
<!-- BEGIN DATABASE-PERSISTENCE -->
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DB_DRIVER %>"><f:message key="admin.caption.databasedriver" /></labe></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DB_DRIVER %>" id="<%= Environment.PROP_DB_DRIVER %>" value="<%= Environment.getValue(Environment.PROP_DB_DRIVER) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DB_TYPE %>"><f:message key="admin.caption.databasetype" /></label></td>
	<td class="normal">
		<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>">
		<option value="mysql"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("mysql") ? " selected" : "" %>>mysql</option>
		<option value="ansi"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("ansi") ? " selected" : "" %>>ansi</option>
		<option value="oracle"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("oracle") ? " selected" : "" %>>oracle</option>
		<option value="postgres"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("postgres") ? " selected" : "" %>>postgres</option>
		</select>
	</td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DB_URL %>"><f:message key="admin.caption.databaseurl" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DB_URL %>" id="<%= Environment.PROP_DB_URL %>" value="<%= Environment.getValue(Environment.PROP_DB_URL) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DB_USERNAME %>"><f:message key="admin.caption.databaseuser" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DB_USERNAME %>" id="<%= Environment.PROP_DB_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_DB_USERNAME) %>" size="30" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DB_PASSWORD %>"><f:message key="admin.caption.databasepass" /></label></td>
	<td class="normal"><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="30" /></td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="bold" colspan="2" align="left"><f:message key="admin.caption.dbcp.header" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_MAX_ACTIVE %>"><f:message key="admin.caption.dbcp.maxactive" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_MAX_ACTIVE %>" id="<%= Environment.PROP_DBCP_MAX_ACTIVE %>" value="<%= Environment.getIntValue(Environment.PROP_DBCP_MAX_ACTIVE) %>" size="5" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_MAX_IDLE %>"><f:message key="admin.caption.dbcp.maxidle" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_MAX_IDLE %>" id="<%= Environment.PROP_DBCP_MAX_IDLE %>" value="<%= Environment.getIntValue(Environment.PROP_DBCP_MAX_IDLE) %>" size="5" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_TEST_ON_BORROW %>"><f:message key="admin.caption.dbcp.testonborrow" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_DBCP_TEST_ON_BORROW %>" id="<%= Environment.PROP_DBCP_TEST_ON_BORROW %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_BORROW) ? " checked" : "" %> /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_TEST_ON_RETURN %>"><f:message key="admin.caption.dbcp.testonreturn" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_DBCP_TEST_ON_RETURN %>" id="<%= Environment.PROP_DBCP_TEST_ON_RETURN %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_RETURN) ? " checked" : "" %> /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_TEST_WHILE_IDLE %>"><f:message key="admin.caption.dbcp.testwhileidle" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_DBCP_TEST_WHILE_IDLE %>" id="<%= Environment.PROP_DBCP_TEST_WHILE_IDLE %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_DBCP_TEST_WHILE_IDLE) ? " checked" : "" %> /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %>"><f:message key="admin.caption.dbcp.minevictableidletime" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %>" id="<%= Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME %>" value="<%= Environment.getIntValue(Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME) %>" size="5" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %>"><f:message key="admin.caption.dbcp.timebetweenevictionruns" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %>" id="<%= Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS %>" value="<%= Environment.getIntValue(Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS) %>" size="5" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %>"><f:message key="admin.caption.dbcp.numtestsperevictionrun" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %>" id="<%= Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN %>" value="<%= Environment.getIntValue(Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN) %>" size="5" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>"><f:message key="admin.caption.dbcp.whenexhaustedaction" /></label></td>
	<td class="normal">
		<select name="<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>" id="<%= Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION %>">
		<option value="<%=GenericObjectPool.WHEN_EXHAUSTED_FAIL%>"<%= Environment.getIntValue(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION) == GenericObjectPool.WHEN_EXHAUSTED_FAIL ? " selected" : "" %>><f:message key="admin.caption.dbcp.whenexhaustedaction.fail" /></option>
		<option value="<%=GenericObjectPool.WHEN_EXHAUSTED_BLOCK%>"<%= Environment.getIntValue(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION) == GenericObjectPool.WHEN_EXHAUSTED_BLOCK ? " selected" : "" %>><f:message key="admin.caption.dbcp.whenexhaustedaction.block" /></option>
		<option value="<%=GenericObjectPool.WHEN_EXHAUSTED_GROW%>"<%=Environment.getIntValue(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION) == GenericObjectPool.WHEN_EXHAUSTED_GROW ? " selected" : "" %>><f:message key="admin.caption.dbcp.whenexhaustedaction.grow" /></option>
		</select>
	</td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_VALIDATION_QUERY %>"><f:message key="admin.caption.dbcp.validationquery" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_VALIDATION_QUERY %>" id="<%= Environment.PROP_DBCP_VALIDATION_QUERY %>" value="<%= Environment.getValue(Environment.PROP_DBCP_VALIDATION_QUERY) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_REMOVE_ABANDONED %>"><f:message key="admin.caption.dbcp.removeabandoned" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_DBCP_REMOVE_ABANDONED %>" id="<%= Environment.PROP_DBCP_REMOVE_ABANDONED %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_DBCP_REMOVE_ABANDONED) ? " checked" : "" %> /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_LOG_ABANDONED %>"><f:message key="admin.caption.dbcp.logabandoned" /></label></td>
	<td class="normal"><input type="checkbox" name="<%= Environment.PROP_DBCP_LOG_ABANDONED %>" id="<%= Environment.PROP_DBCP_LOG_ABANDONED %>" value="true"<%= Environment.getBooleanValue(Environment.PROP_DBCP_LOG_ABANDONED) ? " checked" : "" %> /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %>"><f:message key="admin.caption.dbcp.removeabandonedtimeout" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %>" id="<%= Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT %>" value="<%= Environment.getIntValue(Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT) %>" size="5" /></td>
</tr>
<!-- END DATABASE-PERSISTENCE -->

<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.uploadheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_FILE_MAX_FILE_SIZE %>"><f:message key="admin.caption.maxfilesize" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_FILE_MAX_FILE_SIZE %>" value="<%= maximumFileSize %>" size="10" id="<%= Environment.PROP_FILE_MAX_FILE_SIZE %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_FILE_DIR_FULL_PATH %>"><f:message key="admin.caption.uploaddir" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>"><f:message key="admin.caption.uploaddirrel" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" /></td>
</tr>

<%--
FIXME - LDAP not supported at the moment, comment this out

<!-- BEGIN USERGROUP-TYPE -->
<tr><td colspan="2">&nbsp;</td></tr>
<tr><td class="subHeader" colspan="2" align="left"><f:message key="admin.caption.dbcp.usergroupheader" /></td></tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_TYPE %>"><f:message key="admin.caption.usergroup" /></label></td>
	<td class="normal">
		<select name="<%= Environment.PROP_USERGROUP_TYPE %>" id="<%= Environment.PROP_USERGROUP_TYPE %>" onchange="onUserGroupType()">
		<option value="0"<%= Usergroup.getUsergroupType() == 0 ? " selected" : "" %>><f:message key="admin.usergrouptype.none" /></option>
		<option value="<%=WikiBase.LDAP%>"<%= Usergroup.getUsergroupType() == WikiBase.LDAP ? " selected" : "" %>><f:message key="admin.usergrouptype.ldap" /></option>
		</select>
	</td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_FACTORY %>"><f:message key="admin.caption.usergroup.factory" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_FACTORY %>" id="<%= Environment.PROP_USERGROUP_FACTORY %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_FACTORY) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_URL %>"><f:message key="admin.caption.usergroup.url" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_URL %>" id="<%= Environment.PROP_USERGROUP_URL %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_URL) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_USERNAME %>"><f:message key="admin.caption.usergroup.username" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_USERNAME %>" id="<%= Environment.PROP_USERGROUP_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_USERNAME) %>" size="20" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_PASSWORD %>"><f:message key="admin.caption.usergroup.password" /></label></td>
	<td class="normal"><input type="password" name="<%= Environment.PROP_USERGROUP_PASSWORD %>" id="<%= Environment.PROP_USERGROUP_PASSWORD %>" value="<c:out value="${userGroupPassword}" />" size="10" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_BASIC_SEARCH %>"><f:message key="admin.caption.usergroup.basicSearch" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_BASIC_SEARCH %>" id="<%= Environment.PROP_USERGROUP_BASIC_SEARCH %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_BASIC_SEARCH) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS %>"><f:message key="admin.caption.usergroup.searchRestrictions" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS %>" id="<%= Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS) %>" size="50" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_USERID_FIELD %>"><f:message key="admin.caption.usergroup.userfidield" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_USERID_FIELD %>" id="<%= Environment.PROP_USERGROUP_USERID_FIELD %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_USERID_FIELD) %>" size="20" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_FULLNAME_FIELD %>"><f:message key="admin.caption.usergroup.fullnamefield" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_FULLNAME_FIELD %>" id="<%= Environment.PROP_USERGROUP_FULLNAME_FIELD %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_FULLNAME_FIELD) %>" size="20" /></td>
</tr>
<tr>
	<td class="normal"><label for="<%= Environment.PROP_USERGROUP_MAIL_FIELD %>"><f:message key="admin.caption.usergroup.mailfield" /></label></td>
	<td class="normal"><input type="text" name="<%= Environment.PROP_USERGROUP_MAIL_FIELD %>" id="<%= Environment.PROP_USERGROUP_MAIL_FIELD %>" value="<%= Environment.getValue(Environment.PROP_USERGROUP_MAIL_FIELD) %>" size="20" /></td>
</tr>
<tr>
	<td class="normal" valign="top"><label for="<%= Environment.PROP_USERGROUP_DETAILVIEW %>"><f:message key="admin.caption.usergroup.detailview" /></label></td>
	<td class="normal"><textarea cols="40" rows="5" name="<%= Environment.PROP_USERGROUP_DETAILVIEW %>" id="<%= Environment.PROP_USERGROUP_DETAILVIEW %>"><%= Environment.getValue(Environment.PROP_USERGROUP_DETAILVIEW) %></textarea></td>
</tr>
<!-- END USERGROUP-TYPE -->

--%>

<script>
onPersistenceType()

<%--
FIXME - LDAP not supported at the moment, comment this out

onUserGroupType()

--%>

</script>
<tr>
	<td class="normal"><input type="submit" name="Submit" value="<f:message key="admin.action.save" />"></td>
	<td class="normal">&nbsp;</td>
</tr>
</table>

<input type="hidden" name="function" value="properties">
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_ADMIN %>" />

<%--
  Include a hidden (display:none) password field to prevent Firefox from trying to change the
  admin password.  There is currently (version 1.5 and before) an issue with Firefox where
  anytime two or more password fields are in a form it assumes the password is being
  changed if the last password is different from the saved password.
--%>

<input type="password" name="fakePassword" value="" style="display:none" />
</form>

<hr />

<!-- Refresh Seach Index -->
<p class="subHeader"><f:message key="admin.title.refresh" /></p>
<form name="refreshform" method="post" action="<jamwiki:link value="Special:Admin" />">
<table border="0" class="contents">
<tr>
	<td><f:message key="admin.title.refresh" /></td>
	<td><input type="submit" name="submit" value="<f:message key="admin.action.refresh" />" /></td>
</tr>
</table>
<input type="hidden" name="function" value="refreshIndex" />
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_ADMIN %>" />
</form>

<hr />

<!-- Read-only Topics -->
<p class="subHeader"><f:message key="admin.title.readonly" /></p>
<form name="readOnlyTopics" method="post" action="<jamwiki:link value="Special:Admin" />">
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_ADMIN %>" />
<input type="hidden" name="function" value="readOnly">
<table border="0" class="contents">
<tr>
	<td><input type="text" name="readOnlyTopic"></td>
	<td><input type="submit" name="addReadOnly" value="<f:message key="admin.action.add" />" /></td>
</tr>
<c:if test="${not empty readOnlyTopics}">
<tr>
	<td>
		<table border="0">
		<tr>
			<td><f:message key="common.topic" /></td>
			<td><f:message key="admin.caption.mark" /></td>
		</tr>
		<c:forEach items="${readOnlyTopics}" var="topic">
		<tr>
			<%-- FIXME: need label element --%>
			<td><c:out value="${topic}" /></td>
			<td><input type="checkbox" name="markRemove" value="<c:out value="${topic}" />" /></td>
		</tr>
		</c:forEach>
		</table>
	</td>
	<td valign="middle"><input type="submit" name="removeReadOnly" value="<f:message key="admin.action.remove" />"></td>
</tr>
</c:if>
</table>
</form>

<hr />

<!-- Virtual Wikis -->
<p class="subHeader"><f:message key="admin.title.virtualwiki" /></p>
<table border="0" class="contents">
<tr>
	<th><f:message key="common.name" /></th>
	<th><f:message key="admin.caption.defaulttopic" /></th>
</tr>
<c:forEach items="${wikis}" var="wiki">
<form action="<jamwiki:link value="Special:Admin" />" method="post">
<input type="hidden" name="function" value="addVirtualWiki">
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_ADMIN %>" />
<input type="hidden" name="virtualWikiId" value="<c:out value="${wiki.virtualWikiId}" />" />
<tr>
	<%-- FIXME: need label element --%>
	<td class="normal"><c:out value="${wiki.name}" /></td>
	<td class="normal"><input type="text" name="defaultTopicName" value="<c:out value="${wiki.defaultTopicName}" />" size="30" /></td>
	<td><input type="submit" value="<f:message key="common.update" />" /></td>
</tr>
</form>
</c:forEach>
<form action="<jamwiki:link value="Special:Admin" />" method="post">
<input type="hidden" name="function" value="addVirtualWiki">
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_ADMIN %>" />
<tr>
	<td class="normal"><input type="text" name="name" /></td>
	<td class="normal"><input type="text" name="defaultTopicName" value="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" size="30" /></td>
	<td><input type="submit" value="<f:message key="common.add" />" /></td>
</tr>
</form>
</table>
</form>

<hr />

<!-- Recent Changes -->
<p class="subHeader"><f:message key="admin.title.recentchanges" /></p>
<form action="<jamwiki:link value="Special:Admin" />" method="post">
<table border="0" class="contents">
<tr>
	<td><f:message key="admin.caption.recentchanges" /></td>
	<td><input type="submit" value="<f:message key="admin.caption.reset" />" /></td>
</tr>
</table>
<input type="hidden" name="function" value="recentChanges" />
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_ADMIN %>" />
</form>
