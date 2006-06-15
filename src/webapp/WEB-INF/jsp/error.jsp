<%--
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2003 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
--%>

<%@ page import="
        org.vqwiki.WikiException,
        org.vqwiki.servlets.WikiServlet,
        org.vqwiki.servlets.WikiServletException
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

