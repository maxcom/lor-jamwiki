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
        org.jamwiki.persistency.db.DatabaseHandler
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<html>
<head>
<style>
body {
	background: #f9f9f9;
	color: black;
	margin: 0;
	padding: 5px;
}

body, input, select {
	font: 95% sans-serif, tahoma;
}
td.formcaption {
}
td.formelement {
}
td.formhelp {
	font-size: 85%;
	color: #5f5f5f;
}
.red {
	font: verdana, helvetica, sans-serif;
	font-size: 110%;
	color: #ff0000;
	text-align: center;
}
</style>
</head>
<body>

<script type="text/javascript">
function onPersistenceType() {
	if (document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").options[document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").selectedIndex].value == "<%= WikiBase.PERSISTENCE_INTERNAL_DB %>") {
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

<c:if test="${!empty errorMessage}"><p class="color"><f:message key="${errorMessage.key}"><f:param value="${errorMessage.params[0]}" /></f:message></p></c:if>

<form name="setup" method="post">
<table style="border:2px solid #333333;padding=1em;">
<c:if test="${!empty errors}">
<tr><td class="red" colspan="2"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></td></tr>
</c:if>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_FILE_DIR %>"><f:message key="admin.caption.filedir" /></label>:</td>
	<td><input type="text" name="<%= Environment.PROP_BASE_FILE_DIR %>" value="<%= Environment.getValue(Environment.PROP_BASE_FILE_DIR) %>" size="50" id="<%= Environment.PROP_BASE_FILE_DIR %>" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.filedirhelp" /></td></tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"><f:message key="admin.caption.persistence" /></label>:</td>
	<td class="formelement">
		<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" onchange="onPersistenceType()">
		<option value="<%=WikiBase.PERSISTENCE_INTERNAL_DB%>"<%= WikiBase.getPersistenceType() == WikiBase.PERSISTENCE_INTERNAL_DB ? " selected" : "" %>><f:message key="admin.persistencetype.internal"/></option>
		<option value="<%=WikiBase.PERSISTENCE_EXTERNAL_DB%>"<%= WikiBase.getPersistenceType() == WikiBase.PERSISTENCE_EXTERNAL_DB ? " selected" : "" %>><f:message key="admin.persistencetype.database"/></option>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_DRIVER %>"><f:message key="admin.caption.databasedriver" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_DB_DRIVER %>" id="<%= Environment.PROP_DB_DRIVER %>" value="<%= Environment.getValue(Environment.PROP_DB_DRIVER) %>" size="50"></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_TYPE %>"><f:message key="admin.caption.databasetype" /></label>:</td>
	<td class="formelement">
		<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>">
		<option value="<%= DatabaseHandler.DB_TYPE_ANSI %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ANSI) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_ANSI %></option>
		<option value="<%= DatabaseHandler.DB_TYPE_HSQL %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_HSQL) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_HSQL %></option>
		<option value="<%= DatabaseHandler.DB_TYPE_MYSQL %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_MYSQL) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_MYSQL %></option>
		<option value="<%= DatabaseHandler.DB_TYPE_ORACLE %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_ORACLE %></option>
		<option value="<%= DatabaseHandler.DB_TYPE_POSTGRES %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_POSTGRES) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_POSTGRES %></option>
		<option value="<%= DatabaseHandler.DB_TYPE_DB2 %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_DB2) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_DB2 %> (<f:message key="common.caption.experimental" />)</option>
		<option value="<%= DatabaseHandler.DB_TYPE_DB2_400 %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_DB2_400) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_DB2_400 %> (<f:message key="common.caption.experimental" />)</option>
		<option value="<%= DatabaseHandler.DB_TYPE_MSSQL %>"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_MSSQL) ? " selected" : "" %>><%= DatabaseHandler.DB_TYPE_MSSQL %> (<f:message key="common.caption.experimental" />)</option>
		</select>
	</td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_URL %>"><f:message key="admin.caption.databaseurl" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_DB_URL %>" id="<%= Environment.PROP_DB_URL %>" value="<%= Environment.getValue(Environment.PROP_DB_URL) %>" size="50"></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_USERNAME %>"><f:message key="admin.caption.databaseuser" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_DB_USERNAME %>" id="<%= Environment.PROP_DB_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_DB_USERNAME) %>" size="15"></td>
</tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_DB_PASSWORD %>"><f:message key="admin.caption.databasepass" /></label>:</td>
	<td class="formelement"><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="15"></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_FULL_PATH %>"><f:message key="admin.caption.uploaddir" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.uploaddirhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>"><f:message key="admin.caption.uploaddirrel" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.uploaddirrelhelp" /></td></tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="setupLogin"><f:message key="setup.caption.adminlogin"/></label>:</td>
	<td class="formelement"><input type="text" name="login" value="<c:out value="${login}" />" id="setupLogin" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="setupNewPassword"><f:message key="register.caption.newpassword" /></label>:</td>
	<td class="formelement"><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="setupNewPassword" /></td>
</tr>
<tr>
	<td class="formcaption"><label for="setupConfirmPassword"><f:message key="register.caption.confirmpassword" /></label>:</td>
	<td class="formelement"><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="setupConfirmPassword" /></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr><td colspan="2" align="center"><input type="submit" name="function" value="<f:message key="admin.action.save" />" /></td></tr>
<tr><td colspan="2">&#160;</td></tr>
</table>
</form>

<c:if test="${!empty messages}">
<br />
<table>
<c:forEach items="${messages}" var="message">
<tr><td><c:out value="${message}" /></td></tr>
</c:forEach>
</table>
</c:if>

<script>
onPersistenceType();
</script>

</body>
</html>