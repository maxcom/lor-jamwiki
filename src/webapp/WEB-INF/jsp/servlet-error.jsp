<%@ include file="top.jsp"%>
<%@ page import="org.jamwiki.WikiException,
                 org.jamwiki.servlets.WikiServletException"%>
<script language="JavaScript">
  function cancel(){
    history.go(-1);
  }
</script>
<%
  String title = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.title");
  String contents = "";
  if (request.getAttribute(
    "exception") instanceof WikiServletException ) {
    // came from the container
    WikiServletException wsException = (WikiServletException)request.getAttribute(
    "exception");
    // oh well, better give it something
    if( wsException == null ){
      contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.noexception");
    } else{
      switch( wsException.getType() ){
        case WikiException.UNKNOWN:
          contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.unknown") + wsException.getMessage();
          break;
        case WikiException.LOCK_TIMEOUT:
          contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.locktimeout");
          break;
        case WikiException.READ_ONLY:
          contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.readonly");
          break;
        case WikiException.TOPIC_LOCKED:
          contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.topiclocked");
          break;
        default:
          contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.unknown.2") + wsException.getMessage();
      }
    }
  } else{
    contents = javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, "error.error") + request.getAttribute(
    "exception");
  }
%>
<c:out value="${topArea}" escapeXml="false"/>
<div class="contents">
      <p><%=contents%></p>
      <input type="button" onClick="cancel();" value="<f:message key="common.back"/>">
</div>
<%@ include file="close-document.jsp"%>