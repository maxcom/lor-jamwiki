
<%@ page isErrorPage="true" %>
<c:out value="${topArea}" escapeXml="false"/>
<script language="JavaScript">
  function cancel(){
    history.go(-1);
  }
</script>
<%
  String title = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.title");
  String contents = "";

  if( exception == null ){
      contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.noexception");
  }
  else{
    exception.printStackTrace();
    if( exception.getMessage() == null )
      contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.exception") + " " + exception;
    else {
      contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.exception") + " " + exception.toString();
    }
  }
%>

<div class="contents">
      <p><%=contents%></p>
      <input type="button" onClick="cancel();" value="<f:message key="common.back"/>">
</div>
<%@ include file="close-document.jsp"%>

