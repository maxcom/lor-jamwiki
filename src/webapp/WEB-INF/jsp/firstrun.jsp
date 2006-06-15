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
<%@ include file="top.jsp"%>
<c:out value="${topArea}" escapeXml="false"/>
<%@include file="generalmenu.jsp"%>
<p>
<f:message key="firstuse.message.welcome"/>
</p>
<p>
<f:message key="firstuse.newpassword"/> <c:out value="${env.adminPassword}"/>
</p>
<p>
<f:message key="firstuse.message.warning"/>
</p>
<%@include file="generalmenu.jsp"%>
<%@include file="close-document.jsp"%>