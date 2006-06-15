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
			      | <a href='<c:out value="${pathRoot}"/>Wiki?<c:out value="${env.defaultTopicEncoded}"/>'><c:out value="${env.defaultTopic}"/></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?WikiSearch'><f:message key="generalmenu.search"/></a>
			      | <span class="menuinactive"><f:message key="menu.printablepage"/></span>
			      | <a href='Wiki?<c:out value="${param.topic}"/>'><f:message key="history.current"/></a>
			      <c:if test="${param.type=='version'}">
			        <c:url value="Wiki" var="nextVersionUrl">
			          <c:param name="action" value="${env.actionHistory}"/>
			          <c:param name="type" value="version"/>
			          <c:param name="versionNumber" value="${topicVersion.versionNumber-1}"/>
			          <c:param name="topic" value="${topicVersion.topicName}"/>
			        </c:url>
			        <c:url value="Wiki" var="previousVersionUrl">
			          <c:param name="action" value="${env.actionHistory}"/>
			          <c:param name="type" value="version"/>
			          <c:param name="versionNumber" value="${topicVersion.versionNumber+1}"/>
			          <c:param name="topic" value="${topicVersion.topicName}"/>
			        </c:url>
			        <c:url value="Wiki" var="historyUrl">
			          <c:param name="action" value="${env.actionHistory}"/>
			          <c:param name="type" value="all"/>
			          <c:param name="topic" value="${topicVersion.topicName}"/>
			        </c:url>
			        <c:if test="${param.versionNumber < (numberOfVersions-1)}">
			          | <a href='<c:out value="${previousVersionUrl}"/>'><f:message key="history.prev"/></a>
			        </c:if>
			        <c:if test="${!(param.versionNumber < (numberOfVersions-1))}">
			          | <span class="menuinactive"><f:message key="history.prev"/></span>
			        </c:if>
			        <c:if test="${param.versionNumber > 0}">
			          | <a href='<c:out value="${nextVersionUrl}"/>'><f:message key="history.next"/></a> 
			        </c:if>
			        <c:if test="${!(param.versionNumber > 0)}">
			          | <span class="menuinactive"><f:message key="history.next"/></span> 
			        </c:if>
			        | <a href='<c:out value="${historyUrl}"/>'><f:message key="menu.history"/></a>
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
          <input type="hidden" name="action" value="<c:out value="${env.actionDiff}"/>"/>
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
            <c:url value="Wiki" var="versionURL">
              <c:param name="action" value="${env.actionHistory}"/>
              <c:param name="type" value="version"/>
              <c:param name="versionNumber" value="${version.versionNumber}"/>
              <c:param name="topic" value="${version.topicName}"/>
            </c:url>
            <tr>
            <td><a href='<c:out value="${versionURL}"/>'><c:out value="${revisionDate}"/></a></td>
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
			      | <a href='<c:out value="${pathRoot}"/>Wiki?<c:out value="${env.defaultTopicEncoded}"/>'><c:out value="${env.defaultTopic}"/></a>
			      | <a href='<c:out value="${pathRoot}"/>Wiki?WikiSearch'><f:message key="generalmenu.search"/></a>
			      | <span class="menuinactive"><f:message key="menu.printablepage"/></span>
			      | <a href='Wiki?<c:out value="${param.topic}"/>'><f:message key="history.current"/></a>
			      <c:if test="${param.type=='version'}">
			        <c:if test="${param.versionNumber < (numberOfVersions-1)}">
			          | <a href='<c:out value="${previousVersionUrl}"/>'><f:message key="history.prev"/></a>
			        </c:if>
			        <c:if test="${!(param.versionNumber < (numberOfVersions-1))}">
			          | <span class="menuinactive"><f:message key="history.prev"/></span>
			        </c:if>
			        <c:if test="${param.versionNumber > 0}">
			          | <a href='<c:out value="${nextVersionUrl}"/>'><f:message key="history.next"/></a> 
			        </c:if>
			        <c:if test="${!(param.versionNumber > 0)}">
			          | <span class="menuinactive"><f:message key="history.next"/></span> 
			        </c:if>
			        | <a href='<c:out value="${historyUrl}"/>'><f:message key="menu.history"/></a>
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