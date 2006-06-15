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
<html>
<head>
<%@page import="vqwiki.*" errorPage="error.jsp"%>
<%@ taglib uri="/WEB-INF/classes/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/classes/vqwiki.tld" prefix="vqwiki" %>
<%@ taglib uri="/WEB-INF/classes/fmt.tld" prefix="f" %>
<title><f:message key="common.productname.short"/></title>
<link rel="stylesheet" href="../vqwiki.css" type="text/css"/>
</head>

<body bgcolor="#FFFFFF" text="#000000">
<vqwiki:wiki-version var="wikiversion" />
<p class="pageHeader"><img src="../images/logo.jpg" width="147" height="35"> <f:message key="index.versioninfo"><f:param value="${wikiversion}"/></f:message></p>
<p class="normal">
  <a href="Wiki?<f:message key="specialpages.startingpoints"/>"><f:message key="specialpages.startingpoints"/></a>
</p>
<p class="normal">
  <a href="Wiki?<f:message key="specialpages.textformattingrules"/>"><f:message key="specialpages.textformattingrules"/></a>
</p>
<p class="normal">
  <a href="<f:message key="index.adminguide.link"/>"><f:message key="index.adminguide"/></a>
</p>

<p class="normal"><f:message key="index.homepage"/></p>
<p class="normal"><f:message key="index.development"/></p>
<p class="normal">
<f:message key="index.development.other"/> Michael Demastrie, Mark Goodwin, Robert Brewer, Eric Sheffer, Bill Barnett,
Robert McKinnon, Shawn Samuel, Fritz Freiheit, Noel J. Bergman, Wilhelm Fitzpatrick,
Joachim Lous, Truls Thirud, Konstantin Ignatyev, Sean Kerwick, Paul Shields, Markus Gebhard,
Tobias Schulz-Hess, Aaron Tavistock, Horst G. Reiterer, Mark Chung, Rod Morimoto, Owen Nichols,
Ernst Jan Plugge, Patrick Carl, Luigi R. Viggiano, Martin Kuba, Andre Gauthier, Chris Means, Derek Stevenson,
Jeff Tulley, Subramanya Sastry Rallabhandi Durga Venkata, Philip Aston, Franz Achermann, Cyrille Le Clerc,
Cyril Ronseaux, Anthony Roy
</p>
<p class="normal">Java Diff by Ian F. Darwin, <a href="mailto:ian@darwinsys.com">ian@darwinsys.com</a></p>
<p class="normal">JLex lexical analyser generator at <a href="http://www.cs.princeton.edu/%7Eappel/modern/java/JLex/">http://www.cs.princeton.edu/~appel/modern/java/JLex/</a></p>
<p class="normal">Upload functionality: <a href="http://jakarta.apache.org/commons">Jakarta Commons file upload library</a>.</p>
</body>
</html>
<vqwiki:environment var="env"/>
<c:if test="${!empty env.defaultTopic}">
 <c:redirect url="Wiki?${env.defaultTopic}"/>
</c:if>
