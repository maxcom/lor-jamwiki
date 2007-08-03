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

<c:if test="${!empty messageObject}"><p class="red"><f:message key="${messageObject.key}"><f:param value="${messageObject.params[0]}" /><f:param value="${messageObject.params[1]}" /></f:message></p></c:if>

<form name="setup" method="post">
<input type="hidden" value="<c:out value="${upgrade}" />" />
<table style="border:2px solid #333333;padding=1em;">
<c:if test="${!empty errors}">
<tr><td class="red" colspan="2"><c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach></td></tr>
</c:if>
<c:if test="${!empty upgrade}">
<tr><td colspan="2">&#160;</td></tr>
<tr><td colspan="2"><font color="red"><f:message key="setup.error.upgrade" /></font></td></tr>
<tr><td colspan="2" align="center"><input type="submit" name="override" value="<f:message key="common.continue" />" /></td></tr>
</c:if>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_BASE_FILE_DIR %>"><f:message key="admin.caption.filedir" /></label>:</td>
	<td><input type="text" name="<%= Environment.PROP_BASE_FILE_DIR %>" value="<%= Environment.getValue(Environment.PROP_BASE_FILE_DIR) %>" size="50" id="<%= Environment.PROP_BASE_FILE_DIR %>" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.caption.filedirhelp" /></td></tr>
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
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_FULL_PATH %>"><f:message key="admin.upload.caption.uploaddir" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.upload.caption.uploaddirhelp" /></td></tr>
<tr>
	<td class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>"><f:message key="admin.upload.caption.uploaddirrel" /></label>:</td>
	<td class="formelement"><input type="text" name="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" /></td>
</tr>
<tr><td colspan="2" class="formhelp"><f:message key="admin.upload.help.uploaddirrel" /></td></tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td class="formcaption"><label for="setupLogin"><f:message key="setup.caption.adminlogin"/></label>:</td>
	<td class="formelement"><input type="text" name="username" value="<c:out value="${username}" />" id="setupLogin" /></td>
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