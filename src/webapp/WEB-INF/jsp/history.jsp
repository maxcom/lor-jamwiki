<%@include file="top.jsp"%>
<c:out value="${topArea}" escapeXml="false"/>

<script type="text/javascript">
// enable/disable checkboxes before or after the current element
<%-- FIXME: might be better moved to a vqwiki.js file --%>
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

<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <td>
      <div class="navbar">
        <%@ include file="navbar-virtual-wiki.jsp"%>
        <%@ include file="navbar-history-list.jsp"%>
        &nbsp; <!-- to render the bar even when empty -->
      </div>
    </td>
  </tr>
</table>
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
      <td nowrap class="leftMenu" valign="top" width="10%">
        <c:out value="${leftMenu}" escapeXml="false"/>
      </td>
    </c:if>
    <td valign="top">
      <div class="menu">
		<table style="width: 100%; border: 0px solid;">
			<tr>
				<td class="menu" align=left>
			      | <span class="menuinactive"><f:message key="menu.editpage"/></span>
			      | <span class="menuinactive"><f:message key="menu.attach"/></span>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?RecentChanges'><f:message key="generalmenu.recentchanges"/></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?<%= JSPUtils.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>'><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?WikiSearch'><f:message key="generalmenu.search"/></a>
			      | <span class="menuinactive"><f:message key="menu.printablepage"/></span>
			      | <a href='Wiki?<c:out value="${param.topic}"/>'><f:message key="history.current"/></a>
			      <c:if test="${param.type=='version'}">
			        <c:if test="${param.versionNumber < (numberOfVersions-1)}">
			          | <a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=version&versionNumber=<c:out value="${topicVersion.versionNumber+1}" />&topic=<c:out value="${topicVersion.topicName}" />'><f:message key="history.prev"/></a>
			        </c:if>
			        <c:if test="${!(param.versionNumber < (numberOfVersions-1))}">
			          | <span class="menuinactive"><f:message key="history.prev"/></span>
			        </c:if>
			        <c:if test="${param.versionNumber > 0}">
			          | <a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=version&versionNumber=<c:out value="${topicVersion.versionNumber-1}" />&topic=<c:out value="${topicVersion.topicName}" />'><f:message key="history.next"/></a> 
			        </c:if>
			        <c:if test="${!(param.versionNumber > 0)}">
			          | <span class="menuinactive"><f:message key="history.next"/></span> 
			        </c:if>
			        | <a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=all&topic=<c:out value="${topicVersion.topicName}" />'><f:message key="menu.history"/></a>
			      </c:if>
			      |
	      		</td>
	      	</tr>
	  	</table>
      </div>
      <div class="contents">
          <span class="pageHeader">
          <c:out value="${title}"/>
          </span><p/>

      <c:choose>
        <c:when test="${param.type=='all'}">
          <form action="Wiki" method="post" name="historyForm">
          <input type="hidden" name="action" value="<%= WikiServlet.ACTION_DIFF %>"/>
          <input type="hidden" name="type" value="arbitrary"/>
          <input type="hidden" name="topic" value='<c:out value="${param.topic}"/>'/>
          <table>
          <c:forEach items="${versions}" var="version">
            <f:formatDate
              value="${version.revisionDate}"
              type="both"
              dateStyle="MEDIUM"
              timeStyle="MEDIUM"
              var="revisionDate"
            />
            <tr>
            <td><a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=version&versionNumber=<c:out value="${version.versionNumber}" />&topic=<c:out value="${topic.topicName}" />'><c:out value="${revisionDate}"/></a></td>
            <td><input type="checkbox" name='<c:out value="diff:${version.versionNumber}"/>' onclick="inactive(this)" /></td>
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
            <textarea readonly="true" cols="80" rows="26"><c:out value="${topicVersion.rawContents}" escapeXml="${false}"/></textarea>
          </form>
        </c:when>
      </c:choose>
      </div>

      <div class="menu">
		<table style="width: 100%; border: 0px solid;">
			<tr>
				<td class="menu" align=left>
			      | <span class="menuinactive"><f:message key="menu.editpage"/></span>
			      | <span class="menuinactive"><f:message key="menu.attach"/></span>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?RecentChanges'><f:message key="generalmenu.recentchanges"/></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?<%= JSPUtils.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>'><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?WikiSearch'><f:message key="generalmenu.search"/></a>
			      | <span class="menuinactive"><f:message key="menu.printablepage"/></span>
			      | <a href='Wiki?<c:out value="${param.topic}"/>'><f:message key="history.current"/></a>
			      <c:if test="${param.type=='version'}">
			        <c:if test="${param.versionNumber < (numberOfVersions-1)}">
			          | <a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=version&versionNumber=<c:out value="${topicVersion.versionNumber+1}" />&topic=<c:out value="${topicVersion.topicName}" />'><f:message key="history.prev"/></a>
			        </c:if>
			        <c:if test="${!(param.versionNumber < (numberOfVersions-1))}">
			          | <span class="menuinactive"><f:message key="history.prev"/></span>
			        </c:if>
			        <c:if test="${param.versionNumber > 0}">
			          | <a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=version&versionNumber=<c:out value="${topicVersion.versionNumber-1}" />&topic=<c:out value="${topicVersion.topicName}" />'><f:message key="history.next"/></a> 
			        </c:if>
			        <c:if test="${!(param.versionNumber > 0)}">
			          | <span class="menuinactive"><f:message key="history.next"/></span> 
			        </c:if>
			        | <a href='Wiki?action=<%= WikiServlet.ACTION_HISTORY %>&type=all&topic=<c:out value="${topicVersion.topicName}" />'><f:message key="menu.history"/></a>
			      </c:if>
			      |
	      		</td>
	      	</tr>
	  	</table>
      </div>
    </td>
  </tr>
</table>



<%@ include file="close-document.jsp"%>