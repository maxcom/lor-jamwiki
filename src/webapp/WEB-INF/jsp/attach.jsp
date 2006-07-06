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
