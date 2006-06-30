<%@ include file="top.jsp"%>
<script language="JavaScript">
  function cancel(){
    history.go(-1);
  }
</script>
<%
  String title = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.title");
  String contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.error") + request.getAttribute("exception");
%>
<c:out value="${topArea}" escapeXml="false"/>
<div class="contents">
      <p><%=contents%></p>
      <input type="button" onClick="cancel();" value="<f:message key="common.back"/>">
</div>
<%@ include file="close-document.jsp"%>