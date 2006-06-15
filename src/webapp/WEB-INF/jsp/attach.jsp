<%@include file="top.jsp"%>
<c:out value="${topArea}" escapeXml="false"/>


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
      <%@include file="generalmenu.jsp"%>
      <div class="contents">
      <form name="form1" method="post" action="../SaveAttachmentServlet" enctype="multipart/form-data">
      <table border="0">
        <tr>
          <td><f:message key="attach.info"/></td>
        </tr>
        <tr>
          <td>
              <input type="file" name="file1" size="50">
            </td>
        </tr>
        <tr>
          <td>
              <input type="file" name="file2" size="50">
            </td>
        </tr>
        <tr>
          <td>
              <input type="file" name="file3" size="50">
            </td>
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
      </div>
      <%@ include file="generalmenu.jsp"%>
    </td>
  </tr>
</table>

<%@ include file="close-document.jsp"%>