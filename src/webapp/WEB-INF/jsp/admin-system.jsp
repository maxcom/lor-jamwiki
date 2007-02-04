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
        java.util.Properties,
        org.jamwiki.Environment,
        org.jamwiki.WikiBase,
        org.jamwiki.db.WikiDatabase,
        org.apache.commons.pool.impl.GenericObjectPool
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<!-- Virtual Wikis -->
<fieldset>
<legend><f:message key="admin.title.virtualwiki" /></legend>
<table border="0" class="contents">
<tr>
	<th><f:message key="common.name" /></th>
	<th><f:message key="admin.caption.defaulttopic" /></th>
	<th>&#160;</th>
</tr>
<c:forEach items="${wikis}" var="wiki">
<form action="<jamwiki:link value="Special:System" />" method="post">
<input type="hidden" name="function" value="addVirtualWiki" />
<input type="hidden" name="virtualWikiId" value="<c:out value="${wiki.virtualWikiId}" />" />
<input type="hidden" name="name" value="<c:out value="${wiki.name}" />" />
<tr>
	<%-- FIXME: need label element --%>
	<td class="formcaption"><c:out value="${wiki.name}" /></td>
	<td class="formelement"><input type="text" name="defaultTopicName" value="<c:out value="${wiki.defaultTopicName}" />" size="30" /></td>
	<td class="formelement"><input type="submit" value="<f:message key="common.update" />" /></td>
</tr>
</form>
</c:forEach>
<form action="<jamwiki:link value="Special:System" />" method="post">
<input type="hidden" name="function" value="addVirtualWiki" />
<tr>
	<td class="formelement"><input type="text" name="name" /></td>
	<td class="formelement"><input type="text" name="defaultTopicName" value="<%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %>" size="30" /></td>
	<td class="formelement"><input type="submit" value="<f:message key="common.add" />" /></td>
</tr>
</form>
<tr><td colspan="3">&nbsp;</td></tr>
</table>
</fieldset>

<!-- Refresh Search Index -->
<fieldset>
<legend><f:message key="admin.title.refresh" /></legend>
<form name="refreshform" method="post" action="<jamwiki:link value="Special:System" />">
<table border="0" class="contents">
<tr>
	<td class="formcaption"><f:message key="admin.title.refresh" /></td>
	<td class="formelement"><input type="submit" name="submit" value="<f:message key="admin.action.refresh" />" /></td>
</tr>
</table>
<input type="hidden" name="function" value="refreshIndex" />
</form>
</fieldset>

<!-- Recent Changes -->
<fieldset>
<legend><f:message key="admin.title.recentchanges" /></legend>
<form action="<jamwiki:link value="Special:System" />" method="post">
<table border="0" class="contents">
<tr>
	<td class="formcaption"><f:message key="admin.caption.recentchanges" /></td>
	<td class="formelement"><input type="submit" value="<f:message key="admin.caption.reset" />" /></td>
</tr>
</table>
<input type="hidden" name="function" value="recentChanges" />
</form>
</fieldset>

<jamwiki:enabled property="PROP_TOPIC_SPAM_FILTER">

<!-- Spam Filter -->
<fieldset>
<legend><f:message key="admin.title.spamfilter" /></legend>
<form action="<jamwiki:link value="Special:System" />" method="post">
<table border="0" class="contents">
<tr>
	<td class="formcaption"><f:message key="admin.caption.spamfilter" /></td>
	<td class="formelement"><input type="submit" value="<f:message key="admin.caption.reset" />" /></td>
</tr>
</table>
<input type="hidden" name="function" value="spamFilter" />
</form>
</fieldset>

</jamwiki:enabled>
