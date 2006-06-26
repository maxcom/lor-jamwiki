
<%@ page import="
        org.jamwiki.WikiException,
        org.jamwiki.servlets.WikiServlet,
        org.jamwiki.servlets.WikiServletException
    "
    isErrorPage="true"
%>
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
  else if( exception instanceof WikiException ){
    WikiException wException = (WikiException)exception;
    switch( wException.getType() ){
      case WikiException.UNKNOWN:
        contents = wException.getMessage();
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
        contents = wException.getMessage();
    }
  }
  else if( exception instanceof WikiServletException ){
    WikiServletException wException = (WikiServletException)exception;
    switch( wException.getType() ){
      case WikiException.UNKNOWN:
        contents = wException.getMessage();
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
        contents = wException.getMessage();
    }
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

