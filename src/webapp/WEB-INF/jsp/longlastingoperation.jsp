<html>
<head>
  <%@page errorPage="/WEB-INF/jsp/error.jsp" %>
  <%@ taglib uri="/WEB-INF/classes/c.tld" prefix="c" %>
  <%@ taglib uri="/WEB-INF/classes/jmwiki.tld" prefix="jmwiki" %>
  <%@ taglib uri="/WEB-INF/classes/fmt.tld" prefix="f" %>
  <f:setBundle basename="ApplicationResources"/>

  <link rel="stylesheet" href='<c:out value="${pageContext.request.contextPath}"/>/jmwiki.css' type="text/css" />
  <title><c:out value="${progress}"/>% - <f:message key="longlasting.title"/></title>
  <META HTTP-EQUIV="Content-Type" CONTENT="text/html">
  <script>
    window.setTimeout('location.replace("<c:out value="${url}"/>&id=<c:out value="${id}"/>")', <c:out value="${nextRefresh}"/>000);
  </script>
  <meta http-equiv="refresh" content="<c:out value="${nextRefresh}"/>; <c:out value="${url}"/>&id=<c:out value="${id}"/>"> 
</head>
<body>
<p align="center" class="pageHeader">
<f:message key="longlasting.operationInProgress"/>
</p>
<p>&nbsp;</p>
<p align="center" class="content">
<img src="../images/progress_left.gif" width="2" height="17"><img src="../images/progress_front.gif" width="<c:out value="${2*progress}"/>" height="17"><img src="../images/progress_right.gif" width="2" height="17"><img src="../images/progress_back.gif" width="<c:out value="${200 - (progress*2)}"/>" height="17"><br>
<f:message key="longlasting.progress"><f:param value="${progress}"/></f:message>
</p>
</body>
</html>