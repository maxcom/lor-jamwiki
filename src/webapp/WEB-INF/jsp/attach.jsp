<form name="form1" method="post" action="../SaveAttachmentServlet" enctype="multipart/form-data">
<table border="0">
<tr>
	<td><f:message key="attach.info"/></td>
</tr>
<tr>
	<td><input type="file" name="file1" size="50"></td>
</tr>
<tr>
	<td><input type="file" name="file2" size="50"></td>
</tr>
<tr>
	<td><input type="file" name="file3" size="50"></td>
</tr>
<tr>
	<td>
		<input type="submit" name="save" value="<f:message key="attach.save"/>">
		<input type="submit" name="cancel" value="<f:message key="attach.cancel"/>">
	</td>
</tr>
</table>
<input type="hidden" name="topic" value='<c:out value="${topic}"/>'>
<input type="hidden" name="virtualwiki" value='<c:out value="${virtualWiki}"/>'>
<input type="hidden" name="user" value='<c:out value="${user}"/>'/>
</form>
