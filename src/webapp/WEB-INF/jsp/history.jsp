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
<%@ page errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>

<%@ include file="page-init.jsp" %>

<script type="text/javascript">
// enable/disable checkboxes before or after the current element
<%-- FIXME: might be better moved to a jamwiki.js file --%>
function inactive(element) {
  var found = 0;
  var totalChecked = 0;
  for (i=0; i < document.historyForm.length; i++) {
    if (element.type != document.historyForm.elements[i].type) continue;
    if (document.historyForm.elements[i].checked) totalChecked++;
  }
  for (i=0; i < document.historyForm.length; i++) {
    if (element.type != document.historyForm.elements[i].type) continue;
    if (document.historyForm.elements[i].checked && found < 2) {
      found++;
      continue;
    }
    if (totalChecked == 0) {
      // enable everything
      document.historyForm.elements[i].checked = false;
      document.historyForm.elements[i].disabled = false;
      continue;
    }
    if (found == 0 && totalChecked == 1) {
      // disable everything up to the first one
      document.historyForm.elements[i].checked = false;
      document.historyForm.elements[i].disabled = true;
      continue;
    }
    if (found == 1 && totalChecked >= 1) {
      // un-select everything after the first one
      document.historyForm.elements[i].checked = false;
      document.historyForm.elements[i].disabled = false;
      continue;
    }
    if (found == 2 && totalChecked >= 2) {
      // disable elements after the second one
      document.historyForm.elements[i].checked = false;
      document.historyForm.elements[i].disabled = true;
      continue;
    }
  }
}
</script>

<br />

<div id="change">

<form action="<jamwiki:link value="Special:Diff" />" method="get" name="historyForm">
<input type="hidden" name="action" value="<%= JAMWikiServlet.ACTION_DIFF %>"/>
<input type="hidden" name="type" value="arbitrary"/>
<input type="hidden" name="topic" value='<c:out value="${topic}"/>'/>
<input type="submit" value='<f:message key="history.diff"/>'/>

<br /><br />

<ul>
<c:forEach items="${changes}" var="change">
<li>
	&#160;
	<input type="checkbox" name='<c:out value="diff:${change.topicVersionId}"/>' onclick="inactive(this)" />
	&#160;
	<%-- FIXME: do not hardcode date pattern --%>
	<a href="<jamwiki:link value="Special:History" />?topicVersionId=<c:out value="${change.topicVersionId}" />&topic=<jamwiki:encode value="${topic}" />"><f:formatDate value="${change.editDate}" type="both" pattern="dd-MMM-yyyy HH:mm" /></a>
	&#160;.&#160;.&#160;
	<%-- FIXME: ugly --%>
	<jamwiki:link value="User:${change.authorName}"><c:out value="${change.authorName}" /></jamwiki:link>
	<%-- FIXME: need a better way to denote minor edits --%>
	<c:if test="${change.minor}">&#160;<b>m</b></c:if>
	<c:if test="${!empty change.editComment}">&#160;(<i><c:out value="${change.editComment}" /></i>)</c:if>
</c:forEach>
</ul>

<br />

<input type="submit" value='<f:message key="history.diff"/>'/>
</form>

</div>
