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
<head></head>
<body>

<script type="text/javascript">
function onPersistenceType() {
	if (document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").options[document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>").selectedIndex].value == "<%= WikiBase.FILE %>") {
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

<form name="setup" method="post">
<table style="border:2px solid #333333;padding=1em;">
<c:if test="${!empty errors}">
<tr><td colspan="2" align="center"><div style="color:red;size=110%;"><c:out value="${errorMessage}" />
<c:forEach items="${errors}" var="message"><f:message key="${message.key}"><f:param value="${message.params[0]}" /></f:message><br /></c:forEach>
</div></td></tr>
</c:if>
<c:if test="${!empty message}"><tr><td colspan="2" align="center"><div style="color:green;size=110%;"><f:message key="${message.key}" /></div></td></tr></c:if>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td><f:message key="admin.caption.filedir" />:</td>
	<td><input type="text" name="<%= Environment.PROP_BASE_FILE_DIR %>" value="<%= Environment.getValue(Environment.PROP_BASE_FILE_DIR) %>" size="50" /></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td><f:message key="admin.caption.persistence" />:</td>
	<td>
		<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" onchange="onPersistenceType()">
		<option value="<%=WikiBase.FILE%>"<%= WikiBase.getPersistenceType() == WikiBase.FILE ? " selected" : "" %>><f:message key="admin.persistencetype.flatfile"/></option>
		<option value="<%=WikiBase.DATABASE%>"<%= WikiBase.getPersistenceType() == WikiBase.DATABASE ? " selected" : "" %>><f:message key="admin.persistencetype.database"/></option>
		</select>
	</td>
</tr>
<tr>
	<td><f:message key="admin.caption.databasedriver" />:</td>
	<td><input type="text" name="<%= Environment.PROP_DB_DRIVER %>" id="<%= Environment.PROP_DB_DRIVER %>" value="<%= Environment.getValue(Environment.PROP_DB_DRIVER) %>" size="50"></td>
</tr>
<tr>
	<td><f:message key="admin.caption.databasetype" />:</td>
	<td>
		<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>">
		<option value="mysql"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("mysql") ? " selected" : "" %>>mysql</option>
		<option value="ansi"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("ansi") ? " selected" : "" %>>ansi</option>
		<option value="oracle"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("oracle") ? " selected" : "" %>>oracle</option>
		<option value="postgres"<%= Environment.getValue(Environment.PROP_DB_TYPE).equals("postgres") ? " selected" : "" %>>postgres</option>
		</select>
	</td>
</tr>
<tr>
	<td><f:message key="admin.caption.databaseurl" />:</td>
	<td><input type="text" name="<%= Environment.PROP_DB_URL %>" id="<%= Environment.PROP_DB_URL %>" value="<%= Environment.getValue(Environment.PROP_DB_URL) %>" size="50"></td>
</tr>
<tr>
	<td><f:message key="admin.caption.databaseuser" />:</td>
	<td><input type="text" name="<%= Environment.PROP_DB_USERNAME %>" id="<%= Environment.PROP_DB_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_DB_USERNAME) %>" size="15"></td>
</tr>
<tr>
	<td><f:message key="admin.caption.databasepass" />:</td>
	<td><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="15"></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td><f:message key="admin.caption.uploaddir" />:</td>
	<td><input type="text" name="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH) %>" size="50" /></td>
</tr>
<tr>
	<td><f:message key="admin.caption.uploaddirrel" />:</td>
	<td><input type="text" name="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) %>" size="50" /></td>
</tr>
<tr><td colspan="2">&#160;</td></tr>
<tr>
	<td><f:message key="setup.caption.adminlogin"/>:</td>
	<td><input type="text" name="login" value="<c:out value="${login}" />" /></td>
</tr>
<tr>
	<td><f:message key="register.caption.newpassword"/>:</td>
	<td><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" /></td>
</tr>
<tr>
	<td><f:message key="register.caption.confirmpassword" />:</td>
	<td><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" /></td>
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