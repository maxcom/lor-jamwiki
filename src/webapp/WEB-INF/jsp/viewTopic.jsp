<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false"%>

<%--@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title><fmt:message key="sitetitle"/> - ${model.topicname}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link rel="stylesheet" href='${pageContext.request.contextPath}/css/new_jmwiki.css' type="text/css" />
  </head>

  <body>

  <div id="container">

    <div id="header">
        <a class="logo" href="Wiki"><img src="http://www.jmwiki.org/wiki/images/logo.jpg"></a>
		<span class="title"><fmt:message key="sitetitle"/></span>
	</div>

    <div id="content">

        <div id="menu">
            LEFT MENU
        </div>
        <div id="topic">
            <div id="toolbar">
                TOOLBAR
            </div>
            <h1>${model.topicname}</h1>
            ${model.topiccontent}
            <div id="toolbar">
                TOOLBAR
            </div>
        </div>

    </div>

    <div id="footer">
        <p>Java MediaWiki Version <%= WikiBase.WIKI_VERSION %> | <a href="Wiki?action=action_admin&username=admin">Admin</a></p>
	    <p>All contents copyright of the JMWiki project. © 2005-2006.</p>
    </div>

  </div>

  </body>
</html>