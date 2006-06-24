
<script type="text/javascript">
// enable/disable checkboxes before or after the current element
<%-- FIXME: might be better moved to a jmwiki.js file --%>
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

<c:choose>
	<c:when test="${param.type=='all'}">
<form action="<jmwiki:link value="Special:Diff" />" method="get" name="historyForm">
<input type="hidden" name="action" value="<%= WikiServlet.ACTION_DIFF %>"/>
<input type="hidden" name="type" value="arbitrary"/>
<input type="hidden" name="topic" value='<c:out value="${topic}"/>'/>
<table>
		<c:forEach items="${versions}" var="version">
		<f:formatDate value="${version.editDate}" type="both" dateStyle="MEDIUM" timeStyle="MEDIUM" var="editDate" />
<tr>
	<td><a href="<jmwiki:link value="Special:History" />?type=version&topicVersionId=<c:out value="${version.topicVersionId}" />&topic=<jmwiki:encode value="${topic}" />"><c:out value="${editDate}"/></a></td>
	<td><input type="checkbox" name='<c:out value="diff:${version.topicVersionId}"/>' onclick="inactive(this)" /></td>
</tr>
		</c:forEach>
</table>
<input type="submit" value='<f:message key="history.diff"/>'/>
</form>
	</c:when>
	<c:when test="${param.type=='version'}">
<c:out value="${topicVersion.cookedContents}" escapeXml="${false}"/>
<hr/>
<form>
<textarea readonly="true" cols="80" rows="26"><c:out value="${topicVersion.versionContent}" escapeXml="${false}"/></textarea>
</form>
	</c:when>
</c:choose>
